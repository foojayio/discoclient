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


public enum Feature implements ApiFeature {
    LOOM("Loom", "loom"),
    PANAMA("Panama", "panama"),
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
            default:
                return NOT_FOUND;
        }
    }

    public static List<Feature> getAsList() { return Arrays.asList(values()); }

    @Override public String toString() { return new StringBuilder("\"").append(apiString).append("\"").toString(); }
}
