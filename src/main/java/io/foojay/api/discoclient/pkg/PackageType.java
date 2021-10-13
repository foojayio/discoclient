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


public enum PackageType implements ApiFeature {
    JDK("JDK", "jdk"),
    JRE("JRE", "jre"),
    NONE("-", ""),
    NOT_FOUND("", "");

    private final String uiString;
    private final String apiString;


    PackageType(final String uiString, final String apiString) {
        this.uiString  = uiString;
        this.apiString = apiString;
    }


    @Override public String getUiString() { return uiString; }

    @Override public String getApiString() { return apiString; }

    @Override public PackageType getDefault() { return PackageType.NONE; }

    @Override public PackageType getNotFound() { return PackageType.NOT_FOUND; }

    @Override public PackageType[] getAll() { return values(); }

    public static PackageType fromText(final String text) {
        if (null == text) { return NOT_FOUND; }
        switch (text) {
            case "-jdk":
            case "JDK":
            case "jdk":
            case "jdk+fx":
            case "JDK+FX":
                return JDK;
            case "-jre":
            case "JRE":
            case "jre":
            case "jre+fx":
            case "JRE+FX":
                return JRE;
            default:
                return NOT_FOUND;
        }
    }

    public static List<PackageType> getAsList() { return Arrays.asList(values()); }

    @Override public String toString() { return uiString; }
}
