module Security {
    requires Image;
    requires com.miglayout.swing;
    requires java.desktop;
    requires java.prefs;
    requires com.google.gson;
    requires com.google.common;
    exports com.udacity.security.application;
    exports com.udacity.security.data;
    exports com.udacity.security.service;
    opens com.udacity.security.data to com.google.gson;


}