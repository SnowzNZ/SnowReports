package dev.snowz.snowreports.bukkit.util;

import dev.snowz.snowreports.bukkit.SnowReports;
import lombok.Getter;
import lombok.Setter;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.List;

public final class DiscordWebhook {

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
        this.url = url;
    }

    public void addEmbed(final EmbedObject embed) {
        this.embeds.add(embed);
    }

    public void execute() {
        if (this.content == null && this.embeds.isEmpty()) {
            throw new IllegalArgumentException("Set content or add at least one EmbedObject");
        }

        final Thread webhookThread = new Thread(() -> {
            final JSONObject json = new JSONObject();

            json.put("content", this.content);
            json.put("username", this.username);
            json.put("avatar_url", this.avatarUrl);
            json.put("tts", this.tts);

            if (!this.embeds.isEmpty()) {
                final List<JSONObject> embedObjects = new ArrayList<>();

                for (final EmbedObject embed : this.embeds) {
                    final JSONObject jsonEmbed = new JSONObject();

                    jsonEmbed.put("title", embed.getTitle());
                    jsonEmbed.put("description", embed.getDescription());
                    jsonEmbed.put("url", embed.getUrl());

                    if (embed.getColor() != null) {
                        final Color color = embed.getColor();
                        int rgb = color.getRed();
                        rgb = (rgb << 8) + color.getGreen();
                        rgb = (rgb << 8) + color.getBlue();

                        jsonEmbed.put("color", rgb);
                    }

                    final EmbedObject.Footer footer = embed.getFooter();
                    final EmbedObject.Image image = embed.getImage();
                    final EmbedObject.Thumbnail thumbnail = embed.getThumbnail();
                    final EmbedObject.Author author = embed.getAuthor();
                    final List<EmbedObject.Field> fields = embed.getFields();

                    if (footer != null) {
                        final JSONObject jsonFooter = new JSONObject();

                        jsonFooter.put("text", footer.text());
                        jsonFooter.put("icon_url", footer.iconUrl());
                        jsonEmbed.put("footer", jsonFooter);
                    }

                    if (image != null) {
                        final JSONObject jsonImage = new JSONObject();

                        jsonImage.put("url", image.url());
                        jsonEmbed.put("image", jsonImage);
                    }

                    if (thumbnail != null) {
                        final JSONObject jsonThumbnail = new JSONObject();

                        jsonThumbnail.put("url", thumbnail.url());
                        jsonEmbed.put("thumbnail", jsonThumbnail);
                    }

                    if (author != null) {
                        final JSONObject jsonAuthor = new JSONObject();

                        jsonAuthor.put("name", author.name());
                        jsonAuthor.put("url", author.url());
                        jsonAuthor.put("icon_url", author.iconUrl());
                        jsonEmbed.put("author", jsonAuthor);
                    }

                    final List<JSONObject> jsonFields = new ArrayList<>();
                    for (final EmbedObject.Field field : fields) {
                        final JSONObject jsonField = new JSONObject();

                        jsonField.put("name", field.name());
                        jsonField.put("value", field.value());
                        jsonField.put("inline", field.inline());

                        jsonFields.add(jsonField);
                    }

                    jsonEmbed.put("fields", jsonFields.toArray());
                    embedObjects.add(jsonEmbed);
                }

                json.put("embeds", embedObjects.toArray());
            }
            try {
                final URL url = new URI(this.url).toURL();
                final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.addRequestProperty("Content-Type", "application/json");
                connection.addRequestProperty("User-Agent", "Java-tazpvp-webhook");
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");

                final OutputStream stream = connection.getOutputStream();
                stream.write(json.toString().getBytes());
                stream.flush();
                stream.close();

                connection.getInputStream().close();
                connection.disconnect();
            } catch (final Exception e) {
                SnowReports.getInstance().getLogger().severe("An error occurred while sending the webhook: " + e.getMessage());
            }
        });

        webhookThread.start();
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
            this.title = title;
            return this;
        }

        public EmbedObject setDescription(final String description) {
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
            this.footer = new Footer(text, icon);
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

        private record Footer(String text, String iconUrl) {
        }

        private record Thumbnail(String url) {
        }

        private record Image(String url) {
        }

        private record Author(String name, String url, String iconUrl) {
        }

        private record Field(String name, String value, boolean inline) {
        }
    }

    private static class JSONObject {

        private final HashMap<String, Object> map = new HashMap<>();

        void put(final String key, final Object value) {
            if (value != null) {
                map.put(key, value);
            }
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            final Set<Map.Entry<String, Object>> entrySet = map.entrySet();
            builder.append("{");

            int i = 0;
            for (final Map.Entry<String, Object> entry : entrySet) {
                final Object val = entry.getValue();
                builder.append(quote(entry.getKey())).append(":");

                if (val instanceof String) {
                    builder.append(quote(String.valueOf(val)));
                } else if (val instanceof Integer) {
                    builder.append(Integer.valueOf(String.valueOf(val)));
                } else if (val instanceof Boolean) {
                    builder.append(val);
                } else if (val instanceof JSONObject) {
                    builder.append(val);
                } else if (val.getClass().isArray()) {
                    builder.append("[");
                    final int len = Array.getLength(val);
                    for (int j = 0; j < len; j++) {
                        builder.append(Array.get(val, j).toString()).append(j != len - 1 ? "," : "");
                    }
                    builder.append("]");
                }

                builder.append(++i == entrySet.size() ? "}" : ",");
            }

            return builder.toString();
        }

        private String quote(final String string) {
            return "\"" + string + "\"";
        }
    }
}
