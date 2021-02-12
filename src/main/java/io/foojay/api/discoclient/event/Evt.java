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

import java.util.EventObject;
import java.util.Objects;


public class Evt extends EventObject implements Comparable<Evt> {
    public    static final EvtType<Evt>           ANY = EvtType.ROOT;
    protected        final EvtType<? extends Evt> evtType;
    private          final EvtPriority            priority;


    // ******************** Constructors **************************************
    public Evt(final EvtType<? extends Evt> evtType) {
        this(null, evtType, EvtPriority.NORMAL);
    }
    public Evt(final Object source, final EvtType<? extends Evt> evtType) {
        this(source, evtType, EvtPriority.NORMAL);
    }
    public Evt(final Object source, final EvtType<? extends Evt> evtType, final EvtPriority priority) {
        super(source);
        this.evtType  = evtType;
        this.priority = priority;
    }


    // ******************** Methods *******************************************
    public Object getSource() { return source; }

    public EvtType<? extends Evt> getEvtType() {  return evtType; }

    public EvtPriority getPriority() { return priority; }

    public int compareTo(final Evt evt) {
        return (evt.getPriority().getValue() - this.priority.getValue());
    }

    @Override public int hashCode() {
        return Objects.hash(source, evtType, priority);
    }

    @Override public boolean equals(final Object obj) {
        if (this == obj) { return true; }
        if (null == obj) { return false; }
        if (this.getClass() != obj.getClass()) { return false; }
        Evt evt = (Evt) obj;
        return (evt.getEvtType().equals(this.getEvtType()) &&
                evt.getPriority().getValue() == this.getPriority().getValue() &&
                evt.getSource().equals(this.getSource()));
    }

    @Override public String toString() {
        return new StringBuilder().append("{")
                                  .append("\"class\":\"").append(getClass().getName()).append("\",")
                                  .append("\"type\":\"").append(getEvtType().getClass().getName()).append("\",")
                                  .append("\"priority\":\"").append(getPriority().name()).append("\",")
                                  .append("\"source\":\"").append(null == getSource() ? "null" : getSource().getClass().getName()).append("\"")
                                  .append("}")
                                  .toString();
    }
}
