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
import eu.hansolo.jdktools.ReleaseStatus;
import eu.hansolo.jdktools.TermOfSupport;
import eu.hansolo.jdktools.versioning.Semver;
import eu.hansolo.jdktools.versioning.VersionNumber;
import eu.hansolo.jdktools.util.Helper;

import java.util.ArrayList;
import java.util.List;


public class MajorVersion {
    public  static final String        FIELD_MAJOR_VERSION   = "major_version";
    public  static final String        FIELD_TERM_OF_SUPPORT = "term_of_support";
    public  static final String        FIELD_MAINTAINED      = "maintained";
    public  static final String        FIELD_SCOPE           = "scope";
    public  static final String        FIELD_VERSIONS        = "versions";
    private              List<Semver>  versions              = new ArrayList<>();
    private        final int           majorVersion;
    private        final TermOfSupport termOfSupport;
    private              boolean       maintained;
    private              Scope         scope;


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
        this.maintained    = json.has(FIELD_MAINTAINED)      ? json.get(FIELD_MAINTAINED).getAsBoolean()                             : false;
        this.scope         = json.has(FIELD_SCOPE)           ? Scope.fromText(json.get(FIELD_SCOPE).getAsString())                   : Scope.NOT_FOUND;
        if (json.has(FIELD_VERSIONS)) {
            JsonArray versionsArray = json.getAsJsonArray(FIELD_VERSIONS);
            for (JsonElement jsonElement : versionsArray) {
                this.versions.add(Semver.fromText(jsonElement.getAsString()).getSemver1());
            }
        }
    }


    public int getAsInt() { return majorVersion; }

    public TermOfSupport getTermOfSupport() { return termOfSupport; }

    public boolean isMaintained() { return maintained; }

    public Scope getScope() { return scope; }

    public Boolean isEarlyAccessOnly() {
        return getVersions().stream().filter(semver -> ReleaseStatus.EA == semver.getReleaseStatus()).count() == getVersions().size();
    }

    public List<Semver> getVersions() { return versions; }

    // VersionNumber
    public VersionNumber getVersionNumber() { return new VersionNumber(majorVersion); }

    @Override public String toString() {
        return Integer.toString(getAsInt());
    }
}
