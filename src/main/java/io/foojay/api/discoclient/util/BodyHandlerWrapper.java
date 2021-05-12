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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.ResponseInfo;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;


public class BodyHandlerWrapper implements BodyHandler<String> {
    private static final Logger            LOGGER = LoggerFactory.getLogger(BodyHandlerWrapper.class);

    private        final CountDownLatch      latch  = new CountDownLatch(1);
    private        final BodyHandler<String> handler;
    private              SubscriberWrapper   subscriberWrapper;


    public BodyHandlerWrapper(final BodyHandler<String> handler) {
        this.handler = handler;
    }


    @Override public BodySubscriber<String> apply(final ResponseInfo responseInfo) {
        subscriberWrapper = new SubscriberWrapper(handler.apply(responseInfo), latch);
        return subscriberWrapper;
    }

    public void cancel() {
        CompletableFuture.runAsync(() -> {
            try {
                latch.await();
                subscriberWrapper.cancel();
            } catch (InterruptedException e) {
                LOGGER.debug("Error canceling subscription {}", e.getMessage());
            }
        });
    }
}
