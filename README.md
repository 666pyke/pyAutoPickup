# pyAutoPickup

<img width="1024" height="512" alt="pyAutoPickup banner" src="https://github.com/user-attachments/assets/66cee316-adc6-480c-bd07-f6f00f4bc3aa" />

![Version](https://img.shields.io/badge/version-1.1-brightgreen)
![Minecraft](https://img.shields.io/badge/Minecraft-1.20.x-blue)
![Storage](https://img.shields.io/badge/storage-SQLite-ff8fd2)

A lightweight Minecraft plugin that automatically collects block drops, mob drops, and custom loot into the player's inventory.

## Overview

pyAutoPickup is made for servers that want simple, reliable auto pickup without breaking custom drop systems.

Instead of only relying on vanilla block break drops, the plugin listens for spawned item drops. This makes it work better with custom loot plugins such as RoseLoot and similar systems.

## Features

- Automatic block drop pickup
- Automatic mob drop pickup
- Custom drops support
- Player toggle command
- Full-inventory message toggle
- SQLite storage for player preferences
- Hex color support for messages
- Optional bStats support
- World blacklist support
- Advanced compatibility settings for item spawn handling

## Commands

```txt
/pyautopickup
/pyautopickup toggle
/pyautopickup msgtoggle
/pyautopickup reload
