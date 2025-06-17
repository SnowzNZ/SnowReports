# SnowReports

Lightweight, customizable player reporting plugin.

[![Paper](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/paper_vector.svg)](https://papermc.io/)
[![Modrinth](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg)](https://modrinth.com/plugin/snowreports)

## ✨ Features

- **Report Reason Presets** with auto-completion
- **Multiple Storage Methods**
- **Intuitive GUI** for viewing and managing reports
- **Discord Integration** via webhooks
- **Automatic Update Checking**
- **Highly Customizable** settings and messages
- **Cross-server** support (while using a remote storage method)

## 📝 Usage

### Commands

| Command                     | Description                                 | Permission                         |
|-----------------------------|---------------------------------------------|------------------------------------|
| `/deletereport <id>`        | Delete a specific report                    | `snowreports.command.deletereport` |
| `/myreports [page]`         | View reports you've made                    | `snowreports.command.myreports`    |
| `/report <player> [reason]` | Report a player                             | `snowreports.command.report`       |
| `/reports [player/page]`    | View all reports or player-specific reports | `snowreports.command.reports`      |
| `/setstatus <id> <status>`  | Set the status of a report                  | `snowreports.command.setstatus`    |

### Permissions

| Permission                      | Description                                |
|---------------------------------|--------------------------------------------|
| `snowreports.command.<command>` | Give permisson to a certain command        |
| `snowreports.alerts`            | Receive an alert when a player is reported |
| `snowreports.bypass.cooldown`   | Bypass the report cooldown                 |
| `snowreports.bypass.report`     | Makes a player immune to reports           |

## 📥 Installation

1. Download the latest release from [Modrinth](https://modrinth.com/plugin/snowreports)
   or [Hangar](https://hangar.papermc.io/Snowz/SnowReports)
2. Place the JAR file in your server's `plugins` folder
3. Restart the server
4. Edit the configuration as needed

## 📊 Support

If you encounter any issues or have feature requests:

- Open an issue on our GitHub repository
- Join our support Discord

## 📜 License

SnowReports is licensed under the [GPL-3.0 license](LICENSE).
