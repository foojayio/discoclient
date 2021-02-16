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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.OptionalInt;


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

    private              String          id;
    private              ArchiveType     archiveType;
    private              Distribution    distribution;
    private              MajorVersion    majorVersion;
    private              SemVer          javaVersion;
    private              VersionNumber   distributionVersion;
    private              Boolean         latestBuildAvailable;
    private              ReleaseStatus   releaseStatus;
    private              TermOfSupport   termOfSupport;
    private              OperatingSystem operatingSystem;
    private              LibCType        libcType;
    private              Architecture    architecture;
    private              PackageType     packageType;
    private              Boolean         javafxBundled;
    private              Boolean         directlyDownloadable;
    private              String          fileName;
    private              String          ephemeralId;


    public Pkg(final String packageJson) {
        if (null == packageJson || packageJson.isEmpty()) {
            LOGGER.debug("Package json string cannot be null or empty.");
            throw new IllegalArgumentException("Package json string cannot be null or empty.");
        }
        final Gson       gson = new Gson();
        final JsonObject json = gson.fromJson(packageJson, JsonObject.class);

        this.id                   = json.has(FIELD_ID)                     ? json.get(FIELD_ID).getAsString() : "";
        this.distribution         = json.has(FIELD_DISTRIBUTION)           ? Distribution.fromText(json.get(FIELD_DISTRIBUTION).getAsString())          : Distribution.NOT_FOUND;
        this.majorVersion         = json.has(FIELD_MAJOR_VERSION)          ? new MajorVersion(json.get(FIELD_MAJOR_VERSION).getAsInt())                 : new MajorVersion(1);
        this.javaVersion          = json.has(FIELD_JAVA_VERSION)           ? SemVer.fromText(json.get(FIELD_JAVA_VERSION).getAsString()).getSemVer1()   : new SemVer(new VersionNumber());
        this.distributionVersion  = json.has(FIELD_DISTRIBUTION)           ? VersionNumber.fromText(json.get(FIELD_DISTRIBUTION_VERSION).getAsString()) : new VersionNumber();
        this.latestBuildAvailable = json.has(FIELD_LATEST_BUILD_AVAILABLE) ? json.get(FIELD_LATEST_BUILD_AVAILABLE).getAsBoolean()                      : Boolean.FALSE;
        this.architecture         = json.has(FIELD_ARCHITECTURE)           ? Architecture.fromText(json.get(FIELD_ARCHITECTURE).getAsString())          : Architecture.NOT_FOUND;
        this.operatingSystem      = json.has(FIELD_OPERATING_SYSTEM)       ? OperatingSystem.fromText(json.get(FIELD_OPERATING_SYSTEM).getAsString())   : OperatingSystem.NOT_FOUND;
        this.libcType             = json.has(FIELD_LIB_C_TYPE)             ? LibCType.fromText(json.get(FIELD_LIB_C_TYPE).getAsString())                : LibCType.NOT_FOUND;
        this.packageType          = json.has(FIELD_PACKAGE_TYPE)           ? PackageType.fromText(json.get(FIELD_PACKAGE_TYPE).getAsString())           : PackageType.NOT_FOUND;
        this.releaseStatus        = json.has(FIELD_RELEASE_STATUS)         ? ReleaseStatus.fromText(json.get(FIELD_RELEASE_STATUS).getAsString())       : ReleaseStatus.NOT_FOUND;
        this.archiveType          = json.has(FIELD_ARCHIVE_TYPE)           ? ArchiveType.fromText(json.get(FIELD_ARCHIVE_TYPE).getAsString())           : ArchiveType.NOT_FOUND;
        this.termOfSupport        = json.has(FIELD_TERM_OF_SUPPORT)        ? TermOfSupport.fromText(json.get(FIELD_TERM_OF_SUPPORT).getAsString())      : TermOfSupport.NOT_FOUND;
        this.javafxBundled        = json.has(FIELD_JAVAFX_BUNDLED)         ? json.get(FIELD_JAVAFX_BUNDLED).getAsBoolean()                              : Boolean.FALSE;
        this.directlyDownloadable = json.has(FIELD_DIRECTLY_DOWNLOADABLE)  ? json.get(FIELD_DIRECTLY_DOWNLOADABLE).getAsBoolean()                       : Boolean.FALSE;
        this.fileName             = json.has(FIELD_FILENAME)               ? json.get(FIELD_FILENAME).getAsString()                                     : "";
        this.ephemeralId          = json.has(FIELD_EPHEMERAL_ID)           ? json.get(FIELD_EPHEMERAL_ID).getAsString()                                 : "";
    }


    public String getId() { return id; }

    public Distribution getDistribution() { return distribution; }

    public String getDistributionName() { return this.distribution.name(); }

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
        return new StringBuilder().append("{\n")
                                  .append("  \"").append(FIELD_ID).append("\"").append(":").append(getId()).append(",\n")
                                  .append("  \"").append(FIELD_DISTRIBUTION).append("\"").append(":").append("\"").append(distribution.name()).append("\"").append(",\n")
                                  .append("  \"").append(FIELD_JAVA_VERSION).append("\"").append(":").append("\"").append(javaVersion.toString()).append("\"").append(",\n")
                                  .append("  \"").append(FIELD_DISTRIBUTION_VERSION).append("\"").append(":").append("\"").append(distributionVersion).append("\"").append(",\n")
                                  .append("  \"").append(FIELD_LATEST_BUILD_AVAILABLE).append("\"").append(":").append(latestBuildAvailable).append(",\n")
                                  .append("  \"").append(FIELD_ARCHITECTURE).append("\"").append(":").append("\"").append(architecture.name()).append("\"").append(",\n")
                                  .append("  \"").append(FIELD_OPERATING_SYSTEM).append("\"").append(":").append("\"").append(operatingSystem.name()).append("\"").append(",\n")
                                  .append("  \"").append(FIELD_PACKAGE_TYPE).append("\"").append(":").append("\"").append(packageType.name()).append("\"").append(",\n")
                                  .append("  \"").append(FIELD_RELEASE_STATUS).append("\"").append(":").append("\"").append(releaseStatus.name()).append("\"").append(",\n")
                                  .append("  \"").append(FIELD_ARCHIVE_TYPE).append("\"").append(":").append("\"").append(archiveType.getUiString()).append("\"").append(",\n")
                                  .append("  \"").append(FIELD_TERM_OF_SUPPORT).append("\"").append(":").append("\"").append(termOfSupport.name()).append("\"").append(",\n")
                                  .append("  \"").append(FIELD_JAVAFX_BUNDLED).append("\"").append(":").append(javafxBundled).append(",\n")
                                  .append("  \"").append(FIELD_FILENAME).append("\"").append(":").append(fileName).append(",\n")
                                  .append("  \"").append(FIELD_EPHEMERAL_ID).append("\"").append(":").append("\"").append(ephemeralId).append("\"").append("\n")
                                  .append("}")
                                  .toString();
    }
}
