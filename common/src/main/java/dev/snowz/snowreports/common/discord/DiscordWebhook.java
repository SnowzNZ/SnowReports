package dev.snowz.snowreports.common.discord;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@Accessors(chain = true)
public final class DiscordWebhook {
    private final String url;
    private String content;
    private String username;
    private String avatarUrl;
    private boolean tts;
    private final List<EmbedObject> embeds = new ArrayList<>();

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    public DiscordWebhook(final String url) {
        this.url = url;
    }

    @SuppressWarnings("UnusedReturnValue")
    public DiscordWebhook addEmbed(final EmbedObject embed) {
        this.embeds.add(embed);
        return this;
    }

    public void execute() throws IOException, InterruptedException {
        if (content == null && embeds.isEmpty()) {
            throw new IllegalArgumentException(
                "Set content or add at least one EmbedObject"
            );
        }

        final String json = buildJsonPayload();
        final HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .header("User-Agent", "Java-DiscordWebhook")
            .timeout(Duration.ofSeconds(30))
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() >= 400) {
            throw new IOException(
                "HTTP " + response.statusCode() + ": " + response.body()
            );
        }
    }

    private String buildJsonPayload() {
        final List<Map<String, Object>> embedMaps = embeds.stream()
            .map(this::embedToMap)
            .toList();

        final Map<String, Object> payload = Map.of(
            "content", content,
            "username", username,
            "avatar_url", avatarUrl,
            "tts", tts,
            "embeds", embedMaps
        );
        return JsonUtils.toJson(payload);
    }

    private Map<String, Object> embedToMap(final EmbedObject embed) {
        final Map<String, Object> footerMap = embed.footer != null ?
            Map.of("text", embed.footer.text(), "icon_url", embed.footer.iconUrl()) : null;

        final Map<String, Object> imageMap = embed.image != null ?
            Map.of("url", embed.image.url()) : null;

        final Map<String, Object> thumbnailMap = embed.thumbnail != null ?
            Map.of("url", embed.thumbnail.url()) : null;

        final Map<String, Object> authorMap = embed.author != null ?
            Map.of(
                "name", embed.author.name(), "url", embed.author.url(),
                "icon_url", embed.author.iconUrl()
            ) : null;

        final List<Map<String, Object>> fieldMaps = embed.fields.stream()
            .map(field -> Map.<String, Object>of(
                "name", field.name(),
                "value", field.value(),
                "inline", field.inline()
            ))
            .toList();

        final Map<String, Object> embedMap = new HashMap<>();
        embedMap.put("title", embed.title);
        embedMap.put("description", embed.description);
        embedMap.put("url", embed.url);
        embedMap.put("color", embed.color != null ? colorToRgb(embed.color) : null);
        embedMap.put("footer", footerMap);
        embedMap.put("image", imageMap);
        embedMap.put("thumbnail", thumbnailMap);
        embedMap.put("author", authorMap);
        embedMap.put("fields", fieldMaps);

        return JsonUtils.filterNulls(embedMap);
    }

    private int colorToRgb(final Color color) {
        return (color.getRed() << 16) + (color.getGreen() << 8) + color.getBlue();
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class EmbedObject {
        private String title;
        private String description;
        private String url;
        private Color color;
        private Footer footer;
        private Thumbnail thumbnail;
        private Image image;
        private Author author;
        private final List<Field> fields = new ArrayList<>();

        public EmbedObject setFooter(final String text, final String icon) {
            this.footer = new Footer(text, icon);
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

        public EmbedObject setAuthor(final String name, final String url, final String icon) {
            this.author = new Author(name, url, icon);
            return this;
        }

        public EmbedObject addField(final String name, final String value, final boolean inline) {
            this.fields.add(new Field(name, value, inline));
            return this;
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

    private static class JsonUtils {
        static String toJson(final Map<String, Object> map) {
            final String content = map.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .map(entry -> "\"%s\":%s".formatted(
                    entry.getKey(),
                    valueToJson(entry.getValue())
                ))
                .collect(Collectors.joining(","));

            return "{" + content + "}";
        }

        static Map<String, Object> filterNulls(final Map<String, Object> map) {
            return map.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue
                ));
        }

        @SuppressWarnings("unchecked")
        private static String valueToJson(final Object value) {
            if (value instanceof String) {
                return "\"%s\"".formatted((String) value);
            } else if (value instanceof Number) {
                return value.toString();
            } else if (value instanceof Boolean) {
                return value.toString();
            } else if (value instanceof final List<?> list) {
                final String listContent = list.stream()
                    .map(item -> item instanceof Map ?
                        toJson((Map<String, Object>) item) :
                        valueToJson(item))
                    .collect(Collectors.joining(","));
                return "[" + listContent + "]";
            } else if (value instanceof Map) {
                return toJson((Map<String, Object>) value);
            } else {
                return "null";
            }
        }
    }
}
