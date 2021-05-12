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


public enum Scope implements ApiFeature {
    PUBLIC("Public", "public"),
    DIRECTLY_DOWNLOADABLE("Directly downloadable", "directly_downloadable"),
    NOT_DIRECTLY_DOWNLOADABLE("Not directly downloadable", "not_directly_downloadable"),
    BUILD_OF_OPEN_JDK("Build of OpenJDK", "build_of_openjdk"),
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
            default:
                return NOT_FOUND;
        }
    }

    public static List<Scope> getAsList() { return Arrays.asList(values()); }
}
