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
