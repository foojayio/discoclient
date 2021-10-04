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
