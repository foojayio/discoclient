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

package io.foojay.api.discoclient.pkg;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.foojay.api.discoclient.DiscoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.OptionalInt;

import static io.foojay.api.discoclient.util.Constants.COLON;
import static io.foojay.api.discoclient.util.Constants.COMMA_NEW_LINE;
import static io.foojay.api.discoclient.util.Constants.CURLY_BRACKET_CLOSE;
import static io.foojay.api.discoclient.util.Constants.CURLY_BRACKET_OPEN;
import static io.foojay.api.discoclient.util.Constants.INDENTED_QUOTES;
import static io.foojay.api.discoclient.util.Constants.NEW_LINE;
import static io.foojay.api.discoclient.util.Constants.QUOTES;


public class Pkg {
    private static final Logger          LOGGER                       = LoggerFactory.getLogger(Pkg.class);

    public  static final String          FIELD_ID                     = "id";
    public  static final String          FIELD_ARCHIVE_TYPE           = "archive_type";
    public  static final String          FIELD_DISTRIBUTION           = "distribution";
    public  static final String          FIELD_MAJOR_VERSION          = "major_version";
    public  static final String          FIELD_JAVA_VERSION           = "java_version";
    public  static final String          FIELD_DISTRIBUTION_VERSION   = "distribution_version";
    public  static final String          FIELD_LATEST_BUILD_AVAILABLE = "latest_build_available";
    public  static final String          FIELD_RELEASE_STATUS         = "release_status";
    public  static final String          FIELD_TERM_OF_SUPPORT        = "term_of_support";
    public  static final String          FIELD_OPERATING_SYSTEM       = "operating_system";
    public  static final String          FIELD_LIB_C_TYPE             = "lib_c_type";
    public  static final String          FIELD_ARCHITECTURE           = "architecture";
    public  static final String          FIELD_PACKAGE_TYPE           = "package_type";
    public  static final String          FIELD_JAVAFX_BUNDLED         = "javafx_bundled";
    public  static final String          FIELD_DIRECTLY_DOWNLOADABLE  = "directly_downloadable";
    public  static final String          FIELD_FILENAME               = "filename";
    public  static final String          FIELD_EPHEMERAL_ID           = "ephemeral_id";
    public  static final String          FIELD_USAGE_INFO             = "usage_info";

    private              String          id;
    private              String          ephemeralId;
    private              Distribution    distribution;
    private              MajorVersion    majorVersion;
    private              SemVer          javaVersion;
    private              VersionNumber   distributionVersion;
    private              Architecture    architecture;
    private              OperatingSystem operatingSystem;
    private              LibCType        libcType;
    private              PackageType     packageType;
    private              ReleaseStatus   releaseStatus;
    private              ArchiveType     archiveType;
    private              TermOfSupport   termOfSupport;
    private              Boolean         javafxBundled;
    private              Boolean         latestBuildAvailable;
    private              Boolean         directlyDownloadable;
    private              String          fileName;
    private              UsageInfo       usageInfo;


