/*
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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClientTest {

    @Test
    public void loadDistributionsTest() {
        DiscoClient discoClient = new DiscoClient();
        while (!discoClient.isInitialzed()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
        Map<String, Distribution> distributions = discoClient.getDistros();

//        assertEquals(23, distributions.size());
        Optional<Distribution> optionalDistro = distributions.values().stream().filter(d -> d.getFromText("zulu") != null).findFirst();
        assertTrue(optionalDistro.isPresent(), "Zulu distro not present");
        if (optionalDistro.isPresent()) {
            assertEquals("ZULU", optionalDistro.get().getName());
        }
    }

    @Test
    public void getPkgsTest() throws InterruptedException {
        DiscoClient discoClient = new DiscoClient();
        List<Pkg> pkgs1 = discoClient.getPkgs(null, new VersionNumber(11), Latest.OVERALL, OperatingSystem.NONE, LibCType.NONE,
                Architecture.NONE, Bitness.NONE, ArchiveType.NONE, PackageType.NONE, false, true, Arrays.asList(ReleaseStatus.NONE), TermOfSupport.NONE, Arrays.asList(Scope.PUBLIC), null);

        int pkgs1Size = pkgs1.size();
        System.out.println(pkgs1Size + " pkgs found (sync)");
//        pkgs1.sort(Comparator.comparing(Pkg::getId));
//        System.out.println(pkgs1);

        BlockingQueue<Integer> queue = new SynchronousQueue<>();
        discoClient.getPkgsAsync(null, new VersionNumber(11), Latest.OVERALL, OperatingSystem.NONE, LibCType.NONE,
                Architecture.NONE, Bitness.NONE, ArchiveType.NONE, PackageType.NONE, false, true, Arrays.asList(ReleaseStatus.NONE), TermOfSupport.NONE, Arrays.asList(Scope.PUBLIC), null)
                .thenAcceptAsync(pkgs2 -> {
                    int pkgs2Size = pkgs2.size();
                    System.out.println(pkgs2Size + " pkgs found (async)");
//                  pkgs2.sort(Comparator.comparing(Pkg::getId));
//                  System.out.println(pkgs2);
                    try {
                        queue.offer(pkgs2Size, 10, TimeUnit.SECONDS);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(ex);
                    }
                });

        int pkgs2Size = queue.poll(10, TimeUnit.SECONDS);
        assertEquals(pkgs1Size, pkgs2Size);

    }

    @Test
    public void getPkgsAndTest() {
        DiscoClient discoClient = new DiscoClient();
        List<Pkg> packagesFound = discoClient.getPkgs(Arrays.asList(DiscoClient.getDistributionFromText("zulu")), new VersionNumber(11, 0, 9, 1), Latest.NONE, OperatingSystem.WINDOWS, LibCType.NONE,
                Architecture.X64, Bitness.BIT_64, ArchiveType.ZIP, PackageType.JDK, false, true, Arrays.asList(ReleaseStatus.GA), TermOfSupport.LTS, Arrays.asList(Scope.PUBLIC), Match.ANY);
        assertEquals(1, packagesFound.size());
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
        String packagesFoundJson = discoClient.getPkgsAsJson(Arrays.asList(DiscoClient.getDistributionFromText("zulu")), new VersionNumber(11, 0, 9, 1), Latest.NONE, OperatingSystem.WINDOWS, LibCType.NONE,
                Architecture.X64, Bitness.BIT_64, ArchiveType.ZIP, PackageType.JDK, false, true, Arrays.asList(ReleaseStatus.GA), TermOfSupport.LTS, Arrays.asList(Scope.PUBLIC), Match.ANY);
        Gson gson = new Gson();
        JsonElement element = gson.fromJson(packagesFoundJson, JsonElement.class);
        assertTrue(element instanceof JsonArray, "Not a JsonArray");
        if (element instanceof JsonArray) {
            JsonArray jsonArray = element.getAsJsonArray();
            assertEquals(1, jsonArray.size());
            JsonObject packageJsonObj = jsonArray.get(0).getAsJsonObject();
            Pkg pkg = new Pkg(packageJsonObj.toString());
            assertTrue(pkg.getJavaVersion().toString().startsWith("11.0.9.1"),
                    "pkg.getJavaVersion does not start with expected 11.0.9.1");
        }
    }

    @Test
    public void getReleaseTest() {
        DiscoClient discoClient = new DiscoClient();
        MajorVersion majorVersion = discoClient.getMajorVersion("17");
        assertEquals(17, majorVersion.getAsInt());
        assertEquals(TermOfSupport.LTS, majorVersion.getTermOfSupport());
        assertTrue(majorVersion.isMaintained());
    }

    @Test
    public void getDistributionForVersionTest() {
        DiscoClient discoClient = new DiscoClient();
        List<Distribution> distributionsFound = new LinkedList<>(discoClient.getDistributionsThatSupportVersion("13.0.5.1"));
        assertEquals(1, distributionsFound.size());
        assertEquals(DiscoClient.getDistributionFromText("zulu").getName(),
                distributionsFound.get(0).getName());
    }

    @Test
    public void getVersionsPerDistributionTest() {
        DiscoClient discoClient = new DiscoClient();
        Map<Distribution, List<VersionNumber>> versionsPerDistribution = discoClient.getVersionsPerDistribution();
//        assert versionsPerDistribution.keySet().size() == 21;
//        assertEquals(21, versionsPerDistribution.size());
        assertTrue(!versionsPerDistribution.isEmpty());
    }

    @Test
    public void testDistributionFromText() {
        assert DiscoClient.getDistributionFromText("Liberica").getName().equals("LIBERICA");
        assert DiscoClient.getDistributionFromText("liberica").getName().equals("LIBERICA");
        assert DiscoClient.getDistributionFromText("LIBERICA").getName().equals("LIBERICA");
    }

    @Test
    public void getDistributionForSemver() {
        SemVer semVer = SemVer.fromText("13.0.5.1").getSemVer1();
        DiscoClient discoClient = new DiscoClient();
        try {
            assertEquals(1, discoClient.getDistributionsForSemVerAsync(semVer).get().size());
        } catch (ExecutionException | InterruptedException e) {

        }
    }
}
