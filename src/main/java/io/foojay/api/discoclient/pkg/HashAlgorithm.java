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

public enum HashAlgorithm implements ApiFeature {
    MD5("MSD5", "md5", 128),
    SHA1("SHA-1", "sha1", 160),
    SHA2_256("SHA-2 256", "sha2_256", 256),
    SHA3_256("SHA-3 256", "sha3_256", 256),
    NONE("-", "", 0),
    NOT_FOUND("", "", 0);

    private final String uiString;
    private final String apiString;
    private final int    bit;


    HashAlgorithm(final String uiString, final String apiString, final int bit) {
        this.uiString  = uiString;
        this.apiString = apiString;
        this.bit       = bit;
    }


    @Override public String getUiString() { return uiString; }

    @Override public String getApiString() { return apiString; }

    @Override public ApiFeature getDefault() { return HashAlgorithm.NONE; }

    @Override public ApiFeature getNotFound() { return HashAlgorithm.NOT_FOUND; }

    @Override public ApiFeature[] getAll() { return values(); }

    public int getBit() { return bit; }

    public static HashAlgorithm fromText(final String text) {
        if (null == text) { return NOT_FOUND; }
        switch(text) {
            case "md5":
            case "MD5":
            case "md-5":
            case "md_5":
            case "MD-5":
            case "MD_5":
                return MD5;
            case "sha1":
            case "SHA1":
            case "sha-1":
            case "SHA-1":
            case "sha_1":
            case "SHA_1":
                return SHA1;
            case "sha2_256":
            case "SHA2_256":
            case "sha-2-256":
            case "SHA-2-256":
            case "sha_2_256":
            case "SHA_2_256":
                return SHA2_256;
            case "sha3_256":
            case "SHA3_256":
            case "sha-3-256":
            case "SHA-3-256":
            case "sha_3_256":
            case "SHA_3_256":
                return SHA3_256;
            default:
                return NOT_FOUND;
        }
    }
}