    public Pkg(final String packageJson) {
        if (null == packageJson || packageJson.isEmpty()) {
            LOGGER.debug("Package json string cannot be null or empty.");
            throw new IllegalArgumentException("Package json string cannot be null or empty.");
        }
        final Gson       gson = new Gson();
        final JsonObject json = gson.fromJson(packageJson, JsonObject.class);

        this.id                   = json.has(FIELD_ID)                     ? json.get(FIELD_ID).getAsString() : "";
        this.distribution         = json.has(FIELD_DISTRIBUTION)           ? DiscoClient.getDistributionFromText(json.get(FIELD_DISTRIBUTION).getAsString()) : null;
        this.majorVersion         = json.has(FIELD_MAJOR_VERSION)          ? new MajorVersion(json.get(FIELD_MAJOR_VERSION).getAsInt())                      : new MajorVersion(1);
        this.javaVersion          = json.has(FIELD_JAVA_VERSION)           ? SemVer.fromText(json.get(FIELD_JAVA_VERSION).getAsString()).getSemVer1()        : new SemVer(new VersionNumber());
        this.distributionVersion  = json.has(FIELD_DISTRIBUTION)           ? VersionNumber.fromText(json.get(FIELD_DISTRIBUTION_VERSION).getAsString())      : new VersionNumber();
        this.latestBuildAvailable = json.has(FIELD_LATEST_BUILD_AVAILABLE) ? json.get(FIELD_LATEST_BUILD_AVAILABLE).getAsBoolean()                           : Boolean.FALSE;
        this.architecture         = json.has(FIELD_ARCHITECTURE)           ? Architecture.fromText(json.get(FIELD_ARCHITECTURE).getAsString())               : Architecture.NOT_FOUND;
        this.operatingSystem      = json.has(FIELD_OPERATING_SYSTEM)       ? OperatingSystem.fromText(json.get(FIELD_OPERATING_SYSTEM).getAsString())        : OperatingSystem.NOT_FOUND;
        this.libcType             = json.has(FIELD_LIB_C_TYPE)             ? LibCType.fromText(json.get(FIELD_LIB_C_TYPE).getAsString())                     : LibCType.NOT_FOUND;
        this.packageType          = json.has(FIELD_PACKAGE_TYPE)           ? PackageType.fromText(json.get(FIELD_PACKAGE_TYPE).getAsString())                : PackageType.NOT_FOUND;
        this.releaseStatus        = json.has(FIELD_RELEASE_STATUS)         ? ReleaseStatus.fromText(json.get(FIELD_RELEASE_STATUS).getAsString())            : ReleaseStatus.NOT_FOUND;
        this.archiveType          = json.has(FIELD_ARCHIVE_TYPE)           ? ArchiveType.fromText(json.get(FIELD_ARCHIVE_TYPE).getAsString())                : ArchiveType.NOT_FOUND;
        this.termOfSupport        = json.has(FIELD_TERM_OF_SUPPORT)        ? TermOfSupport.fromText(json.get(FIELD_TERM_OF_SUPPORT).getAsString())           : TermOfSupport.NOT_FOUND;
        this.javafxBundled        = json.has(FIELD_JAVAFX_BUNDLED)         ? json.get(FIELD_JAVAFX_BUNDLED).getAsBoolean()                                   : Boolean.FALSE;
        this.directlyDownloadable = json.has(FIELD_DIRECTLY_DOWNLOADABLE)  ? json.get(FIELD_DIRECTLY_DOWNLOADABLE).getAsBoolean()                            : Boolean.FALSE;
        this.fileName             = json.has(FIELD_FILENAME)               ? json.get(FIELD_FILENAME).getAsString()                                          : "";
        this.ephemeralId          = json.has(FIELD_EPHEMERAL_ID)           ? json.get(FIELD_EPHEMERAL_ID).getAsString()                                      : "";
        this.usageInfo            = json.has(FIELD_USAGE_INFO)             ? UsageInfo.fromText(json.get(FIELD_USAGE_INFO).getAsString())                    : UsageInfo.NOT_FOUND;
    }


    public String getId() { return id; }

    public Distribution getDistribution() { return distribution; }

    public String getDistributionName() { return this.distribution.getName(); }

    public MajorVersion getMajorVersion() { return majorVersion; }

    public SemVer getJavaVersion() { return javaVersion; }

    public VersionNumber getDistributionVersion() { return distributionVersion; }

    public Boolean isLatestBuildAvailable() { return latestBuildAvailable; }

    public OptionalInt getFeatureVersion() { return javaVersion.getVersionNumber().getFeature(); }

    public OptionalInt getInterimVersion() { return javaVersion.getVersionNumber().getInterim(); }

    public OptionalInt getUpdateVersion() { return javaVersion.getVersionNumber().getUpdate(); }

    public OptionalInt getPatchVersion() { return javaVersion.getVersionNumber().getPatch(); }

    public Architecture getArchitecture() { return architecture; }

    public Bitness getBitness() { return architecture == Architecture.NOT_FOUND ? Bitness.NOT_FOUND : architecture.getBitness(); }

    public OperatingSystem getOperatingSystem() { return operatingSystem; }

    public LibCType getLibCType() { return libcType; }

    public PackageType getPackageType() { return packageType; }

    public ReleaseStatus getReleaseStatus() { return releaseStatus; }

    public ArchiveType getArchiveType() { return archiveType; }

    public TermOfSupport getTermOfSupport() { return termOfSupport; }

    public Boolean isJavaFXBundled() { return javafxBundled; }

    public Boolean isDirectlyDownloadable() { return directlyDownloadable; }

    public String getFileName() { return fileName; }

    public String getEphemeralId() { return ephemeralId; }

    public UsageInfo getUsageInfo() { return usageInfo; }

