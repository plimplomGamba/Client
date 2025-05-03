package de.plimplom.addonreader.dto;

import java.util.List;

public record ClientData(
        String accountId,
        List<HighLowData> highLowDataList,
        List<DeathrollData> deathrollDataList
) {

}
