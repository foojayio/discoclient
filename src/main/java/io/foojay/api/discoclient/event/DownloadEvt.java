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
