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


public enum FPU implements ApiFeature {
    HARD_FLOAT("hardfloat", "hard_float"),
    SOFT_FLOAT("softfloat", "soft_float"),
    UNKNOWN("unknown", "unknown"),
    NONE("-", ""),
    NOT_FOUND("", "");

    private final String uiString;
    private final String apiString;


    FPU(final String uiString, final String apiString) {
        this.uiString  = uiString;
        this.apiString = apiString;
    }


    @Override public String getUiString() { return uiString; }

    @Override public String getApiString() { return apiString; }

    @Override public FPU getDefault() { return FPU.UNKNOWN; }

    @Override public FPU getNotFound() { return FPU.NOT_FOUND; }

    @Override public FPU[] getAll() { return values(); }

    public static FPU fromText(final String text) {
        if (null == text) { return NOT_FOUND; }
        switch (text) {
            case "hard_float":
            case "HARD_FLOAT":
            case "hard-float":
            case "HARD-FLOAT":
            case "hardfloat":
            case "HARDFLOAT":
            case "hflt":
            case "HFLT":
                return HARD_FLOAT;
            case "soft_float":
            case "SOFT_FLOAT":
            case "soft-float":
            case "SOFT-FLOAT":
            case "softfloat":
            case "SOFTFLOAT":
            case "sflt":
            case "SFLT":
                return SOFT_FLOAT;
            case "unknown":
            case "UNKNOWN":
                return UNKNOWN;
            default:
                return NOT_FOUND;
        }
    }

    public static List<FPU> getAsList() { return Arrays.asList(values()); }
}
