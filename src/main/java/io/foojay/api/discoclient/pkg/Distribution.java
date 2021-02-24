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
import java.util.stream.Collectors;


public enum Distribution implements ApiFeature {
    AOJ("AOJ", "aoj"),
    AOJ_OPENJ9("AOJ OpenJ9", "aoj_openj9"),
    CORRETTO("Corretto", "corretto"),
    DRAGONWELL("Dragonwell", "dragonwell"),
    GRAALVM_CE8("Graal VM CE 8", "graalvm_ce8"),
    GRAALVM_CE11("Graal VM CE 11", "graalvm_ce11"),
    LIBERICA("Liberica", "liberica"),
    LIBERICA_NATIVE("Liberica Native", "liberica_native"),
    MANDREL("Mandrel", "mandrel"),
    OJDK_BUILD("OJDKBuild", "ojdk_build"),
    ORACLE("Oracle", "oracle"),
    ORACLE_OPEN_JDK("Oracle OpenJDK", "oracle_openjdk"),
    RED_HAT("Red Hat", "redhat"),
    SAP_MACHINE("SAP Machine", "sapmachine"),
    ZULU("Zulu", "zulu"),
    NONE("-", ""),
    NOT_FOUND("", "");

    private final String uiString;
    private final String apiString;


    Distribution(final String uiString, final String apiString) {
        this.uiString  = uiString;
        this.apiString = apiString;
    }


    @Override public String getUiString() { return uiString; }

    @Override public String getApiString() { return apiString; }

    @Override public Distribution getDefault() { return Distribution.NONE; }

    @Override public Distribution getNotFound() { return Distribution.NOT_FOUND; }

    @Override public Distribution[] getAll() { return values(); }

    public static Distribution fromText(final String text) {
        switch (text) {
            case "aoj":
            case "AOJ":
            case "adopt":
            case "ADOPT":
            case "adoptopenjdk":
            case "Adopt":
            case "AdoptOpenJDK":
                return AOJ;
            case "aoj_openj9":
            case "AOJ_OpenJ9":
            case "AOJ_OPENJ9":
            case "AOJ OpenJ9":
            case "AOJ OPENJ9":
            case "aoj openj9":
            case "adopt_openj9":
            case "ADOPT_OPENJ9":
            case "Adopt OpenJ9":
            case "adoptopenjdk_openj9":
            case "Adopt_OpenJ9":
            case "AdoptOpenJDK_OpenJ9":
                return AOJ_OPENJ9;
            case "corretto":
            case "CORRETTO":
            case "Corretto":
                return CORRETTO;
            case "dragonwell":
            case "DRAGONWELL":
            case "Dragonwell":
                return DRAGONWELL;
            case "graalvm_ce8":
            case "graalvmce8":
            case "GraalVM CE 8":
            case "GraalVMCE8":
            case "GraalVM_CE8":
                return GRAALVM_CE8;
            case "graalvm_ce11":
            case "graalvmce11":
            case "GraalVM CE 11":
            case "GraalVMCE11":
            case "GraalVM_CE11":
                return GRAALVM_CE11;
            case "liberica":
            case "LIBERICA":
            case "Liberica":
                return LIBERICA;
            case "liberica_native":
            case "LIBERICA_NATIVE":
            case "libericaNative":
            case "LibericaNative":
            case "liberica native":
            case "LIBERICA NATIVE":
            case "Liberica Native":
                return LIBERICA_NATIVE;
            case "mandrel":
            case "MANDREL":
            case "Mandrel":
                return MANDREL;
            case "ojdk_build":
            case "OJDK_BUILD":
            case "ojdkbuild":
            case "OJDKBuild":
                return OJDK_BUILD;
            case "oracle_open_jdk":
            case "ORACLE_OPEN_JDK":
            case "oracle_openjdk":
            case "ORACLE_OPENJDK":
            case "Oracle_OpenJDK":
            case "Oracle OpenJDK":
            case "oracle openjdk":
            case "ORACLE OPENJDK":
            case "open_jdk":
            case "openjdk":
            case "OpenJDK":
            case "Open JDK":
            case "OPEN_JDK":
            case "open-jdk":
            case "OPEN-JDK":
            case "Oracle-OpenJDK":
            case "oracle-openjdk":
            case "ORACLE-OPENJDK":
            case "oracle-open-jdk":
            case "ORACLE-OPEN-JDK":
                return ORACLE_OPEN_JDK;
            case "oracle":
            case "Oracle":
            case "ORACLE":
                return ORACLE;
            case "sap_machine":
            case "sapmachine":
            case "SAPMACHINE":
            case "SAP_MACHINE":
            case "SAPMachine":
            case "SAP Machine":
            case "sap-machine":
            case "SAP-Machine":
            case "SAP-MACHINE":
                return SAP_MACHINE;
            case "RedHat":
            case "redhat":
            case "REDHAT":
            case "Red Hat":
            case "red hat":
            case "RED HAT":
            case "Red_Hat":
            case "red_hat":
            case "red-hat":
            case "Red-Hat":
            case "RED-HAT":
                return RED_HAT;
            case "zulu":
            case "ZULU":
            case "Zulu":
                return ZULU;
            default:
                return NOT_FOUND;
        }
    }

    public static List<Distribution> getDistributions() {
        return Arrays.stream(values()).filter(distro -> Distribution.NONE != distro && Distribution.NOT_FOUND != distro).collect(Collectors.toList());
    }

    public static List<Distribution> getAsList() { return Arrays.asList(values()); }

    public static List<Distribution> getDistributionsBasedOnOpenJDK() {
        return Arrays.stream(values())
                     .filter(distribution -> Distribution.NONE != distribution)
                     .filter(distribution -> Distribution.NOT_FOUND != distribution)
                     .filter(distribution -> Distribution.GRAALVM_CE11 != distribution)
                     .filter(distribution -> Distribution.GRAALVM_CE8 != distribution)
                     .filter(distribution -> Distribution.MANDREL != distribution)
                     .collect(Collectors.toList());
    }

    public static List<Distribution> getDistributionsBasedOnGraalVm() {
        return Arrays.stream(values())
                     .filter(distribution -> Distribution.NONE == distribution)
                     .filter(distribution -> Distribution.NOT_FOUND == distribution)
                     .filter(distribution -> Distribution.GRAALVM_CE11 == distribution)
                     .filter(distribution -> Distribution.GRAALVM_CE8 == distribution)
                     .filter(distribution -> Distribution.MANDREL == distribution)
                     .collect(Collectors.toList());
    }
}
