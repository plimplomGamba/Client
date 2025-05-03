package de.plimplom.addonreader.event;

import de.plimplom.addonreader.view.message.MessageBox;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class InfoMessageEvent extends ApplicationEvent {
    private final EventType eventType;
    private final EventName eventName;
    private final VBox message;

    public InfoMessageEvent(EventType eventType, EventName eventName, MessageBox message) {
        this.eventType = eventType;
        this.eventName = eventName;
        this.message = message;
    }

    public InfoMessageEvent(EventType eventType, EventName eventName) {
        this.message = null;
        this.eventType = eventType;
        this.eventName = eventName;
    }

    public enum InfoMessageType {
        INFO,
        WARNING,
        ERROR
    }

    public enum EventType {
        CREATE, DELETE
    }

    public enum EventName {
        INVALID_FOLDER,
        ADDON_NOT_INSTALLED,
        SYNC_FAILED
    }
}
