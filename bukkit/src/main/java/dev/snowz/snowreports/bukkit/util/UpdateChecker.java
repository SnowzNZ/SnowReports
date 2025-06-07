package dev.snowz.snowreports.bukkit.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.snowz.snowreports.bukkit.SnowReports;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

public final class UpdateChecker {

    private static final String API_URL = "https://api.modrinth.com/v3/project/nt3ehUXJ/version";
    private static final String DOWNLOAD_URL = "https://modrinth.com/plugin/snowreports/";

    public static boolean checkForUpdates(final String currentVersion) {
        final Logger logger = SnowReports.getInstance().getLogger();
        logger.info("Checking for updates...");

        try {
            final HttpURLConnection connection = (HttpURLConnection) new URI(API_URL).toURL().openConnection();
            connection.setRequestMethod("GET");

            final int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (final InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                    final JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
                    if (jsonArray != null && !jsonArray.isEmpty()) {
                        final JsonObject firstEntry = jsonArray.get(0).getAsJsonObject();
                        final String latestVersion = firstEntry.get("version_number").getAsString();

                        if (isUpToDate(currentVersion, latestVersion)) {
                            logger.info("You are running the latest version of SnowReports!");
                            return false;
                        } else {
                            logger.warning("There is a newer version of SnowReports available!");
                            logger.warning("You are running version " + currentVersion + " and the latest version is " + latestVersion);
                            logger.warning("Download the latest version at " + DOWNLOAD_URL);
                            return true;
                        }
                    } else {
                        logger.severe("Error checking for updates: Received empty JSON array.");
                    }
                }
            } else {
                logger.severe("Error checking for updates: Response code " + responseCode);
            }
        } catch (final IOException | URISyntaxException e) {
            logger.severe("Error checking for updates: " + e.getMessage());
            return false;
        }
        return false;
    }

    private static boolean isUpToDate(final String currentVersion, final String latestVersion) {
        return compareVersions(currentVersion, latestVersion) >= 0;
    }

    private static int compareVersions(final String v1, final String v2) {
        final String[] v1Parts = v1.split("\\.");
        final String[] v2Parts = v2.split("\\.");
        final int length = Math.max(v1Parts.length, v2Parts.length);

        for (int i = 0; i < length; i++) {
            final int v1Part = i < v1Parts.length ? Integer.parseInt(v1Parts[i]) : 0;
            final int v2Part = i < v2Parts.length ? Integer.parseInt(v2Parts[i]) : 0;

            if (v1Part < v2Part) {
                return -1;
            } else if (v1Part > v2Part) {
                return 1;
            }
        }
        return 0;
    }
}
