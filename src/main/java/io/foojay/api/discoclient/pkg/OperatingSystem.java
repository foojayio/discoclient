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


public enum OperatingSystem implements ApiFeature {
    ALPINE_LINUX("Alpine Linux", "linux", LibCType.MUSL),
    LINUX("Linux", "linux", LibCType.GLIBC),
    LINUX_MUSL("Linux Musl", "linux", LibCType.MUSL),
    MACOS("Mac OS", "macos", LibCType.LIBC),
    WINDOWS("Windows", "windows", LibCType.C_STD_LIB),
    SOLARIS("Solaris", "solaris", LibCType.LIBC),
    QNX("QNX", "qnx", LibCType.LIBC),
    AIX("AIX", "aix", LibCType.LIBC),
    NONE("-", "", LibCType.NONE),
    NOT_FOUND("", "", LibCType.NOT_FOUND);

    private final String uiString;
    private final String apiString;
    private final LibCType libCType;


    OperatingSystem(final String uiString, final String apiString, final LibCType libCType) {
        this.uiString  = uiString;
        this.apiString = apiString;
        this.libCType  = libCType;
    }


    @Override public String getUiString() { return uiString; }

    @Override public String getApiString() { return apiString; }

    @Override public OperatingSystem getDefault() { return OperatingSystem.NONE; }

    @Override public OperatingSystem getNotFound() { return OperatingSystem.NOT_FOUND; }

    @Override public OperatingSystem[] getAll() { return values(); }

    public static OperatingSystem fromText(final String text) {
        switch (text) {
            case "-linux":
            case "linux":
            case "Linux":
            case "LINUX":
                return LINUX;
            case "-linux-musl":
            case "-linux_musl":
            case "Linux-Musl":
            case "linux-musl":
            case "Linux_Musl":
            case "LINUX_MUSL":
            case "linux_musl":
            case "alpine-linux":
            case "ALPINE-LINUX":
            case "alpine_linux":
            case "Alpine Linux":
            case "alpine linux":
            case "ALPINE LINUX":
                return ALPINE_LINUX;
            case "-solaris":
            case "solaris":
            case "SOLARIS":
            case "Solaris":
                return SOLARIS;
            case "-qnx":
            case "qnx":
            case "QNX":
                return QNX;
            case"-aix":
            case "aix":
            case "AIX":
                return AIX;
            case "darwin":
            case "-darwin":
            case "-macosx":
            case "-MACOSX":
            case "MacOS":
            case "Mac OS":
            case "mac_os":
            case "Mac_OS":
            case "mac-os":
            case "Mac-OS":
            case "mac":
            case "MAC":
            case "macos":
            case "MACOS":
            case "osx":
            case "OSX":
            case "macosx":
            case "MACOSX":
            case "Mac OSX":
            case "mac osx":
                return MACOS;
            case "-win":
            case "windows":
            case "Windows":
            case "WINDOWS":
            case "win":
            case "Win":
            case "WIN":
                return WINDOWS;
            default:
                return NOT_FOUND;
        }
    }

    public LibCType getLibCType() { return libCType; }

    public static List<OperatingSystem> getAsList() { return Arrays.asList(values()); }
}

