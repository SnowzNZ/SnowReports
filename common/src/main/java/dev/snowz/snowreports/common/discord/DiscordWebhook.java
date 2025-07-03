package dev.snowz.snowreports.common.discord;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Accessors(chain = true)
public final class DiscordWebhook {

    private static final Gson GSON = new GsonBuilder().create();

    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(0);
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "SnowReports-Webhook-" + THREAD_COUNTER.incrementAndGet());
        t.setDaemon(true);
        return t;
    });

    private final String url;
    private final List<EmbedObject> embeds = new ArrayList<>();

    @Setter
    private String content;
    @Setter
    private String username;
    @Setter
    private String avatarUrl;
    @Setter
    private boolean tts;

    public DiscordWebhook(final String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("Webhook URL cannot be null or empty");
        }
        this.url = url;
    }

    public DiscordWebhook addEmbed(final EmbedObject embed) {
        if (embed == null) {
            throw new IllegalArgumentException("Embed cannot be null");
        }
        if (this.embeds.size() >= 10) {
            throw new IllegalArgumentException("Cannot add more than 10 embeds");
        }
        this.embeds.add(embed);
        return this;
    }

    public CompletableFuture<Void> executeAsync() {
        return CompletableFuture.runAsync(this::executeSynchronously, EXECUTOR);
    }

    public void execute() {
        EXECUTOR.submit(this::executeSynchronously);
    }

    public void executeSynchronously() {
        if (content == null && embeds.isEmpty()) {
            throw new IllegalArgumentException("Set content or add at least one EmbedObject");
        }

        try {
            final WebhookPayload payload = createPayload();
            sendWebhook(payload);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to send Discord webhook", e);
        }
    }

    private WebhookPayload createPayload() {
        final WebhookPayload payload = new WebhookPayload();
        payload.content = this.content;
        payload.username = this.username;
        payload.avatarUrl = this.avatarUrl;
        payload.tts = this.tts;

        if (!this.embeds.isEmpty()) {
            payload.embeds = new ArrayList<>();
            for (final EmbedObject embed : this.embeds) {
                payload.embeds.add(embed.toPayloadEmbed());
            }
        }

        return payload;
    }

    private void sendWebhook(final WebhookPayload payload) throws IOException {
        final URL url = URI.create(this.url).toURL();
        final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "SnowReports-Webhook/1.0");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);

            final String jsonPayload = GSON.toJson(payload);

            try (final OutputStream stream = connection.getOutputStream()) {
                stream.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
                stream.flush();
            }

            final int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                throw new IOException("HTTP " + responseCode + ": " + connection.getResponseMessage());
            }

            try {
                connection.getInputStream().close();
            } catch (final IOException ignored) {
            }

        } finally {
            connection.disconnect();
        }
    }

    @Getter
    public static class EmbedObject {
        private final List<Field> fields = new ArrayList<>();
        private String title;
        private String description;
        private String url;
        private Color color;
        private Footer footer;
        private Thumbnail thumbnail;
        private Image image;
        private Author author;

        public EmbedObject setTitle(final String title) {
            validateLength(title, 256, "Title");
            this.title = title;
            return this;
        }

        public EmbedObject setDescription(final String description) {
            validateLength(description, 4096, "Description");
            this.description = description;
            return this;
        }

        public EmbedObject setUrl(final String url) {
            this.url = url;
            return this;
        }

        public EmbedObject setColor(final Color color) {
            this.color = color;
            return this;
        }

        public EmbedObject setThumbnail(final String url) {
            this.thumbnail = new Thumbnail(url);
            return this;
        }

        public EmbedObject setImage(final String url) {
            this.image = new Image(url);
            return this;
        }

        public EmbedObject setFooter(final String text, final String icon) {
            validateLength(text, 2048, "Footer text");
            this.footer = new Footer(text, icon);
            return this;
        }

        public EmbedObject setAuthor(final String name, final String url, final String icon) {
            validateLength(name, 256, "Author name");
            this.author = new Author(name, url, icon);
            return this;
        }

        public EmbedObject addField(final String name, final String value, final boolean inline) {
            if (fields.size() >= 25) {
                throw new IllegalArgumentException("Cannot add more than 25 fields");
            }
            validateLength(name, 256, "Field name");
            validateLength(value, 1024, "Field value");
            this.fields.add(new Field(name, value, inline));
            return this;
        }

        private void validateLength(final String text, final int maxLength, final String fieldName) {
            if (text != null && text.length() > maxLength) {
                throw new IllegalArgumentException(fieldName + " cannot exceed " + maxLength + " characters");
            }
        }

        private PayloadEmbed toPayloadEmbed() {
            final PayloadEmbed embed = new PayloadEmbed();
            embed.title = this.title;
            embed.description = this.description;
            embed.url = this.url;

            if (this.color != null) {
                embed.color = (this.color.getRed() << 16) + (this.color.getGreen() << 8) + this.color.getBlue();
            }

            if (this.footer != null) {
                embed.footer = new PayloadFooter();
                embed.footer.text = this.footer.text();
                embed.footer.iconUrl = this.footer.iconUrl();
            }

            if (this.image != null) {
                embed.image = new PayloadImage();
                embed.image.url = this.image.url();
            }

            if (this.thumbnail != null) {
                embed.thumbnail = new PayloadThumbnail();
                embed.thumbnail.url = this.thumbnail.url();
            }

            if (this.author != null) {
                embed.author = new PayloadAuthor();
                embed.author.name = this.author.name();
                embed.author.url = this.author.url();
                embed.author.iconUrl = this.author.iconUrl();
            }

            if (!this.fields.isEmpty()) {
                embed.fields = new ArrayList<>();
                for (final Field field : this.fields) {
                    final PayloadField payloadField = new PayloadField();
                    payloadField.name = field.name();
                    payloadField.value = field.value();
                    payloadField.inline = field.inline();
                    embed.fields.add(payloadField);
                }
            }

            return embed;
        }

        public record Footer(String text, String iconUrl) {
        }

        public record Thumbnail(String url) {
        }

        public record Image(String url) {
        }

        public record Author(String name, String url, String iconUrl) {
        }

        public record Field(String name, String value, boolean inline) {
        }
    }

    private static class WebhookPayload {
        public String content;
        public String username;
        public String avatarUrl;
        public boolean tts;
        public List<PayloadEmbed> embeds;
    }

    private static class PayloadEmbed {
        public String title;
        public String description;
        public String url;
        public Integer color;
        public PayloadFooter footer;
        public PayloadImage image;
        public PayloadThumbnail thumbnail;
        public PayloadAuthor author;
        public List<PayloadField> fields;
    }

    private static class PayloadFooter {
        public String text;
        public String iconUrl;
    }

    private static class PayloadImage {
        public String url;
    }

    private static class PayloadThumbnail {
        public String url;
    }

    private static class PayloadAuthor {
        public String name;
        public String url;
        public String iconUrl;
    }

    private static class PayloadField {
        public String name;
        public String value;
        public boolean inline;
    }
}
