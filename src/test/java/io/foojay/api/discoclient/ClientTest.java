/*
 * Copyright (c) 2022 by Gerrit Grunwald
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
import eu.hansolo.jdktools.Architecture;
import eu.hansolo.jdktools.ArchiveType;
import eu.hansolo.jdktools.Bitness;
import eu.hansolo.jdktools.Latest;
import eu.hansolo.jdktools.LibCType;
import eu.hansolo.jdktools.Match;
import eu.hansolo.jdktools.OperatingSystem;
import eu.hansolo.jdktools.PackageType;
import eu.hansolo.jdktools.ReleaseStatus;
import eu.hansolo.jdktools.TermOfSupport;
import eu.hansolo.jdktools.versioning.Semver;
import eu.hansolo.jdktools.versioning.VersionNumber;
import io.foojay.api.discoclient.pkg.Distribution;
import io.foojay.api.discoclient.pkg.MajorVersion;
import io.foojay.api.discoclient.pkg.Pkg;
import io.foojay.api.discoclient.pkg.Scope;
import io.foojay.api.discoclient.util.Helper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static io.foojay.api.discoclient.util.Constants.PROPERTY_KEY_DISTRIBUTION_JSON_URL;


public class ClientTest {
    private static final int         NO_OF_DISTRIBUTIONS       = 29;
    private static final int         VERSIONS_PER_DISTRIBUTION = 29;
    private        final DiscoClient discoClient               = new DiscoClient();

    @Test
    public void loadDistributionsTest() {
        while(!discoClient.isInitialzed()) {
            try { Thread.sleep(10); } catch (InterruptedException e) {}
        }
        Map<String, Distribution> distributions = discoClient.getDistros();
        assertEquals(NO_OF_DISTRIBUTIONS, distributions.size());

        Optional<Distribution> optionalDistro  = distributions.values().stream().filter(d -> d.getFromText("zulu") != null).findFirst();
        assert optionalDistro.isPresent();
        if (optionalDistro.isPresent()) {
            assertEquals("ZULU", optionalDistro.get().getName());
        }
    }

    @Test
    public void preloadDistributionsTest() {
        Map<String, Distribution> distributions = new ConcurrentHashMap<>();
        Helper.getAsync(PropertyManager.INSTANCE.getString(PROPERTY_KEY_DISTRIBUTION_JSON_URL), "").thenAccept(response -> {
            if (null != response) {
                if (response.statusCode() == 200) {
                    String                    jsonText           = response.body();
                    Map<String, Distribution> distributionsFound = new ConcurrentHashMap<>();
                    if (!jsonText.isEmpty()) {
                        distributionsFound.putAll(Helper.getDistributionsFromJsonText(jsonText));
                    }
                    if (!distributionsFound.isEmpty()) {
                        distributions.putAll(distributionsFound);
                    }
                }
            }
            assertEquals(NO_OF_DISTRIBUTIONS, distributions.size());
        }).join();
    }

    @Test
    public void cancelRequestTest() {
        List<Pkg> pkgsSync  = discoClient.getPkgs(null, new VersionNumber(11), Latest.OVERALL, OperatingSystem.NONE, LibCType.NONE, Architecture.NONE, Bitness.NONE, ArchiveType.NONE, PackageType.NONE, false, true, List.of(ReleaseStatus.NONE), TermOfSupport.NONE, List.of(Scope.PUBLIC), Match.ANY);

        List<Pkg> pkgsAsync = new ArrayList<>();
        discoClient.getPkgsAsync(null, new VersionNumber(11), Latest.OVERALL, OperatingSystem.NONE, LibCType.NONE,
                                 Architecture.NONE, Bitness.NONE, ArchiveType.NONE, PackageType.NONE, false, true, List.of(ReleaseStatus.NONE), TermOfSupport.NONE, List.of(Scope.PUBLIC), Match.ANY).thenAccept(e -> pkgsAsync.addAll(e));

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        discoClient.cancelRequest();
        assertTrue(pkgsSync.size() != pkgsAsync.size());
    }

    @Test
    public void getPkgsAndTest() {
        List<Pkg> packagesFound = discoClient.getPkgs(List.of(DiscoClient.getDistributionFromText("zulu")), new VersionNumber(11, 0, 9, 1), Latest.NONE, OperatingSystem.WINDOWS, LibCType.NONE,
                                                      Architecture.X64, Bitness.BIT_64, ArchiveType.ZIP, PackageType.JDK, false, true, List.of(ReleaseStatus.GA), TermOfSupport.LTS, List.of(Scope.PUBLIC), Match.ANY);
        assertEquals(1, packagesFound.size());
    }

    @Test
    public void updateAvailableForTest() {
        List<Pkg> availableUpdates = discoClient.updateAvailableFor(DiscoClient.getDistributionFromText("zulu"), Semver.fromText("19-ea+25").getSemver1(), OperatingSystem.MACOS, Architecture.fromText("aarch64"), false, null, "");
        assertTrue(!availableUpdates.isEmpty());
    }

    @Test
    public void findUpdateTest() {
        DiscoClient     discoClient          = new DiscoClient();
        Distribution    distribution         = DiscoClient.getDistributionFromText("zulu");
        OperatingSystem operatingSystem      = OperatingSystem.MACOS;
        Architecture    architecture         = Architecture.X64;
        Boolean         javafxBundled        = Boolean.FALSE;
        Semver          semVer               = Semver.fromText("17-ea+35").getSemver1();
        Boolean         directlyDownloadable = Boolean.TRUE;

        // updateAvailableFor()
        assertTrue(discoClient.updateAvailableFor(distribution, semVer, architecture, javafxBundled, directlyDownloadable).size() > 0);

        // updateAvailableForAsync()
        discoClient.updateAvailableForAsync(distribution, semVer, architecture, javafxBundled,directlyDownloadable).thenAccept(r -> assertTrue(r.size() > 0));


        // getPkgs()
        assertTrue(discoClient.getPkgs(null == distribution ? null : List.of(distribution), semVer.getVersionNumber(), Latest.AVAILABLE, discoClient.getOperatingSystem(), LibCType.NONE, architecture, Bitness.NONE, ArchiveType.NONE, PackageType.JDK, javafxBundled,
                           directlyDownloadable, List.of(ReleaseStatus.EA, ReleaseStatus.GA), TermOfSupport.NONE, List.of(Scope.PUBLIC), Match.ANY).size() > 0);

        // getPkgsAsync()
        discoClient.getPkgsAsync(null == distribution ? null : List.of(distribution), semVer.getVersionNumber(), Latest.AVAILABLE, discoClient.getOperatingSystem(), LibCType.NONE, architecture, Bitness.NONE, ArchiveType.NONE, PackageType.JDK, javafxBundled,
                     directlyDownloadable, List.of(ReleaseStatus.EA, ReleaseStatus.GA), TermOfSupport.NONE, List.of(Scope.PUBLIC), Match.ANY).thenAccept(pkgs -> assertTrue(pkgs.size() > 0));
    }

    /*
    @Test
    public void downloadPkgTest() {
        DiscoClient discoClient = new DiscoClient();
        List<Pkg> packagesFound = discoClient.getPkgs(DiscoClient.getDistributionFromText("zulu"), new VersionNumber(11, 0, 9, 1), null, OperatingSystem.WINDOWS, LibCType.C_STD_LIB,
                                                      Architecture.X64, Bitness.BIT_64, ArchiveType.ZIP, PackageType.JRE, false, true, ReleaseStatus.GA, TermOfSupport.LTS, Scope.PUBLIC);
        assert packagesFound.size() > 0;

        Pkg     pkg     = packagesFound.get(0);
        PkgInfo pkgInfo = discoClient.getPkgInfo(pkg.getEphemeralId(), pkg.getJavaVersion());

        assert null != pkgInfo;
        assert "https://cdn.azul.com/zulu/bin/zulu11.43.55-ca-jre11.0.9.1-win_x64.zip".equals(pkgInfo.getDirectDownloadUri());

        Future<?> future = discoClient.downloadPkg(pkgInfo, "./" + pkgInfo.getFileName());

        try {
            assert null == future.get();
            Path path = Paths.get("./" + pkgInfo.getFileName());
            assert Files.exists(path);
            if (Files.exists(path)) { new File("./" + pkgInfo.getFileName()).delete(); }
        } catch (InterruptedException | ExecutionException e) {
            System.out.println(e);
        }
    }
    */

    @Test
    public void getPkgsAsJsonTest() {
        DiscoClient discoClient = new DiscoClient();
        String packagesFoundJson = discoClient.getPkgsAsJson(List.of(DiscoClient.getDistributionFromText("zulu")), new VersionNumber(11, 0, 9, 1), Latest.NONE, OperatingSystem.WINDOWS, LibCType.NONE,
                                                             Architecture.X64, Bitness.BIT_64, ArchiveType.ZIP, PackageType.JDK, false, true, List.of(ReleaseStatus.GA), TermOfSupport.LTS, List.of(Scope.PUBLIC), Match.ANY);
        Gson        gson    = new Gson();
        JsonElement element = gson.fromJson(packagesFoundJson, JsonElement.class);
        assert (element instanceof JsonArray);
        if (element instanceof JsonArray) {
            JsonArray jsonArray = element.getAsJsonArray();
            assert (1 == jsonArray.size());
            JsonObject packageJsonObj = jsonArray.get(0).getAsJsonObject();
            Pkg        pkg           = new Pkg(packageJsonObj.toString());
            assert (pkg.getJavaVersion().toString().equals("11.0.9.1+1"));
        }
    }

    @Test
    public void getReleaseTest() {
        DiscoClient  discoClient  = new DiscoClient();
        MajorVersion majorVersion = discoClient.getMajorVersion("6");
        assert (majorVersion.getAsInt() == 6);
        assert (majorVersion.getTermOfSupport() == TermOfSupport.LTS);
        assert (!majorVersion.isMaintained());
    }

    @Test
    public void getDistributionForVersionTest() {
        DiscoClient       discoClient        = new DiscoClient();
        List<Distribution> distributionsFound = new LinkedList<>(discoClient.getDistributionsThatSupportVersion("13.0.5.1"));
        assert distributionsFound.size() == 1;
        assert distributionsFound.get(0).getName().equals(DiscoClient.getDistributionFromText("zulu").getName());
    }

    @Test
    public void getVersionsPerDistributionTest() {
        DiscoClient                            discoClient             = new DiscoClient();
        Map<Distribution, List<VersionNumber>> versionsPerDistribution = discoClient.getVersionsPerDistribution();

        assertEquals(VERSIONS_PER_DISTRIBUTION, versionsPerDistribution.keySet().size());
    }

    @Test
    public void testDistributionFromText() {
        assertEquals("LIBERICA", DiscoClient.getDistributionFromText("Liberica").getName());
        assertEquals("LIBERICA", DiscoClient.getDistributionFromText("liberica").getName());
        assertEquals("LIBERICA", DiscoClient.getDistributionFromText("LIBERICA").getName());
    }

    @Test
    public void getDistributionForSemver() {
        Semver      semVer      = Semver.fromText("13.0.5.1").getSemver1();
        DiscoClient discoClient = new DiscoClient();
        try {
            assertEquals(1, discoClient.getDistributionsForSemverAsync(semVer).get().size());
        } catch (ExecutionException | InterruptedException e) {

        }
    }

    @Test
    public void releaseDetailsUrlTest() {
        DiscoClient discoClient       = new DiscoClient();
        String      javaVersion       = "13.0.5.1";
        Semver      semver            = Semver.fromText(javaVersion).getSemver1();
        String      releaseDetailsUrl = discoClient.getReleaseDetailsUrl(semver);
        assertEquals("https://foojay.io/java-13/?tab=allissues&version=13.0.5", releaseDetailsUrl);
    }

    @Test
    public void getPkgDirectDownloadUriTest() {
        String directDownloadUri = discoClient.getPkgDirectDownloadUri("3c0cbf96ac87a7bbcf6bba8e8a9450b6");
        assertEquals("https://cdn.azul.com/zulu/bin/zulu19.0.65-ea-jdk19.0.0-ea.28-macosx_aarch64.zip", directDownloadUri);
    }
}
