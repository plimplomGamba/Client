package de.plimplom.addonreader.dto;

import java.util.List;

public record HighLowData(String player, List<HighLowPlayerData> highLowPlayerData) {
}
