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

import java.util.Arrays;
import java.util.List;

import static io.foojay.api.discoclient.util.Constants.COLON;
import static io.foojay.api.discoclient.util.Constants.COMMA;
import static io.foojay.api.discoclient.util.Constants.CURLY_BRACKET_CLOSE;
import static io.foojay.api.discoclient.util.Constants.CURLY_BRACKET_OPEN;
import static io.foojay.api.discoclient.util.Constants.QUOTES;


public enum Verification implements ApiFeature {
    YES("yes", "yes"),
    NO("no", "no"),
    UNKNOWN("unknown", "unknown"),
    NONE("-", ""),
    NOT_FOUND("", "");

    private final String uiString;
    private final String apiString;


    Verification(final String uiString, final String apiString) {
        this.uiString  = uiString;
        this.apiString = apiString;
    }


    @Override public String getUiString() { return uiString; }

    @Override public String getApiString() { return apiString; }

    @Override public Verification getDefault() { return Verification.UNKNOWN; }

    @Override public Verification getNotFound() { return Verification.NOT_FOUND; }

    @Override public Verification[] getAll() { return values(); }

    @Override public String toString() {
        return new StringBuilder().append(CURLY_BRACKET_OPEN)
                                  .append(QUOTES).append("name").append(QUOTES).append(COLON).append(QUOTES).append(name()).append(QUOTES).append(COMMA)
                                  .append(QUOTES).append("ui_string").append(QUOTES).append(COLON).append(QUOTES).append(uiString).append(QUOTES).append(COMMA)
                                  .append(QUOTES).append("api_string").append(QUOTES).append(COLON).append(QUOTES).append(apiString).append(QUOTES)
                                  .append(CURLY_BRACKET_CLOSE).toString();
    }

    public Boolean getAsBoolean() {
        switch (Verification.this) {
            case YES: return Boolean.TRUE;
            case NO : return Boolean.FALSE;
            default : return null;
        }
    }

    public static Verification fromText(final String text) {
        if (null == text) { return NOT_FOUND; }
        switch (text) {
            case "yes":
            case "YES":
            case "Yes":
            case "y"  :
            case "Y"  :
            case "true":
            case "TRUE":
                return YES;
            case "no":
            case "NO":
            case "No":
            case "n" :
            case "N" :
            case "false":
            case "FALSE":
                return NO;
            case "unknown":
            case "UNKNOWN":
            case "Unknown":
                return UNKNOWN;
            case "":
                return NONE;
            default:
                return NOT_FOUND;
        }
    }

    public static List<Verification> getAsList() { return Arrays.asList(values()); }
}
