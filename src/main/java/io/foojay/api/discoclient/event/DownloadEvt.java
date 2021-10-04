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

package io.foojay.api.discoclient.event;

public class DownloadEvt extends DCEvt {
    public  static final EvtType<DownloadEvt> ANY               = new EvtType<>(DCEvt.ANY, "DOWNLOAD");
    public  static final EvtType<DownloadEvt> DOWNLOAD_STARTED  = new EvtType<>(DownloadEvt.ANY, "DOWNLOAD_STARTED");
    public  static final EvtType<DownloadEvt> DOWNLOAD_PROGRESS = new EvtType<>(DownloadEvt.ANY, "DOWNLOAD_PROGRESS");
    public  static final EvtType<DownloadEvt> DOWNLOAD_FINISHED = new EvtType<>(DownloadEvt.ANY, "DOWNLOAD_FINISHED");
    public  static final EvtType<DownloadEvt> DOWNLOAD_FAILED   = new EvtType<>(DownloadEvt.ANY, "DOWNLOAD_FAILED");

    private final long fileSize;
    private final long fraction;


    public DownloadEvt(final Object source, final EvtType<? extends DownloadEvt> evtType, final long fileSize) {
        this(source, evtType, fileSize, 0, EvtPriority.NORMAL);
    }
    public DownloadEvt(final Object source, final EvtType<? extends DownloadEvt> evtType, final long fileSize, final EvtPriority priority) {
        this(source, evtType, fileSize, 0, priority);
    }
    public DownloadEvt(final Object source, final EvtType<? extends DownloadEvt> evtType, final long fileSize, final long fraction) {
        this(source, evtType, fileSize, fraction, EvtPriority.NORMAL);
    }
    public DownloadEvt(final Object source, final EvtType<? extends DownloadEvt> evtType, final long fileSize, final long fraction, final EvtPriority priority) {
        super(source, evtType, priority);
        this.fileSize = fileSize;
        this.fraction = fraction;
    }


    public EvtType<? extends DownloadEvt> getEvtType() {
        return (EvtType<? extends DownloadEvt>) super.getEvtType();
    }

    public final long getFileSize() { return fileSize; }

    public final long getFraction() { return fraction; }
}
