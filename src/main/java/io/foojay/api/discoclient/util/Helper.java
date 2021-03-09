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

import io.foojay.api.discoclient.pkg.TermOfSupport;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequests;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.io.CloseMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Scanner;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


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


    // ******************** REST calls ****************************************
    public static final String get(final String uri) {
        String result;
        try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
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
        final CloseableHttpAsyncClient client = HttpAsyncClients.createHttp2Default();
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
