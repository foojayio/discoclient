/*
 * Copyright (c) 2021 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.foojay.api.discoclient;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.foojay.api.discoclient.event.DCEvt;
import io.foojay.api.discoclient.event.DownloadEvt;
import io.foojay.api.discoclient.event.Evt;
import io.foojay.api.discoclient.event.EvtObserver;
import io.foojay.api.discoclient.event.EvtType;
import io.foojay.api.discoclient.pkg.Architecture;
import io.foojay.api.discoclient.pkg.ArchiveType;
import io.foojay.api.discoclient.pkg.Bitness;
import io.foojay.api.discoclient.pkg.Distribution;
import io.foojay.api.discoclient.pkg.Feature;
import io.foojay.api.discoclient.pkg.Latest;
import io.foojay.api.discoclient.pkg.LibCType;
import io.foojay.api.discoclient.pkg.MajorVersion;
import io.foojay.api.discoclient.pkg.Match;
import io.foojay.api.discoclient.pkg.OperatingSystem;
import io.foojay.api.discoclient.pkg.PackageType;
import io.foojay.api.discoclient.pkg.Pkg;
import io.foojay.api.discoclient.pkg.ReleaseStatus;
import io.foojay.api.discoclient.pkg.Scope;
import io.foojay.api.discoclient.pkg.SemVer;
import io.foojay.api.discoclient.pkg.TermOfSupport;
import io.foojay.api.discoclient.pkg.VersionNumber;
import io.foojay.api.discoclient.util.Constants;
import io.foojay.api.discoclient.util.Helper;
import io.foojay.api.discoclient.util.OutputFormat;
import io.foojay.api.discoclient.util.PkgInfo;
import io.foojay.api.discoclient.util.ReadableConsumerByteChannel;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static io.foojay.api.discoclient.util.Constants.API_VERSION_V2;
import static io.foojay.api.discoclient.util.Constants.PROPERTY_KEY_DISTRIBUTION_JSON_URL;


public class DiscoClient {
    public static final  ConcurrentHashMap<String, List<Scope>> SCOPE_LOOKUP         = new ConcurrentHashMap<>();
    private static final Map<String, Distribution>              DISTRIBUTIONS        = new ConcurrentHashMap<>();
    private static final String[]                               DETECT_ALPINE_CMDS   = { "/bin/sh", "-c", "cat /etc/os-release | grep 'NAME=' | grep -ic 'Alpine'" };
    private static final String[]                               UX_DETECT_ARCH_CMDS  = { "/bin/sh", "-c", "uname -m" };
    private static final String[]                               WIN_DETECT_ARCH_CMDS = { "/bin/sh", "-c", "uname -m" };
    private final        Map<String, List<EvtObserver>>         observers            = new ConcurrentHashMap<>();
    private static       AtomicBoolean                          initialized          = new AtomicBoolean(false);
    private              String                                 userAgent            = "";


    public DiscoClient() {
        this("");
    }
    public DiscoClient(final String userAgent) {
        this.userAgent = userAgent;
        preloadDistributions();
    }


    private static void preloadDistributions() {
        Helper.preloadDistributions().thenAccept(distros -> {
            DISTRIBUTIONS.putAll(distros);
            DISTRIBUTIONS.entrySet().stream().forEach(entry -> SCOPE_LOOKUP.put(entry.getKey(), entry.getValue().getScopes()));
            Helper.getAsync(PropertyManager.INSTANCE.getString(PROPERTY_KEY_DISTRIBUTION_JSON_URL), "").thenAccept(response -> {
                if (null != response) {
                    if (response.statusCode() == 200) {
                        String                    jsonText           = response.body();
                        Map<String, Distribution> distributionsFound = new ConcurrentHashMap<>();
                        if (!jsonText.isEmpty()) {
                            distributionsFound.putAll(Helper.getDistributionsFromJsonText(jsonText));
                        }
                        if (!distributionsFound.isEmpty()) {
                            DISTRIBUTIONS.clear();
                            DISTRIBUTIONS.putAll(distributionsFound);
                        }
                    }
                }
                initialized.set(true);
            });
        });
    }

    public boolean isInitialzed() { return initialized.get(); }


    public Queue<Pkg> getAllPackages() {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getPackagesPath())
                                                        .append("?release_status=ea")
                                                        .append("&release_status=ga");

        String query = queryBuilder.toString();
        if (query.isEmpty()) { return new ConcurrentLinkedQueue<>(); }

        Queue<Pkg> pkgs      = new ConcurrentLinkedQueue<>();
        Set<Pkg>   pkgsFound = new HashSet<>();

        String      bodyText = Helper.get(query, userAgent).body();
        Gson        gson     = new Gson();
        JsonElement element  = gson.fromJson(bodyText, JsonElement.class);
        if (element instanceof JsonObject) {
            JsonObject jsonObject = element.getAsJsonObject();
            JsonArray jsonArray = jsonObject.getAsJsonArray("result");
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject bundleJsonObj = jsonArray.get(i).getAsJsonObject();
                pkgsFound.add(new Pkg(bundleJsonObj.toString()));
            }
        }

        pkgs.addAll(pkgsFound);
        return pkgs;
    }
    public CompletableFuture<Queue<Pkg>> getAllPackagesAsync() {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getPackagesPath())
                                                        .append("?release_status=ea")
                                                        .append("&release_status=ga");
        String query = queryBuilder.toString();

        CompletableFuture<Queue<Pkg>> future = Helper.getAsync(query, userAgent).thenApply(response -> {
            Queue<Pkg>  pkgsFound = new ConcurrentLinkedQueue<>();
            Gson        gson      = new Gson();
            JsonElement element   = gson.fromJson(response.body(), JsonElement.class);
            if (element instanceof JsonObject) {
                JsonObject jsonObject = element.getAsJsonObject();
                JsonArray jsonArray = jsonObject.getAsJsonArray("result");
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject bundleJsonObj = jsonArray.get(i).getAsJsonObject();
                    pkgsFound.add(new Pkg(bundleJsonObj.toString()));
                }
            }
            return pkgsFound;
        });
        return future;
    }


    public List<Pkg> getPkgs(final List<Distribution> distributions, final VersionNumber versionNumber, final Latest latest, final OperatingSystem operatingSystem,
                             final LibCType libcType, final Architecture architecture, final Bitness bitness, final ArchiveType archiveType, final PackageType packageType,
                             final Boolean javafxBundled, final Boolean directlyDownloadable, final List<ReleaseStatus> releaseStatus, final TermOfSupport termOfSupport, final List<Scope> scopes, final Match match) {
        return getPkgs(distributions, versionNumber, latest, operatingSystem, libcType, architecture, bitness, archiveType, packageType, javafxBundled, directlyDownloadable, releaseStatus, termOfSupport, new ArrayList(), scopes, match);
    }
    public List<Pkg> getPkgs(final List<Distribution> distributions, final VersionNumber versionNumber, final Latest latest, final OperatingSystem operatingSystem,
                             final LibCType libcType, final Architecture architecture, final Bitness bitness, final ArchiveType archiveType, final PackageType packageType,
                             final Boolean javafxBundled, final Boolean directlyDownloadable, final List<ReleaseStatus> releaseStatus, final TermOfSupport termOfSupport, final List<String> ftrs, final List<Scope> scopes, final Match match) {

        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getPackagesPath());
        final int initialLength = queryBuilder.length();

        if (null != distributions && !distributions.isEmpty()) {
            distributions.forEach(distribution -> {
                if (null != distribution) {
                    queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
                    queryBuilder.append(Constants.API_DISTRIBUTION).append("=").append(distribution.getApiString());
                }
            });
        }

        if (null != versionNumber) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_VERSION).append("=").append(Helper.encodeValue(versionNumber.toString(OutputFormat.REDUCED_COMPRESSED, true, true)));
        }

        if (null != latest && Latest.NONE != latest && Latest.NOT_FOUND != latest) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_LATEST).append("=").append(latest.getApiString());
        }

        if (null != operatingSystem && OperatingSystem.NONE != operatingSystem && OperatingSystem.NOT_FOUND != operatingSystem) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_OPERATING_SYSTEM).append("=").append(operatingSystem.getApiString());
        }

        if (null != libcType && LibCType.NONE != libcType && LibCType.NOT_FOUND != libcType) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_LIBC_TYPE).append("=").append(libcType.getApiString());
        }

        if (null != architecture && Architecture.NONE != architecture && Architecture.NOT_FOUND != architecture) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_ARCHITECTURE).append("=").append(architecture.getApiString());
        }

        if (null != bitness && Bitness.NONE != bitness && Bitness.NOT_FOUND != bitness) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_BITNESS).append("=").append(bitness.getApiString());
        }

        if (null != archiveType && ArchiveType.NONE != archiveType && ArchiveType.NOT_FOUND != archiveType) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_ARCHIVE_TYPE).append("=").append(archiveType.getApiString());
        }

        if (null != packageType && PackageType.NONE != packageType && PackageType.NOT_FOUND != packageType) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_PACKAGE_TYPE).append("=").append(packageType.getApiString());
        }

        if (null != scopes && !scopes.isEmpty()) {
            scopes.forEach(scope -> {
                if (null != scope && Scope.NONE != scope && Scope.NOT_FOUND != scope) {
                    queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
                    queryBuilder.append(Constants.API_DISCOVERY_SCOPE_ID).append("=").append(scope.getApiString());
                }
            });
        }

        if (null != match && Match.NONE != match && Match.NOT_FOUND != match) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_MATCH).append("=").append(match.getApiString());
        }

        if (null != javafxBundled) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_JAVAFX_BUNDLED).append("=").append(javafxBundled);
        }

        if (null != directlyDownloadable) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_DIRECTLY_DOWNLOADABLE).append("=").append(directlyDownloadable);
        }

        if (null != releaseStatus && !releaseStatus.isEmpty()) {
            releaseStatus.forEach(rs -> {
                if (null != rs && ReleaseStatus.NONE != rs && ReleaseStatus.NOT_FOUND != rs) {
                    queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
                    queryBuilder.append(Constants.API_RELEASE_STATUS).append("=").append(rs.getApiString());
                }
            });
        }

        if (null != termOfSupport && TermOfSupport.NONE != termOfSupport && TermOfSupport.NOT_FOUND != termOfSupport) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_SUPPORT_TERM).append("=").append(termOfSupport.getApiString());
        }

        if (null != ftrs && !ftrs.isEmpty()) {
            List<Feature> features;
            if (ftrs.isEmpty()) {
                features = new ArrayList<>();
            } else {
                Set<Feature> featuresFound = new HashSet<>();
                for (String featureString : ftrs) {
                    Feature feat = Feature.fromText(featureString);
                    if (Feature.NOT_FOUND == feat || Feature.NONE == feat) {
                        continue;
                    }
                    featuresFound.add(feat);
                }
                if (featuresFound.isEmpty()) {
                    features = new ArrayList();
                } else {
                    features = new ArrayList<>(featuresFound);
                }
            }
            features.forEach(feature -> {
                queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
                queryBuilder.append(Constants.API_FEATURE).append("=").append(feature.getApiString());
            });
        }

        String query = queryBuilder.toString();
        if (query.isEmpty()) { return new ArrayList(); }

        List<Pkg>   pkgs     = new LinkedList<>();
        String      bodyText = Helper.get(query, userAgent).body();

        Set<Pkg>    pkgsFound = new HashSet<>();
        Gson        gson      = new Gson();
        JsonElement element   = gson.fromJson(bodyText, JsonElement.class);
        if (element instanceof JsonObject) {
            JsonObject jsonObject = element.getAsJsonObject();
            JsonArray jsonArray = jsonObject.getAsJsonArray("result");
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject pkgJsonObj = jsonArray.get(i).getAsJsonObject();
                pkgsFound.add(new Pkg(pkgJsonObj.toString()));
            }
        }
        pkgs.addAll(pkgsFound);
        return pkgs;
    }

    public CompletableFuture<List<Pkg>> getPkgsAsync(final List<Distribution> distributions, final VersionNumber versionNumber, final Latest latest, final OperatingSystem operatingSystem,
                                                     final LibCType libCType, final Architecture architecture, final Bitness bitness, final ArchiveType archiveType, final PackageType packageType,
                                                     final Boolean javafxBundled, final Boolean directlyDownloadable, final List<ReleaseStatus> releaseStatus, final TermOfSupport termOfSupport, final List<Scope> scopes, final Match match) {
        return getPkgsAsync(distributions, versionNumber, latest, operatingSystem, libCType, architecture, bitness, archiveType, packageType, javafxBundled, directlyDownloadable, releaseStatus, termOfSupport, new ArrayList<>(), scopes, match);
    }
    public CompletableFuture<List<Pkg>> getPkgsAsync(final List<Distribution> distributions, final VersionNumber versionNumber, final Latest latest, final OperatingSystem operatingSystem,
                                                 final LibCType libCType, final Architecture architecture, final Bitness bitness, final ArchiveType archiveType, final PackageType packageType,
                                                 final Boolean javafxBundled, final Boolean directlyDownloadable, final List<ReleaseStatus> releaseStatus, final TermOfSupport termOfSupport, final List<String> ftrs, final List<Scope> scopes, final Match match) {

    StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                    .append(PropertyManager.INSTANCE.getPackagesPath());
    final int initialLength = queryBuilder.length();

    if (null != distributions && !distributions.isEmpty()) {
        distributions.forEach(distribution -> {
            if (null != distribution) {
                queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
                queryBuilder.append(Constants.API_DISTRIBUTION).append("=").append(distribution.getApiString());
            }
        });
    }

    if (null != versionNumber) {
        queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
        queryBuilder.append(Constants.API_VERSION).append("=").append(Helper.encodeValue(versionNumber.toString(OutputFormat.REDUCED_COMPRESSED, true, true)));
    }

    if (null != latest && Latest.NONE != latest && Latest.NOT_FOUND != latest) {
        queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
        queryBuilder.append(Constants.API_LATEST).append("=").append(latest.getApiString());
    }

    if (null != operatingSystem && OperatingSystem.NONE != operatingSystem && OperatingSystem.NOT_FOUND != operatingSystem) {
        queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
        queryBuilder.append(Constants.API_OPERATING_SYSTEM).append("=").append(operatingSystem.getApiString());
    }

    if (null != libCType && LibCType.NONE != libCType && LibCType.NOT_FOUND != libCType) {
        queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
        queryBuilder.append(Constants.API_LIBC_TYPE).append("=").append(libCType.getApiString());
    }

    if (null != architecture && Architecture.NONE != architecture && Architecture.NOT_FOUND != architecture) {
        queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
        queryBuilder.append(Constants.API_ARCHITECTURE).append("=").append(architecture.getApiString());
    }

    if (null != bitness && Bitness.NONE != bitness && Bitness.NOT_FOUND != bitness) {
        queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
        queryBuilder.append(Constants.API_BITNESS).append("=").append(bitness.getApiString());
    }

    if (null != archiveType && ArchiveType.NONE != archiveType && ArchiveType.NOT_FOUND != archiveType) {
        queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
        queryBuilder.append(Constants.API_ARCHIVE_TYPE).append("=").append(archiveType.getApiString());
    }

    if (null != packageType && PackageType.NONE != packageType && PackageType.NOT_FOUND != packageType) {
        queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
        queryBuilder.append(Constants.API_PACKAGE_TYPE).append("=").append(packageType.getApiString());
    }

    if (null != scopes && !scopes.isEmpty()) {
        scopes.forEach(scope -> {
            if (null != scope && Scope.NONE != scope && Scope.NOT_FOUND != scope) {
                queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
                queryBuilder.append(Constants.API_DISCOVERY_SCOPE_ID).append("=").append(scope.getApiString());
            }
        });
    }

    if (null != match && Match.NONE != match && Match.NOT_FOUND != match) {
        queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
        queryBuilder.append(Constants.API_MATCH).append("=").append(match.getApiString());
    }

    if (null != javafxBundled) {
        queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
        queryBuilder.append(Constants.API_JAVAFX_BUNDLED).append("=").append(javafxBundled);
    }

    if (null != directlyDownloadable) {
        queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
        queryBuilder.append(Constants.API_DIRECTLY_DOWNLOADABLE).append("=").append(directlyDownloadable);
    }

    if (null != releaseStatus && !releaseStatus.isEmpty()) {
        releaseStatus.forEach(rs -> {
            if (null != rs && ReleaseStatus.NONE != rs && ReleaseStatus.NOT_FOUND != rs) {
                queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
                queryBuilder.append(Constants.API_RELEASE_STATUS).append("=").append(rs.getApiString());
            }
        });
    }

    if (null != termOfSupport && TermOfSupport.NONE != termOfSupport && TermOfSupport.NOT_FOUND != termOfSupport) {
        queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
        queryBuilder.append(Constants.API_SUPPORT_TERM).append("=").append(termOfSupport.getApiString());
    }

    if (null != ftrs && !ftrs.isEmpty()) {
        List<Feature> features;
        if (ftrs.isEmpty()) {
            features = new ArrayList<>();
        } else {
            Set<Feature> featuresFound = new HashSet<>();
            for (String featureString : ftrs) {
                Feature feat = Feature.fromText(featureString);
                if (Feature.NOT_FOUND == feat || Feature.NONE == feat) {
                    continue;
                }
                featuresFound.add(feat);
            }
            if (featuresFound.isEmpty()) {
                features = new ArrayList();
            } else {
                features = new ArrayList<>(featuresFound);
            }
        }
        features.forEach(feature -> {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_FEATURE).append("=").append(feature.getApiString());
        });
    }

    String query = queryBuilder.toString();
    if (query.isEmpty()) { return new CompletableFuture<>(); }

    return Helper.getAsync(query, userAgent).thenApply(response -> {
        List<Pkg>   pkgs      = new LinkedList<>();
        Set<Pkg>    pkgsFound = new HashSet<>();
        Gson        gson      = new Gson();
        JsonElement element   = gson.fromJson(response.body(), JsonElement.class);
        if (element instanceof JsonObject) {
            JsonObject jsonObject = element.getAsJsonObject();
            JsonArray jsonArray = jsonObject.getAsJsonArray("result");
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject pkgJsonObj = jsonArray.get(i).getAsJsonObject();
                pkgsFound.add(new Pkg(pkgJsonObj.toString()));
            }
        }
        pkgs.addAll(pkgsFound);
        return pkgs;
    });
}


    public String getPkgsAsJson(final List<Distribution> distributions, final VersionNumber versionNumber, final Latest latest, final OperatingSystem operatingSystem,
                                final LibCType libcType, final Architecture architecture, final Bitness bitness, final ArchiveType archiveType, final PackageType packageType,
                                final Boolean javafxBundled, final Boolean directlyDownloadable, final List<ReleaseStatus> releaseStatus, final TermOfSupport termOfSupport, final List<Scope> scopes, final Match match) {
        return getPkgs(distributions, versionNumber, latest, operatingSystem, libcType, architecture, bitness, archiveType, packageType, javafxBundled, directlyDownloadable, releaseStatus, termOfSupport, scopes, match).toString();
    }
    public CompletableFuture<String> getPkgsAsJsonAsync(final List<Distribution> distributions, final VersionNumber versionNumber, final Latest latest, final OperatingSystem operatingSystem,
                                                        final LibCType libcType, final Architecture architecture, final Bitness bitness, final ArchiveType archiveType, final PackageType packageType,
                                                        final Boolean javafxBundled, final Boolean directlyDownloadable, final List<ReleaseStatus> releaseStatus, final TermOfSupport termOfSupport, final List<Scope> scopes, final Match match) {
        return getPkgsAsync(distributions, versionNumber, latest, operatingSystem, libcType, architecture, bitness, archiveType, packageType, javafxBundled, directlyDownloadable, releaseStatus, termOfSupport, scopes, match).thenApply(pkgs -> pkgs.toString());
    }


    public List<Pkg> getPkgsForFeatureVersion(final List<Distribution> distributions, final int featureVersion, final List<ReleaseStatus> releaseStatus, final Boolean directlyDownloadable, final List<Scope> scopes, final Match match) {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getPackagesPath());
        final int initialLength = queryBuilder.length();
        if (featureVersion <= 6) { throw new IllegalArgumentException("Feature version has to be larger or equal to 6"); }

        if (null != distributions && !distributions.isEmpty()) {
            distributions.forEach(distribution -> {
                if (null != distribution) {
                    queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
                    queryBuilder.append(Constants.API_DISTRIBUTION).append("=").append(distribution.getApiString());
                }
            });
        }

        queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
        queryBuilder.append(Constants.API_VERSION).append("=").append(Helper.encodeValue(featureVersion + "..<" + (featureVersion + 1)));

        if (null != scopes && !scopes.isEmpty()) {
            scopes.forEach(scope -> {
                if (null != scope && Scope.NONE != scope && Scope.NOT_FOUND != scope) {
                    queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
                    queryBuilder.append(Constants.API_DISCOVERY_SCOPE_ID).append("=").append(scope.getApiString());
                }
            });
        }

        if (null != match && Match.NONE != match && Match.NOT_FOUND != match) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_MATCH).append("=").append(match.getApiString());
        }

        if (null != directlyDownloadable) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_DIRECTLY_DOWNLOADABLE).append("=").append(directlyDownloadable);
        }

        if (null != releaseStatus && !releaseStatus.isEmpty()) {
            releaseStatus.forEach(rs -> {
                if (null != rs && ReleaseStatus.NONE != rs && ReleaseStatus.NOT_FOUND != rs) {
                    queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
                    queryBuilder.append(Constants.API_RELEASE_STATUS).append("=").append(rs.getApiString());
                }
            });
        }

        String query = queryBuilder.toString();
        if (query.isEmpty()) { return new ArrayList(); }

        List<Pkg>   pkgs     = new LinkedList<>();
        String      bodyText = Helper.get(query, userAgent).body();

        Set<Pkg>    pkgsFound = new HashSet<>();
        Gson        gson      = new Gson();
        JsonElement element   = gson.fromJson(bodyText, JsonElement.class);
        if (element instanceof JsonObject) {
            JsonObject jsonObject = element.getAsJsonObject();
            JsonArray jsonArray = jsonObject.getAsJsonArray("result");
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject pkgJsonObj = jsonArray.get(i).getAsJsonObject();
                pkgsFound.add(new Pkg(pkgJsonObj.toString()));
            }
        }

        pkgs.addAll(pkgsFound);
        return pkgs;
    }
    public CompletableFuture<List<Pkg>> getPkgsForFeatureVersionAsync(final List<Distribution> distributions, final int featureVersion, final List<ReleaseStatus> releaseStatus, final Boolean directlyDownloadable, final List<Scope> scopes, final Match match) {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getPackagesPath());
        final int initialLength = queryBuilder.length();
        if (featureVersion <= 6) { throw new IllegalArgumentException("Feature version has to be larger or equal to 6"); }

        if (null != distributions && !distributions.isEmpty()) {
            distributions.forEach(distribution -> {
                if (null != distribution) {
                    queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
                    queryBuilder.append(Constants.API_DISTRIBUTION).append("=").append(distribution.getApiString());
                }
            });
        }

        queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
        queryBuilder.append(Constants.API_VERSION).append("=").append(Helper.encodeValue(featureVersion + "..<" + (featureVersion + 1)));

        if (null != scopes && !scopes.isEmpty()) {
            scopes.forEach(scope -> {
                if (null != scope && Scope.NONE != scope && Scope.NOT_FOUND != scope) {
                    queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
                    queryBuilder.append(Constants.API_DISCOVERY_SCOPE_ID).append("=").append(scope.getApiString());
                }
            });
        }

        if (null != match && Match.NONE != match && Match.NOT_FOUND != match) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_MATCH).append("=").append(match.getApiString());
        }

        if (null != directlyDownloadable) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_DIRECTLY_DOWNLOADABLE).append("=").append(directlyDownloadable);
        }

        if (null != releaseStatus && !releaseStatus.isEmpty()) {
            releaseStatus.forEach(rs -> {
                if (null != rs && ReleaseStatus.NONE != rs && ReleaseStatus.NOT_FOUND != rs) {
                    queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
                    queryBuilder.append(Constants.API_RELEASE_STATUS).append("=").append(rs.getApiString());
                }
            });
        }

        String query = queryBuilder.toString();
        return Helper.getAsync(query, userAgent).thenApply(response -> {
            List<Pkg>   pkgs      = new LinkedList<>();
            Set<Pkg>    pkgsFound = new HashSet<>();
            Gson        gson      = new Gson();
            JsonElement element   = gson.fromJson(response.body(), JsonElement.class);
            if (element instanceof JsonObject) {
                JsonObject jsonObject = element.getAsJsonObject();
                JsonArray jsonArray = jsonObject.getAsJsonArray("result");
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject pkgJsonObj = jsonArray.get(i).getAsJsonObject();
                    pkgsFound.add(new Pkg(pkgJsonObj.toString()));
                }
            }
            pkgs.addAll(pkgsFound);
            return pkgs;
        });
    }

    public final MajorVersion getMajorVersion(final String parameter) {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getMajorVersionsPath());
        if (null != parameter || !parameter.isEmpty()) {
            queryBuilder.append("/").append(parameter);
        }

        String query = queryBuilder.toString();
        if (query.isEmpty()) {
            return null;
        }
        String      bodyText = Helper.get(query, userAgent).body();
        Gson        gson     = new Gson();
        JsonElement element  = gson.fromJson(bodyText, JsonElement.class);
        if (element instanceof JsonObject) {
            JsonObject jsonObject = element.getAsJsonObject();
            JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
            if (jsonArray.size() > 0) {
                JsonObject   json         = jsonArray.get(0).getAsJsonObject();
                MajorVersion majorVersion = new MajorVersion(json.toString());
                return majorVersion;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
    public final CompletableFuture<MajorVersion> getMajorVersionAsync(final String parameter) {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getMajorVersionsPath());
        if (null != parameter || !parameter.isEmpty()) {
            queryBuilder.append("/").append(parameter);
        }

        String query = queryBuilder.toString();
        if (query.isEmpty()) {
            return null;
        }
        return Helper.getAsync(query, userAgent).thenApply(response -> {
            Gson        gson     = new Gson();
            JsonElement element  = gson.fromJson(response.body(), JsonElement.class);
            if (element instanceof JsonObject) {
                JsonObject    jsonObject   = element.getAsJsonObject();
                JsonArray     jsonArray    = jsonObject.getAsJsonArray("result");
                if (jsonArray.size() > 0) {
                    JsonObject    json         = jsonArray.get(0).getAsJsonObject();
                    MajorVersion  majorVersion = new MajorVersion(json.toString());
                    return majorVersion;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        });
    }


    public final Queue<MajorVersion> getAllMajorVersions() { return getAllMajorVersions(false); }
    public final Queue<MajorVersion> getAllMajorVersions(final boolean include_ea) { return getAllMajorVersions(include_ea, true); }
    public final Queue<MajorVersion> getAllMajorVersions(final boolean include_ea, final boolean include_build) {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getMajorVersionsPath())
                                                        .append("?ea=")
                                                        .append(include_ea)
                                                        .append(include_build ? "" : "&include_build=false");

        String              query              = queryBuilder.toString();
        String              bodyText           = Helper.get(query, userAgent).body();
        Queue<MajorVersion> majorVersionsFound = new ConcurrentLinkedQueue<>();

        Gson        gson     = new Gson();
        JsonElement element  = gson.fromJson(bodyText, JsonElement.class);
        if (element instanceof JsonObject) {
            JsonObject jsonObject = element.getAsJsonObject();
            JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject majorVersionJsonObj = jsonArray.get(i).getAsJsonObject();
                majorVersionsFound.add(new MajorVersion(majorVersionJsonObj.toString()));
            }
        }
        return majorVersionsFound;
    }
    public final List<MajorVersion> getAllMajorVersions(final Optional<Boolean> maintained, final Optional<Boolean> include_ea, final Optional<Boolean> include_ga, final Optional<Boolean> include_build) {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getMajorVersionsPath());
        int initialLength = queryBuilder.length();
        if (null != maintained && maintained.isPresent()) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append("maintained=").append(maintained.get());
        }
        if (null != include_ea && include_ea.isPresent()) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append("ea=").append(include_ea.get());
        }
        if (null != include_ga && include_ga.isPresent()) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append("ga=").append(include_ga.get());
        }
        if (null != include_build && include_build.isPresent() && !include_build.get()) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append("include_build=false");
        }

        String             query              = queryBuilder.toString();
        String             bodyText           = Helper.get(query, userAgent).body();
        List<MajorVersion> majorVersionsFound = new ArrayList<>();

        Gson        gson     = new Gson();
        JsonElement element  = gson.fromJson(bodyText, JsonElement.class);
        if (element instanceof JsonObject) {
            JsonObject jsonObject = element.getAsJsonObject();
            JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject majorVersionJsonObj = jsonArray.get(i).getAsJsonObject();
                majorVersionsFound.add(new MajorVersion(majorVersionJsonObj.toString()));
            }
        }
        return majorVersionsFound;
    }


    public final CompletableFuture<List<MajorVersion>> getAllMajorVersionsAsync() { return getAllMajorVersionsAsync(false); }
    public final CompletableFuture<List<MajorVersion>> getAllMajorVersionsAsync(final boolean include_ea) { return getAllMajorVersionsAsync(include_ea, true); }
    public final CompletableFuture<List<MajorVersion>> getAllMajorVersionsAsync(final boolean include_ea, final boolean include_build) {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getMajorVersionsPath())
                                                        .append("?ea=").append(include_ea)
                                                        .append(include_build ? "" : "&include_build=false");
        String query = queryBuilder.toString();
        return Helper.getAsync(query, userAgent).thenApply(reponse -> {
            List<MajorVersion> majorVersionsFound = new CopyOnWriteArrayList<>();
            Gson        gson     = new Gson();
            JsonElement element  = gson.fromJson(reponse.body(), JsonElement.class);
            if (element instanceof JsonObject) {
                JsonObject jsonObject = element.getAsJsonObject();
                JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject majorVersionJsonObj = jsonArray.get(i).getAsJsonObject();
                    majorVersionsFound.add(new MajorVersion(majorVersionJsonObj.toString()));
                }
            }
            return majorVersionsFound;
        });
    }
    public final CompletableFuture<List<MajorVersion>> getAllMajorVersionsAsync(final Optional<Boolean> maintained, final Optional<Boolean> include_ea, final Optional<Boolean> include_ga, final Optional<Boolean> include_build) {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getMajorVersionsPath());
        int initialLength = queryBuilder.length();
        if (null != maintained && maintained.isPresent()) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append("maintained=").append(maintained.get());
        }
        if (null != include_ea && include_ea.isPresent()) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append("ea=").append(include_ea.get());
        }
        if (null != include_ga && include_ga.isPresent()) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append("ga=").append(include_ga.get());
        }
        if (null != include_build && include_build.isPresent() && !include_build.get()) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append("include_build=false");
        }

        String query = queryBuilder.toString();
        return Helper.getAsync(query, userAgent).thenApply(response -> {
            List<MajorVersion> majorVersionsFound = new ArrayList<>();
            Gson        gson     = new Gson();
            JsonElement element  = gson.fromJson(response.body(), JsonElement.class);
            if (element instanceof JsonObject) {
                JsonObject jsonObject = element.getAsJsonObject();
                JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject majorVersionJsonObj = jsonArray.get(i).getAsJsonObject();
                    majorVersionsFound.add(new MajorVersion(majorVersionJsonObj.toString()));
                }
            }
            return majorVersionsFound;
        });
    }


    public final MajorVersion getMajorVersion(final int featureVersion, final boolean include_ea) { return getMajorVersion(featureVersion, include_ea, true); }
    public final MajorVersion getMajorVersion(final int featureVersion, final boolean include_ea, final boolean include_build) {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getMajorVersionsPath())
                                                        .append("?include_ea=").append(include_ea)
                                                        .append(include_build ? "": "&include_build=false");

        String query    = queryBuilder.toString();
        String bodyText = Helper.get(query, userAgent).body();

        Gson        gson     = new Gson();
        JsonElement element  = gson.fromJson(bodyText, JsonElement.class);
        if (element instanceof JsonObject) {
            JsonObject jsonObject = element.getAsJsonObject();
            JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject   json         = jsonArray.get(i).getAsJsonObject();
                MajorVersion majorVersion = new MajorVersion(json.toString());
                if (majorVersion.getAsInt() == featureVersion) {
                    return majorVersion;
                }
            }
            return null;
        } else {
            return null;
        }
    }
    public final CompletableFuture<MajorVersion> getMajorVersionAsync(final int featureVersion, final boolean include_ea) { return getMajorVersionAsync(featureVersion, include_ea, true); }
    public final CompletableFuture<MajorVersion> getMajorVersionAsync(final int featureVersion, final boolean include_ea, final boolean include_build) {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getMajorVersionsPath())
                                                        .append("?include_ea=").append(include_ea)
                                                        .append(include_build ? "" : "&include_build=false");

        String query = queryBuilder.toString();
        return Helper.getAsync(query, userAgent).thenApply(response -> {
            Gson        gson     = new Gson();
            JsonElement element  = gson.fromJson(response.body(), JsonElement.class);
            if (element instanceof JsonObject) {
                JsonObject jsonObject = element.getAsJsonObject();
                JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject   json         = jsonArray.get(i).getAsJsonObject();
                    MajorVersion majorVersion = new MajorVersion(json.toString());
                    if (majorVersion.getAsInt() == featureVersion) {
                        return majorVersion;
                    }
                }
                return null;
            } else {
                return null;
            }
        });
    }


    public final String getMajorVersionAsJson(final String parameter) {
        MajorVersion majorVersion = getMajorVersion(parameter);
        if (null == majorVersion) {
            return new StringBuilder().append("{").append("\n")
                                      .append("  \"value\"").append(":").append("\"").append(majorVersion).append("\"").append(",").append("\n")
                                      .append("  \"detail\"").append(":").append("\"Requested release has wrong format or is null.\"").append(",").append("\n")
                                      .append("  \"supported\"").append(":").append("\"1 - next early access").append(",current, last, latest, next, prev, last_lts, latest_lts, last_mts, latest_mts, last_sts, latest_mts, next_lts, next_mts, next_sts\"").append("\n")
                                      .append("}")
                                      .toString();
        } else {
            return majorVersion.toString();
        }
    }
    public final CompletableFuture<String> getMajorVersionAsJsonAsync(final String parameter) {
        return getMajorVersionAsync(parameter).thenApply(majorVersion -> {
            if (null == majorVersion) {
                return new StringBuilder().append("{").append("\n")
                                          .append("  \"value\"").append(":").append("\"").append(majorVersion).append("\"").append(",").append("\n")
                                          .append("  \"detail\"").append(":").append("\"Requested release has wrong format or is null.\"").append(",").append("\n")
                                          .append("  \"supported\"").append(":").append("\"1 - next early access").append(",current, last, latest, next, prev, last_lts, latest_lts, last_mts, latest_mts, last_sts, latest_mts, next_lts, next_mts, next_sts\"").append("\n")
                                          .append("}")
                                          .toString();
            } else {
                return majorVersion.toString();
            }
        });
    }


    public final List<MajorVersion> getMaintainedMajorVersions() { return getMaintainedMajorVersions(false); }
    public final List<MajorVersion> getMaintainedMajorVersions(final boolean include_ea) { return getMaintainedMajorVersions(include_ea, true); }
    public final List<MajorVersion> getMaintainedMajorVersions(final boolean include_ea, final boolean include_build) {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getMajorVersionsPath())
                                                        .append("?maintained=true&ga=true")
                                                        .append(include_ea ? "&ea=true" : "")
                                                        .append(include_build ? "" : "&include_build=false");

        String             query              = queryBuilder.toString();
        String             bodyText           = Helper.get(query, userAgent).body();
        List<MajorVersion> majorVersionsFound = new ArrayList<>();

        Gson        gson     = new Gson();
        JsonElement element  = gson.fromJson(bodyText, JsonElement.class);
        if (element instanceof JsonObject) {
            JsonObject jsonObject = element.getAsJsonObject();
            JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject majorVersionJsonObj = jsonArray.get(i).getAsJsonObject();
                majorVersionsFound.add(new MajorVersion(majorVersionJsonObj.toString()));
            }
        }
        return majorVersionsFound;
    }


    public final CompletableFuture<List<MajorVersion>> getMaintainedMajorVersionsAsync() { return getMaintainedMajorVersionsAsync(false); }
    public final CompletableFuture<List<MajorVersion>> getMaintainedMajorVersionsAsync(final boolean include_ea) {
        return getMaintainedMajorVersionsAsync(include_ea, true);
    }
    public final CompletableFuture<List<MajorVersion>> getMaintainedMajorVersionsAsync(final boolean include_ea, final boolean include_build) {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getMajorVersionsPath())
                                                        .append("?maintained=true&ga=true")
                                                        .append(include_ea ? "&ea=true" : "")
                                                        .append(include_build ? "" : "&include_build=false");

        String query = queryBuilder.toString();
        return Helper.getAsync(query, userAgent).thenApply(response -> {
            List<MajorVersion> majorVersionsFound = new ArrayList<>();

            Gson        gson     = new Gson();
            JsonElement element  = gson.fromJson(response.body(), JsonElement.class);
            if (element instanceof JsonObject) {
                JsonObject jsonObject = element.getAsJsonObject();
                JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject majorVersionJsonObj = jsonArray.get(i).getAsJsonObject();
                    majorVersionsFound.add(new MajorVersion(majorVersionJsonObj.toString()));
                }
            }
            return majorVersionsFound;
        });
    }


    public final List<MajorVersion> getUsefulMajorVersions() {
        return getUsefulMajorVersions(true);
    }
    public final List<MajorVersion> getUsefulMajorVersions(final boolean include_build) {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getMajorVersionsPath())
                                                        .append("/useful")
                                                        .append(include_build ? "" : "&include_build=false");

        String             query              = queryBuilder.toString();
        String             bodyText           = Helper.get(query, userAgent).body();
        List<MajorVersion> majorVersionsFound = new ArrayList<>();

        Gson        gson     = new Gson();
        JsonElement element  = gson.fromJson(bodyText, JsonElement.class);
        if (element instanceof JsonObject) {
            JsonObject jsonObject = element.getAsJsonObject();
            JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject majorVersionJsonObj = jsonArray.get(i).getAsJsonObject();
                majorVersionsFound.add(new MajorVersion(majorVersionJsonObj.toString()));
            }
        }
        return majorVersionsFound;
    }


    public final CompletableFuture<List<MajorVersion>> getUsefulMajorVersionsAsync() {
        return getUsefulMajorVersionsAsync(true);
    }
    public final CompletableFuture<List<MajorVersion>> getUsefulMajorVersionsAsync(final boolean include_build) {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getMajorVersionsPath())
                                                        .append("/useful")
                                                        .append(include_build ? "" : "include_build=false");

        String query = queryBuilder.toString();
        return Helper.getAsync(query, userAgent).thenApply(response -> {
            List<MajorVersion> majorVersionsFound = new ArrayList<>();

            Gson        gson     = new Gson();
            JsonElement element  = gson.fromJson(response.body(), JsonElement.class);
            if (element instanceof JsonObject) {
                JsonObject jsonObject = element.getAsJsonObject();
                JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject majorVersionJsonObj = jsonArray.get(i).getAsJsonObject();
                    majorVersionsFound.add(new MajorVersion(majorVersionJsonObj.toString()));
                }
            }
            return majorVersionsFound;
        });
    }


    public final MajorVersion getLatestLts(final boolean include_ea) { return getLatestLts(include_ea, true); }
    public final MajorVersion getLatestLts(final boolean include_ea, final boolean include_build) {
        Queue<MajorVersion> majorVersions = getAllMajorVersions(include_ea, include_build);
        return majorVersions.stream()
                            .filter(majorVersion -> TermOfSupport.LTS == majorVersion.getTermOfSupport())
                            .filter(majorVersion -> include_ea ? majorVersion.getVersions().size() > 0 : majorVersion.getVersions().size() > 1)
                            .findFirst().get();
    }
    public final CompletableFuture<MajorVersion> getLatestLtsAsync(final boolean include_ea) { return getLatestLtsAsync(include_ea, true); }
    public final CompletableFuture<MajorVersion> getLatestLtsAsync(final boolean include_ea, final boolean include_build) {
        return getAllMajorVersionsAsync(include_ea, include_build).thenApply(majorVersions -> majorVersions.stream()
                                                                                              .filter(majorVersion -> TermOfSupport.LTS == majorVersion.getTermOfSupport())
                                                                                              .filter(majorVersion -> include_ea ? majorVersion.getVersions().size() > 0 : majorVersion.getVersions().size() > 1)
                                                                                              .findFirst().get());

    }


    public final MajorVersion getLatestMts(final boolean include_ea) { return getLatestMts(include_ea, true); }
    public final MajorVersion getLatestMts(final boolean include_ea, final boolean include_build) {
        Queue<MajorVersion> majorVersions = getAllMajorVersions(include_ea, include_build);
        return majorVersions.stream()
                            .filter(majorVersion -> TermOfSupport.MTS == majorVersion.getTermOfSupport())
                            .filter(majorVersion -> include_ea ? majorVersion.getVersions().size() > 0 : majorVersion.getVersions().size() > 1)
                            .findFirst().get();
    }
    public final CompletableFuture<MajorVersion> getLatestMtsAsync(final boolean include_ea) { return getLatestLtsAsync(include_ea, true); }
    public final CompletableFuture<MajorVersion> getLatestMtsAsync(final boolean include_ea, final boolean include_build) {
        return getAllMajorVersionsAsync(include_ea, include_build).thenApply(majorVersions -> majorVersions.stream()
                                                                                                           .filter(majorVersion -> TermOfSupport.MTS == majorVersion.getTermOfSupport())
                                                                                                           .filter(majorVersion -> include_ea ? majorVersion.getVersions().size() > 0 : majorVersion.getVersions().size() > 1)
                                                                                                           .findFirst().get());
    }


    public final MajorVersion getLatestSts(final boolean include_ea) { return getLatestSts(include_ea, true); }
    public final MajorVersion getLatestSts(final boolean include_ea, final boolean include_build) {
        Queue<MajorVersion> majorVersions = getAllMajorVersions(include_ea, include_build);
        return majorVersions.stream()
                            .filter(majorVersion -> TermOfSupport.LTS != majorVersion.getTermOfSupport())
                            .filter(majorVersion -> include_ea ? majorVersion.getVersions().size() > 0 : majorVersion.getVersions().size() > 1)
                            .findFirst().get();
    }
    public final CompletableFuture<MajorVersion> getLatestStsAsync(final boolean include_ea) { return getLatestStsAsync(include_ea, true); }
    public final CompletableFuture<MajorVersion> getLatestStsAsync(final boolean include_ea, final boolean include_build) {
        return getAllMajorVersionsAsync(include_ea, include_build).thenApply(majorVersions -> majorVersions.stream()
                                                                                                           .filter(majorVersion -> TermOfSupport.LTS != majorVersion.getTermOfSupport())
                                                                                                           .filter(majorVersion -> include_ea ? majorVersion.getVersions().size() > 0 : majorVersion.getVersions().size() > 1)
                                                                                                           .findFirst().get());
    }


    public final Set<Distribution> getDistributionsThatSupportVersion(final String version) {
        SemVer semver = SemVer.fromText(version).getSemVer1();
        if (null == semver) {
            return new HashSet<>();
        }
        return getDistributionsThatSupportVersion(semver);
    }
    public final Set<Distribution> getDistributionsThatSupportVersion(final SemVer semVer) { return getDistributionsForSemVer(semVer); }
    public final CompletableFuture<Set<Distribution>> getDistributionsThatSupportSemVerAsync(final SemVer semVer) { return getDistributionsForSemVerAsync(semVer); }


    public final CompletableFuture<List<Distribution>> getDistributionsThatSupportVersionAsync(final String version) {
        return getDistributionsThatSupportVersionAsync(VersionNumber.fromText(version));
    }
    public final CompletableFuture<List<Distribution>> getDistributionsThatSupportVersionAsync(final VersionNumber versionNumber) {
        return getDistributionsForVersionAsync(versionNumber);
    }
    public final List<Distribution> getDistributionsThatSupportVersion(final VersionNumber versionNumber) {
        return getDistributionsForVersion(versionNumber);
    }


    public final List<Distribution> getDistributionsThatSupport(final SemVer semVer, final OperatingSystem operatingSystem, final Architecture architecture,
                                                                final LibCType libcType, final ArchiveType archiveType, final PackageType packageType,
                                                                final Boolean javafxBundled, final Boolean directlyDownloadable) {
        return getPkgs(null, semVer.getVersionNumber(), Latest.NONE, operatingSystem, libcType, architecture,
                       Bitness.NONE, archiveType, packageType, javafxBundled, directlyDownloadable, List.of(semVer.getReleaseStatus()),
                       TermOfSupport.NONE, List.of(Scope.PUBLIC), Match.ANY).stream()
                                                                            .map(pkg -> pkg.getDistribution())
                                                                            .distinct()
                                                                            .collect(Collectors.toList());
    }
    public final CompletableFuture<List<Distribution>> getDistributionsThatSupportAsync(final SemVer semVer, final OperatingSystem operatingSystem, final Architecture architecture,
                                                                                        final LibCType libcType, final ArchiveType archiveType, final PackageType packageType,
                                                                                        final Boolean javafxBundled, final Boolean directlyDownloadable) {
        return getPkgsAsync(null, semVer.getVersionNumber(), Latest.NONE, operatingSystem, libcType, architecture,
                            Bitness.NONE, archiveType, packageType, javafxBundled, directlyDownloadable, List.of(semVer.getReleaseStatus()),
                            TermOfSupport.NONE, List.of(Scope.PUBLIC), Match.ANY).thenApply(pkgs -> pkgs.stream()
                                                                                                        .map(pkg -> pkg.getDistribution())
                                                                                                        .distinct()
                                                                                                        .collect(Collectors.toList()));
    }


    public final List<Pkg> updateAvailableFor(final Distribution distribution, final SemVer semver, final Boolean javafxBundled) {
        return updateAvailableFor(distribution, semver, getOperatingSystem(), getArchitecture(), javafxBundled, Boolean.TRUE);
    }
    public final List<Pkg> updateAvailableFor(final Distribution distribution, final SemVer semver, final Architecture architecture, final Boolean javafxBundled) {
        return updateAvailableFor(distribution, semver, getOperatingSystem(), architecture, javafxBundled, Boolean.TRUE);
    }
    public final List<Pkg> updateAvailableFor(final Distribution distribution, final SemVer semver, final Architecture architecture, final Boolean javafxBundled, final Boolean directlyDownloadable) {
        return updateAvailableFor(distribution, semver, getOperatingSystem(), architecture, javafxBundled, directlyDownloadable);
    }
    public final List<Pkg> updateAvailableFor(final Distribution distribution, final SemVer semver, final OperatingSystem operatingSystem, final Architecture architecture, final Boolean javafxBundled, final Boolean directlyDownloadable) {
        return updateAvailableFor(distribution, semver, operatingSystem, architecture, javafxBundled, directlyDownloadable, null);
    }
    public final List<Pkg> updateAvailableFor(final Distribution distribution, final SemVer semver, final OperatingSystem operatingSystem, final Architecture architecture, final Boolean javafxBundled, final Boolean directlyDownloadable, final String feature) {
        List<Pkg> updatesFound = new ArrayList<>();
        try {
            List<String> features = null == feature ? List.of() : List.of(feature);
            List<Pkg> pkgs = getPkgs(null == distribution ? null : List.of(distribution), semver.getVersionNumber(), Latest.AVAILABLE, operatingSystem, LibCType.NONE, architecture, Bitness.NONE, ArchiveType.NONE, PackageType.JDK, javafxBundled, directlyDownloadable, List.of(ReleaseStatus.EA, ReleaseStatus.GA), TermOfSupport.NONE, features, List.of(Scope.PUBLIC), Match.ANY);
            Collections.sort(pkgs, Comparator.comparing(Pkg::getJavaVersion).reversed());
            if (pkgs.isEmpty()) {
                return updatesFound;
            } else {
                Pkg    firstEntry  = pkgs.get(0);
                SemVer semVerFound = firstEntry.getJavaVersion();
                if (ReleaseStatus.EA == semVerFound.getReleaseStatus()) {
                    if (semVerFound.compareTo(semver) > 0) {
                        updatesFound = pkgs.stream().filter(pkg -> pkg.getJavaVersion().compareTo(semVerFound) == 0).collect(Collectors.toList());
                    }
                } else {
                    if (semVerFound.compareToIgnoreBuild(semver) > 0) {
                        updatesFound = pkgs.stream().filter(pkg -> pkg.getJavaVersion().compareToIgnoreBuild(semVerFound) == 0).collect(Collectors.toList());
                    }
                }
                return updatesFound;
            }
        } catch (RuntimeException e) {
            System.out.println("Error getting updates for " + distribution.getName() + ": " + e);
        }
        return updatesFound;
    }

    public final CompletableFuture<List<Pkg>> updateAvailableForAsync(final Distribution distribution, final SemVer semver, final Boolean javafxBundled) {
        return updateAvailableForAsync(distribution, semver, getOperatingSystem(), getArchitecture(), javafxBundled, Boolean.TRUE);
    }
    public final CompletableFuture<List<Pkg>> updateAvailableForAsync(final Distribution distribution, final SemVer semver, final Architecture architecture, final Boolean javafxBundled) {
        return updateAvailableForAsync(distribution, semver, getOperatingSystem(), architecture, javafxBundled, Boolean.TRUE);
    }
    public final CompletableFuture<List<Pkg>> updateAvailableForAsync(final Distribution distribution, final SemVer semver, final Architecture architecture, final Boolean javafxBundled, final Boolean directlyDownloadable) {
        return updateAvailableForAsync(distribution, semver, getOperatingSystem(), architecture, javafxBundled, directlyDownloadable);
    }
    public final CompletableFuture<List<Pkg>> updateAvailableForAsync(final Distribution distribution, final SemVer semver, final OperatingSystem operatingSystem, final Architecture architecture, final Boolean javafxBundled, final Boolean directlyDownloadable) {
        return getPkgsAsync(null == distribution ? null : List.of(distribution), semver.getVersionNumber(), Latest.AVAILABLE, operatingSystem, LibCType.NONE, architecture, Bitness.NONE, ArchiveType.NONE, PackageType.JDK, javafxBundled,
                            directlyDownloadable, List.of(ReleaseStatus.EA, ReleaseStatus.GA), TermOfSupport.NONE, List.of(Scope.PUBLIC), Match.ANY).thenApplyAsync(pkgs -> {
            Collections.sort(pkgs, Comparator.comparing(Pkg::getJavaVersion).reversed());

            List<Pkg> updatesFound = new ArrayList<>();
            if (pkgs.isEmpty()) {
                return updatesFound;
            } else {
                Pkg    firstEntry  = pkgs.get(0);
                SemVer semVerFound = firstEntry.getJavaVersion();
                if (ReleaseStatus.EA == semVerFound.getReleaseStatus()) {
                    if (semVerFound.compareTo(semver) > 0) {
                        updatesFound = pkgs.stream().filter(pkg -> pkg.getJavaVersion().compareTo(semVerFound) == 0).collect(Collectors.toList());
                    }
                } else {
                    if (semVerFound.compareToIgnoreBuild(semver) > 0) {
                        updatesFound = pkgs.stream().filter(pkg -> pkg.getJavaVersion().compareToIgnoreBuild(semVerFound) == 0).collect(Collectors.toList());
                    }
                }
                return updatesFound;
            }
        });
    }


    public final List<Distribution> getDistributions() {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getDistributionsPath());

        String             query              = queryBuilder.toString();
        String             bodyText           = Helper.get(query, userAgent).body();
        List<Distribution> distributionsFound = new LinkedList<>();

        Gson        gson     = new Gson();
        JsonElement element  = gson.fromJson(bodyText, JsonElement.class);
        if (element instanceof JsonObject) {
            JsonObject jsonObject = element.getAsJsonObject();
            JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject distributionJsonObj = jsonArray.get(i).getAsJsonObject();
                final String api_parameter = distributionJsonObj.get("api_parameter").getAsString();
                Distribution distribution = getDistributionFromText(api_parameter);
                if (null == distribution) { continue; }
                distributionsFound.add(distribution);
            }
        }
        return distributionsFound;
    }
    public final CompletableFuture<List<Distribution>> getDistributionsAsync() {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getDistributionsPath());
        String query = queryBuilder.toString();
        return Helper.getAsync(query, userAgent).thenApply(response -> {
            List<Distribution> distributionsFound = new LinkedList<>();
            Gson               gson               = new Gson();
            JsonElement        element            = gson.fromJson(response.body(), JsonElement.class);
            if (element instanceof JsonObject) {
                JsonObject jsonObject = element.getAsJsonObject();
                JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject distributionJsonObj = jsonArray.get(i).getAsJsonObject();
                    final String       api_parameter     = distributionJsonObj.get("api_parameter").getAsString();
                    final Distribution distributionFound = getDistributionFromText(api_parameter);
                    if (null == distributionFound) { continue; }
                    distributionsFound.add(distributionFound);
                }
            }
            return distributionsFound;
        });
    }
    
    public final Set<Distribution> getDistributionsForSemVer(final SemVer semVer) {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getDistributionsPath())
                                                        .append("/versions/")
                                                        .append(Helper.encodeValue(semVer.toString(true)));
        String             query              = queryBuilder.toString();
        String             bodyText           = Helper.get(query, userAgent).body();
        Set<Distribution> distributionsFound = new LinkedHashSet<>();

        Gson        gson     = new Gson();
        JsonElement element  = gson.fromJson(bodyText, JsonElement.class);
        if (element instanceof JsonObject) {
            JsonObject jsonObject = element.getAsJsonObject();
            JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject distributionJsonObj = jsonArray.get(i).getAsJsonObject();
                final String       api_parameter     = distributionJsonObj.has("api_parameter") ? distributionJsonObj.get("api_parameter").getAsString() : "";
                final Distribution distributionFound = api_parameter.isEmpty() ? null : getDistributionFromText(api_parameter);
                if (null == distributionFound) { continue; }
                distributionsFound.add(distributionFound);
            }
        }
        return distributionsFound;
    }
    public final CompletableFuture<Set<Distribution>> getDistributionsForSemVerAsync(final SemVer semVer) {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getDistributionsPath())
                                                        .append("/versions/")
                                                        .append(Helper.encodeValue(semVer.toString(true)));
        String query = queryBuilder.toString();
        return Helper.getAsync(query, userAgent).thenApply(response -> {
            Set<Distribution> distributionsFound = new LinkedHashSet<>();
            Gson              gson               = new Gson();
            JsonElement       element            = gson.fromJson(response.body(), JsonElement.class);
            if (element instanceof JsonObject) {
                JsonObject jsonObject = element.getAsJsonObject();
                JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject distributionJsonObj = jsonArray.get(i).getAsJsonObject();
                    final String       api_parameter     = distributionJsonObj.has("api_parameter") ? distributionJsonObj.get("api_parameter").getAsString() : "";
                    final Distribution distributionFound = api_parameter.isEmpty() ? null : getDistributionFromText(api_parameter);
                    if (null == distributionFound) { continue; }
                    distributionsFound.add(distributionFound);
                }
            }
            return distributionsFound;
        });
    }

    public final List<Distribution> getDistributionsForVersion(final VersionNumber versionNumber) {
        return getDistributionsForVersion(versionNumber, List.of(), Match.ANY);
    }
    public final List<Distribution> getDistributionsForVersion(final VersionNumber versionNumber, final List<Scope> scopes, final Match match) {
        StringBuilder scopeBuilder = new StringBuilder();
        if (!scopes.isEmpty()) {
            scopeBuilder.append("?discovery_scope_id=")
                        .append(scopes.stream().map(scope -> scope.getApiString()).collect(Collectors.joining("&discovery_scope_id=")));
            if (null != match && Match.NONE != match && Match.NOT_FOUND != match) {
                scopeBuilder.append("&match=").append(match.getApiString());
            }
        }

        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getDistributionsPath())
                                                        .append("/versions/")
                                                        .append(Helper.encodeValue(versionNumber.toString(OutputFormat.REDUCED_COMPRESSED, true, true)))
                                                        .append(scopeBuilder);

        String             query              = queryBuilder.toString();
        String             bodyText           = Helper.get(query, userAgent).body();
        List<Distribution> distributionsFound = new LinkedList<>();

        Gson        gson     = new Gson();
        JsonElement element  = gson.fromJson(bodyText, JsonElement.class);
        if (element instanceof JsonObject) {
            JsonObject jsonObject = element.getAsJsonObject();
            JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject distributionJsonObj = jsonArray.get(i).getAsJsonObject();
                final String       api_parameter     = distributionJsonObj.get("api_parameter").getAsString();
                final Distribution distributionFound = getDistributionFromText(api_parameter);
                if (null == distributionFound) { continue; }
                distributionsFound.add(distributionFound);
            }
        }
        return distributionsFound;
    }
    public final CompletableFuture<List<Distribution>> getDistributionsForVersionAsync(final VersionNumber versionNumber) {
        return getDistributionsForVersionAsync(versionNumber, List.of(), Match.ANY);
    }
    public final CompletableFuture<List<Distribution>> getDistributionsForVersionAsync(final VersionNumber versionNumber, final List<Scope> scopes, final Match match) {
        StringBuilder scopeBuilder = new StringBuilder();
        if (!scopes.isEmpty()) {
            scopeBuilder.append("?discovery_scope_id=")
                        .append(scopes.stream().map(scope -> scope.getApiString()).collect(Collectors.joining("&discovery_scope_id=")));
            if (null != match && Match.NONE != match && Match.NOT_FOUND != match) {
                scopeBuilder.append("&match=").append(match.getApiString());
            }
        }

        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getDistributionsPath())
                                                        .append("/versions/")
                                                        .append(Helper.encodeValue(versionNumber.toString(OutputFormat.REDUCED_COMPRESSED, true, true)))
                                                        .append(scopeBuilder);

        String query = queryBuilder.toString();
        return Helper.getAsync(query, userAgent).thenApply(response -> {
            List<Distribution> distributionsFound = new LinkedList<>();
            Gson               gson               = new Gson();
            JsonElement        element            = gson.fromJson(response.body(), JsonElement.class);
            if (element instanceof JsonObject) {
                JsonObject jsonObject = element.getAsJsonObject();
                JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject distributionJsonObj = jsonArray.get(i).getAsJsonObject();
                    final String       api_parameter     = distributionJsonObj.get("api_parameter").getAsString();
                    final Distribution distributionFound = getDistributionFromText(api_parameter);
                    if (null == distributionFound) { continue; }
                    distributionsFound.add(distributionFound);
                }
            }
            return distributionsFound;
        });
    }

    public static Map<Distribution, List<VersionNumber>> getVersionsPerDistribution() {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getDistributionsPath());
        String                                 query              = queryBuilder.toString();
        String                                 bodyText           = Helper.get(query, "").body();
        Map<Distribution, List<VersionNumber>> distributionsFound = new LinkedHashMap<>();
        Gson                                   gson               = new Gson();
        JsonElement                            element            = gson.fromJson(bodyText, JsonElement.class);
        if (element instanceof JsonObject) {
            JsonObject jsonObject = element.getAsJsonObject();
            JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
            for (int i = 0; i < jsonArray.size(); i++) {
                final JsonObject          distributionJsonObj = jsonArray.get(i).getAsJsonObject();
                final String              api_parameter = distributionJsonObj.get("api_parameter").getAsString();
                final Distribution        distribution  = getDistributionFromText(api_parameter);
                final List<VersionNumber> versions      = new LinkedList<>();
                final JsonArray           versionsArray = distributionJsonObj.get("versions").getAsJsonArray();
                if (null == distribution) { continue; }
                for (int j = 0 ; j < versionsArray.size() ; j++) {
                    VersionNumber versionNumber = VersionNumber.fromText(versionsArray.get(j).getAsString());
                    versions.add(versionNumber);
                }
                distributionsFound.put(distribution, versions);
            }
        }
        return distributionsFound;
    }
    public static CompletableFuture<Map<Distribution, List<VersionNumber>>> getVersionsPerDistributionAsync() {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getDistributionsPath());

        String query = queryBuilder.toString();
        return Helper.getAsync(query, "").thenApply(response -> {
            Map<Distribution, List<VersionNumber>> distributionsFound = new LinkedHashMap<>();
            Gson                                   gson               = new Gson();
            JsonElement                            element            = gson.fromJson(response.body(), JsonElement.class);
            if (element instanceof JsonObject) {
                JsonObject jsonObject = element.getAsJsonObject();
                JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
                for (int i = 0; i < jsonArray.size(); i++) {
                    final JsonObject          distributionJsonObj = jsonArray.get(i).getAsJsonObject();
                    final String              api_parameter = distributionJsonObj.get("api_parameter").getAsString();
                    final Distribution        distribution  = getDistributionFromText(api_parameter);
                    final List<VersionNumber> versions      = new LinkedList<>();
                    final JsonArray           versionsArray       = distributionJsonObj.get("versions").getAsJsonArray();
                    if (null == distribution) { continue; }
                    for (int j = 0 ; j < versionsArray.size() ; j++) {
                        VersionNumber versionNumber = VersionNumber.fromText(versionsArray.get(j).getAsString());
                        versions.add(versionNumber);
                    }
                    distributionsFound.put(distribution, versions);
                }
            }
            return distributionsFound;
        });
    }


    public Map<String, Distribution> getDistros() { return DISTRIBUTIONS; }


    public List<Distribution> getDistributionsBasedOnOpenJDK() {
        return DISTRIBUTIONS.values()
                            .stream()
                            .filter(distribution -> distribution.getScopes().contains(Scope.BUILD_OF_OPEN_JDK))
                            .filter(distribution -> distribution.getScopes().contains(Scope.PUBLIC))
                            .collect(Collectors.toList());
    }

    public List<Distribution> getDistributionsBasedOnGraalVm() {
        return DISTRIBUTIONS.values()
                            .stream()
                            .filter(distribution -> distribution.getScopes().contains(Scope.BUILD_OF_GRAALVM))
                            .filter(distribution -> distribution.getScopes().contains(Scope.PUBLIC))
                            .collect(Collectors.toList());
    }


    public final String getPkgDirectDownloadUri(final String pkgId) {
        if (null == pkgId || pkgId.isEmpty()) { throw new IllegalArgumentException("Package ID not valid"); }
        final Pkg pkg = getPkg(pkgId);
        if (PropertyManager.INSTANCE.getApiVersion().equals(API_VERSION_V2)) {
            return getPkgInfoByEphemeralId(pkg.getEphemeralId(), pkg.getJavaVersion()).getDirectDownloadUri();
        } else {
            return getPkgInfoByPkgId(pkgId, pkg.getJavaVersion()).getDirectDownloadUri();
        }
    }
    public final CompletableFuture<String> getPkgDirectDownloadUriAsync(final String pkgId) {
        if (null == pkgId || pkgId.isEmpty()) { throw new IllegalArgumentException("Package ID not valid"); }
        if (PropertyManager.INSTANCE.getApiVersion().equals(API_VERSION_V2)) {
            return getPkgAsync(pkgId).thenApply(pkg -> getPkgInfoByEphemeralIdAsync(pkg.getEphemeralId(), pkg.getJavaVersion()).thenApply(pkgInfo -> pkgInfo.getDirectDownloadUri())).join();
        } else {
            return getPkgAsync(pkgId).thenApply(pkg -> getPkgInfoByPkgIdAsync(pkgId, pkg.getJavaVersion()).thenApply(pkgInfo -> pkgInfo.getDirectDownloadUri())).join();
        }
    }


    public final String getPkgDownloadSiteUri(final String pkgId) {
        if (null == pkgId || pkgId.isEmpty()) { throw new IllegalArgumentException("Package ID not valid"); }
        final Pkg pkg = getPkg(pkgId);
        if (PropertyManager.INSTANCE.getApiVersion().equals(API_VERSION_V2)) {
            return getPkgInfoByEphemeralId(pkg.getEphemeralId(), pkg.getJavaVersion()).getDownloadSiteUri();
        } else {
            return getPkgInfoByPkgId(pkgId, pkg.getJavaVersion()).getDownloadSiteUri();
        }
    }
    public final CompletableFuture<String> getPkgDownloadSiteUriAsync(final String pkgId) {
        if (null == pkgId || pkgId.isEmpty()) { throw new IllegalArgumentException("Package ID not valid"); }
        if (PropertyManager.INSTANCE.getApiVersion().equals(API_VERSION_V2)) {
            return getPkgAsync(pkgId).thenApply(pkg -> getPkgInfoByEphemeralIdAsync(pkg.getEphemeralId(), pkg.getJavaVersion()).thenApply(pkgInfo -> pkgInfo.getDownloadSiteUri())).join();
        } else {
            return getPkgAsync(pkgId).thenApply(pkg -> getPkgInfoByPkgIdAsync(pkgId, pkg.getJavaVersion()).thenApply(pkgInfo -> pkgInfo.getDownloadSiteUri())).join();
        }
    }


    public PkgInfo getPkgInfoByEphemeralId(final String ephemeralId, final SemVer javaVersion) {
        if (null == ephemeralId || ephemeralId.isEmpty() || null == javaVersion) { throw new IllegalArgumentException("ephemeralId or javaVersion cannot be null"); }
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getEphemeralIdsPath())
                                                        .append("/")
                                                        .append(ephemeralId);

        String      query              = queryBuilder.toString();
        String      packageInfoBody    = Helper.get(query, userAgent).body();
        Gson        packageInfoGson    = new Gson();
        JsonElement packageInfoElement = packageInfoGson.fromJson(packageInfoBody, JsonElement.class);
        if (packageInfoElement instanceof JsonObject) {
            JsonObject jsonObject = packageInfoElement.getAsJsonObject();
            JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
            if (jsonArray.size() > 0) {
                final JsonObject packageInfoJson   = jsonArray.get(0).getAsJsonObject();
                final String     filename          = packageInfoJson.has(PkgInfo.FIELD_FILENAME)            ? packageInfoJson.get(PkgInfo.FIELD_FILENAME).getAsString()            : "";
                final String     directDownloadUri = packageInfoJson.has(PkgInfo.FIELD_DIRECT_DOWNLOAD_URI) ? packageInfoJson.get(PkgInfo.FIELD_DIRECT_DOWNLOAD_URI).getAsString() : "";
                final String     downloadSiteUri   = packageInfoJson.has(PkgInfo.FIELD_DIRECT_DOWNLOAD_URI) ? packageInfoJson.get(PkgInfo.FIELD_DOWNLOAD_SITE_URI).getAsString()   : "";
                if (null == filename) { return null; }
                return new PkgInfo(filename, javaVersion, directDownloadUri, downloadSiteUri);
            } else {
                return null;
            }
        }
        return null;
    }
    public CompletableFuture<PkgInfo> getPkgInfoByEphemeralIdAsync(final String ephemeralId, final SemVer javaVersion) {
        if (null == ephemeralId || ephemeralId.isEmpty() || null == javaVersion) { throw new IllegalArgumentException("ephemeralId or javaVersion cannot be null"); }
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getEphemeralIdsPath())
                                                        .append("/")
                                                        .append(ephemeralId);
        String query           = queryBuilder.toString();
        return Helper.getAsync(query, userAgent).thenApply(response -> {
            Gson        packageInfoGson    = new Gson();
            JsonElement packageInfoElement = packageInfoGson.fromJson(response.body(), JsonElement.class);
            if (packageInfoElement instanceof JsonObject) {
                JsonObject jsonObject = packageInfoElement.getAsJsonObject();
                JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
                if (jsonArray.size() > 0) {
                    final JsonObject packageInfoJson   = jsonArray.get(0).getAsJsonObject();
                    final String     filename          = packageInfoJson.has(PkgInfo.FIELD_FILENAME)            ? packageInfoJson.get(PkgInfo.FIELD_FILENAME).getAsString()            : "";
                    final String     directDownloadUri = packageInfoJson.has(PkgInfo.FIELD_DIRECT_DOWNLOAD_URI) ? packageInfoJson.get(PkgInfo.FIELD_DIRECT_DOWNLOAD_URI).getAsString() : "";
                    final String     downloadSiteUri   = packageInfoJson.has(PkgInfo.FIELD_DIRECT_DOWNLOAD_URI) ? packageInfoJson.get(PkgInfo.FIELD_DOWNLOAD_SITE_URI).getAsString()   : "";
                    if (null == filename) { return null; }
                    return new PkgInfo(filename, javaVersion, directDownloadUri, downloadSiteUri);
                } else {
                    return null;
                }
            }
            return null;
        });
    }

    public PkgInfo getPkgInfoByPkgId(final String pkgId, final SemVer javaVersion) {
        if (null == pkgId || pkgId.isEmpty() || null == javaVersion) { throw new IllegalArgumentException("pkgId or javaVersion cannot be null"); }
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getIdsPath())
                                                        .append("/")
                                                        .append(pkgId);

        String      query              = queryBuilder.toString();
        String      packageInfoBody    = Helper.get(query, userAgent).body();
        Gson        packageInfoGson    = new Gson();
        JsonElement packageInfoElement = packageInfoGson.fromJson(packageInfoBody, JsonElement.class);
        if (packageInfoElement instanceof JsonObject) {
            JsonObject jsonObject = packageInfoElement.getAsJsonObject();
            JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
            if (jsonArray.size() > 0) {
                final JsonObject packageInfoJson   = jsonArray.get(0).getAsJsonObject();
                final String     filename          = packageInfoJson.has(PkgInfo.FIELD_FILENAME)            ? packageInfoJson.get(PkgInfo.FIELD_FILENAME).getAsString()            : "";
                final String     directDownloadUri = packageInfoJson.has(PkgInfo.FIELD_DIRECT_DOWNLOAD_URI) ? packageInfoJson.get(PkgInfo.FIELD_DIRECT_DOWNLOAD_URI).getAsString() : "";
                final String     downloadSiteUri   = packageInfoJson.has(PkgInfo.FIELD_DIRECT_DOWNLOAD_URI) ? packageInfoJson.get(PkgInfo.FIELD_DOWNLOAD_SITE_URI).getAsString()   : "";
                if (null == filename) { return null; }
                return new PkgInfo(filename, javaVersion, directDownloadUri, downloadSiteUri);
            } else {
                return null;
            }
        }
        return null;
    }
    public CompletableFuture<PkgInfo> getPkgInfoByPkgIdAsync(final String pkgId, final SemVer javaVersion) {
        if (null == pkgId || pkgId.isEmpty() || null == javaVersion) { throw new IllegalArgumentException("pkgId or javaVersion cannot be null"); }
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getIdsPath())
                                                        .append("/")
                                                        .append(pkgId);
        String query           = queryBuilder.toString();
        return Helper.getAsync(query, userAgent).thenApply(response -> {
            Gson        packageInfoGson    = new Gson();
            JsonElement packageInfoElement = packageInfoGson.fromJson(response.body(), JsonElement.class);
            if (packageInfoElement instanceof JsonObject) {
                JsonObject jsonObject = packageInfoElement.getAsJsonObject();
                JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
                if (jsonArray.size() > 0) {
                    final JsonObject packageInfoJson   = jsonArray.get(0).getAsJsonObject();
                    final String     filename          = packageInfoJson.has(PkgInfo.FIELD_FILENAME)            ? packageInfoJson.get(PkgInfo.FIELD_FILENAME).getAsString()            : "";
                    final String     directDownloadUri = packageInfoJson.has(PkgInfo.FIELD_DIRECT_DOWNLOAD_URI) ? packageInfoJson.get(PkgInfo.FIELD_DIRECT_DOWNLOAD_URI).getAsString() : "";
                    final String     downloadSiteUri   = packageInfoJson.has(PkgInfo.FIELD_DIRECT_DOWNLOAD_URI) ? packageInfoJson.get(PkgInfo.FIELD_DOWNLOAD_SITE_URI).getAsString()   : "";
                    if (null == filename) { return null; }
                    return new PkgInfo(filename, javaVersion, directDownloadUri, downloadSiteUri);
                } else {
                    return null;
                }
            }
            return null;
        });
    }


    public final Future<?> downloadPkg(final String pkgId, final String targetFileName) throws InterruptedException {
        if (null == pkgId || pkgId.isEmpty()) { throw new IllegalArgumentException("pkgId cannot be null or empty"); }
        if (null == targetFileName || targetFileName.isEmpty()) { throw new IllegalArgumentException("targetFileName cannot be null or empty"); }
        Pkg pkg = getPkg(pkgId);
        if (null == pkg) {
            return null;
        } else {
            if (PropertyManager.INSTANCE.getApiVersion().equals(API_VERSION_V2)) {
                final SemVer javaVersion = pkg.getJavaVersion();
                final String ephemeralId = pkg.getEphemeralId();
                return downloadPkg(ephemeralId, javaVersion, targetFileName);
            } else {
                final SemVer javaVersion = pkg.getJavaVersion();
                return downloadPkgByPkgId(pkgId, javaVersion, targetFileName);
            }
        }
    }

    public final Future<?> downloadPkg(final String ephemeralId, final SemVer javaVersion, final String targetFileName) throws InterruptedException {
        if (null == ephemeralId || ephemeralId.isEmpty()) { throw new IllegalArgumentException("ephemeralId cannot be null or empty"); }
        if (null == javaVersion) { throw new IllegalArgumentException("javaVersion cannot be null"); }
        if (null == targetFileName || targetFileName.isEmpty()) { throw new IllegalArgumentException("targetFileName cannot be null or empty"); }
        final String              url      = getPkgInfoByEphemeralId(ephemeralId, javaVersion).getDirectDownloadUri();
        final FutureTask<Boolean> task     = createDownloadTask(targetFileName, url);
        final ExecutorService     executor = Executors.newSingleThreadExecutor();
        final Future<?>           future   = executor.submit(task);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);
        return future;
    }
    public final Future<?> downloadPkg(final PkgInfo pkgInfo, final String targetFileName) throws InterruptedException {
        if (null == pkgInfo) { throw new IllegalArgumentException("pkgInfo cannot be null"); }
        if (null == targetFileName || targetFileName.isEmpty()) { throw new IllegalArgumentException("targetFileName cannot be null or empty"); }
        final FutureTask<Boolean> task     = createDownloadTask(targetFileName, pkgInfo.getDirectDownloadUri());
        final ExecutorService     executor = Executors.newSingleThreadExecutor();
        final Future<?>           future   = executor.submit(task);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);
        return future;
    }

    private final Future<?> downloadPkgByPkgId(final String pkgId, final SemVer javaVersion, final String targetFileName) throws InterruptedException {
        final String              url      = getPkgInfoByPkgId(pkgId, javaVersion).getDirectDownloadUri();
        final FutureTask<Boolean> task     = createDownloadTask(targetFileName, url);
        final ExecutorService     executor = Executors.newSingleThreadExecutor();
        final Future<?>           future   = executor.submit(task);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);
        return future;
    }


    public Pkg getPkg(final String pkgId) {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getPackagesPath())
                                                        .append("/")
                                                        .append(pkgId);

        String query    = queryBuilder.toString();
        String bodyText = Helper.get(query, userAgent).body();

        Gson        pkgGson    = new Gson();
        JsonElement pkgElement = pkgGson.fromJson(bodyText, JsonElement.class);
        if (pkgElement instanceof JsonObject) {
            JsonObject jsonObject = pkgElement.getAsJsonObject();
            JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
            if (jsonArray.size() > 0) {
                final JsonObject pkgJson = jsonArray.get(0).getAsJsonObject();
                return new Pkg(pkgJson.toString());
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
    public CompletableFuture<Pkg> getPkgAsync(final String pkgId) {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(PropertyManager.INSTANCE.getPackagesPath())
                                                        .append("/")
                                                        .append(pkgId);
        String query = queryBuilder.toString();
        return Helper.getAsync(query, userAgent).thenApply(response -> {
            Gson        pkgGson    = new Gson();
            JsonElement pkgElement = pkgGson.fromJson(response.body(), JsonElement.class);
            if (pkgElement instanceof JsonObject) {
                JsonObject jsonObject = pkgElement.getAsJsonObject();
                JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
                if (jsonArray.size() > 0) {
                    final JsonObject pkgJson = jsonArray.get(0).getAsJsonObject();
                    return new Pkg(pkgJson.toString());
                } else {
                    return null;
                }
            } else {
                return null;
            }
        });
    }


    public static Distribution getDistributionFromText(final String text) {
        if (null == text) { return null; }
        if (DISTRIBUTIONS.isEmpty()) {
            preloadDistributions();
            while (initialized.get() == false) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {

                }
            }
        }
        return DISTRIBUTIONS.values().stream().filter(distribution -> distribution.getFromText(text) != null).findFirst().orElse(null);
    }


    public void cancelRequest() { Helper.cancelRequest(); }


    public String getUserAgent() { return userAgent; }
    public void setUserAgent(final String userAgent) {
        if (null == userAgent || userAgent.isEmpty()) { return; }
        this.userAgent = userAgent;
    }

    public static final OperatingSystem getOperatingSystem() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("win") >= 0) {
            return OperatingSystem.WINDOWS;
        } else if (os.indexOf("mac") >= 0) {
            return OperatingSystem.MACOS;
        } else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(DETECT_ALPINE_CMDS);
                Process        process        = processBuilder.start();
                String         result         = new BufferedReader(new InputStreamReader(process.getInputStream())).lines().collect(Collectors.joining("\n"));
                return null == result ? OperatingSystem.LINUX : result.equals("1") ? OperatingSystem.ALPINE_LINUX : OperatingSystem.LINUX;
            } catch (IOException e) {
                e.printStackTrace();
                return OperatingSystem.LINUX;
            }
        } else if (os.indexOf("sunos") >= 0) {
            return OperatingSystem.SOLARIS;
        } else {
            return OperatingSystem.NONE;
        }
    }

    public static Architecture getArchitecture() {
        try {
            ProcessBuilder processBuilder = OperatingSystem.WINDOWS == getOperatingSystem() ? new ProcessBuilder(WIN_DETECT_ARCH_CMDS) : new ProcessBuilder(UX_DETECT_ARCH_CMDS);
            Process        process        = processBuilder.start();
            String         result         = new BufferedReader(new InputStreamReader(process.getInputStream())).lines().collect(Collectors.joining("\n"));
            return Architecture.fromText(result);
        } catch (IOException e) {
            e.printStackTrace();
            return Architecture.NOT_FOUND;
        }
    }


    public final List<ArchiveType> getArchiveTypes(final OperatingSystem os) {
        switch (os) {
            case WINDOWS     : return List.of(ArchiveType.CAB, ArchiveType.MSI, ArchiveType.TAR, ArchiveType.ZIP);
            case MACOS       : return List.of(ArchiveType.DMG, ArchiveType.PKG, ArchiveType.TAR, ArchiveType.ZIP);
            case LINUX       : return List.of(ArchiveType.APK, ArchiveType.DEB, ArchiveType.RPM, ArchiveType.TAR, ArchiveType.ZIP);
            case LINUX_MUSL  : return List.of(ArchiveType.DEB, ArchiveType.RPM, ArchiveType.TAR, ArchiveType.ZIP);
            case ALPINE_LINUX: return List.of(ArchiveType.DEB, ArchiveType.RPM, ArchiveType.TAR, ArchiveType.ZIP);
            case SOLARIS     : return List.of(ArchiveType.DEB, ArchiveType.RPM, ArchiveType.TAR, ArchiveType.ZIP);
            case AIX         : return List.of(ArchiveType.DEB, ArchiveType.RPM, ArchiveType.TAR, ArchiveType.ZIP);
            case QNX         : return List.of(ArchiveType.DEB, ArchiveType.RPM, ArchiveType.TAR, ArchiveType.ZIP);
            default          : return Arrays.stream(ArchiveType.values()).filter(ext -> ArchiveType.NONE != ext).filter(ext -> ArchiveType.NOT_FOUND != ext).collect(Collectors.toList());
        }
    }

    private final FutureTask<Boolean> createDownloadTask(final String fileName, final String url) {
        return new FutureTask<>(() -> {
            try {
                final URLConnection         connection = new URL(url).openConnection();
                final int                   fileSize   = connection.getContentLength();
                fireEvt(new DownloadEvt(DiscoClient.this, DownloadEvt.DOWNLOAD_STARTED, fileSize));
                ReadableByteChannel         rbc  = Channels.newChannel(connection.getInputStream());
                ReadableConsumerByteChannel rcbc = new ReadableConsumerByteChannel(rbc, (b) -> fireEvt(new DownloadEvt(DiscoClient.this, DownloadEvt.DOWNLOAD_PROGRESS, fileSize, b)));
                FileOutputStream            fos  = new FileOutputStream(fileName);
                fos.getChannel().transferFrom(rcbc, 0, Long.MAX_VALUE);
                fos.close();
                rcbc.close();
                rbc.close();
                fireEvt(new DownloadEvt(DiscoClient.this, DownloadEvt.DOWNLOAD_FINISHED, fileSize));
                return true;
            } catch (IOException ex) {
                fireEvt(new DownloadEvt(DiscoClient.this, DownloadEvt.DOWNLOAD_FAILED, 0));
                return false;
            }
        });
    }


    // ******************** Event Handling ************************************
    public final void setOnEvt(final EvtType<? extends Evt> type, final EvtObserver observer) {
        if (!observers.keySet().contains(type.getName())) { observers.put(type.getName(), new CopyOnWriteArrayList<>()); }
        if (!observers.get(type.getName()).contains(observer)) { observers.get(type.getName()).add(observer); }
    }
    public final void removeOnEvt(final EvtType<? extends Evt> type, final EvtObserver observer) {
        if (!observers.keySet().contains(type.getName())) { return; }
        if (observers.get(type.getName()).contains(observer)) { observers.get(type.getName()).remove(observer); }
    }
    public final void removeAllObservers() { observers.entrySet().forEach(entry -> entry.getValue().clear()); }

    public final void fireEvt(final Evt evt) {
        final EvtType<? extends Evt> type = evt.getEvtType();
        if (observers.containsKey(type.getName())) {
            observers.get(type.getName()).forEach(observer -> observer.handle(evt));
        }
        if (observers.keySet().contains(DCEvt.ANY.getName())) {
            observers.get(DCEvt.ANY.getName()).forEach(observer -> observer.handle(evt));
        }
    }
}
