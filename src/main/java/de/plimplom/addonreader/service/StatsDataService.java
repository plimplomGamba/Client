package de.plimplom.addonreader.service;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import de.plimplom.addonreader.dto.ClientData;
import de.plimplom.addonreader.dto.DeathrollData;
import de.plimplom.addonreader.dto.HighLowData;
import de.plimplom.addonreader.dto.HighLowPlayerData;
import de.plimplom.addonreader.event.DataSyncEvent;
import de.plimplom.addonreader.event.EventBus;
import de.plimplom.addonreader.model.ApplicationProperties;
import de.plimplom.addonreader.util.HttpClientFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
public class StatsDataService {
    private final ApplicationProperties applicationProperties;
    private final ExecutorService executorService;
    private final EventBus eventBus;

    public StatsDataService(ApplicationProperties applicationProperties, ExecutorService executorService, EventBus eventBus) {
        this.applicationProperties = applicationProperties;
        this.executorService = executorService;
        this.eventBus = eventBus;
    }

    public CompletableFuture<Void> sendData(JsonElement data, String accountId) {
        return CompletableFuture.runAsync(() -> {
            try {
                ClientData clientData = processData(data, accountId);
                sendHttpRequest(clientData);
                eventBus.publish(new DataSyncEvent(true, null));
            } catch (Exception e) {
                log.error("Failed to send data", e);
                eventBus.publish(new DataSyncEvent(false, e.getMessage()));
            }
        }, executorService);
    }

    private ClientData processData(JsonElement jsonElement, String accountId) {
        var clientData = new ClientData(accountId, new ArrayList<>(), new ArrayList<>());

        var jsonObject = jsonElement.getAsJsonObject();

        if (jsonObject.has("Deathroll")) {
            var deathroll = jsonObject.getAsJsonObject().get("Deathroll").getAsJsonObject();
            for (String key : deathroll.keySet()) {
                var value = deathroll.get(key).getAsJsonObject();
                clientData.deathrollDataList().add(new DeathrollData(value.get("timestamp").getAsLong(), value.get("winner").getAsString(), value.get("loser").getAsString(), value.get("amount").getAsLong()));
            }
        }

        if (jsonObject.has("HighLow")) {
            if (jsonObject.get("HighLow").getAsJsonObject().has("Specific")) {
                var specific = jsonObject.get("HighLow").getAsJsonObject().get("Specific").getAsJsonObject();
                for (String key : specific.keySet()) {
                    var value = specific.get(key);
                    var highLowData = new HighLowData(key, new ArrayList<>());
                    for (String valueKey : value.getAsJsonObject().keySet()) {
                        var valueKeyValue = value.getAsJsonObject().get(valueKey);
                        highLowData.highLowPlayerData().add(new HighLowPlayerData(valueKey, valueKeyValue.getAsLong()));
                    }
                    clientData.highLowDataList().add(highLowData);
                }
            }
        }
        return clientData;
    }

    private void sendHttpRequest(ClientData clientData) throws IOException, InterruptedException {
        try (HttpClient client = HttpClientFactory.createDefaultClient()) {
            String authHeader = Base64.getEncoder().encodeToString(
                    String.format("%s:%s", "gamba", applicationProperties.syncPassProperty().get()).getBytes()
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(applicationProperties.syncHostProperty().get()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Basic " + authHeader)
                    .POST(HttpRequest.BodyPublishers.ofString(new GsonBuilder().create().toJsonTree(clientData).toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 202) {
                throw new IOException("Error sending data: " + response.statusCode() + " - " + response.body());
            }
        }
    }
}
