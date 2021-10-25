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

    @Override public String toString() { return uiString; }
}
