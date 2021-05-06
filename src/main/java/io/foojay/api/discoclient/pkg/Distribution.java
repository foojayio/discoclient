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
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class Distribution {
    public static final String       FIELD_NAME       = "name";
    public static final String       FIELD_UI_STRING  = "ui_string";
    public static final String       FIELD_API_STRING = "api_string";
    public static final String       FIELD_SYNONYMS   = "synonyms";
    public static final String       FIELD_SCOPES     = "scopes";
    private final       String       name;
    private final       String       uiString;
    private final       String       apiString;
    private final       List<String> synonyms;
    private final       List<Scope>  scopes;


    public Distribution(final String name, final String uiString, final String apiString) {
        this(name, uiString, apiString, List.of(), List.of());
    }
    public Distribution(final String name, final String uiString, final String apiString, final String... synonyms) {
        this(name, uiString, apiString, Arrays.asList(synonyms), List.of());
    }
    public Distribution(final String name, final String uiString, final String apiString, final List<String> synonyms, final List<Scope> scopes) {
        this.name      = name;
        this.uiString  = uiString;
        this.apiString = apiString;
        this.synonyms  = new ArrayList<>(synonyms);
        this.scopes    = new ArrayList<>(scopes);
    }
    public Distribution(final String jsonText) {
        if (null == jsonText || jsonText.isEmpty()) { throw new IllegalArgumentException("Json text cannot be null or empty"); }
        final Gson       gson = new Gson();
        final JsonObject json = gson.fromJson(jsonText, JsonObject.class);

        this.name      = json.get(FIELD_NAME).getAsString();
        this.uiString  = json.get(FIELD_UI_STRING).getAsString();
        this.apiString = json.get(FIELD_API_STRING).getAsString();
        this.synonyms  = new ArrayList<>();
        this.scopes    = new ArrayList<>();

        if (json.has(FIELD_SYNONYMS)) {
            JsonArray synonyms = json.get(FIELD_SYNONYMS).getAsJsonArray();
            synonyms.forEach(element -> this.synonyms.add(element.getAsString()));
        }
        if (json.has(FIELD_SCOPES)) {
            JsonArray scopes = json.get(FIELD_SCOPES).getAsJsonArray();
            scopes.forEach(element -> {
                Scope scopeFound = Scope.fromText(element.getAsString());
                if (Scope.NOT_FOUND != scopeFound) { this.scopes.add(scopeFound); }
            });
        }
    }


    public String getName() { return name; }

    public String getUiString() { return uiString; }

    public String getApiString() { return apiString; }

    public List<String> getSynonyms() { return synonyms; }

    public List<Scope> getScopes() { return scopes; }

    public Distribution getFromText(final String text) {
        return synonyms.contains(text) ? Distribution.this : null;
    }

    @Override public String toString() {
        return new StringBuilder().append("{")
                                  .append("\"").append(FIELD_NAME).append("\":\"").append(name).append("\"").append(",")
                                  .append("\"").append(FIELD_UI_STRING).append("\":\"").append(uiString).append("\"").append(",")
                                  .append("\"").append(FIELD_API_STRING).append("\":\"").append(apiString).append("\"").append(",")
                                  .append("\"").append(FIELD_SYNONYMS).append("\":").append(synonyms.stream().collect(Collectors.joining("\",\"", "[\"", "\"]"))).append(",")
                                  .append("\"").append(FIELD_SCOPES).append("\":").append(scopes.stream().map(scope -> scope.getApiString()).collect(Collectors.joining("\",\"", "[\"", "\"]")))
                                  .append("}")
                                  .toString();
    }
}
