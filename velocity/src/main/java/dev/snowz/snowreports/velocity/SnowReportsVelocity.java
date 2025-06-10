package dev.snowz.snowreports.velocity;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

import java.util.logging.Logger;

@Plugin(
    id = "snowreports",
    name = "SnowReports",
    version = "1.1.0",
    description = "Lightweight, customizable player reporting plugin.",
    authors = { "Snowz" }
)
public final class SnowReportsVelocity {
    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public SnowReportsVelocity(final ProxyServer server, final Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(final ProxyInitializeEvent event) {
        server.getChannelRegistrar().register(MinecraftChannelIdentifier.from("snowreports:main"));
    }

    @Subscribe
    public void onPluginMessage(final PluginMessageEvent event) {
        if (!event.getIdentifier().equals(MinecraftChannelIdentifier.from("snowreports:main"))) {
            return;
        }

        final byte[] data = event.getData();
        final ByteArrayDataInput input = ByteStreams.newDataInput(data);

        forwardReportToAllServers(input.readUTF());
    }

    private void forwardReportToAllServers(final String input) {
        final ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF(input);

        server.getAllServers().forEach(serverInfo -> {
            serverInfo.sendPluginMessage(
                MinecraftChannelIdentifier.from("snowreports:main"),
                output.toByteArray()
            );
        });
    }
}
