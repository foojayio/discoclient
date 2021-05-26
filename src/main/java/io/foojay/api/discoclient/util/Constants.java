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

package io.foojay.api.discoclient.util;

import io.foojay.api.discoclient.DiscoClient;
import io.foojay.api.discoclient.pkg.Distribution;
import io.foojay.api.discoclient.pkg.Scope;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;


public class Constants {
    public static final String  NAME                      = "discoclient";

    public static final String  DISCO_API_BASE_URL        = "https://api.foojay.io";

    public static final String  DISTRIBUTION_JSON         = "distributions.json";

    public static final String  PROPERTY_KEY_DISCO_URL    = "url";

    public static final Pattern POSITIVE_INTEGER_PATTERN  = Pattern.compile("\\d+");

    public static final String  PACKAGES_PATH             = "/disco/v2.0/packages";
    public static final String  EPHEMERAL_IDS_PATH        = "/disco/v2.0/ephemeral_ids";
    public static final String  MAJOR_VERSIONS_PATH       = "/disco/v2.0/major_versions";
    public static final String  DISTRIBUTIONS_PATH        = "/disco/v2.0/distributions";

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
    public static final String  API_MATCH                 = "match";
    public static final String  API_FEATURE               = "feature";

    public static final String  SQUARE_BRACKET_OPEN       = "[";
    public static final String  SQUARE_BRACKET_CLOSE      = "]";
    public static final String  CURLY_BRACKET_OPEN        = "{";
    public static final String  CURLY_BRACKET_CLOSE       = "}";
    public static final String  INDENTED_QUOTES           = "  \"";
    public static final String  QUOTES                    = "\"";
    public static final String  COLON                     = ":";
    public static final String  COMMA                     = ",";
    public static final String  NEW_LINE                  = "\n";
    public static final String  COMMA_NEW_LINE            = ",\n";
}
