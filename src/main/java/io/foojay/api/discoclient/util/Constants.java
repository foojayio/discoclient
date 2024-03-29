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

import java.io.File;
import java.util.regex.Pattern;


public class Constants {
    public static final String  NAME                                = "discoclient";

    public static final String  HOME_FOLDER                         = new StringBuilder(System.getProperty("user.home")).append(File.separator).toString();
    public static final String  PROPERTIES_FILE_NAME                = Constants.NAME + ".properties";

    public static final String  PROPERTY_KEY_DISCO_URL              = "url";
    public static final String  PROPERTY_KEY_DISCO_VERSION          = "api_version";
    public static final String  PROPERTY_KEY_DISTRIBUTION_JSON_URL  = "distro_url";

    public static final String  DISCO_API_BASE_URL                  = "https://api.foojay.io";
    public static final String  API_VERSION_V3                      = "3.0";
    public static final String  DISTRIBUTION_JSON_URL               = "https://github.com/foojayio/distributions/raw/main/distributions.json";
    public static final String  RELEASE_DETAILS                     = "release_details";

    public static final String  DISTRIBUTION_JSON                   = "distributions.json";

    public static final Pattern POSITIVE_INTEGER_PATTERN            = Pattern.compile("\\d+");

    public static final String  API_DISTRIBUTION                    = "distro";
    public static final String  API_VERSION                         = "version";
    public static final String  API_VERSION_BY_DEFINITION           = "version_by_definition";
    public static final String  API_ARCHITECTURE                    = "architecture";
    public static final String  API_ARCHIVE_TYPE                    = "archive_type";
    public static final String  API_PACKAGE_TYPE                    = "package_type";
    public static final String  API_OPERATING_SYSTEM                = "operating_system";
    public static final String  API_LIBC_TYPE                       = "libc_type";
    public static final String  API_RELEASE_STATUS                  = "release_status";
    public static final String  API_SUPPORT_TERM                    = "term_of_support";
    public static final String  API_BITNESS                         = "bitness";
    public static final String  API_JAVAFX_BUNDLED                  = "javafx_bundled";
    public static final String  API_DIRECTLY_DOWNLOADABLE           = "directly_downloadable";
    public static final String  API_LATEST                          = "latest";
    public static final String  API_DISCOVERY_SCOPE_ID              = "discovery_scope_id";
    public static final String  API_MATCH                           = "match";
    public static final String  API_FEATURE                         = "feature";

    public static final String  FIELD_RELEASE_DETAILS_URL           = "release_details_url";

    public static final String  SLASH                               = "/";

    public static final long    SECONDS_PER_HOUR                    = 3_600;
}
