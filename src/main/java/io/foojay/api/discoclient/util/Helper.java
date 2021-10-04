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
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequests;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.io.CloseMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Scanner;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.nio.charset.StandardCharsets.UTF_8;


public class Helper {
    private static final Logger  LOGGER                 = LoggerFactory.getLogger(Helper.class);
    public  static final Pattern NUMBER_IN_TEXT_PATTERN = Pattern.compile("(.*)?([0-9]+)(.*)?");


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

    public static Stream<MatchResult> findAll(Scanner s, Pattern pattern) {
        return StreamSupport.stream(new Spliterators.AbstractSpliterator<MatchResult>(
            1000, Spliterator.ORDERED | Spliterator.NONNULL) {
            public boolean tryAdvance(Consumer<? super MatchResult> action) {
                if(s.findWithinHorizon(pattern, 0)!=null) {
                    action.accept(s.match());
                    return true;
                }
                else return false;
            }
        }, false);
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
    
    public static final String get(final String uri) {
        return get(uri, "");
    }
    
    public static final String get(final String uri, String userAgent) {
        String result;
        final RequestConfig config = RequestConfig.custom()
                                                  .setConnectTimeout(20, TimeUnit.SECONDS)
                                                  .setConnectionRequestTimeout(60, TimeUnit.SECONDS)
                                                  .setResponseTimeout(60, TimeUnit.SECONDS)
                                                  .build();
        try (final CloseableHttpClient httpClient = HttpClientBuilder.create()
                                                                     .setUserAgent("DiscoClient (NetBeans)")
                                                                     .setDefaultRequestConfig(config)
                                                                     .build()) {
            final HttpGet httpGet = new HttpGet(URI.create(uri));
            httpGet.addHeader(HttpHeaders.USER_AGENT, "DiscoClient");

            try (final CloseableHttpResponse response = httpClient.execute(httpGet)) {
                if (response.getCode() == 200) {
                    final HttpEntity entity = response.getEntity();
                    result = null == entity ? "" : EntityUtils.toString(entity);
                    EntityUtils.consume(entity);
                } else {
                    result = "";
                    LOGGER.debug("Error executing get request {}, response code {}", uri, response.getCode());
                }
            }
        } catch (IOException | ParseException e) {
            LOGGER.debug("Error executing get request {}", uri);
            result = "";
        }
        return result;
    }
    
    public static final CompletableFuture<String> getAsync(final String uri) {
        return getAsync(uri, "");
    }
    
    public static final CompletableFuture<String> getAsync(final String uri, final String userAgent) {
        final RequestConfig config = RequestConfig.custom()
                                                  .setConnectTimeout(20, TimeUnit.SECONDS)
                                                  .setConnectionRequestTimeout(60, TimeUnit.SECONDS)
                                                  .setResponseTimeout(60, TimeUnit.SECONDS)
                                                  .build();
        final CloseableHttpAsyncClient client = HttpAsyncClientBuilder.create()
                                                                      .setUserAgent("DiscoClient (NetBeans)")
                                                                      .setDefaultRequestConfig(config)
                                                                      .build();
        client.start();

        final CompletableFuture<String>  toComplete = new CompletableFuture<>();
        final SimpleHttpRequest          request    = SimpleHttpRequests.get(URI.create(uri));
        final Future<SimpleHttpResponse> future     = client.execute(request, new FutureCallback<SimpleHttpResponse>() {
            @Override public void completed(final SimpleHttpResponse response) {
                toComplete.complete(response.getBodyText());
            }

            @Override public void failed(final Exception e) {
                LOGGER.debug("Error executing get request {}, {}", uri, e.getMessage());
                toComplete.completeExceptionally(e);
            }

            @Override public void cancelled() {
                LOGGER.debug("Request to {} was cancelled", uri);
                toComplete.cancel(true);
            }
        });

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.debug("Error executing get request {}, {}", uri, e.getMessage());
        }
        client.close(CloseMode.GRACEFUL);

        return toComplete;
    }
}
