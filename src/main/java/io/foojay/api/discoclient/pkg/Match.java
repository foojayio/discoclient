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


public enum Match implements ApiFeature {
    ANY("Any", "any"),
    ALL("All", "all"),
    NONE("-", ""),
    NOT_FOUND("", "");

    private final String uiString;
    private final String apiString;


    Match(final String uiString, final String apiString) {
        this.uiString  = uiString;
        this.apiString = apiString;
    }


    @Override public String getUiString() { return uiString; }

    @Override public String getApiString() { return apiString; }

    @Override public Match getDefault() { return Match.ANY; }

    @Override public Match getNotFound() { return Match.ANY; }

    @Override public Match[] getAll() { return values(); }

    public static Match fromText(final String text) {
        if (null == text) { return ANY; }
        switch (text) {
            case "any":
            case "ANY":
            case "Any":
                return ANY;
            case "all":
            case "ALL":
            case "All":
                return ALL;
            default:
                return ANY;
        }
    }

    public static List<Match> getAsList() { return Arrays.asList(values()); }

    @Override public String toString() { return uiString; }
}
