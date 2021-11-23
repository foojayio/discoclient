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
import io.foojay.api.discoclient.DiscoClient;
import io.foojay.api.discoclient.pkg.Distribution;
import io.foojay.api.discoclient.pkg.HashAlgorithm;
import io.foojay.api.discoclient.pkg.TermOfSupport;

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
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;


public class Helper {
    private static BodyHandlerWrapper handlerWrapper = null;
    private static HttpClient         httpClient;


    public static boolean isPositiveInteger(final String text) {
        if (null == text) { return false; }
        return Constants.POSITIVE_INTEGER_PATTERN.matcher(text).matches();
    }

    public static String trimPrefix(final String text, final String prefix) {
        return text.replaceFirst(prefix, "");
    }

    public static boolean isSTS(final int featureVersion) {
        if (featureVersion < 9) { return false; }
        switch(featureVersion) {
            case 9 :
            case 10: return true;
            default: return !isLTS(featureVersion);
        }
    }

    public static boolean isMTS(final int featureVersion) {
        if (featureVersion < 13) { return false; }
        return (!isLTS(featureVersion)) && featureVersion % 2 != 0;
    }

    public static boolean isLTS(final int featureVersion) {
        if (featureVersion < 1) { throw new IllegalArgumentException("Feature version number cannot be smaller than 1"); }
        if (featureVersion <= 8) { return true; }
        if (featureVersion < 11) { return false; }
        return ((featureVersion - 11.0) / 6.0) % 1 == 0;
    }

    public static TermOfSupport getTermOfSupport(final int featureVersion) {
        if (featureVersion < 1) { throw new IllegalArgumentException("Feature version number cannot be smaller than 1"); }
        if (isLTS(featureVersion)) {
            return TermOfSupport.LTS;
        } else if (isMTS(featureVersion)) {
            return TermOfSupport.MTS;
        } else if (isSTS(featureVersion)) {
            return TermOfSupport.STS;
        } else {
            return TermOfSupport.NOT_FOUND;
        }
    }

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
