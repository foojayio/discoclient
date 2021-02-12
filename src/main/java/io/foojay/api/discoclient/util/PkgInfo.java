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

package io.foojay.api.discoclient.util;


import io.foojay.api.discoclient.pkg.SemVer;


public class PkgInfo {
    public static final String FIELD_FILENAME            = "filename";
    public static final String FIELD_JAVA_VERSION        = "java_version";
    public static final String FIELD_DIRECT_DOWNLOAD_URI = "direct_download_uri";
    public static final String FIELD_DOWNLOAD_SITE_URI   = "download_site_uri";

    private final String fileName;
    private final SemVer javaVersion;
    private final String directDownloadUri;
    private final String downloadSiteUri;


    public PkgInfo(final String fileName, final SemVer javaVersion, final String directDownloadUri, final String downloadSiteUri) {
        this.fileName          = fileName;
        this.javaVersion       = javaVersion;
        this.directDownloadUri = directDownloadUri;
        this.downloadSiteUri   = downloadSiteUri;
    }


    public final String getFileName() { return fileName; }

    public final SemVer getJavaVersion() { return javaVersion; }

    public final String getDirectDownloadUri() { return directDownloadUri; }

    public final String getDownloadSiteUri() { return downloadSiteUri; }

    @Override public String toString() {
        return new StringBuilder().append("{\n")
                                  .append("  \"").append(PkgInfo.FIELD_FILENAME).append("\"").append(":").append("\"").append(fileName).append("\"").append(",\n")
                                  .append("  \"").append(PkgInfo.FIELD_JAVA_VERSION).append("\"").append(":").append("\"").append(javaVersion.toString()).append("\"").append(",\"")
                                  .append("  \"").append(PkgInfo.FIELD_DIRECT_DOWNLOAD_URI).append("\"").append(":").append("\"").append(directDownloadUri).append("\"").append(",\n")
                                  .append("  \"").append(PkgInfo.FIELD_DOWNLOAD_SITE_URI).append("\"").append(":").append("\"").append(downloadSiteUri).append("\"").append("\n")
                                  .append("}")
                                  .toString();
    }
}
