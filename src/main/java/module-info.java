module de.plimplom.addonreader {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    requires org.kordamp.bootstrapfx.core;
    requires static lombok;
    requires com.google.gson;
    requires party.iroiro.luajava;
    requires party.iroiro.luajava.lua54;
    requires jdk.security.jgss;
    requires java.net.http;
    requires org.slf4j;
    requires fr.brouillard.oss.cssfx;
    requires MaterialFX;
    requires ch.qos.logback.core;
    requires ch.qos.logback.classic;
    requires org.apache.commons.lang3;

    opens de.plimplom.addonreader.dto to com.google.gson;
    exports de.plimplom.addonreader.app;
    opens de.plimplom.addonreader.app to javafx.fxml;
    exports de.plimplom.addonreader.model;
    opens de.plimplom.addonreader.model to javafx.fxml;
    exports de.plimplom.addonreader.service;
    exports de.plimplom.addonreader.event;
    opens de.plimplom.addonreader.service to javafx.fxml;
    exports de.plimplom.addonreader.view;
    opens de.plimplom.addonreader.view to javafx.fxml;
    exports de.plimplom.addonreader.repository;
    opens de.plimplom.addonreader.repository to javafx.fxml;
    exports de.plimplom.addonreader.view.custom;
    opens de.plimplom.addonreader.view.custom to javafx.fxml;
    exports de.plimplom.addonreader.view.message;
    opens de.plimplom.addonreader.view.message to javafx.fxml;
}