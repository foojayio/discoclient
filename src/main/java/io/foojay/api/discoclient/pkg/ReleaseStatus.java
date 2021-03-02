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


public enum ReleaseStatus implements ApiFeature {
    GA("General Access", "ga",""),
    EA("Early Access", "ea", "-ea"),
    NONE("-", "", ""),
    NOT_FOUND("", "", "");

    private final String uiString;
    private final String apiString;
    private final String preReleaseId;


    ReleaseStatus(final String uiString, final String apiString, final String preReleaseId) {
        this.uiString     = uiString;
        this.apiString    = apiString;
        this.preReleaseId = preReleaseId;
    }


    @Override public String getUiString() { return uiString; }

    @Override public String getApiString() { return apiString; }

    @Override public ReleaseStatus getDefault() { return ReleaseStatus.NONE; }

    @Override public ReleaseStatus getNotFound() { return ReleaseStatus.NOT_FOUND; }

    @Override public ReleaseStatus[] getAll() { return values(); }

    public static ReleaseStatus fromText(final String text) {
        if (null == text) { return NOT_FOUND; }
        switch (text) {
            case "-ea":
            case "-EA":
            case "_ea":
            case "_EA":
            case "ea":
            case "EA":
            case "ea_":
            case "EA_":
                return EA;
            case "-ga":
            case "-GA":
            case "_ga":
            case "_GA":
            case "ga":
            case "GA":
            case "ga_":
            case "GA_":
                return GA;
            default:
                return NOT_FOUND;
        }
    }

    public String getPreReleaseId() { return preReleaseId; }

    public static List<ReleaseStatus> getAsList() { return Arrays.asList(values()); }
}
