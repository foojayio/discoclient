module io.foojay.api.discoclient {
    // Java
    requires java.base;
    requires java.net.http;
    requires java.logging;
    requires java.validation;

    // 3rd party
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.slf4j;
    requires org.slf4j;
    requires com.google.gson;

    exports io.foojay.api.discoclient;
    exports io.foojay.api.discoclient.event;
    exports io.foojay.api.discoclient.pkg;
    exports io.foojay.api.discoclient.util;
}