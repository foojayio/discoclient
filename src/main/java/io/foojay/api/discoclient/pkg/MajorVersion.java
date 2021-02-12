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

    public List<SemVer> getVersions() { return versions; }

    // VersionNumber
    public VersionNumber getVersionNumber() { return new VersionNumber(majorVersion); }
}
