# PyAutoPickup - Minecraft Auto Pickup Plugin
![Version](https://img.shields.io/badge/version-1.0-brightgreen)
![Minecraft](https://img.shields.io/badge/Minecraft-1.20.x-blue)

Automatically collects block & mob drops!

## 👀 Overview
PyAutoPickup is a minecraft plugin designed to automatically collect block and mob drops directly into the player's inventory. 
Whether you're mining, farming, or fighting mobs, this plugin ensures that all drops go straight to your inventory.

## 🚀 Features
- 🟢 **Automatic Block Drop Pickup:** Instantly collect all block drops into your inventory.
- 🟢 **Automatic Mob Drop Pickup:** Automatically pick up mob drops after killing entities.
- 🟢 **Compatibility with Other Plugins:** Supports custom drops added by other plugins like RoseLoot and more.
- 🟢 **Full Inventory Handling:** If your inventory is full, the drops will fall naturally on the ground, and you'll receive a customizable notification.

## ⚙️ Configuration

```yaml
plugin-enabled: true

autopickup:
  blocks: true
  mob-drops: true
  works-in-creative: false

full-inventory:
  chat-message: true
  chat-message-format: "&cYour inventory is full! Items have been dropped on the ground."
  title-message: true
  title-message-format: "&cFULL INVENTORY!"
  subtitle-message-format: "&7You don't have any more space!"
```

## 🛠 How It Works
![PyAutoPickup Demo](https://imgur.com/a/SSVnKU1.gif)

## 🤝 Support & Feedback
If you encounter any issues or have suggestions for future features, feel free to reach out via discord (666pyke)
or leave a comment on the plugin page.