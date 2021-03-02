/*
 * Copyright (c) 2021, Azul
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer
 *   in the documentation and/or other materials provided with the distribution.
 * - Neither the name of Azul nor the names of its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL AZUL BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.foojay.api.discoclient.pkg;

import java.util.Arrays;
import java.util.List;


public enum TermOfSupport implements ApiFeature {
    STS("short term stable", "sts"),
    MTS("mid term stable", "mts"),
    LTS("long term stable", "lts"),
    NONE("-", ""),
    NOT_FOUND("", "");

    private final String uiString;
    private final String apiString;


    TermOfSupport(final String uiString, final String apiString) {
        this.uiString  = uiString;
        this.apiString = apiString;
    }


    @Override public String getUiString() { return uiString; }

    @Override public String getApiString() { return apiString; }

    @Override public TermOfSupport getDefault() { return TermOfSupport.NONE; }

    @Override public TermOfSupport getNotFound() { return TermOfSupport.NOT_FOUND; }

    @Override public TermOfSupport[] getAll() { return values(); }

    public static TermOfSupport fromText(final String text) {
        if (null == text) { return NOT_FOUND; }
        switch(text) {
            case "long_term_stable":
            case "LongTermStable":
            case "lts":
            case "LTS":
            case "Lts":
                return LTS;
            case "mid_term_stable":
            case "MidTermStable":
            case "mts":
            case "MTS":
            case "Mts":
                return MTS;
            case "short_term_stable":
            case "ShortTermStable":
            case "sts":
            case "STS":
            case "Sts":
                return STS;
            default: return NOT_FOUND;

        }
    }

    public static List<TermOfSupport> getAsList() { return Arrays.asList(values()); }
}
