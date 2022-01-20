module io.foojay.api.discoclient {
    // Java
    requires java.base;
    requires java.net.http;
    requires java.logging;

    // 3rd party
    requires com.google.gson;
    requires eu.hansolo.jdktools;

    exports io.foojay.api.discoclient;
    exports io.foojay.api.discoclient.event;
    exports io.foojay.api.discoclient.pkg;
    exports io.foojay.api.discoclient.util;
}