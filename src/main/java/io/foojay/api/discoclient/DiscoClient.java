/*
 * Copyright (c) 2021, Azul
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer
 *   in the documentation and/or other materials provided with the distribution.
 * - Neither the name of Azul nor the names of its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL AZUL BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
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


public class DiscoClient {
    private static final Logger                                 LOGGER        = LoggerFactory.getLogger(DiscoClient.class);
    public  static final ConcurrentHashMap<String, List<Scope>> SCOPE_LOOKUP  = new ConcurrentHashMap<>();
    private static final Map<String, Distribution>              DISTRIBUTIONS = new ConcurrentHashMap<>();
    private static final String                                 DISTRO_URL    = "https://github.com/foojay2020/distributions/raw/main/distributions.json";
    private        final Map<String, List<EvtObserver>>         observers     = new ConcurrentHashMap<>();
    private              AtomicBoolean                          initialzed    = new AtomicBoolean(false);


    public DiscoClient() {
        preloadDistributions();
    }


    private void preloadDistributions() {
        Helper.preloadDistributions().thenAccept(distros -> {
            DISTRIBUTIONS.putAll(distros);
            DISTRIBUTIONS.entrySet().stream().forEach(entry -> SCOPE_LOOKUP.put(entry.getKey(), entry.getValue().getScopes()));
            Helper.getAsync(DISTRO_URL).thenAccept(response -> {
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
                initialzed.set(true);
            });
        });
    }


    public boolean isInitialzed() { return initialzed.get(); }


    public Queue<Pkg> getAllPackages() {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(Constants.PACKAGES_PATH)
                                                        .append("/all");

        String query = queryBuilder.toString();
        if (query.isEmpty()) { return new ConcurrentLinkedQueue<>(); }

        Queue<Pkg> pkgs     = new ConcurrentLinkedQueue<>();
        List<Pkg> pkgsFound = new ArrayList<>();

        String      bodyText = Helper.get(query).body();
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
        HashSet<Pkg> unique = new HashSet<>(pkgs);
        pkgs = new LinkedList<>(unique);

        return pkgs;
    }
    public CompletableFuture<Queue<Pkg>> getAllPackagesAsync() {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(Constants.PACKAGES_PATH)
                                                        .append("/all");
        String query = queryBuilder.toString();

        CompletableFuture<Queue<Pkg>> future = Helper.getAsync(query).thenApply(response -> {
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


    public List<Pkg> getPkgs(final Distribution distribution, final VersionNumber versionNumber, final Latest latest, final OperatingSystem operatingSystem,
                             final LibCType libcType, final Architecture architecture, final Bitness bitness, final ArchiveType archiveType, final PackageType packageType,
                             final Boolean javafxBundled, final Boolean directlyDownloadable, final ReleaseStatus releaseStatus, final TermOfSupport termOfSupport, final Scope scope) {
        return getPkgs(distribution, versionNumber, latest, operatingSystem, libcType, architecture, bitness, archiveType, packageType, javafxBundled, directlyDownloadable, releaseStatus, termOfSupport, List.of(), scope);
    }
    public List<Pkg> getPkgs(final Distribution distribution, final VersionNumber versionNumber, final Latest latest, final OperatingSystem operatingSystem,
                             final LibCType libcType, final Architecture architecture, final Bitness bitness, final ArchiveType archiveType, final PackageType packageType,
                             final Boolean javafxBundled, final Boolean directlyDownloadable, final ReleaseStatus releaseStatus, final TermOfSupport termOfSupport, final List<String> ftrs, final Scope scope) {

        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(Constants.PACKAGES_PATH);
        final int initialLength = queryBuilder.length();

        if (null != distribution) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_DISTRIBUTION).append("=").append(distribution.getApiString());
        }

        if (null != versionNumber) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_VERSION).append("=").append(versionNumber.toString());
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

        if (null != scope && Scope.NONE != scope && Scope.NOT_FOUND != scope) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_DISCOVERY_SCOPE_ID).append("=").append(scope.getApiString());
        }

        if (null != javafxBundled) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_JAVAFX_BUNDLED).append("=").append(javafxBundled);
        }

        if (null != directlyDownloadable) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_DIRECTLY_DOWNLOADABLE).append("=").append(directlyDownloadable);
        }

        if (null != releaseStatus && ReleaseStatus.NONE != releaseStatus && ReleaseStatus.NOT_FOUND != releaseStatus) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_RELEASE_STATUS).append("=").append(releaseStatus.getApiString());
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
                    features = List.of();
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
        if (query.isEmpty()) { return List.of(); }

        List<Pkg>   pkgs     = new LinkedList<>();
        String      bodyText = Helper.get(query).body();

        List<Pkg>   pkgsFound = new ArrayList<>();
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
        HashSet<Pkg> unique = new HashSet<>(pkgs);
        pkgs = new LinkedList<>(unique);

        return pkgs;
    }
    public CompletableFuture<List<Pkg>> getPkgsAsync(final Distribution distribution, final VersionNumber versionNumber, final Latest latest, final OperatingSystem operatingSystem,
                                                     final LibCType libCType, final Architecture architecture, final Bitness bitness, final ArchiveType archiveType, final PackageType packageType,
                                                     final Boolean javafxBundled, final Boolean directlyDownloadable, final ReleaseStatus releaseStatus, final TermOfSupport termOfSupport, final Scope scope) {

        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(Constants.PACKAGES_PATH);
        final int initialLength = queryBuilder.length();

        if (null != distribution) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_DISTRIBUTION).append("=").append(distribution.getApiString());
        }

        if (null != versionNumber) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_VERSION).append("=").append(versionNumber.toString(OutputFormat.REDUCED, true, true));
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

        if (null != scope && Scope.NONE != scope && Scope.NOT_FOUND != scope) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_DISCOVERY_SCOPE_ID).append("=").append(scope.getApiString());
        }

        if (null != javafxBundled) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_JAVAFX_BUNDLED).append("=").append(javafxBundled);
        }

        if (null != directlyDownloadable) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_DIRECTLY_DOWNLOADABLE).append("=").append(directlyDownloadable);
        }

        if (null != releaseStatus && ReleaseStatus.NONE != releaseStatus && ReleaseStatus.NOT_FOUND != releaseStatus) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_RELEASE_STATUS).append("=").append(releaseStatus.getApiString());
        }

        if (null != termOfSupport && TermOfSupport.NONE != termOfSupport && TermOfSupport.NOT_FOUND != termOfSupport) {
            queryBuilder.append(queryBuilder.length() == initialLength ? "?" : "&");
            queryBuilder.append(Constants.API_SUPPORT_TERM).append("=").append(termOfSupport.getApiString());
        }

        String query = queryBuilder.toString();
        if (query.isEmpty()) { return new CompletableFuture<>(); }

        return Helper.getAsync(query).thenApply(response -> {
            List<Pkg>   pkgs      = new LinkedList<>();
            List<Pkg>   pkgsFound = new ArrayList<>();
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
            HashSet<Pkg> unique = new HashSet<>(pkgs);
            pkgs = new LinkedList<>(unique);
            return pkgs;
        });
    }


    public String getPkgsAsJson(final Distribution distribution, final VersionNumber versionNumber, final Latest latest, final OperatingSystem operatingSystem,
                                final LibCType libcType, final Architecture architecture, final Bitness bitness, final ArchiveType archiveType, final PackageType packageType,
                                final Boolean javafxBundled, final Boolean directlyDownloadable, final ReleaseStatus releaseStatus, final TermOfSupport termOfSupport, final Scope scope) {
        return getPkgs(distribution, versionNumber, latest, operatingSystem, libcType, architecture, bitness, archiveType, packageType, javafxBundled, directlyDownloadable, releaseStatus, termOfSupport, scope).toString();
    }
    public CompletableFuture<String> getPkgsAsJsonAsync(final Distribution distribution, final VersionNumber versionNumber, final Latest latest, final OperatingSystem operatingSystem,
                                                        final LibCType libcType, final Architecture architecture, final Bitness bitness, final ArchiveType archiveType, final PackageType packageType,
                                                        final Boolean javafxBundled, final Boolean directlyDownloadable, final ReleaseStatus releaseStatus, final TermOfSupport termOfSupport, final Scope scope) {
        return getPkgsAsync(distribution, versionNumber, latest, operatingSystem, libcType, architecture, bitness, archiveType, packageType, javafxBundled, directlyDownloadable, releaseStatus, termOfSupport, scope).thenApply(pkgs -> pkgs.toString());
    }


    public final MajorVersion getMajorVersion(final String parameter) {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(Constants.MAJOR_VERSIONS_PATH);
        if (null != parameter || !parameter.isEmpty()) {
            queryBuilder.append("/").append(parameter);
        }

        String query = queryBuilder.toString();
        if (query.isEmpty()) {
            LOGGER.debug("No major version found for given parameter {}.", parameter);
            return null;
        }
        String      bodyText = Helper.get(query).body();
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
                                                        .append(Constants.MAJOR_VERSIONS_PATH);
        if (null != parameter || !parameter.isEmpty()) {
            queryBuilder.append("/").append(parameter);
        }

        String query = queryBuilder.toString();
        if (query.isEmpty()) {
            LOGGER.debug("No major version found for given parameter {}.", parameter);
            return null;
        }
        return Helper.getAsync(query).thenApply(response -> {
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
                                                        .append(Constants.MAJOR_VERSIONS_PATH)
                                                        .append("?ea=")
                                                        .append(include_ea)
                                                        .append(include_build ? "" : "&include_build=false");

        String              query              = queryBuilder.toString();
        String              bodyText           = Helper.get(query).body();
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
                                                        .append(Constants.MAJOR_VERSIONS_PATH);
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
        String             bodyText           = Helper.get(query).body();
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
                                                        .append(Constants.MAJOR_VERSIONS_PATH)
                                                        .append("?ea=").append(include_ea)
                                                        .append(include_build ? "" : "&include_build=false");
        String query = queryBuilder.toString();
        return Helper.getAsync(query).thenApply(reponse -> {
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
                                                        .append(Constants.MAJOR_VERSIONS_PATH);
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
        return Helper.getAsync(query).thenApply(response -> {
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
                                                        .append(Constants.MAJOR_VERSIONS_PATH)
                                                        .append("?include_ea=").append(include_ea)
                                                        .append(include_build ? "": "&include_build=false");

        String query    = queryBuilder.toString();
        String bodyText = Helper.get(query).body();

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
                                                        .append(Constants.MAJOR_VERSIONS_PATH)
                                                        .append("?include_ea=").append(include_ea)
                                                        .append(include_build ? "" : "&include_build=false");

        String query = queryBuilder.toString();
        return Helper.getAsync(query).thenApply(response -> {
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
    public final List<MajorVersion> getMaintainedMajorVersions(final boolean include_ea) {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(Constants.MAJOR_VERSIONS_PATH)
                                                        .append("?maintained=true&ga=true")
                                                        .append(include_ea ? "&ea=true" : "")
                                                        .append(include_ea);

        String             query              = queryBuilder.toString();
        String             bodyText           = Helper.get(query).body();
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
                                                        .append(Constants.MAJOR_VERSIONS_PATH)
                                                        .append("?maintained=true&ga=true")
                                                        .append(include_ea ? "&ea=true" : "")
                                                        .append(include_build ? "" : "&include_build=false");

        String query = queryBuilder.toString();
        return Helper.getAsync(query).thenApply(response -> {
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
                                                        .append(Constants.MAJOR_VERSIONS_PATH)
                                                        .append("/useful")
                                                        .append(include_build ? "" : "&include_build=false");

        String             query              = queryBuilder.toString();
        String             bodyText           = Helper.get(query).body();
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
                                                        .append(Constants.MAJOR_VERSIONS_PATH)
                                                        .append("/useful")
                                                        .append(include_build ? "" : "include_build=false");

        String query = queryBuilder.toString();
        return Helper.getAsync(query).thenApply(response -> {
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


    public final List<Distribution> getDistributionsThatSupportVersion(final String version) {
        SemVer semver = SemVer.fromText(version).getSemVer1();
        if (null == semver) {
            LOGGER.debug("Error parsing version string {} to semver", version);
            return new ArrayList<>();
        }
        return getDistributionsThatSupportVersion(semver);
    }
    public final List<Distribution> getDistributionsThatSupportVersion(final SemVer semVer) { return getDistributionsForSemVer(semVer); }
    public final CompletableFuture<List<Distribution>> getDistributionsThatSupportSemVerAsync(final SemVer semVer) { return getDistributionsForSemVerAsync(semVer); }


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
                       Bitness.NONE, archiveType, packageType, javafxBundled, directlyDownloadable, semVer.getReleaseStatus(),
                       TermOfSupport.NONE, Scope.PUBLIC).stream()
                                                        .map(pkg -> pkg.getDistribution())
                                                        .distinct()
                                                        .collect(Collectors.toList());
    }
    public final CompletableFuture<List<Distribution>> getDistributionsThatSupportAsync(final SemVer semVer, final OperatingSystem operatingSystem, final Architecture architecture,
                                                                                        final LibCType libcType, final ArchiveType archiveType, final PackageType packageType,
                                                                                        final Boolean javafxBundled, final Boolean directlyDownloadable) {
        return getPkgsAsync(null, semVer.getVersionNumber(), Latest.NONE, operatingSystem, libcType, architecture,
                            Bitness.NONE, archiveType, packageType, javafxBundled, directlyDownloadable, semVer.getReleaseStatus(),
                            TermOfSupport.NONE, Scope.PUBLIC).thenApply(pkgs -> pkgs.stream()
                                                                                    .map(pkg -> pkg.getDistribution())
                                                                                    .distinct()
                                                                                    .collect(Collectors.toList()));
    }


    public final List<Pkg> updateAvailableFor(final Distribution distribution, final SemVer semVer, final Architecture architecture, final Boolean javafxBundled) {
        List<Pkg> pkgs = getPkgs(distribution, semVer.getVersionNumber(), Latest.OVERALL, getOperatingSystem(), LibCType.NONE, architecture, Bitness.NONE, ArchiveType.NONE, PackageType.JDK, javafxBundled,
                                 Boolean.TRUE, semVer.getReleaseStatus(), TermOfSupport.NONE, Scope.PUBLIC);
        List<Pkg> updatesFound = new ArrayList<>();
        if (pkgs.isEmpty()) {
            return updatesFound;
        } else {
            Pkg firstEntry = pkgs.get(0);
            SemVer semVerFound = firstEntry.getJavaVersion();
            if (semVerFound.compareTo(semVer) > 0) {
                updatesFound = pkgs.stream().filter(pkg -> pkg.getJavaVersion().compareTo(semVerFound) == 0).collect(Collectors.toList());
            }
            return updatesFound;
        }
    }
    public final CompletableFuture<List<Pkg>> updateAvailableForAsync(final Distribution distribution, final SemVer semVer, final Architecture architecture, final Boolean javafxBundled) {
        return getPkgsAsync(distribution, semVer.getVersionNumber(), Latest.OVERALL, getOperatingSystem(), LibCType.NONE, architecture, Bitness.NONE, ArchiveType.NONE, PackageType.JDK, javafxBundled,
                            Boolean.TRUE, semVer.getReleaseStatus(), TermOfSupport.NONE, Scope.PUBLIC).thenApplyAsync(pkgs -> {
            List<Pkg> updatesFound = new ArrayList<>();
            if (pkgs.isEmpty()) {
                return updatesFound;
            } else {
                Pkg    firstEntry  = pkgs.get(0);
                SemVer semVerFound = firstEntry.getJavaVersion();
                if (semVerFound.compareTo(semVer) > 0) {
                    updatesFound = pkgs.stream().filter(pkg -> pkg.getJavaVersion().compareTo(semVerFound) == 0).collect(Collectors.toList());
                }
                return updatesFound;
            }
        });
    }


    public final List<Distribution> getDistributions() {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(Constants.DISTRIBUTIONS_PATH);

        String             query              = queryBuilder.toString();
        String             bodyText           = Helper.get(query).body();
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
                                                        .append(Constants.DISTRIBUTIONS_PATH);
        String query = queryBuilder.toString();
        return Helper.getAsync(query).thenApply(response -> {
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


    public final List<Distribution> getDistributionsForSemVer(final SemVer semVer) {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(Constants.DISTRIBUTIONS_PATH)
                                                        .append("/versions/")
                                                        .append(semVer.toString());
        String             query              = queryBuilder.toString();
        String             bodyText           = Helper.get(query).body();
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
    public final CompletableFuture<List<Distribution>> getDistributionsForSemVerAsync(final SemVer semVer) {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(Constants.DISTRIBUTIONS_PATH)
                                                        .append("/versions/")
                                                        .append(semVer.toString());

        String query = queryBuilder.toString();
        return Helper.getAsync(query).thenApply(response -> {
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


    public final List<Distribution> getDistributionsForVersion(final VersionNumber versionNumber) {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(Constants.DISTRIBUTIONS_PATH)
                                                        .append("/versions/")
                                                        .append(versionNumber.toString());

        String             query              = queryBuilder.toString();
        String             bodyText           = Helper.get(query).body();
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
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(Constants.DISTRIBUTIONS_PATH)
                                                        .append("/versions/")
                                                        .append(versionNumber.toString());

        String query = queryBuilder.toString();
        return Helper.getAsync(query).thenApply(response -> {
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
                                                        .append(Constants.DISTRIBUTIONS_PATH);
        String                                 query              = queryBuilder.toString();
        String                                 bodyText           = Helper.get(query).body();
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
                                                        .append(Constants.DISTRIBUTIONS_PATH);

        String query = queryBuilder.toString();
        return Helper.getAsync(query).thenApply(response -> {
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
                            .filter(distribution -> distribution.getName() != "NONE")
                            .filter(distribution -> distribution.getName() != "NOT_FOUND")
                            .filter(distribution -> distribution.getName() != "GRAALVM_CE8")
                            .filter(distribution -> distribution.getName() != "GRAALVM_CE11")
                            .filter(distribution -> distribution.getName() != "GRAALVM_CE16")
                            .filter(distribution -> distribution.getName() != "MANDREL")
                            .filter(distribution -> distribution.getName() != "LIBERICA_NATIVE")
                            .collect(Collectors.toList());
    }

    public List<Distribution> getDistributionsBasedOnGraalVm() {
        return DISTRIBUTIONS.values()
                            .stream()
                            .filter(distribution -> distribution.getName() != "NONE")
                            .filter(distribution -> distribution.getName() != "NOT_FOUND")
                            .filter(distribution -> distribution.getName() == "GRAALVM_CE8")
                            .filter(distribution -> distribution.getName() == "GRAALVM_CE11")
                            .filter(distribution -> distribution.getName() == "GRAALVM_CE16")
                            .filter(distribution -> distribution.getName() == "MANDREL")
                            .filter(distribution -> distribution.getName() == "LIBERICA_NATIVE")
                            .collect(Collectors.toList());
    }


    public final String getPkgDirectDownloadUri(final String id, final SemVer javaVersion) {
        return getPkgInfo(id, javaVersion).getDirectDownloadUri();
    }
    public final CompletableFuture<String> getPkgDirectDownloadUriAsync(final String id, final SemVer javaVersion) {
        return getPkgInfoAsync(id, javaVersion).thenApply(pkgInfo -> pkgInfo.getDirectDownloadUri());
    }


    public PkgInfo getPkgInfo(final String ephemeralId, final SemVer javaVersion) {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(Constants.EPHEMERAL_IDS_PATH)
                                                        .append("/")
                                                        .append(ephemeralId);

        String query           = queryBuilder.toString();
        String packageInfoBody = Helper.get(query).body();

        Gson        packageInfoGson    = new Gson();
        JsonElement packageInfoElement = packageInfoGson.fromJson(packageInfoBody, JsonElement.class);
        if (packageInfoElement instanceof JsonObject) {
            JsonObject jsonObject = packageInfoElement.getAsJsonObject();
            JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
            if (jsonArray.size() > 0) {
                final JsonObject packageInfoJson   = jsonArray.get(0).getAsJsonObject();
                final String     filename          = packageInfoJson.get(PkgInfo.FIELD_FILENAME).getAsString();
                final String     directDownloadUri = packageInfoJson.get(PkgInfo.FIELD_DIRECT_DOWNLOAD_URI).getAsString();
                final String     downloadSiteUri   = packageInfoJson.get(PkgInfo.FIELD_DOWNLOAD_SITE_URI).getAsString();
                return new PkgInfo(filename, javaVersion, directDownloadUri, downloadSiteUri);
            } else {
                return null;
            }
        }
        return null;
    }
    public CompletableFuture<PkgInfo> getPkgInfoAsync(final String ephemeralId, final SemVer javaVersion) {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(Constants.EPHEMERAL_IDS_PATH)
                                                        .append("/")
                                                        .append(ephemeralId);
        String query           = queryBuilder.toString();
        return Helper.getAsync(query).thenApply(response -> {
            Gson        packageInfoGson    = new Gson();
            JsonElement packageInfoElement = packageInfoGson.fromJson(response.body(), JsonElement.class);
            if (packageInfoElement instanceof JsonObject) {
                JsonObject jsonObject = packageInfoElement.getAsJsonObject();
                JsonArray  jsonArray  = jsonObject.getAsJsonArray("result");
                if (jsonArray.size() > 0) {
                    final JsonObject packageInfoJson   = jsonArray.get(0).getAsJsonObject();
                    final String     filename          = packageInfoJson.get(PkgInfo.FIELD_FILENAME).getAsString();
                    final String     directDownloadUri = packageInfoJson.get(PkgInfo.FIELD_DIRECT_DOWNLOAD_URI).getAsString();
                    final String     downloadSiteUri   = packageInfoJson.get(PkgInfo.FIELD_DOWNLOAD_SITE_URI).getAsString();
                    return new PkgInfo(filename, javaVersion, directDownloadUri, downloadSiteUri);
                } else {
                    return null;
                }
            }
            return null;
        });
    }


    public final Future<?> downloadPkg(final String pkgId, final String targetFileName) {
        Pkg pkg = getPkg(pkgId);
        if (null == pkg) {
            return null;
        } else {
            final SemVer javaVersion = pkg.getJavaVersion();
            final String ephemeralId = pkg.getEphemeralId();
            return downloadPkg(ephemeralId, javaVersion, targetFileName);
        }
    }
    public final Future<?> downloadPkg(final String ephemeralId, final SemVer javaVersion, final String targetFileName) {
        final String              url      = getPkgInfo(ephemeralId,javaVersion).getDirectDownloadUri();
        final FutureTask<Boolean> task     = createDownloadTask(targetFileName, url);
        final ExecutorService     executor = Executors.newSingleThreadExecutor();
        final Future<?>           future   = executor.submit(task);
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            LOGGER.error("Error shutdown executor. {}", e.getMessage());
        }
        return future;
    }
    public final Future<?> downloadPkg(final PkgInfo pkgInfo, final String targetFileName) {
        final FutureTask<Boolean> task     = createDownloadTask(targetFileName, pkgInfo.getDirectDownloadUri());
        final ExecutorService     executor = Executors.newSingleThreadExecutor();
        final Future<?>           future   = executor.submit(task);
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            LOGGER.error("Error shutdown executor. {}", e.getMessage());
        }
        return future;
    }


    public Pkg getPkg(final String pkgId) {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(Constants.PACKAGES_PATH)
                                                        .append("/")
                                                        .append(pkgId);

        String query    = queryBuilder.toString();
        String bodyText = Helper.get(query).body();

        Gson        pkgGson    = new Gson();
        JsonElement pkgElement = pkgGson.fromJson(bodyText, JsonElement.class);
        if (pkgElement instanceof JsonObject) {
            return new Pkg(pkgElement.getAsJsonObject().toString());
        } else {
            return null;
        }
    }
    public CompletableFuture<Pkg> getPkgAsync(final String pkgId) {
        StringBuilder queryBuilder = new StringBuilder().append(PropertyManager.INSTANCE.getString(Constants.PROPERTY_KEY_DISCO_URL))
                                                        .append(Constants.PACKAGES_PATH)
                                                        .append("/")
                                                        .append(pkgId);
        String query = queryBuilder.toString();
        return Helper.getAsync(query).thenApply(response -> {
            Gson        pkgGson    = new Gson();
            JsonElement pkgElement = pkgGson.fromJson(response.body(), JsonElement.class);
            if (pkgElement instanceof JsonObject) {
                return new Pkg(pkgElement.getAsJsonObject().toString());
            } else {
                return null;
            }
        });
    }


    public static Distribution getDistributionFromText(final String text) {
        if (null == text) { return null; }
        return DISTRIBUTIONS.values().stream().filter(distribution -> distribution.getFromText(text) != null).findFirst().orElse(null);
    }


    public void cancelRequest() { Helper.cancelRequest(); }


    public static final OperatingSystem getOperatingSystem() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("win") >= 0) {
            return OperatingSystem.WINDOWS;
        } else if (os.indexOf("mac") >= 0) {
            return OperatingSystem.MACOS;
        } else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
            return OperatingSystem.LINUX;
        } else if (os.indexOf("sunos") >= 0) {
            return OperatingSystem.SOLARIS;
        } else {
            return OperatingSystem.NONE;
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
                LOGGER.debug("Successfully downloaded file: {}", fileName);
                return true;
            } catch (IOException ex) {
                fireEvt(new DownloadEvt(DiscoClient.this, DownloadEvt.DOWNLOAD_FAILED, 0));
                LOGGER.error("Error downloading file: {} with message {}", fileName, ex.getMessage());
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
