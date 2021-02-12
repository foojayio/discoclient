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

import io.foojay.api.discoclient.pkg.SemVer;

import java.util.function.Predicate;


public class SemVerParsingResult {
    private SemVer            semVer1;
    private Error             error1;
    private SemVer            semVer2;
    private Error             error2;
    private Predicate<SemVer> filter;


    public SemVerParsingResult() {
        semVer1 = null;
        error1  = null;
        semVer2 = null;
        error2  = null;
        filter  = null;
    }


    public SemVer getSemVer1() { return semVer1; }
    public void setSemVer1(final SemVer semVer) { semVer1 = semVer; }

    public Error getError1() { return error1; }
    public void setError1(final Error error) { error1 = error; }

    public SemVer getSemVer2() { return semVer2; }
    public void setSemVer2(final SemVer semVer) { semVer2 = semVer; }

    public Error getError2() { return error2; }
    public void setError2(final Error error) { error2 = error; }

    public Predicate<SemVer> getFilter() { return filter; }
    public void setFilter(final Predicate<SemVer> filter) { this.filter = filter; }
}
