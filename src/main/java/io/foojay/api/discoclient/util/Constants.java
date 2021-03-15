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

package io.foojay.api.discoclient.util;

import io.foojay.api.discoclient.pkg.Distribution;
import io.foojay.api.discoclient.pkg.Scope;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;


public class Constants {
    public static final String  NAME                      = "discoclient";

    public static final String  DISCO_API_BASE_URL        = "https://api.foojay.io";

    public static final String  PROPERTY_KEY_DISCO_URL    = "url";

    public static final Pattern POSITIVE_INTEGER_PATTERN  = Pattern.compile("\\d+");

    public static final String  PACKAGES_PATH             = "/disco/v1.0/packages";
    public static final String  EPHEMERAL_IDS_PATH        = "/disco/v1.0/ephemeral_ids";
    public static final String  MAJOR_VERSIONS_PATH       = "/disco/v1.0/major_versions";
    public static final String  DISTRIBUTIONS_PATH        = "/disco/v1.0/distributions";

    public static final String  API_DISTRIBUTION          = "distro";
    public static final String  API_VERSION               = "version";
    public static final String  API_VERSION_BY_DEFINITION = "version_by_definition";
    public static final String  API_ARCHITECTURE          = "architecture";
    public static final String  API_ARCHIVE_TYPE          = "archive_type";
    public static final String  API_PACKAGE_TYPE          = "package_type";
    public static final String  API_OPERATING_SYSTEM      = "operating_system";
    public static final String  API_LIBC_TYPE             = "libc_type";
    public static final String  API_RELEASE_STATUS        = "release_status";
    public static final String  API_SUPPORT_TERM          = "term_of_support";
    public static final String  API_BITNESS               = "bitness";
    public static final String  API_JAVAFX_BUNDLED        = "javafx_bundled";
    public static final String  API_DIRECTLY_DOWNLOADABLE = "directly_downloadable";
    public static final String  API_LATEST                = "latest";
    public static final String  API_DISCOVERY_SCOPE_ID    = "discovery_scope_id";

    public static final ConcurrentHashMap<Distribution, List<Scope>> SCOPE_LOOKUP = new ConcurrentHashMap<>();
    static {
        SCOPE_LOOKUP.put(Distribution.AOJ, Arrays.asList(Scope.PUBLIC, Scope.BUILD_OF_OPEN_JDK, Scope.DIRECTLY_DOWNLOADABLE));
        SCOPE_LOOKUP.put(Distribution.AOJ_OPENJ9, Arrays.asList(Scope.PUBLIC, Scope.DIRECTLY_DOWNLOADABLE));
        SCOPE_LOOKUP.put(Distribution.CORRETTO, Arrays.asList(Scope.PUBLIC, Scope.BUILD_OF_OPEN_JDK, Scope.DIRECTLY_DOWNLOADABLE));
        SCOPE_LOOKUP.put(Distribution.DRAGONWELL, Arrays.asList(Scope.PUBLIC, Scope.BUILD_OF_OPEN_JDK, Scope.DIRECTLY_DOWNLOADABLE));
        SCOPE_LOOKUP.put(Distribution.GRAALVM_CE8, Arrays.asList(Scope.PUBLIC, Scope.DIRECTLY_DOWNLOADABLE));
        SCOPE_LOOKUP.put(Distribution.GRAALVM_CE11, Arrays.asList(Scope.PUBLIC, Scope.DIRECTLY_DOWNLOADABLE));
        SCOPE_LOOKUP.put(Distribution.LIBERICA, Arrays.asList(Scope.PUBLIC, Scope.BUILD_OF_OPEN_JDK, Scope.DIRECTLY_DOWNLOADABLE));
        SCOPE_LOOKUP.put(Distribution.LIBERICA_NATIVE, Arrays.asList(Scope.PUBLIC, Scope.DIRECTLY_DOWNLOADABLE));
        SCOPE_LOOKUP.put(Distribution.MANDREL, Arrays.asList(Scope.PUBLIC, Scope.DIRECTLY_DOWNLOADABLE));
        SCOPE_LOOKUP.put(Distribution.OJDK_BUILD, Arrays.asList(Scope.PUBLIC, Scope.BUILD_OF_OPEN_JDK, Scope.DIRECTLY_DOWNLOADABLE));
        SCOPE_LOOKUP.put(Distribution.ORACLE, Arrays.asList(Scope.PUBLIC, Scope.BUILD_OF_OPEN_JDK, Scope.NOT_DIRECTLY_DOWNLOADABLE));
        SCOPE_LOOKUP.put(Distribution.ORACLE_OPEN_JDK, Arrays.asList(Scope.PUBLIC, Scope.BUILD_OF_OPEN_JDK, Scope.DIRECTLY_DOWNLOADABLE));
        SCOPE_LOOKUP.put(Distribution.RED_HAT, Arrays.asList(Scope.PUBLIC, Scope.BUILD_OF_OPEN_JDK, Scope.NOT_DIRECTLY_DOWNLOADABLE));
        SCOPE_LOOKUP.put(Distribution.SAP_MACHINE, Arrays.asList(Scope.PUBLIC, Scope.BUILD_OF_OPEN_JDK, Scope.DIRECTLY_DOWNLOADABLE));
        SCOPE_LOOKUP.put(Distribution.TRAVA, Arrays.asList(Scope.PUBLIC, Scope.BUILD_OF_OPEN_JDK, Scope.DIRECTLY_DOWNLOADABLE));
        SCOPE_LOOKUP.put(Distribution.ZULU, Arrays.asList(Scope.PUBLIC, Scope.BUILD_OF_OPEN_JDK, Scope.DIRECTLY_DOWNLOADABLE));
    }
}
