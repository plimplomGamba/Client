package de.plimplom.addonreader.dto;

public record DeathrollData(
        Long timestamp,
        String winner,
        String loser,
        Long amount
) {
}
