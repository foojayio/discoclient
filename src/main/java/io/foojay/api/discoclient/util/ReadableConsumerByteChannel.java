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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.function.IntConsumer;


public class ReadableConsumerByteChannel implements ReadableByteChannel {
    private final ReadableByteChannel rbc;
    private final IntConsumer         onRead;
    private       int                 totalByteRead;


    public ReadableConsumerByteChannel(ReadableByteChannel rbc, IntConsumer onBytesRead) {
        this.rbc    = rbc;
        this.onRead = onBytesRead;
    }


    @Override public int read(ByteBuffer dst) throws IOException {
        int nRead = rbc.read(dst);
        notifyBytesRead(nRead);
        return nRead;
    }

    protected void notifyBytesRead(int nRead){
        if(nRead <= 0) { return; }
        totalByteRead += nRead;
        onRead.accept(totalByteRead);
    }
    @Override public boolean isOpen() {
        return rbc.isOpen();
    }

    @Override public void close() throws IOException {
        rbc.close();
    }
}
