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

import java.net.http.HttpResponse.BodySubscriber;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow.Subscription;


public class SubscriberWrapper implements BodySubscriber<String> {
    private static final Logger                 LOGGER = LoggerFactory.getLogger(SubscriberWrapper.class);

    private        final CountDownLatch         latch;
    private        final BodySubscriber<String> subscriber;
    private              Subscription           subscription;


    public SubscriberWrapper(final BodySubscriber<String> subscriber, final CountDownLatch latch) {
        this.subscriber = subscriber;
        this.latch      = latch;
    }


    @Override public CompletionStage<String> getBody() {
        return subscriber.getBody();
    }

    @Override public void onSubscribe(final Subscription subscription) {
        subscriber.onSubscribe(subscription);
        this.subscription = subscription;
        latch.countDown();
    }

    @Override public void onNext(final List<ByteBuffer> item) {
        subscriber.onNext(item);
    }

    @Override public void onError(final Throwable throwable) {
        subscriber.onError(throwable);
    }

    @Override public void onComplete() {
        subscriber.onComplete();
    }

    public void cancel() {
        subscription.cancel();
        LOGGER.debug("Subscription canceled");
    }
}
