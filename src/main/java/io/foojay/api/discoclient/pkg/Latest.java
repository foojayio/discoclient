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

public enum Latest {
    OVERALL("overall", "overall"),
    PER_DISTRIBUTION("per distribution", "per_distro"),
    PER_VERSION("per version", "per_version"),
    AVAILABLE("available", "available"),
    NONE("-", ""),
    NOT_FOUND("", "");;

    private final String uiString;
    private final String apiString;

    Latest(final String uiString, final String apiString) {
        this.uiString  = uiString;
        this.apiString = apiString;
    }


    public String getUiString() {
        return uiString;
    }

    public String getApiString() { return apiString; }

    public static Latest fromText(final String text) {
        switch (text) {
            case "per_distro":
            case "per-distro":
            case "per-distribution":
            case "per_distribution":
            case "perdistro":
            case "PER_DISTRO":
            case "PER-DISTRO":
            case "PER_DISTRIBUTION":
            case "PER-DISTRIBUTION":
            case "PERDISTRO":
                return PER_DISTRIBUTION;
            case "overall":
            case "OVERALL":
            case "in_general":
            case "in-general":
            case "IN_GENERAL":
            case "IN-GENERAL":
                return OVERALL;
            case "per_version":
            case "per-version":
            case "perversion":
            case "PER_VERSION":
            case "PER-VERSION":
            case "PERVERSION":
                return PER_VERSION;
            case "available":
            case "AVAILABLE":
            case "Available":
                return AVAILABLE;
            default:
                return NOT_FOUND;
        }
    }

    @Override public String toString() { return uiString; }
}
