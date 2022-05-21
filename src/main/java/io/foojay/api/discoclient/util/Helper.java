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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import eu.hansolo.jdktools.HashAlgorithm;
import eu.hansolo.jdktools.TermOfSupport;
import io.foojay.api.discoclient.DiscoClient;
import io.foojay.api.discoclient.pkg.Distribution;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;


public class Helper {
    private static BodyHandlerWrapper handlerWrapper = null;
    private static HttpClient         httpClient;


    public static String getHash(final HashAlgorithm hashAlgorithm, final String text) {
        switch (hashAlgorithm) {
            case MD5     : return getMD5(text);
            case SHA1    : return getSHA1(text);
            case SHA256  : return getSHA256(text);
            case SHA3_256: return getSHA3_256(text);
            default      : return "";
        }
    }

    public static String getMD5(final String text) { return bytesToHex(getMD5Bytes(text.getBytes(UTF_8))); }
    public static String getMD5(final byte[] bytes) {
        return bytesToHex(getMD5Bytes(bytes));
    }
    public static byte[] getMD5Bytes(final byte[] bytes) {
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return new byte[]{};
        }
        final byte[] result = md.digest(bytes);
        return result;
    }

    public static String getSHA1(final String text) { return bytesToHex(getSHA1Bytes(text.getBytes(UTF_8))); }
    public static String getSHA1(final byte[] bytes) {
        return bytesToHex(getSHA1Bytes(bytes));
    }
    public static byte[] getSHA1Bytes(final byte[] bytes) {
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            return new byte[]{};
        }
        final byte[] result = md.digest(bytes);
        return result;
    }

    public static String getSHA256(final String text) { return bytesToHex(getSHA256Bytes(text.getBytes(UTF_8))); }
    public static String getSHA256(final byte[] bytes) {
        return bytesToHex(getSHA256Bytes(bytes));
    }
    public static byte[] getSHA256Bytes(final byte[] bytes) {
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            return new byte[]{};
        }
        final byte[] result = md.digest(bytes);
        return result;
    }

    public static String getSHA3_256(final String text) { return bytesToHex(getSHA3_256Bytes(text.getBytes(UTF_8))); }
    public static String getSHA3_256(final byte[] bytes) {
        return bytesToHex(getSHA3_256Bytes(bytes));
    }
    public static byte[] getSHA3_256Bytes(final byte[] bytes) {
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA3-256");
        } catch (NoSuchAlgorithmException e) {
            return new byte[]{};
        }
        final byte[] result = md.digest(bytes);
        return result;
    }

    public static String bytesToHex(final byte[] bytes) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : bytes) { builder.append(String.format("%02x", b)); }
        return builder.toString();
    }

    public static String readFromInputStream(final InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

    public static CompletableFuture<Map<String,Distribution>> preloadDistributions() {
        return CompletableFuture.supplyAsync(() -> {
            final Map<String,Distribution> distributions = new ConcurrentHashMap();
            try {
                final InputStream inputStream = DiscoClient.class.getResourceAsStream(Constants.DISTRIBUTION_JSON);
                if (null == inputStream) {
                    return distributions;
                }
                String jsonText = Helper.readFromInputStream(inputStream);

                distributions.putAll(getDistributionsFromJsonText(jsonText));
                return distributions;
            } catch (IOException e) {
                return distributions;
            }
        });
    }

    public static Map<String,Distribution> getDistributionsFromJsonText(final String jsonText) {
        final Map<String,Distribution> distributions = new ConcurrentHashMap();
        final Gson       gson       = new Gson();
        final JsonObject jsonObject = gson.fromJson(jsonText, JsonObject.class);
        final JsonArray  jsonArray  = jsonObject.get("distributions").getAsJsonArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            final JsonObject   pkgJsonObj   = jsonArray.get(i).getAsJsonObject();
            final Distribution distribution = new Distribution(pkgJsonObj.toString());
            distributions.put(distribution.getApiString(), distribution);
        }
        return distributions;
    }

    public static Distribution getDistributionFromText(final String text) {
        if (null == text) { return null; }
        switch (text) {
            case "zulu":
            case "ZULU":
            case "Zulu":
            case "zulucore":
            case "ZULUCORE":
            case "ZuluCore":
            case "zulu_core":
            case "ZULU_CORE":
            case "Zulu_Core":
            case "zulu core":
            case "ZULU CORE":
            case "Zulu Core":
                return new Distribution("ZULU", "Zulu", "zulu");
            case "zing":
            case "ZING":
            case "Zing":
            case "prime":
            case "PRIME":
            case "Prime":
            case "zuluprime":
            case "ZULUPRIME":
            case "ZuluPrime":
            case "zulu_prime":
            case "ZULU_PRIME":
            case "Zulu_Prime":
            case "zulu prime":
            case "ZULU PRIME":
            case "Zulu Prime":
                return new Distribution("ZULU_PRIME", "Zulu Prime", "zulu_prime");
            case "aoj":
            case "AOJ":
                return new Distribution("AOJ", "AOJ", "aoj");
            case "aoj_openj9":
            case "AOJ_OpenJ9":
            case "AOJ_OPENJ9":
            case "AOJ OpenJ9":
            case "AOJ OPENJ9":
            case "aoj openj9":
                return new Distribution("AOJ_OPENJ9", "AOJ OpenJ9", "aoj_openj9");
            case "corretto":
            case "CORRETTO":
            case "Corretto":
                return new Distribution("CORRETTO", "Corretto", "corretto");
            case "dragonwell":
            case "DRAGONWELL":
            case "Dragonwell":
                return new Distribution("DRAGONWELL", "Dragonwell", "dragonwell");
            case "graalvm_ce8":
            case "graalvmce8":
            case "GraalVM CE 8":
            case "GraalVMCE8":
            case "GraalVM_CE8":
                return new Distribution("GRAALVM_CE8", "GraalVM CE8", "graalvm_ce8");
            case "graalvm_ce11":
            case "graalvmce11":
            case "GraalVM CE 11":
            case "GraalVMCE11":
            case "GraalVM_CE11":
                return new Distribution("GRAALVM_CE11", "GraalVM CE11", "graalvm_ce11");
            case "graalvm_ce16":
            case "graalvmce16":
            case "GraalVM CE 16":
            case "GraalVMCE16":
            case "GraalVM_CE16":
                return new Distribution("GRAALVM_CE16", "GraalVM CE16", "graalvm_ce16");
            case "graalvm_ce17":
            case "graalvmce17":
            case "GraalVM CE 17":
            case "GraalVMCE17":
            case "GraalVM_CE17":
                return new Distribution("GRAALVM_CE17", "GraalVM CE17", "graalvm_ce17");
            case "jetbrains":
            case "JetBrains":
            case "JETBRAINS":
                return new Distribution("JETBRAINS", "Jetbrains", "jetbrains");
            case "liberica":
            case "LIBERICA":
            case "Liberica":
                return new Distribution("LIBERICA", "Liberica", "liberica");
            case "liberica_native":
            case "LIBERICA_NATIVE":
            case "libericaNative":
            case "LibericaNative":
            case "liberica native":
            case "LIBERICA NATIVE":
            case "Liberica Native":
                return new Distribution("LIBERICA_NATIVE", "Liberica Native", "liberica_native");
            case "mandrel":
            case "MANDREL":
            case "Mandrel":
                return new Distribution("MANDREL", "Mandrel", "mandrel");
            case "microsoft":
            case "Microsoft":
            case "MICROSOFT":
            case "Microsoft OpenJDK":
            case "Microsoft Build of OpenJDK":
                return new Distribution("MICROSOFT", "Microsoft", "microsoft");
            case "ojdk_build":
            case "OJDK_BUILD":
            case "OJDK Build":
            case "ojdk build":
            case "ojdkbuild":
            case "OJDKBuild":
                return new Distribution("OJDK_BUILD", "OJDK Build", "ojdk_build");
            case "openlogic":
            case "OPENLOGIC":
            case "OpenLogic":
            case "open_logic":
            case "OPEN_LOGIC":
            case "Open Logic":
            case "OPEN LOGIC":
            case "open logic":
                return new Distribution("OPEN_LOGIC", "Open Logic", "openlogic");
            case "oracle":
            case "Oracle":
            case "ORACLE":
                return new Distribution("ORACLE", "Oracle", "oracle");
            case "oracle_open_jdk":
            case "ORACLE_OPEN_JDK":
            case "oracle_openjdk":
            case "ORACLE_OPENJDK":
            case "Oracle_OpenJDK":
            case "Oracle OpenJDK":
            case "oracle openjdk":
            case "ORACLE OPENJDK":
            case "open_jdk":
            case "openjdk":
            case "OpenJDK":
            case "Open JDK":
            case "OPEN_JDK":
            case "open-jdk":
            case "OPEN-JDK":
            case "Oracle-OpenJDK":
            case "oracle-openjdk":
            case "ORACLE-OPENJDK":
            case "oracle-open-jdk":
            case "ORACLE-OPEN-JDK":
                return new Distribution("ORACLE_OPEN_JDK", "Oracle OpenJDK", "oracle_open_jdk");
            case "RedHat":
            case "redhat":
            case "REDHAT":
            case "Red Hat":
            case "red hat":
            case "RED HAT":
            case "Red_Hat":
            case "red_hat":
            case "red-hat":
            case "Red-Hat":
            case "RED-HAT":
                return new Distribution("RED_HAT", "Red Hat", "redhat");
            case "sap_machine":
            case "sapmachine":
            case "SAPMACHINE":
            case "SAP_MACHINE":
            case "SAPMachine":
            case "SAP Machine":
            case "sap-machine":
            case "SAP-Machine":
            case "SAP-MACHINE":
                return new Distribution("SAP_MACHINE", "SAP Machine", "sap_machine");
            case "semeru":
            case "Semeru":
            case "SEMERU":
                return new Distribution("SEMERU", "Semeru", "semeru");
            case "semeru_certified":
            case "SEMERU_CERTIFIED":
            case "Semeru_Certified":
            case "Semeru_certified":
            case "semeru certified":
            case "SEMERU CERTIFIED":
            case "Semeru Certified":
            case "Semeru certified":
                return new Distribution("SEMERU_CERTIFIED", "Semeru certified", "semeru_certified");
            case "temurin":
            case "Temurin":
            case "TEMURIN":
                return new Distribution("TEMURIN", "Temurin", "temurin");
            case "trava":
            case "TRAVA":
            case "Trava":
            case "trava_openjdk":
            case "TRAVA_OPENJDK":
            case "trava openjdk":
            case "TRAVA OPENJDK":
                return new Distribution("TRAVA", "Trava", "trava");
            case "kona":
            case "KONA":
            case "Kona":
                return new Distribution("KONA", "Kona", "kona");
            case "bisheng":
            case "BISHENG":
            case "BiSheng":
            case "bi_sheng":
            case "BI_SHENG":
            case "bi-sheng":
            case "BI-SHENG":
            case "bi sheng":
            case "Bi Sheng":
            case "BI SHENG":
                return new Distribution("BISHENG", "Bi Sheng", "bisheng");
            case "debian":
            case "DEBIAN":
            case "Debian":
                return new Distribution("DEBIAN", "Debian", "debian");
            default:
                return null;
        }
    }

    public static String encodeValue(final String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }


    // ******************** REST calls ****************************************
    public static HttpClient createHttpClient() {
        return HttpClient.newBuilder()
                         .connectTimeout(Duration.ofSeconds(20))
                         .followRedirects(Redirect.NORMAL)
                         .version(java.net.http.HttpClient.Version.HTTP_2)
                         .build();
    }

    public static final HttpResponse<String> get(final String uri) { return get(uri, ""); }
    public static final HttpResponse<String> get(final String uri, final String userAgent) {
        if (null == httpClient) { httpClient = createHttpClient(); }
        final String userAgentText = (null == userAgent || userAgent.isEmpty()) ? "DiscoClient V2" : "DiscoClient V2 (" + userAgent + ")";
        HttpRequest request = HttpRequest.newBuilder()
                                         .GET()
                                         .uri(URI.create(uri))
                                         .setHeader("Accept", "application/json")
                                         .setHeader("User-Agent", userAgentText)
                                         .timeout(Duration.ofSeconds(60))
                                         .build();
        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response;
            } else {
                // Problem with url request
                return response;
            }
        } catch (CompletionException | InterruptedException | IOException e) {
            return null;
        }
    }

    public static final CompletableFuture<HttpResponse<String>> getAsync(final String uri) { return getAsync(uri, ""); }
    public static final CompletableFuture<HttpResponse<String>> getAsync(final String uri, final String userAgent) {
        if (null == httpClient) { httpClient = createHttpClient(); }

        final String userAgentText = (null == userAgent || userAgent.isEmpty()) ? "DiscoClient" : "DiscoClient (" + userAgent + ")";
        final HttpRequest request = HttpRequest.newBuilder()
                                               .GET()
                                               .uri(URI.create(uri))
                                               .setHeader("Accept", "application/json")
                                               .setHeader("User-Agent", userAgentText)
                                               .timeout(Duration.ofSeconds(60))
                                               .build();
        return httpClient.sendAsync(request, BodyHandlers.ofString());
    }

    public static final void cancelRequest() {
        if (null != handlerWrapper) { handlerWrapper.cancel(); }
    }
}
