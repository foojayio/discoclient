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

public class CacheEvt extends DCEvt {
    public  static final EvtType<CacheEvt> ANY            = new EvtType<>(DCEvt.ANY, "CACHE");
    public  static final EvtType<CacheEvt> CACHE_READY    = new EvtType<>(CacheEvt.ANY, "CACHE_READY");
    public  static final EvtType<CacheEvt> CACHE_UPDATING = new EvtType<>(CacheEvt.ANY, "CACHE_UPDATING");


    public CacheEvt(final Object source, final EvtType<? extends CacheEvt> evtType) {
        this(source, evtType, EvtPriority.NORMAL);
    }
    public CacheEvt(final Object source, final EvtType<? extends CacheEvt> evtType, final EvtPriority priority) {
        super(source, evtType, priority);
    }


    public EvtType<? extends CacheEvt> getEvtType() {
        return (EvtType<? extends CacheEvt>) super.getEvtType();
    }
}
