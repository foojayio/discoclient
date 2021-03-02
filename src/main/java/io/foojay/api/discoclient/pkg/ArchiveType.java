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

package io.foojay.api.discoclient.pkg;

import java.util.Arrays;
import java.util.List;


public enum ArchiveType implements ApiFeature {
    APK("apk", "apk", "apk"),
    BIN("bin", "bin", "bin"),
    CAB("cab", "cab",".cab"),
    DEB("deb", "deb",".deb"),
    DMG("dmg", "dmg",".dmg"),
    MSI("msi", "msi",".msi"),
    PKG("pkg", "pkg",".pkg"),
    RPM("rpm", "rpm",".rpm"),
    SRC_TAR("src.tar.gz", "src_tar",".src.tar.gz", ".source.tar.gz", "source.tar.gz"),
    TAR("tar", "tar", ".tar"),
    TAR_GZ("tar.gz", "tar.gz", ".tar.gz"),
    TAR_Z("tar.Z", "tar.z", ".tar.Z"),
    ZIP("zip", "zip", ".zip"),
    EXE("exe", "exe", ".exe"),
    NONE("-", "", "-"),
    NOT_FOUND("", "", "");

    private final String       uiString;
    private final String       apiString;
    private final List<String> fileEndings;


    ArchiveType(final String uiString, final String apiString, final String... fileEndings) {
        this.uiString    = uiString;
        this.apiString   = apiString;
        this.fileEndings = List.of(fileEndings);
    }


    @Override public String getUiString() { return uiString; }

    @Override public String getApiString() { return apiString; }

    @Override public ArchiveType getDefault() { return ArchiveType.NONE; }

    @Override public ArchiveType getNotFound() { return ArchiveType.NOT_FOUND; }

    @Override public ArchiveType[] getAll() { return values(); }

    public static ArchiveType fromText(final String text) {
        if (null == text) { return NOT_FOUND; }
        switch (text) {
            case "apk":
            case ".apk":
            case "APK":
                return APK;
            case "bin":
            case ".bin":
            case "BIN":
                return BIN;
            case "cab":
            case ".cab":
            case "CAB":
                return CAB;
            case "deb":
            case ".deb":
            case "DEB":
                return DEB;
            case "dmg":
            case ".dmg":
            case "DMG":
                return DMG;
            case "msi":
            case ".msi":
            case "MSI":
                return MSI;
            case "pkg":
            case ".pkg":
            case "PKG":
                return PKG;
            case "rpm":
            case ".rpm":
            case "RPM":
                return RPM;
            case "src.tar.gz":
            case ".src.tar.gz":
            case "source.tar.gz":
            case "SRC.TAR.GZ":
            case "src_tar":
            case "SRC_TAR":
                return SRC_TAR;
            case "tar.Z":
            case ".tar.Z":
            case "TAR.Z":
                return TAR_Z;
            case "tar.gz":
            case ".tar.gz":
            case "TAR.GZ":
                return TAR_GZ;
            case "tar":
            case ".tar":
            case "TAR":
                return TAR;
            case "zip":
            case ".zip":
            case "ZIP":
                return ZIP;
            default:
                return NOT_FOUND;
        }
    }

    public List<String> getFileEndings() { return fileEndings; }

    public static ArchiveType getFromFileName(final String fileName) {
        for (ArchiveType ext : values()) {
            for (String ending : ext.getFileEndings()) {
                if (fileName.toLowerCase().endsWith(ending)) { return ext; }
            }
        }
        return ArchiveType.NONE;
    }

    public static List<ArchiveType> getAsList() { return Arrays.asList(values()); }
}

