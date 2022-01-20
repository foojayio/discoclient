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


import eu.hansolo.jdktools.HashAlgorithm;
import eu.hansolo.jdktools.versioning.Semver;


public class PkgInfo {
    public static final String FIELD_FILENAME            = "filename";
    public static final String FIELD_JAVA_VERSION        = "java_version";
    public static final String FIELD_DIRECT_DOWNLOAD_URI = "direct_download_uri";
    public static final String FIELD_DOWNLOAD_SITE_URI   = "download_site_uri";
    public static final String FIELD_SIGNATURE_URI       = "signature_uri";
    public static final String FIELD_CHECKSUM_URI        = "checksum_uri";
    public static final String FIELD_CHECKSUM            = "checksum";
    public static final String FIELD_CHECKSUM_TYPE       = "checksum_type";


    private final String        fileName;
    private final Semver        javaVersion;
    private final String        directDownloadUri;
    private final String        downloadSiteUri;
    private final String        signatureUri;
    private final String        checksumUri;
    private final String        checksum;
    private final HashAlgorithm checksumType;


    public PkgInfo(final String fileName, final Semver javaVersion, final String directDownloadUri, final String downloadSiteUri, final String signatureUri, final String checksumUri, final String checksum, final HashAlgorithm checksumType) {
        this.fileName          = fileName;
        this.javaVersion       = javaVersion;
        this.directDownloadUri = directDownloadUri;
        this.downloadSiteUri   = downloadSiteUri;
        this.signatureUri      = signatureUri;
        this.checksumUri       = checksumUri;
        this.checksum          = checksum;
        this.checksumType      = checksumType;
    }


    public final String getFileName() { return fileName; }

    public final Semver getJavaVersion() { return javaVersion; }

    public final String getDirectDownloadUri() { return directDownloadUri; }

    public final String getDownloadSiteUri() { return downloadSiteUri; }

    public final String getSignatureUri() { return signatureUri; }

    public final String getChecksumUri() { return checksumUri; }

    public final String getChecksum() { return checksum; }

    public final HashAlgorithm getChecksumType() { return checksumType; }

    @Override public String toString() {
        return new StringBuilder().append("{")
                                  .append("\"").append(PkgInfo.FIELD_FILENAME).append("\"").append(":").append("\"").append(fileName).append("\"").append(",")
                                  .append("\"").append(PkgInfo.FIELD_JAVA_VERSION).append("\"").append(":").append("\"").append(javaVersion.toString()).append("\"").append(",")
                                  .append("\"").append(PkgInfo.FIELD_DIRECT_DOWNLOAD_URI).append("\"").append(":").append("\"").append(directDownloadUri).append("\"").append(",")
                                  .append("\"").append(PkgInfo.FIELD_DOWNLOAD_SITE_URI).append("\"").append(":").append("\"").append(downloadSiteUri).append("\"").append(",")
                                  .append("\"").append(PkgInfo.FIELD_SIGNATURE_URI).append("\"").append(":").append("\"").append(signatureUri).append("\"").append(",")
                                  .append("\"").append(PkgInfo.FIELD_CHECKSUM_URI).append("\"").append(":").append("\"").append(checksumUri).append("\"").append(",")
                                  .append("\"").append(PkgInfo.FIELD_CHECKSUM).append("\"").append(":").append("\"").append(checksum).append("\"").append(",")
                                  .append("\"").append(PkgInfo.FIELD_CHECKSUM_TYPE).append("\"").append(":").append("\"").append(checksumType.getApiString()).append("\"")
                                  .append("}")
                                  .toString();
    }
}
