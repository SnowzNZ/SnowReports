package dev.snowz.snowreports.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.snowz.snowreports.SnowReports;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {

    public static void checkUpdates(String currentVersion) {
        String apiUrl = "https://api.modrinth.com/v2/project/nt3ehUXJ/version";

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                JsonArray jsonArray = getJsonArray(connection);

                if (!jsonArray.isEmpty()) {
                    JsonElement firstEntry = jsonArray.get(0);
                    String latestVersion = firstEntry.getAsJsonObject().get("version_number").getAsString();

                    if (latestVersion.equals(currentVersion)) {
                        SnowReports.getInstance().getLogger().warning("You are running the latest version of SnowReports!");
                    } else {
                        SnowReports.getInstance().getLogger().warning("There is a newer version of SnowReports available!");
                        SnowReports.getInstance().getLogger().warning("You are running version " + currentVersion + " and the latest version is " + latestVersion);
                        SnowReports.getInstance().getLogger().warning("Download the latest version at https://modrinth.com/plugin/snowreports/");
                    }
                } else {
                    SnowReports.getInstance().getLogger().warning("Error checking for updates!");
                }
            } else {
                SnowReports.getInstance().getLogger().warning("Error checking for updates!");
            }
        } catch (IOException e) {
            SnowReports.getInstance().getLogger().warning("Error checking for updates!");
        }
    }

    private static JsonArray getJsonArray(HttpURLConnection connection) throws IOException {
        InputStreamReader reader = new InputStreamReader(connection.getInputStream());
        JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
        reader.close();
        return jsonArray;
    }
}
