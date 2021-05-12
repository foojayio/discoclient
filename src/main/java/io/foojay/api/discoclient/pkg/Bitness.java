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


public enum Bitness implements ApiFeature {
    BIT_32("32 Bit", "32", 32),
    BIT_64("64 Bit", "64", 64),
    NONE("-", "", 0),
    NOT_FOUND("", "", 0);

    private final String uiString;
    private final String apiString;
    private final int    bits;


    Bitness(final String uiString, final String apiString, final int bits) {
        this.uiString  = uiString;
        this.apiString = apiString;
        this.bits      = bits;
    }


    @Override public String getUiString() { return uiString; }

    @Override public String getApiString() { return apiString; }

    @Override public Bitness getDefault() { return Bitness.NONE; }

    @Override public Bitness getNotFound() { return Bitness.NOT_FOUND; }

    @Override public Bitness[] getAll() { return values(); }

    public int getAsInt() { return bits; }

    public String getAsString() { return Integer.toString(bits); }

    public static Bitness fromText(final String text) {
        if (null == text) { return NOT_FOUND; }
        switch (text) {
            case "32":
            case "32bit":
            case "32Bit":
            case "32BIT":
                return BIT_32;
            case "64":
            case "64bit":
            case "64Bit":
            case "64BIT":
                return BIT_64;
            default:
                return NOT_FOUND;
        }
    }

    public static Bitness fromInt(final Integer bits) {
        switch (bits) {
            case 32: return BIT_32;
            case 64: return BIT_64;
            default: return NOT_FOUND;
        }
    }

    public static List<Bitness> getAsList() { return Arrays.asList(values()); }
}
