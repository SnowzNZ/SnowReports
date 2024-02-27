package dev.snowz.snowreports.util;

import dev.snowz.snowreports.SnowReports;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {

    public static void checkUpdates(String currentVersion) {
        SnowReports.getInstance().getLogger().info("Checking for updates!");

        String apiUrl = "https://api.modrinth.com/v2/project/nt3ehUXJ/version";

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                JSONArray jsonArray = getJsonArray(connection);

                if (!jsonArray.isEmpty()) {
                    JSONObject firstEntry = (JSONObject) jsonArray.get(0);
                    String latestVersion = (String) firstEntry.get("version_number");

                    if (latestVersion.equals(currentVersion)) {
                        SnowReports.getInstance().getLogger().info("You are running the latest version of SnowReports!");
                    } else {
                        SnowReports.getInstance().getLogger().warning("There is a newer version of SnowReports available!");
                        SnowReports.getInstance().getLogger().warning("You are running version " + currentVersion + " and the latest version is " + latestVersion);
                        SnowReports.getInstance().getLogger().warning("Download the latest version at https://modrinth.com/plugin/snowreports/");
                    }
                } else {
                    SnowReports.getInstance().getLogger().severe("Error checking for updates!");
                }
            } else {
                SnowReports.getInstance().getLogger().severe("Error checking for updates!");
            }
        } catch (IOException e) {
            SnowReports.getInstance().getLogger().severe("Error checking for updates: " + e.getMessage());
        }
    }

    private static JSONArray getJsonArray(HttpURLConnection connection) throws IOException {
        InputStreamReader reader = new InputStreamReader(connection.getInputStream());
        JSONParser parser = new JSONParser();
        JSONArray jsonArray = null;
        try {
            jsonArray = (JSONArray) parser.parse(reader);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            reader.close();
        }
        return jsonArray;
    }
}
