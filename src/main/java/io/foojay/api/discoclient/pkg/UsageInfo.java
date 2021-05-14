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


public enum UsageInfo implements ApiFeature {
    FREE_TO_USE("Free to use", "free_to_use"),
    LICENSE_NEEDED("License needed for production", "license_needed"),
    AZURE_ONLY("Azure only", "azure_only"),
    NONE("", ""),
    NOT_FOUND("", "");

    private final String uiString;
    private final String apiString;


    UsageInfo(final String uiString, final String apiString) {
        this.uiString  = uiString;
        this.apiString = apiString;
    }


    @Override public String getUiString() { return uiString; }

    @Override public String getApiString() { return apiString; }

    @Override public ApiFeature getDefault() { return NONE; }

    @Override public ApiFeature getNotFound() { return NOT_FOUND; }

    @Override public ApiFeature[] getAll() { return values(); }

    public static UsageInfo fromText(final String text) {
        if (null == text) { return NOT_FOUND; }
        switch (text) {
            case "free_to_use":
            case "FREE_TO_USE":
            case "free":
            case "FREE":
                return FREE_TO_USE;
            case "license_needed":
            case "LICENSE_NEEDED":
            case "license":
            case "LICENSE":
                return LICENSE_NEEDED;
            case "azure_only":
            case "AZURE_ONLY":
            case "azure":
            case "AZURE":
                return AZURE_ONLY;
            default:
                return NOT_FOUND;
        }
    }

    public static List<UsageInfo> getAsList() { return Arrays.asList(values()); }

}