    @Override public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pkg pkg = (Pkg) o;
        return javafxBundled == pkg.javafxBundled && distribution.equals(pkg.distribution) && javaVersion.equalTo(pkg.javaVersion) && architecture == pkg.architecture &&
               operatingSystem == pkg.operatingSystem && packageType == pkg.packageType && releaseStatus == pkg.releaseStatus &&
               archiveType == pkg.archiveType && termOfSupport == pkg.termOfSupport && ephemeralId.equals(pkg.ephemeralId) && latestBuildAvailable == pkg.latestBuildAvailable;
    }

    @Override public int hashCode() {
        return Objects.hash(distribution, javaVersion, latestBuildAvailable, architecture, operatingSystem, packageType, releaseStatus, archiveType, termOfSupport, javafxBundled, ephemeralId);
    }

    @Override public String toString() {
        return new StringBuilder().append(CURLY_BRACKET_OPEN).append(NEW_LINE)
                                  .append(INDENTED_QUOTES).append(FIELD_ID).append(QUOTES).append(COLON).append(getId()).append(COMMA_NEW_LINE)
                                  .append(INDENTED_QUOTES).append(FIELD_DISTRIBUTION).append(QUOTES).append(COLON).append(QUOTES).append(distribution.getName()).append(QUOTES).append(COMMA_NEW_LINE)
                                  .append(INDENTED_QUOTES).append(FIELD_JAVA_VERSION).append(QUOTES).append(COLON).append(QUOTES).append(javaVersion.toString()).append(QUOTES).append(COMMA_NEW_LINE)
                                  .append(INDENTED_QUOTES).append(FIELD_DISTRIBUTION_VERSION).append(QUOTES).append(COLON).append(QUOTES).append(distributionVersion).append(QUOTES).append(COMMA_NEW_LINE)
                                  .append(INDENTED_QUOTES).append(FIELD_LATEST_BUILD_AVAILABLE).append(QUOTES).append(COLON).append(latestBuildAvailable).append(COMMA_NEW_LINE)
                                  .append(INDENTED_QUOTES).append(FIELD_ARCHITECTURE).append(QUOTES).append(COLON).append(QUOTES).append(architecture.name()).append(QUOTES).append(COMMA_NEW_LINE)
                                  .append(INDENTED_QUOTES).append(FIELD_OPERATING_SYSTEM).append(QUOTES).append(COLON).append(QUOTES).append(operatingSystem.name()).append(QUOTES).append(COMMA_NEW_LINE)
                                  .append(INDENTED_QUOTES).append(FIELD_PACKAGE_TYPE).append(QUOTES).append(COLON).append(QUOTES).append(packageType.name()).append(QUOTES).append(COMMA_NEW_LINE)
                                  .append(INDENTED_QUOTES).append(FIELD_RELEASE_STATUS).append(QUOTES).append(COLON).append(QUOTES).append(releaseStatus.name()).append(QUOTES).append(COMMA_NEW_LINE)
                                  .append(INDENTED_QUOTES).append(FIELD_ARCHIVE_TYPE).append(QUOTES).append(COLON).append(QUOTES).append(archiveType.getUiString()).append(QUOTES).append(COMMA_NEW_LINE)
                                  .append(INDENTED_QUOTES).append(FIELD_TERM_OF_SUPPORT).append(QUOTES).append(COLON).append(QUOTES).append(termOfSupport.name()).append(QUOTES).append(COMMA_NEW_LINE)
                                  .append(INDENTED_QUOTES).append(FIELD_JAVAFX_BUNDLED).append(QUOTES).append(COLON).append(javafxBundled).append(COMMA_NEW_LINE)
                                  .append(INDENTED_QUOTES).append(FIELD_FILENAME).append(QUOTES).append(COLON).append(fileName).append(COMMA_NEW_LINE)
                                  .append(INDENTED_QUOTES).append(FIELD_EPHEMERAL_ID).append(QUOTES).append(COLON).append(QUOTES).append(ephemeralId).append(QUOTES).append(COMMA_NEW_LINE)
                                  .append(INDENTED_QUOTES).append(FIELD_USAGE_INFO).append(QUOTES).append(COLON).append(QUOTES).append(usageInfo.getUiString()).append(QUOTES).append(NEW_LINE)
                                  .append(CURLY_BRACKET_CLOSE)
                                  .toString();
    }
}
