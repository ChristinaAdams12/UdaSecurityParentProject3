module Image {
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.services.rekognition;
    requires java.prefs;
    requires java.desktop;
    requires org.slf4j;
    exports com.udacity.image.interfaces;
    exports com.udacity.image.service;
}