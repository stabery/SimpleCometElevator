# SimpleCometElevator

**A simple and lightweight elevator plugin for Minecraft Paper.**

シンプルで軽量なMinecraft Paper用エレベータープラグイン。

---

## ⭐ Features / 特徴

- **Simple by design** — Easy to set up and start using immediately.
- **Built-in extras** — BossBar display, movement sounds, and cooldown out of the box.
- **Highly configurable** — Customize block types, display format, sounds, and more.

---

## ⏳ Quick Start / クイックスタート

### How to Build an Elevator

1. **Stack floor blocks vertically** with at least **2 empty/passable blocks above each one** — that's one floor.
   - Each elevator shaft should use the same block type.
   - Default floor blocks: most metal blocks (except copper), and wool blocks.

2. **Jump** on a floor block → move **up** ⬆

3. **Sneak** on a floor block → move **down** ⬇

### Minimum Requirements

- The floor block must be listed in `base-blocks`.
- Each floor must have at least `required-air` empty blocks above it (default: `2`).
- Those empty blocks must be listed in `passable-blocks`.

> You can view and edit all settings in `config.yml` inside the plugin folder.

---

## ⚡ Built-in Features / 機能

### BossBar Floor Display
- Shows current floor and total floors on screen.
- Customize color, style, and format.

### Sound Effects
- Different sounds for going up and going down.
- Adjust volume and pitch individually, or disable entirely.

### Cooldown
- Optional cooldown to prevent spam teleporting.
- Configurable duration and message format.

---

## ⚙️ Configuration / 設定

All settings are in `plugins/SimpleCometElevator/config.yml`.

### Floor Blocks
```yaml
elevator:
  floor:
    base-blocks:
      - IRON_BLOCK
      - GOLD_BLOCK
      - DIAMOND_BLOCK
      - EMERALD_BLOCK
      - NETHERITE_BLOCK
      - REDSTONE_BLOCK
      - LAPIS_BLOCK
      - $WOOLS        # Tag-based bulk selection is supported
    passable-blocks:
      - $AIR
      - $CARPETS
      # Blocks treated as empty space above floors
    required-air: 2          # Minimum empty blocks above each floor
    tolerance-height: 0.3   # How far off a floor block a player can be (in blocks)
    allow-mixed-blocks: false  # Allow different block types in one shaft
```

### Cooldown
```yaml
elevator:
  cooldown:
    enabled: false
    seconds: 1.0
    format: "§cCooldown: {time}s"
```

### Sounds
```yaml
elevator:
  sound:
    up:
      enabled: true
      type: "minecraft:entity.experience_orb.pickup"
      volume: 1.0
      pitch: 0.69
    down:
      enabled: true
      type: "minecraft:entity.experience_orb.pickup"
      volume: 1.0
      pitch: 0.56
```

### BossBar
```yaml
elevator:
  floorbar:
    enabled: true
    color: BLUE          # BLUE, GREEN, PINK, PURPLE, RED, WHITE, YELLOW
    style: SOLID         # SOLID, SEGMENTED_6, SEGMENTED_10, SEGMENTED_12, SEGMENTED_20
    format: "§e{current}F / {total}F"
    use-y-progress: false  # Use Y-coordinate range for progress bar instead of floor count
```

---

## 📋 Admin Commands / 管理コマンド

**Permission:** `simplecometelevator.admin`

| Command | Description |
|---------|-------------|
| `/scelevator reload` | Reload `config.yml` |
| `/scelevator get <path>` | Display a config value |
| `/scelevator set <path> <value>` | Update and apply a config value |

**Examples:**
```
/scelevator set elevator.cooldown.enabled true
/scelevator set elevator.cooldown.seconds 1.5
/scelevator reload
```

---

## 🎯 Compatibility / 対応バージョン

- **Minecraft**: 1.21.x
- **Server software**: Paper
- **Java**: 21+

---

## 🛠️ Advanced Settings / 高度な設定

### Tag Notation (`$`)
Use Minecraft block tags for bulk selection:
```yaml
base-blocks:
  - $WOOLS       # All wool blocks
  - $BUTTONS     # All button types
  - IRON_BLOCK   # Individual block still works
```
Available tags: [Spigot Javadocs](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Tag.html)

### Exclusion Notation (`!`)
Exclude specific blocks or tags:
```yaml
passable-blocks:
  - $TRAPDOORS
  - !IRON_TRAPDOOR  # Exclude iron trapdoors specifically
```

---

## 💡 Tips

- After changing `config.yml`, always run `/scelevator reload` to apply changes.
- Keep `tolerance-height` below `0.5` to avoid false detections.
- For very tall buildings, consider splitting into multiple elevator shafts by purpose (e.g., express / local).
- Use different sounds for up and down for better feedback.

---

## 🐛 Troubleshooting / トラブルシューティング

**Elevator not working?** Check the following:

1. Is the floor block listed in `base-blocks`?
2. Are there at least `required-air` empty blocks above the floor? (default: `2`)
3. Are the blocks in that space listed in `passable-blocks`?
4. Did you run `/scelevator reload` after changing `config.yml`?

---

## 📖 Full Documentation / 詳細ドキュメント

- **English full guide**: [README.en.md](https://github.com/stabery/SimpleCometElevator/blob/main/README.en.md)
- **日本語ガイドもあるわよ！**: [README.ja.md](https://github.com/stabery/SimpleCometElevator/blob/main/README.ja.md)

---

**Author**: [Hisui.A](https://github.com/stabery)  
**Source**: https://github.com/stabery/SimpleCometElevator

Bug reports and feature requests → [GitHub Issues](https://github.com/stabery/SimpleCometElevator/issues)  
Pull requests are welcome!
