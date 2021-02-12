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

import java.util.Objects;


public final class EvtType<T extends Evt> {
    public  static final EvtType<Evt>       ROOT = new EvtType<>("EVENT", null);
    private        final EvtType<? super T> superType;
    private        final String             name;


    // ******************** Constructors **************************************
    public EvtType(final EvtType<? super T> superType) {
        this(superType, null);
    }
    public EvtType(final String name) {
        this(ROOT, name);
    }
    public EvtType(final EvtType<? super T> superType, final String name) {
        if (null == superType) { throw new NullPointerException("Event super type must not be null (EvtType.name: " + name + ")"); }
        this.superType = superType;
        this.name      = name;
    }
    EvtType(final String name, final EvtType<? super T> superType) {
        this.superType = superType;
        this.name      = name;
    }


    // ******************** Methods *******************************************
    public final EvtType<? super T> getSuperType() { return superType; }

    public final String getName() { return name; }

    @Override public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        EvtType<?> evtType = (EvtType<?>) o;
        return superType.equals(evtType.superType) && name.equals(evtType.name);
    }

    @Override public int hashCode() {
        return Objects.hash(superType, name);
    }

    @Override public String toString() {
        return (null != name) ? new StringBuilder().append("{")
                                                   .append("\"class\":\"").append(getClass().getName()).append("\",")
                                                   .append("\"name\":\"").append(getName()).append("\",")
                                                   .append("\"supertype\":\"").append(getSuperType().name).append("\",")
                                                   .append("}")
                                                   .toString() :
               super.toString();
    }
}
