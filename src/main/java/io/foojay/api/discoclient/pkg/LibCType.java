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


public enum LibCType implements ApiFeature {
    GLIBC("glibc", "glibc"),
    MUSL("musl", "musl"),
    LIBC("libc", "libc"),
    C_STD_LIB("c std. lib", "c_std_lib"),
    NONE("-", ""),
    NOT_FOUND("", "");

    private final String uiString;
    private final String apiString;


    LibCType(final String uiString, final String apiString) {
        this.uiString  = uiString;
        this.apiString = apiString;
    }


    @Override public String getUiString() { return uiString; }

    @Override public String getApiString() { return apiString; }

    @Override public LibCType getDefault() { return LibCType.NONE; }

    @Override public LibCType getNotFound() { return LibCType.NOT_FOUND; }

    @Override public LibCType[] getAll() { return values(); }

    public static LibCType fromText(final String text) {
        if (null == text) { return NOT_FOUND; }
        switch (text) {
            case "musl":
            case "MUSL":
            case "linux_musl":
            case "linux-musl":
            case "alpine_linux":
            case "alpine":
            case "alpine-linux":
                return MUSL;
            case "glibc":
            case "GLIBC":
            case "linux":
            case "Linux":
            case "LINUX":
                return GLIBC;
            case "c_std_lib":
            case "C_STD_LIB":
            case "c-std-lib":
            case "C-STD-LIB":
            case "windows":
            case "Windows":
            case "win":
            case "Win":
                return C_STD_LIB;
            case "libc":
            case "LIBC":
            case "macos":
            case "MACOS":
            case "macosx":
            case "MACOSX":
            case "aix":
            case "AIX":
            case "qnx":
            case "QNX":
            case "solaris":
            case "SOLARIS":
            case "darwin":
            case "DARWIN":
                return LIBC;
            default:
                return NOT_FOUND;
        }
    }

    public static List<LibCType> getAsList() { return Arrays.asList(values()); }
}
