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

package io.foojay.api.discoclient.pkg;

import java.util.Arrays;
import java.util.List;


public enum Architecture implements ApiFeature {
    AARCH64("AARCH64", "aarch64", Bitness.BIT_64),
    ARM("Arm", "arm", Bitness.BIT_32),
    ARM64("Arm64", "arm64", Bitness.BIT_64),
    MIPS("Mips", "mips", Bitness.BIT_32),
    PPC("Power PC", "ppc", Bitness.BIT_32),
    PPC64("PPC64", "ppc64", Bitness.BIT_64),
    PPC64LE("PPC64LE", "ppc64le", Bitness.BIT_64),
    RISCV64("RISCv64", "riscv64", Bitness.BIT_64),
    S390X("S390X", "s390x", Bitness.BIT_64),
    SPARC("Sparc", "sparc", Bitness.BIT_32),
    SPARCV9("Sparc V9", "sparcv9", Bitness.BIT_64),
    X64("X64", "x64", Bitness.BIT_64),
    X86("X86", "x86", Bitness.BIT_32),
    AMD64("AMD64", "amd64", Bitness.BIT_64),
    IA64("IA-64", "ia64", Bitness.BIT_64),
    NONE("-", "", Bitness.NONE),
    NOT_FOUND("", "", Bitness.NOT_FOUND);

    private final String  uiString;
    private final String   apiString;
    private final Bitness bitness;


    Architecture(final String uiString, final String apiString, final Bitness bitness) {
        this.uiString  = uiString;
        this.apiString = apiString;
        this.bitness   = bitness;
    }


    @Override public String getUiString() { return uiString; }

    @Override public String getApiString() { return apiString; }

    @Override public Architecture getDefault() { return Architecture.NONE; }

    @Override public Architecture getNotFound() { return Architecture.NOT_FOUND; }

    @Override public Architecture[] getAll() { return values(); }

    public static Architecture fromText(final String text) {
        switch (text) {
            case "aarch64":
            case "AARCH64":
                return AARCH64;
            case "amd64":
            case "AMD64":
            case "_amd64":
                return AMD64;
            case "aarch32":
            case "AARCH32":
            case "arm32":
            case "ARM32":
            case "arm":
            case "ARM":
                return ARM;
            case "arm64":
            case "ARM64":
                return ARM64;
            case "mips":
            case "MIPS":
                return MIPS;
            case "ppc":
            case "PPC":
                return PPC;
            case "ppc64el":
            case "PPC64EL":
            case "ppc64le":
            case "PPC64LE":
                return PPC64LE;
            case "ppc64":
            case "PPC64":
                return PPC64;
            case "riscv64":
            case "RISCV64":
                return RISCV64;
            case "s390" :
            case "s390x":
            case "S390X":
                return S390X;
            case "sparc":
            case "SPARC":
                return SPARC;
            case "sparcv9":
            case "SPARCV9":
                return SPARCV9;
            case "x64":
            case "X64":
            case "x86-64":
            case "X86-64":
            case "x86_64":
            case "X86_64":
                return X64;
            case "x86":
            case "X86":
            case "i386":
            case "i486":
            case "i586":
            case "i686":
            case "x86-32":
                return X86;
            case "ia64":
            case "IA64":
            case "ia-64":
            case "IA-64":
                return IA64;
            default:
                return NOT_FOUND;
        }
    }

    public Bitness getBitness() { return bitness; }

    public static List<Architecture> getAsList() { return Arrays.asList(values()); }
}
