# Setup (MusicBot community fork)

This repo is a maintained fork of JMusicBot. These instructions match the current codebase (JDA 5+).

## 1) Create a Discord bot + enable intents

In the Discord Developer Portal, for your bot:

- Enable **MESSAGE CONTENT INTENT** (required for prefix commands like `!play`)
- Enable **SERVER MEMBERS INTENT** only if you need it (this bot doesn’t require it for basic usage)

Invite the bot to your server with permissions to read/send messages and connect/speak in voice.

## 2) Java

Install Java 17+ (Java 21 LTS recommended).

## 3) Download a jar

- Go to Releases: https://github.com/abdmohrat/MusicBot/releases
- Download the `*-All.jar` file

## 4) Configure

Create or edit `config.txt` next to the jar:

- `token` = your bot token
- `owner` = your Discord user ID
- `prefix` = e.g. `!`
- If you changed the playlists location: `playlistsfolder`

Tip: Set `updatealerts = false` if you don’t want update messages.

### YouTube age-restricted content (optional)

Some YouTube / YouTube Music tracks are age-restricted and won't play without authentication.

- Run the owner command `!auth` and follow the DM instructions (use a burner/secondary Google account).
- The bot will store a refresh token in `config.txt` under:
  - `youtube.oauth2.enabled = true`
  - `youtube.oauth2.refreshToken = "<token>"`

### YouTube "sig function" errors (optional)

If you see errors like `Must find sig function`, YouTube changed their player script and local deciphering broke.

- Run a remote cipher server (see https://github.com/lavalink-devs/youtube-source#remote-cipher)
- Configure in `config.txt`:
  - `youtube.remoteCipher.url`
  - `youtube.remoteCipher.password`
  - `youtube.remoteCipher.userAgent`

## 5) Run

From the folder with the jar:

```bash
java -jar JMusicBot-<version>-All.jar
```

## 6) Local playlists

Playlists are plain text files in the playlists folder (default `Playlists/`).

- File name: `Playlists/<name>.txt`
- Each line: a URL or local file path

Commands:

- List playlists: `!playlists`
- Play playlist: `!play playlist <name>` (or `!play pl <name>`)
