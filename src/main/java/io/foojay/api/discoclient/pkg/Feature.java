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


public enum Feature implements Api {
    LOOM("Loom", "loom"),
    PANAMA("Panama", "panama"),
    LANAI("Lanai", "lanai"),
    VALHALLA("Valhalla", "valhalla"),
    KONA_FIBER("KonaFiber", "kona_fiber"),
    CRAC("CRaC", "crac"),
    NONE("-", ""),
    NOT_FOUND("", "");

    private final String uiString;
    private final String apiString;


    Feature(final String uiString, final String apiString) {
        this.uiString  = uiString;
        this.apiString = apiString;
    }


    @Override public String getUiString() { return uiString; }

    @Override public String getApiString() { return apiString; }

    @Override public Feature getDefault() { return Feature.NONE; }

    @Override public Feature getNotFound() { return Feature.NOT_FOUND; }

    @Override public Feature[] getAll() { return values(); }

    public String toString(final OutputFormat outputFormat) {
        StringBuilder msgBuilder = new StringBuilder();
        switch(outputFormat) {
            case FULL:
            case REDUCED:
            case REDUCED_ENRICHED:
                msgBuilder.append(CURLY_BRACKET_OPEN).append(NEW_LINE)
                          .append(INDENTED_QUOTES).append("name").append(QUOTES).append(COLON).append(QUOTES).append(name()).append(QUOTES).append(COMMA_NEW_LINE)
                          .append(INDENTED_QUOTES).append("ui_string").append(QUOTES).append(COLON).append(QUOTES).append(uiString).append(QUOTES).append(COMMA_NEW_LINE)
                          .append(INDENTED_QUOTES).append("api_string").append(QUOTES).append(COLON).append(QUOTES).append(apiString).append(QUOTES).append(NEW_LINE)
                          .append(CURLY_BRACKET_CLOSE);
                break;
            default:
                msgBuilder.append(CURLY_BRACKET_OPEN)
                          .append(QUOTES).append("name").append(QUOTES).append(COLON).append(QUOTES).append(name()).append(QUOTES).append(COMMA)
                          .append(QUOTES).append("ui_string").append(QUOTES).append(COLON).append(QUOTES).append(uiString).append(QUOTES).append(COMMA)
                          .append(QUOTES).append("api_string").append(QUOTES).append(COLON).append(QUOTES).append(apiString).append(QUOTES)
                          .append(CURLY_BRACKET_CLOSE);
                break;
        }
        return msgBuilder.toString();
    }

    @Override public String toString() { return toString(OutputFormat.FULL_COMPRESSED); }

    public static Feature fromText(final String text) {
        if (null == text) { return NOT_FOUND; }
        switch (text) {
            case "loom":
            case "LOOM":
            case "Loom":
                return LOOM;
            case "panama":
            case "PANAMA":
            case "Panama":
                return PANAMA;
            case "lanai":
            case "LANAI":
            case "Lanai":
                return LANAI;
            case "valhalla":
            case "VALHALLA":
            case "Valhalla":
                return VALHALLA;
            case "kona_fiber":
            case "KONA_FIBER":
            case "Kona Fiber":
            case "KONA FIBER":
            case "Kona_Fiber":
            case "KonaFiber":
            case "konafiber":
            case "KONAFIBER":
                return KONA_FIBER;
            case "crac":
            case "CRAC":
            case "CRaC":
                return CRAC;
            default:
                return NOT_FOUND;
        }
    }

    public static List<Feature> getAsList() { return Arrays.asList(values()); }
}
