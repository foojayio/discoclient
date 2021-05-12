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
        if (null == text) { return NOT_FOUND; }
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

