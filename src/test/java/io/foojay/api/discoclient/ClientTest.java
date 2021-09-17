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
import io.foojay.api.discoclient.pkg.Architecture;
import io.foojay.api.discoclient.pkg.ArchiveType;
import io.foojay.api.discoclient.pkg.Bitness;
import io.foojay.api.discoclient.pkg.Distribution;
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
import io.foojay.api.discoclient.util.Helper;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static io.foojay.api.discoclient.util.Constants.QUOTES;


public class ClientTest {

    @Test
    public void loadDistributionsTest() {
        DiscoClient discoClient = new DiscoClient();
        while(!discoClient.isInitialzed()) {
            try { Thread.sleep(10); } catch (InterruptedException e) {}
        }
        Map<String, Distribution> distributions = discoClient.getDistros();
        assert distributions.size() == 23;

        Optional<Distribution> optionalDistro  = distributions.values().stream().filter(d -> d.getFromText("zulu") != null).findFirst();
        assert optionalDistro.isPresent();
        if (optionalDistro.isPresent()) {
            assert optionalDistro.get().getName().equals("ZULU");
        }
    }

    @Test
    public void cancelRequestTest() {
        DiscoClient discoClient = new DiscoClient();
        List<Pkg> pkgs = discoClient.getPkgs(null, new VersionNumber(11), Latest.OVERALL, OperatingSystem.NONE, LibCType.NONE,
                                             Architecture.NONE, Bitness.NONE, ArchiveType.NONE, PackageType.NONE, false, true, List.of(ReleaseStatus.NONE), TermOfSupport.NONE, List.of(Scope.PUBLIC), Match.ANY);
        System.out.println(pkgs.size() + " pkgs found");

        discoClient.getPkgsAsync(null, new VersionNumber(11), Latest.OVERALL, OperatingSystem.NONE, LibCType.NONE,
                                 Architecture.NONE, Bitness.NONE, ArchiveType.NONE, PackageType.NONE, false, true, List.of(ReleaseStatus.NONE), TermOfSupport.NONE, List.of(Scope.PUBLIC), Match.ANY).thenAccept(e -> System.out.println(e.size() + " pkgs found async"));

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

        discoClient.cancelRequest();
    }

    @Test
    public void getPkgsAndTest() {
        DiscoClient discoClient = new DiscoClient();
        List<Pkg> packagesFound = discoClient.getPkgs(List.of(DiscoClient.getDistributionFromText("zulu")), new VersionNumber(11, 0, 9, 1), Latest.NONE, OperatingSystem.WINDOWS, LibCType.NONE,
                                                      Architecture.X64, Bitness.BIT_64, ArchiveType.ZIP, PackageType.JDK, false, true, List.of(ReleaseStatus.GA), TermOfSupport.LTS, List.of(Scope.PUBLIC), Match.ANY);
        assert packagesFound.size() == 1;
    }

    /*
    @Test
    public void findUpdateTest() {
        DiscoClient     discoClient          = new DiscoClient();
        Distribution    distribution         = DiscoClient.getDistributionFromText("zulu");
        OperatingSystem operatingSystem      = OperatingSystem.MACOS;
        Architecture    architecture         = Architecture.X64;
        Boolean         javafxBundled        = Boolean.FALSE;
        SemVer          semVer               = SemVer.fromText("17-ea+35").getSemVer1();
        Boolean         directlyDownloadable = Boolean.TRUE;

        //System.out.println("updateAvailableFor() ----------------------");

        //discoClient.updateAvailableFor(distribution, semVer, architecture, javafxBundled, directlyDownloadable).forEach(pkg -> System.out.println(pkg));

        System.out.println("updateAvailableForAsync() ----------------------");

        discoClient.updateAvailableForAsync(distribution, semVer, architecture, javafxBundled,directlyDownloadable).thenAccept(r -> {
            r.forEach(pkg -> System.out.println(pkg));
        });


        System.out.println("getPkgs() ----------------------");
        discoClient.getPkgs(null == distribution ? null : List.of(distribution), semVer.getVersionNumber(), Latest.AVAILABLE, discoClient.getOperatingSystem(), LibCType.NONE, architecture, Bitness.NONE, ArchiveType.NONE, PackageType.JDK, javafxBundled,
                           directlyDownloadable, List.of(ReleaseStatus.EA, ReleaseStatus.GA), TermOfSupport.NONE, List.of(Scope.PUBLIC), Match.ANY).forEach(pkg -> System.out.println(pkg));

        System.out.println("getPkgsAsync() ----------------------");
        discoClient.getPkgsAsync(null == distribution ? null : List.of(distribution), semVer.getVersionNumber(), Latest.AVAILABLE, discoClient.getOperatingSystem(), LibCType.NONE, architecture, Bitness.NONE, ArchiveType.NONE, PackageType.JDK, javafxBundled,
                     directlyDownloadable, List.of(ReleaseStatus.EA, ReleaseStatus.GA), TermOfSupport.NONE, List.of(Scope.PUBLIC), Match.ANY).thenAccept(pkgs -> {
                         pkgs.forEach(pkg -> System.out.println(pkg));
        });
    }
    */

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
            assert (pkg.getJavaVersion().toString().equals("11.0.9.1"));
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
        assert versionsPerDistribution.keySet().size() == 21;
    }

    @Test
    public void testDistributionFromText() {
        assert DiscoClient.getDistributionFromText("Liberica").getName().equals("LIBERICA");
        assert DiscoClient.getDistributionFromText("liberica").getName().equals("LIBERICA");
        assert DiscoClient.getDistributionFromText("LIBERICA").getName().equals("LIBERICA");
    }

    @Test
    public void getDistributionForSemver() {
        SemVer      semVer      = SemVer.fromText("13.0.5.1").getSemVer1();
        DiscoClient discoClient = new DiscoClient();
        try {
            assert discoClient.getDistributionsForSemVerAsync(semVer).get().size() == 1;
        } catch (ExecutionException | InterruptedException e) {

        }
    }
}
