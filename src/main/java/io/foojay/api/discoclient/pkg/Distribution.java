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
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class Distribution {
    public  static final String       FIELD_NAME       = "name";
    public  static final String       FIELD_UI_STRING  = "ui_string";
    public  static final String       FIELD_API_STRING = "api_string";
    public  static final String       FIELD_SYNONYMS   = "synonyms";
    public  static final String       FIELD_SCOPES     = "scopes";
    public  static final String       FIELD_MAINTAINED = "maintained";
    private        final String       name;
    private        final String       uiString;
    private        final String       apiString;
    private        final List<String> synonyms;
    private        final List<Scope>  scopes;
    private        final boolean      maintained;


    public Distribution(final String name, final String uiString, final String apiString) {
        this(name, uiString, apiString, List.of(), List.of(), true);
    }
    public Distribution(final String name, final String uiString, final String apiString, final String... synonyms) {
        this(name, uiString, apiString, Arrays.asList(synonyms), List.of(), true);
    }
    public Distribution(final String name, final String uiString, final String apiString, final List<String> synonyms, final List<Scope> scopes, final boolean maintained) {
        this.name       = name;
        this.uiString   = uiString;
        this.apiString  = apiString;
        this.synonyms   = new ArrayList<>(synonyms);
        this.scopes     = new ArrayList<>(scopes);
        this.maintained = maintained;
    }
    public Distribution(final String jsonText) {
        if (null == jsonText || jsonText.isEmpty()) { throw new IllegalArgumentException("Json text cannot be null or empty"); }
        final Gson       gson = new Gson();
        final JsonObject json = gson.fromJson(jsonText, JsonObject.class);

        this.name       = json.has(FIELD_NAME)       ? json.get(FIELD_NAME).getAsString()        : "";
        this.uiString   = json.has(FIELD_UI_STRING)  ? json.get(FIELD_UI_STRING).getAsString()   : "";
        this.apiString  = json.has(FIELD_API_STRING) ? json.get(FIELD_API_STRING).getAsString()  : "";
        this.synonyms   = new ArrayList<>();
        this.scopes     = new ArrayList<>();
        this.maintained = json.has(FIELD_MAINTAINED) ? json.get(FIELD_MAINTAINED).getAsBoolean() : true;


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

    public boolean isMaintained() { return maintained; }

    public Distribution getFromText(final String text) {
        return synonyms.contains(text) ? Distribution.this : null;
    }

    @Override public String toString() {
        return new StringBuilder().append("{")
                                  .append("\"").append(FIELD_NAME).append("\":\"").append(name).append("\"").append(",")
                                  .append("\"").append(FIELD_UI_STRING).append("\":\"").append(uiString).append("\"").append(",")
                                  .append("\"").append(FIELD_API_STRING).append("\":\"").append(apiString).append("\"").append(",")
                                  .append("\"").append(FIELD_MAINTAINED).append("\":").append(maintained).append(",")
                                  .append("\"").append(FIELD_SYNONYMS).append("\":").append(synonyms.stream().collect(Collectors.joining("\",\"", "[\"", "\"]"))).append(",")
                                  .append("\"").append(FIELD_SCOPES).append("\":").append(scopes.stream().map(scope -> scope.getApiString()).collect(Collectors.joining("\",\"", "[\"", "\"]")))
                                  .append("}")
                                  .toString();
    }
}
