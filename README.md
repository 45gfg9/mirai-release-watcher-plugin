# GitHub Release Watcher

[![CodeFactor](https://www.codefactor.io/repository/github/45gfg9/mirai-release-watcher-plugin/badge)](https://www.codefactor.io/repository/github/45gfg9/mirai-release-watcher-plugin)

*Finally, some good f\*\*king network IOs.*

This is a [mirai-console](https://github.com/mamoe/mirai-console) plugin.

With this plugin, you can watch all your favorite repos' releases and get notified right in your fans groups. (

## Usage
* Generate a [GitHub Personal Access Token](https://help.github.com/en/github/authenticating-to-github/creating-a-personal-access-token-for-the-command-line).
  * Only minimal permission is required (PUBLIC_ACCESS). That means when generating you can leave all checkboxes blank.
  * Of course, more privileges allow you to have a wider access to your own (private) repositories.
* Download & install the plugin.
* Use command `grw set token <Token>` to set your token
* Use command `grw start` to start running.
* In any group, anyone can send...
    * `/watch-list` to show repos with their latest release tag version that group is watching.
    * `/watch-release <repos...>` to watch those repos for that group.
    * `/unwatch-release <repos...>` to remove those repos from that group's watch list.

*`<repos...>` accepts 3 formats. See JavaDoc of [RepoId.java](./src/main/java/net/im45/bot/watcher/gh/RepoId.java) for examples.*

## Settings & Commands for Manager
* `grw start`
    * start running.
* `grw stop`
    * stop running. 
* `grw set token <Token>`
    * set Personal access token.
* `grw set interval <ms>`
    * set interval between two fetches, in milliseconds.
    * default: `30000`
* `grw set timeout <ms>`
    * set network IO read timeout, in milliseconds.
    * default: `15000`
* `grw set autostart <true|false>` 
    * set whether plugin should auto start running on the startup.
    * *any non-`"true"` arg will be treated as `false`*.
    * default: `false`
* `grw set bot <bot QQ>`
    * used to update group outputs.
    * **This command should be run each time your console restarts, in order to update outputs so groups will be able to receive messages about new releases.**
* `grw dump`
    * dump all data to console (***for debug purpose only***)

## Contributing
If you find a bug, please do not hesitate to open an Issue.

Pull Request is also welcomed.
