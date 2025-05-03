package de.plimplom.addonreader.event;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class DataSyncEvent extends ApplicationEvent{
    private final boolean success;
    private final String errorMessage;

    public DataSyncEvent(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }

}
