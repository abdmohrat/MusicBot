<img align="right" src="https://i.imgur.com/zrE80HY.png" height="200" width="200">

# MusicBot (Community Fork)

[![Downloads](https://img.shields.io/github/downloads/abdmohrat/MusicBot/total.svg)](https://github.com/abdmohrat/MusicBot/releases/latest)
[![Stars](https://img.shields.io/github/stars/abdmohrat/MusicBot.svg)](https://github.com/abdmohrat/MusicBot/stargazers)
[![Release](https://img.shields.io/github/release/abdmohrat/MusicBot.svg)](https://github.com/abdmohrat/MusicBot/releases/latest)
[![License](https://img.shields.io/github/license/abdmohrat/MusicBot.svg)](https://github.com/abdmohrat/MusicBot/blob/master/LICENSE)
[![Build](https://github.com/abdmohrat/MusicBot/actions/workflows/build.yml/badge.svg)](https://github.com/abdmohrat/MusicBot/actions/workflows/build.yml)
[![Release](https://github.com/abdmohrat/MusicBot/actions/workflows/release.yml/badge.svg)](https://github.com/abdmohrat/MusicBot/actions/workflows/release.yml)

This is a maintained community fork of **JMusicBot** with updated dependencies (JDA 5+, newer Lavaplayer/YouTube) and ongoing maintenance.

## Features
  * Easy to run (just make sure Java is installed, and run!)
  * Fast loading of songs
  * No external keys needed (besides a Discord Bot token)
  * Smooth playback
  * Server-specific setup for the "DJ" role that can moderate the music
  * Clean and beautiful menus
  * Supports many sites, including Youtube, Soundcloud, and more
  * Supports many online radio/streams
  * Supports local files
  * Playlist support (both web/youtube, and local)

## Supported sources and formats
JMusicBot supports all sources and formats supported by [lavaplayer](https://github.com/sedmelluq/lavaplayer#supported-formats):
### Sources
  * YouTube
  * SoundCloud
  * Bandcamp
  * Vimeo
  * Twitch streams
  * Local files
  * HTTP URLs
### Formats
  * MP3
  * FLAC
  * WAV
  * Matroska/WebM (AAC, Opus or Vorbis codecs)
  * MP4/M4A (AAC codec)
  * OGG streams (Opus, Vorbis and FLAC codecs)
  * AAC streams
  * Stream playlists (M3U and PLS)

## Example
![Loading Example...](https://i.imgur.com/kVtTKvS.gif)

## Setup
See `docs/SETUP.md` for setup instructions for this fork (Discord intents, config, playlists, and running the jar).

## Questions/Suggestions/Bug Reports
Use GitHub Discussions for questions and GitHub Issues for bugs/feature requests:
- Discussions: https://github.com/abdmohrat/MusicBot/discussions
- Issues: https://github.com/abdmohrat/MusicBot/issues

## Editing
This bot (and the source code here) might not be easy to edit for inexperienced programmers. The main purpose of having the source public is to show the capabilities of the libraries, to allow others to understand how the bot works, and to allow those knowledgeable about java, JDA, and Discord bot development to contribute. There are many requirements and dependencies required to edit and compile it, and there will not be support provided for people looking to make changes on their own. Instead, consider making a feature request (see the above section). If you choose to make edits, please do so in accordance with the Apache 2.0 License.

## Releases
This fork publishes releases automatically when you push a git tag that starts with `v` (example: `v0.4.6`).
