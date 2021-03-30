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
import io.foojay.api.discoclient.pkg.Architecture;
import io.foojay.api.discoclient.pkg.Bitness;
import io.foojay.api.discoclient.pkg.LibCType;
import io.foojay.api.discoclient.pkg.Pkg;
import io.foojay.api.discoclient.pkg.PackageType;
import io.foojay.api.discoclient.pkg.Distribution;
import io.foojay.api.discoclient.pkg.ArchiveType;
import io.foojay.api.discoclient.pkg.Latest;
import io.foojay.api.discoclient.pkg.OperatingSystem;
import io.foojay.api.discoclient.pkg.MajorVersion;
import io.foojay.api.discoclient.pkg.ReleaseStatus;
import io.foojay.api.discoclient.pkg.Scope;
import io.foojay.api.discoclient.pkg.SemVer;
import io.foojay.api.discoclient.pkg.TermOfSupport;
import io.foojay.api.discoclient.pkg.VersionNumber;
import io.foojay.api.discoclient.util.Helper;
import io.foojay.api.discoclient.util.PkgInfo;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class ClientTest {
    /*
    @Test
    public void getPkgsAndTest() {
        DiscoClient discoClient = new DiscoClient();
        List<Pkg> packagesFound = discoClient.getPkgs(Distribution.ZULU, new VersionNumber(11, 0, 9, 1), Latest.NONE, OperatingSystem.WINDOWS, LibCType.NONE,
                                                      Architecture.X64, Bitness.BIT_64, ArchiveType.ZIP, PackageType.JDK, false, true, ReleaseStatus.GA, TermOfSupport.LTS, Scope.PUBLIC);
        assert packagesFound.size() == 1;

        packagesFound = discoClient.getPkgs(Distribution.ZULU, VersionNumber.fromText("14-ea.36"), Latest.NONE, OperatingSystem.WINDOWS, LibCType.NONE,
                                            Architecture.X64, Bitness.BIT_64, ArchiveType.ZIP, PackageType.JDK, false, true, ReleaseStatus.GA, TermOfSupport.LTS, Scope.PUBLIC);
    }

    @Test
    public void downloadPkgTest() {
        DiscoClient discoClient = new DiscoClient();
        List<Pkg> packagesFound = discoClient.getPkgs(Distribution.ZULU, new VersionNumber(11, 0, 9, 1), Latest.EXPLICIT, OperatingSystem.WINDOWS, LibCType.C_STD_LIB,
                                                      Architecture.X64, Bitness.BIT_64, ArchiveType.ZIP, PackageType.JRE, false, true, ReleaseStatus.GA, TermOfSupport.LTS, Scope.PUBLIC);
        assert packagesFound.size() > 0;

        Pkg     pkg     = packagesFound.get(0);
        PkgInfo pkgInfo = discoClient.getPkgInfo(pkg.getEphemeralId(), pkg.getJavaVersion());

        assert "https://cdn.azul.com/zulu/bin/zulu11.43.55-ca-jre11.0.9.1-win_x64.zip".equals(pkgInfo.getDirectDownloadUri());

        Future<?> future = discoClient.downloadPkg(pkgInfo, "./" + pkgInfo.getFileName());
        try {
            assert null == future.get();
            Path path = Paths.get("./" + pkgInfo.getFileName());
            assert Files.exists(path);
            if (Files.exists(path)) { new File("./" + pkgInfo.getFileName()).delete(); }

        } catch (InterruptedException | ExecutionException e) {

        }
    }

    @Test
    public void getPkgsAsJsonTest() {
        DiscoClient discoClient = new DiscoClient();
        String packagesFoundJson = discoClient.getPkgsAsJson(Distribution.ZULU, new VersionNumber(11, 0, 9, 1), Latest.NONE, OperatingSystem.WINDOWS, LibCType.NONE,
                                                             Architecture.X64, Bitness.BIT_64, ArchiveType.ZIP, PackageType.JDK, false, true, ReleaseStatus.GA, TermOfSupport.LTS, Scope.PUBLIC);
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
        DiscoClient discoClient = new DiscoClient();
        List<Distribution> distributionsFound = discoClient.getDistributionsThatSupportVersion("13.0.5.1");
        assert distributionsFound.size() == 1;
        assert distributionsFound.get(0).equals(Distribution.ZULU);
    }

    @Test
    public void getVersionsPerDistributionTest() {
        DiscoClient discoClient = new DiscoClient();
        Map<Distribution, List<VersionNumber>> versionsPerDistribution = discoClient.getVersionsPerDistribution();
        assert versionsPerDistribution.keySet().size() == 15;
    }

    @Test
    public void testCache() {
        DiscoClient discoClient = new DiscoClient();
        long       start   = System.currentTimeMillis();
        Queue<Pkg> allPkgs = discoClient.getAllPackages();
        assert (System.currentTimeMillis() - start) > 1000;
        long numberOfPackages = allPkgs.size();
        while (!discoClient.cacheReady.get()) { }

        start = System.currentTimeMillis();
        Queue<Pkg> allPkgsFromCache = discoClient.getAllPackages();
        assert (System.currentTimeMillis() - start) < 1000;
        assert allPkgsFromCache.size() == numberOfPackages;

        start = System.currentTimeMillis();
        List<Pkg> allPkgsAsyncFromCache = new LinkedList<>();
        try {
            allPkgsAsyncFromCache.addAll(discoClient.getAllPackagesAsync().get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        assert (System.currentTimeMillis() - start) < 1000;
        assert allPkgsAsyncFromCache.size() == numberOfPackages;
    }

    @Test
    public void testCacheAccess() {
        DiscoClient discoClient = new DiscoClient();

        SemVer        semVer        = SemVer.fromText("11.0.10-ea").getSemVer1();
        ReleaseStatus releaseStatus = semVer.getReleaseStatus();
        try {
            assert discoClient.getPkgsAsync(Distribution.NONE, semVer.getVersionNumber(), Latest.NONE, OperatingSystem.MACOS, LibCType.NONE, Architecture.NONE, Bitness.NONE, ArchiveType.NONE, PackageType.JDK, null, true, releaseStatus, TermOfSupport.NONE, Scope.PUBLIC).get().size() >= 34;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        while (!discoClient.cacheReady.get()) { }
        try {
            assert discoClient.getPkgsAsync(Distribution.NONE, semVer.getVersionNumber(), Latest.NONE, OperatingSystem.MACOS, LibCType.NONE, Architecture.NONE, Bitness.NONE, ArchiveType.NONE, PackageType.JDK, null, true, releaseStatus, TermOfSupport.NONE, Scope.PUBLIC).get().size() >= 34;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDistributionFromText() {
        assert Distribution.LIBERICA.equals(Distribution.fromText("Liberica"));
        assert Distribution.LIBERICA.equals(Distribution.fromText("liberica"));
        assert Distribution.LIBERICA.equals(Distribution.fromText("LIBERICA"));
    }
    */
}
