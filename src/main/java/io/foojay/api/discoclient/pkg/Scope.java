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
            case "build_of_openjdk":
            case "BUILD_OF_OPENJDK":
            case "build_of_open_jdk":
            case "BUILD_OF_OPEN_JDK":
                return BUILD_OF_OPEN_JDK;
            default:
                return NOT_FOUND;
        }
    }

    public static List<Scope> getAsList() { return Arrays.asList(values()); }
}
