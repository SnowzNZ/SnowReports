# SnowReports

Lightweight, customizable player reporting plugin.

[![Paper](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/paper_vector.svg)](https://papermc.io/)
[![Modrinth](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg)](https://modrinth.com/plugin/snowreports)

## Features

- **Report Reason Presets** with auto-completion
- **Multiple Storage Methods**
- **Intuitive GUI** for viewing and managing reports
- **Discord Integration** via webhooks
- **Automatic Update Checking**
- **Highly Customizable** settings and messages
- **Cross-server** support (while using a remote storage method)

## Usage

### Commands

| Command                              | Description                                 | Permission                         |
|--------------------------------------|---------------------------------------------|------------------------------------|
| `/snowreports`                       | Main command                                | `snowreports.command.snowreports`  |
| `/deletereport <id> [confirm]`       | Delete a specific report                    | `snowreports.command.deletereport` |
| `/myreports [page]`                  | View reports you've made                    | `snowreports.command.myreports`    |
| `/report <player> [reason]`          | Report a player                             | `snowreports.command.report`       |
| `/reports [player/page]`             | View all reports or player-specific reports | `snowreports.command.reports`      |
| `/setstatus <id> <status> [confirm]` | Set the status of a report                  | `snowreports.command.setstatus`    |

### Permissions

| Permission                      | Description                                         |
|---------------------------------|-----------------------------------------------------|
| `snowreports.command.<command>` | Give permission to a certain command                |
| `snowreports.alerts`            | Receive an alert when a player is reported          |
| `snowreports.bypass.cooldown`   | Bypass the report cooldown                          |
| `snowreports.bypass.report`     | Makes a player immune to reports                    |
| `snowreports.update`            | Get notified if an update is available when joining |

### Placeholders

Requires [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)

| Placeholder                        | Description                         |
|------------------------------------|-------------------------------------|
| `%snowreports_reports_total%`      | Total number of reports             |
| `%snowreports_reports_open%`       | Total number of open reports        |
| `%snowreports_reports_inprogress%` | Total number of in progress reports |
| `%snowreports_reports_resolved%`   | Total number of resolved reports    |

## License

SnowReports is licensed under the [GPL-3.0 license](LICENSE).
