module security.service {
    requires image.service;
    requires java.desktop;
    requires java.prefs;
    requires com.google.common;
    requires com.google.gson;
    requires org.slf4j;
    requires miglayout.swing;
    opens com.service.security.data to com.google.gson;
}