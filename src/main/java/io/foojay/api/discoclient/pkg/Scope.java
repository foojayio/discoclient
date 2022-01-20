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

import eu.hansolo.jdktools.Api;
import eu.hansolo.jdktools.util.OutputFormat;

import java.util.Arrays;
import java.util.List;

import static eu.hansolo.jdktools.Constants.COLON;
import static eu.hansolo.jdktools.Constants.COMMA;
import static eu.hansolo.jdktools.Constants.COMMA_NEW_LINE;
import static eu.hansolo.jdktools.Constants.CURLY_BRACKET_CLOSE;
import static eu.hansolo.jdktools.Constants.CURLY_BRACKET_OPEN;
import static eu.hansolo.jdktools.Constants.INDENTED_QUOTES;
import static eu.hansolo.jdktools.Constants.NEW_LINE;
import static eu.hansolo.jdktools.Constants.QUOTES;


public enum Scope implements Api {
    PUBLIC("Public", "public"),
    DIRECTLY_DOWNLOADABLE("Directly downloadable", "directly_downloadable"),
    NOT_DIRECTLY_DOWNLOADABLE("Not directly downloadable", "not_directly_downloadable"),
    BUILD_OF_OPEN_JDK("Build of OpenJDK", "build_of_openjdk"),
    BUILD_OF_GRAALVM("Build of GraalVM", "build_of_graalvm"),
    FREE_TO_USE_IN_PRODUCTION("Free to use in production", "free_to_use_in_production"),
    LICENSE_NEEDED_FOR_PRODUCTION("License needed for production", "license_needed_for_production"),
    NONE("-", ""),
    NOT_FOUND("", "");

    private final String uiString;
    private final String apiString;


    Scope(final String uiString, final String apiString) {
        this.uiString  = uiString;
        this.apiString = apiString;
    }


    @Override public String getUiString() { return uiString; }

    @Override public String getApiString() { return apiString; }

    @Override public Scope getDefault() { return Scope.NONE; }

    @Override public Scope getNotFound() { return Scope.NOT_FOUND; }

    @Override public Scope[] getAll() { return values(); }

    public String toString(final OutputFormat outputFormat) {
        StringBuilder msgBuilder = new StringBuilder();
        switch(outputFormat) {
            case FULL:
            case REDUCED:
            case REDUCED_ENRICHED: {
                msgBuilder.append(CURLY_BRACKET_OPEN).append(NEW_LINE)
                          .append(INDENTED_QUOTES).append("name").append(QUOTES).append(COLON).append(QUOTES).append(name()).append(QUOTES).append(COMMA_NEW_LINE)
                          .append(INDENTED_QUOTES).append("ui_string").append(QUOTES).append(COLON).append(QUOTES).append(uiString).append(QUOTES).append(COMMA_NEW_LINE)
                          .append(INDENTED_QUOTES).append("api_string").append(QUOTES).append(COLON).append(QUOTES).append(apiString).append(QUOTES).append(NEW_LINE)
                          .append(CURLY_BRACKET_CLOSE);
            }
            case FULL_COMPRESSED:
            case REDUCED_COMPRESSED:
            case REDUCED_ENRICHED_COMPRESSED: {
                msgBuilder.append(CURLY_BRACKET_OPEN)
                          .append(QUOTES).append("name").append(QUOTES).append(COLON).append(QUOTES).append(name()).append(QUOTES).append(COMMA)
                          .append(QUOTES).append("ui_string").append(QUOTES).append(COLON).append(QUOTES).append(uiString).append(QUOTES).append(COMMA)
                          .append(QUOTES).append("api_string").append(QUOTES).append(COLON).append(QUOTES).append(apiString).append(QUOTES)
                          .append(CURLY_BRACKET_CLOSE);
            }
        }
        return msgBuilder.toString();
    }

    @Override public String toString() { return toString(OutputFormat.FULL_COMPRESSED); }

    public static Scope fromText(final String text) {
        switch (text) {
            case "public":
            case "PUBLIC":
                return PUBLIC;
            case "directly_downloadable":
            case "DIRECTLY_DOWNLOADABLE":
            case "direct_download":
            case "DIRECT_DOWNLOAD":
                return DIRECTLY_DOWNLOADABLE;
            case "not_directly_downloadable":
            case "NOT_DIRECTLY_DOWNLOADABLE":
            case "no_direct_download":
            case "NO_DIRECT_DOWNLOAD":
                return NOT_DIRECTLY_DOWNLOADABLE;
            case "build_of_open_jdk":
            case "BUILD_OF_OPEN_JDK":
            case "build_of_openjdk":
            case "BUILD_OF_OPENJDK":
                return BUILD_OF_OPEN_JDK;
            case "build_of_graalvm":
            case "BUILD_OF_GRAALVM":
                return BUILD_OF_GRAALVM;
            case "free":
            case "free_to_use":
            case "free_to_use_in_production":
                return FREE_TO_USE_IN_PRODUCTION;
            case "license":
            case "license_needed":
            case "license_needed_for_production":
                return LICENSE_NEEDED_FOR_PRODUCTION;
            default:
                return NOT_FOUND;
        }
    }

    public static List<Scope> getAsList() { return Arrays.asList(values()); }
}
