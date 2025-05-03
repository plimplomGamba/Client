package de.plimplom.addonreader.event;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class SettingsChangedEvent extends ApplicationEvent {
    protected final String settingName;
    protected final Object newValue;

    public SettingsChangedEvent( String settingName1, Object newValue1) {
        this.settingName = settingName1;
        this.newValue = newValue1;
    }
}
