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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.foojay.api.discoclient.util.Helper;

import java.util.ArrayList;
import java.util.List;


public class MajorVersion {
    public  static final String        FIELD_MAJOR_VERSION   = "major_version";
    public  static final String        FIELD_TERM_OF_SUPPORT = "term_of_support";
    public  static final String        FIELD_MAINTAINED      = "maintained";
    public  static final String        FIELD_VERSIONS        = "versions";
    private              List<SemVer>  versions = new ArrayList<>();
    private        final int           majorVersion;
    private        final TermOfSupport termOfSupport;
    private              boolean       maintained;


    public MajorVersion(final int majorVersion) {
        this(majorVersion, Helper.getTermOfSupport(majorVersion));
    }
    public MajorVersion(final int majorVersion, final TermOfSupport termOfSupport) {
        if (majorVersion <= 0) { throw new IllegalArgumentException("Major version cannot be <= 0"); }
        this.majorVersion  = majorVersion;
        this.termOfSupport = termOfSupport;
        this.maintained    = false;
    }
    public MajorVersion(final String jsonText) {
        if (null == jsonText || jsonText.isEmpty()) { throw new IllegalArgumentException("json text cannot be null or empty"); }
        final Gson       gson = new Gson();
        final JsonObject json = gson.fromJson(jsonText, JsonObject.class);

        this.majorVersion  = json.has(FIELD_MAJOR_VERSION)   ? json.get(FIELD_MAJOR_VERSION).getAsInt()                              : 1;
        this.termOfSupport = json.has(FIELD_TERM_OF_SUPPORT) ? TermOfSupport.fromText(json.get(FIELD_TERM_OF_SUPPORT).getAsString()) : TermOfSupport.NOT_FOUND;
        this.maintained    = json.has(FIELD_MAINTAINED)      ? Boolean.valueOf(json.get(FIELD_MAINTAINED).toString().toLowerCase())  : false;
        if (json.has(FIELD_VERSIONS)) {
            JsonArray versionsArray = json.getAsJsonArray(FIELD_VERSIONS);
            for (JsonElement jsonElement : versionsArray) {
                this.versions.add(SemVer.fromText(jsonElement.getAsString()).getSemVer1());
            }
        }
    }


    public int getAsInt() { return majorVersion; }

    public TermOfSupport getTermOfSupport() { return termOfSupport; }

    public boolean isMaintained() { return maintained; }

    public Boolean isEarlyAccessOnly() {
        return getVersions().stream().filter(semver -> ReleaseStatus.EA == semver.getReleaseStatus()).count() == getVersions().size();
    }

    public List<SemVer> getVersions() { return versions; }

    // VersionNumber
    public VersionNumber getVersionNumber() { return new VersionNumber(majorVersion); }
}
