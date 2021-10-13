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

package io.foojay.api.discoclient.pkg;

import java.util.Arrays;
import java.util.List;


public enum SignatureType implements ApiFeature {
    RSA("RSA", "rsa"), // Rivest Shamir Adleman
    DSA("DSA", "dsa"), // Digital Signature Algorithm
    ECDSA("ECDSA", "ecdsa"), // Elliptic Curve Cryptography DSA
    EDDSA("EdDSA", "eddsa"), // Edwards Curve DSA
    NONE("-", ""),
    NOT_FOUND("", "");

    private final String uiString;
    private final String apiString;


    SignatureType(final String uiString, final String apiString) {
        this.uiString  = uiString;
        this.apiString = apiString;
    }


    @Override public String getUiString() { return uiString; }

    @Override public String getApiString() { return apiString; }

    @Override public SignatureType getDefault() { return SignatureType.NONE; }

    @Override public SignatureType getNotFound() { return SignatureType.NOT_FOUND; }

    @Override public SignatureType[] getAll() { return values(); }

    public static SignatureType fromText(final String text) {
        if (null == text) { return NOT_FOUND; }
        switch (text) {
            case "rsa":
            case "RSA":
                return RSA;
            case "dsa":
            case "DSA":
                return DSA;
            case "ecdsa":
            case "ECDSA":
                return ECDSA;
            case "eddsa":
            case "EdDSA":
            case "EDDSA":
                return EDDSA;
            default:
                return NOT_FOUND;
        }
    }

    public static List<SignatureType> getAsList() { return Arrays.asList(values()); }

    @Override public String toString() { return uiString; }
}
