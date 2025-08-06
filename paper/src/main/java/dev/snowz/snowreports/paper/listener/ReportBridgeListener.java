package dev.snowz.snowreports.paper.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSyntaxException;
import dev.snowz.snowreports.api.model.ChatMessage;
import dev.snowz.snowreports.api.model.ReportModel;
import dev.snowz.snowreports.common.config.Config;
import dev.snowz.snowreports.common.database.entity.Report;
import dev.snowz.snowreports.paper.SnowReports;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public final class ReportBridgeListener implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(final String channel, @NotNull final Player player, final byte[] message) {
        if (!channel.equals("snowreports:main")) return;
        if (!Config.get().getStorageMethod().isRemote()) return;

        try {
            final ByteArrayDataInput input = ByteStreams.newDataInput(message);
            final String reportJson = input.readUTF();

            final Gson gson = new GsonBuilder()
                .registerTypeAdapter(
                    ChatMessage[].class, (JsonDeserializer<ChatMessage[]>) (json, typeOfT, context) -> {
                        if (json.isJsonArray()) {
                            return context.deserialize(json, ChatMessage[].class);
                        } else {
                            final String chatHistoryStr = json.getAsString();
                            try {
                                final ChatMessage message1 = new Gson().fromJson(chatHistoryStr, ChatMessage.class);
                                return new ChatMessage[]{ message1 };
                            } catch (final JsonSyntaxException e) {
                                return new ChatMessage[]{ new ChatMessage(
                                    chatHistoryStr,
                                    Instant.now().getEpochSecond()
                                ) };
                            }
                        }
                    }
                )
                .create();

            final ReportModel report = gson.fromJson(reportJson, ReportModel.class);

            if (report.server().equals(Config.get().getServerName())) {
                return;
            }

            SnowReports.getAlertManager().broadcastAlert(Report.fromModel(report));
        } catch (final Exception e) {
            SnowReports.getInstance().getLogger().severe("Failed to read report from bridge: " + e.getMessage());
        }
    }
}
