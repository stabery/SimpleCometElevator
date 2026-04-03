# SimpleCometElevator Guide (English, Detailed)

> For a quick overview, start with [`README.md`](README.md).

SimpleCometElevator is a plugin for Minecraft (Paper).

Supported version: **Paper / Minecraft 1.21.x**

It is a lightweight, minimal elevator plugin focused on simple setup and use.

## Table of Contents

- [In-game Usage](#in-game-usage)
- [Configuration File](#configuration-file)
  - [Elevator Floor Settings (`elevator.floor`)](#elevator-floor-settings-elevatorfloor)
  - [Cooldown Settings (`elevator.cooldown`)](#cooldown-settings-elevatorcooldown)
  - [Sound Settings (`elevator.sound`)](#sound-settings-elevatorsound)
  - [Command Sound Settings (`elevator.sound.command`)](#command-sound-settings-elevatorsoundcommand)
  - [Floor Bar Settings (`elevator.floorbar`)](#floor-bar-settings-elevatorfloorbar)
  - [Title Settings (`elevator.title`)](#title-settings-elevatortitle)
  - [ActionBar Settings (`elevator.actionbar`)](#actionbar-settings-elevatoractionbar)
  - [Messages Settings (`messages`)](#messages-settings-messages)
- [Special Notation](#special-notation)
- [Configuration Examples](#configuration-examples)
- [Tips](#tips)
- [Notes](#notes)
- [Other](#other)

---

## In-game Usage

### Creating an Elevator

Place the same floor blocks vertically with enough space between them.

**Requirements**:
- Multiple floor blocks of the same type (or mixed types if configured) are stacked vertically.
- Above each floor block, there is enough empty space for a player (`required-air`).
- Blocks in that space must be passable (`passable-blocks`).

**Example:**

```
[AIR]
[AIR]
[IRON_BLOCK] (floor)
[AIR]
[AIR]
[IRON_BLOCK] (floor)
```

```
[AIR]
[PRESSURE_PLATE]
[LAPIS_BLOCK] (floor)
[OAK_PLANKS] (optional ceiling block)
[AIR]
[PRESSURE_PLATE]
[LAPIS_BLOCK] (floor)
```

> **Tip**: Different floor block types are recognized as separate elevators by default.
> Example: if 1F/3F use `IRON_BLOCK` and 2F/4F use `GOLD_BLOCK`, each pair is treated as an independent elevator.
> You can change this behavior with `allow-mixed-blocks`.

### Elevator Controls

| Action | Input                | Effect                  |
|--------|----------------------|-------------------------|
| Up     | Jump on floor block  | Teleport to upper floor |
| Down   | Sneak on floor block | Teleport to lower floor |

> **Tip**: You can customize floor and passable blocks in `config.yml`.

**Built-in Features**:
- Plays sound on up/down movement
- Shows current floor in BossBar
- Shows remaining cooldown time when cooldown is enabled

> **Tip**: All of these can be disabled or adjusted in `config.yml`.

---

## Configuration File

`plugins/SimpleCometElevator/user-settings/config.yml`

### Admin Commands

Players with admin permission (`simplecometelevator.admin`) can manage settings in-game.

- `/simplecometelevator reload` (`/scelevator reload`)
  - Reloads `config.yml` and applies changes immediately.
- `/simplecometelevator help` (`/scelevator help`)
  - Shows the admin command help.
- `/simplecometelevator get <path>`
  - Displays the current value for a config path.
- `/simplecometelevator set <path> <value>`
  - Updates and saves the value, then applies it immediately.
- `/simplecometelevator reset <path>`
  - Resets a specific path to its default value and applies it immediately.
- `/simplecometelevator reset all` -> `/simplecometelevator reset all confirm`
  - Resets all editable paths (requires confirm within 30 seconds).
- `/simplecometelevator info` (`/scelevator info`)
  - Shows plugin version, author, and repository links.

### Move Command

Players with move permission (`simplecometelevator.move`) can teleport to a specific floor directly.

- `/simplecometelevator move <floor>` (`/scelevator move <floor>`)
  - Teleports to the specified floor number while standing on an elevator.
  - Tab completion shows available floor numbers.

**Examples**:

```mcfunction
/scelevator set elevator.cooldown.enabled true
/scelevator set elevator.cooldown.seconds 1.5
/scelevator set elevator.floor.base-blocks IRON_BLOCK,GOLD_BLOCK,$WOOLS
/scelevator set elevator.sound.up.entries {minecraft:entity.experience_orb.pickup,1.0,0.69,0}
/scelevator set elevator.sound.up.entries {minecraft:block.note_block.pling,1.0,1.2,0}, {minecraft:entity.experience_orb.pickup,0.5,1.5,200}
/scelevator move 3
/scelevator reload
```

### Paths Editable via `set`

#### floor
- `elevator.floor.base-blocks`
- `elevator.floor.passable-blocks`
- `elevator.floor.required-air`
- `elevator.floor.tolerance-height`
- `elevator.floor.allow-mixed-blocks`

#### cooldown
- `elevator.cooldown.enabled`
- `elevator.cooldown.seconds`
- `elevator.cooldown.format`

#### command
- `elevator.command.move.enabled`

#### sound.up / sound.down
- `elevator.sound.up.enabled`
- `elevator.sound.up.entries` (sound entries list)
- `elevator.sound.down.enabled`
- `elevator.sound.down.entries` (sound entries list)
- `elevator.sound.command.up.enabled`
- `elevator.sound.command.up.entries` (entries for `/scelevator move` going up)
- `elevator.sound.command.down.enabled`
- `elevator.sound.command.down.entries` (entries for `/scelevator move` going down)

#### `entries` path command syntax

Use `{type,volume,pitch,delayMs}` format for each entry.  
Separate multiple entries with `, ` to play sounds **simultaneously or in sequence**.

```
/scelevator set <path> {type,volume,pitch,delay} [, {type,volume,pitch,delay} ...]
```

| Field | Type | Description |
|---|---|---|
| `type` | string | Minecraft sound ID (e.g. `minecraft:entity.experience_orb.pickup`) |
| `volume` | decimal | Volume (e.g. `1.0`) |
| `pitch` | decimal | Pitch (e.g. `0.69`) |
| `delay` | integer | Delay in milliseconds (`0` = immediate) |

**Command examples**:
```mcfunction
# Set a single up-sound entry
/scelevator set elevator.sound.up.entries {minecraft:entity.experience_orb.pickup,1.0,0.69,0}

# Two entries played simultaneously
/scelevator set elevator.sound.up.entries {minecraft:block.note_block.pling,1.0,1.2,0}, {minecraft:ui.button.click,0.8,1.0,0}

# Two entries played in sequence (200ms apart)
/scelevator set elevator.sound.up.entries {minecraft:block.note_block.pling,1.0,1.2,0}, {minecraft:entity.experience_orb.pickup,0.5,1.5,200}

# Set down-sound entry
/scelevator set elevator.sound.down.entries {minecraft:entity.experience_orb.pickup,1.0,0.56,0}

# Set command-move up-sound
/scelevator set elevator.sound.command.up.entries {minecraft:ui.button.click,1.0,1.0,0}

# Check current up-sound entries
/scelevator get elevator.sound.up.entries
```

> **Tip**: Entries with different `delay` values play in sequence; entries with the same `delay` (or both `0`) play simultaneously.  
> See the [Minecraft Wiki](https://minecraft.wiki/w/Sounds.json) for available sound IDs.

#### floorbar
- `elevator.floorbar.enabled`
- `elevator.floorbar.color`
- `elevator.floorbar.style`
- `elevator.floorbar.format`
- `elevator.floorbar.use-y-progress`

#### title
- `elevator.title.up.enabled`
- `elevator.title.up.title`
- `elevator.title.up.subtitle`
- `elevator.title.up.fade-in`
- `elevator.title.up.stay`
- `elevator.title.up.fade-out`
- `elevator.title.down.enabled`
- `elevator.title.down.title`
- `elevator.title.down.subtitle`
- `elevator.title.down.fade-in`
- `elevator.title.down.stay`
- `elevator.title.down.fade-out`
- `elevator.title.command.up.enabled`
- `elevator.title.command.up.title`
- `elevator.title.command.up.subtitle`
- `elevator.title.command.up.fade-in`
- `elevator.title.command.up.stay`
- `elevator.title.command.up.fade-out`
- `elevator.title.command.down.enabled`
- `elevator.title.command.down.title`
- `elevator.title.command.down.subtitle`
- `elevator.title.command.down.fade-in`
- `elevator.title.command.down.stay`
- `elevator.title.command.down.fade-out`

#### actionbar
- `elevator.actionbar.up.enabled`
- `elevator.actionbar.up.format`
- `elevator.actionbar.down.enabled`
- `elevator.actionbar.down.format`
- `elevator.actionbar.command.up.enabled`
- `elevator.actionbar.command.up.format`
- `elevator.actionbar.command.down.enabled`
- `elevator.actionbar.command.down.format`

#### messages
- `messages.language`

---

## Elevator Floor Settings (`elevator.floor`)

### `base-blocks`
- **Type**: list of strings
- **Default**: `[IRON_BLOCK, GOLD_BLOCK, REDSTONE_BLOCK, EMERALD_BLOCK, LAPIS_BLOCK, DIAMOND_BLOCK, NETHERITE_BLOCK, $WOOLS]`
- **Description**: Blocks that can act as elevator floors.

```yaml
base-blocks:
  - IRON_BLOCK
  - GOLD_BLOCK
  - $WOOLS
```

### `passable-blocks`
- **Type**: list of strings
- **Default**: `[$AIR, WATER, $BUTTONS, $SIGNS, $BANNERS, $CARPETS, $FLOWERS, $SMALL_FLOWERS, $TALL_FLOWERS, $SAPLINGS, $CROPS, $RAILS, $PRESSURE_PLATES, $TRAPDOORS, $DOORS, $CORAL_FANS, $SEA_GRASS, $KELP]`
- **Description**: Blocks considered passable above floor blocks.

```yaml
passable-blocks:
  - $AIR
  - WATER
  - $CARPETS
```

### `required-air`
- **Type**: integer
- **Default**: `2`
- **Description**: Minimum vertical air/passable blocks required above a floor block.

```yaml
required-air: 2
```

### `tolerance-height`
- **Type**: decimal
- **Default**: `0.2`
- **Description**: Height tolerance (in blocks) when checking if a player is on a floor block.

```yaml
tolerance-height: 0.2
```

### `allow-mixed-blocks`
- **Type**: boolean
- **Default**: `false`
- **Description**: If `true`, different floor block types in `base-blocks` can belong to the same elevator.

```yaml
allow-mixed-blocks: true
```

---

## Cooldown Settings (`elevator.cooldown`)

### `enabled`
- **Type**: boolean
- **Default**: `false`

```yaml
enabled: true
```

### `seconds`
- **Type**: decimal
- **Default**: `1`

```yaml
seconds: 1.5
```

### `format`
- **Type**: string
- **Default**: `"§cCooldown: {time}s"`

```yaml
format: "§eCooldown: {time}s"
```

---

## Sound Settings (`elevator.sound`)

Sound settings use an **entries list** format. Each direction supports multiple sound entries played simultaneously or with a delay.

### Up Sound (`up`)

#### `enabled`
- **Type**: boolean
- **Default**: `true`

```yaml
enabled: true
```

#### `broadcast`
- **Type**: boolean
- **Default**: `false`
- **Description**: `true` plays sound in the world (audible to nearby players), `false` plays only for the moved player.
- **Note**: `broadcast` is not editable via `/scelevator set`; edit `config.yml` directly.

#### `entries`
- **Type**: list of sound entries
- **Description**: Each entry has the following fields:
  - `type` — Minecraft sound ID (e.g. `"minecraft:entity.experience_orb.pickup"`)
  - `volume` — Volume (default: `1.0`)
  - `pitch` — Pitch (default: `1.0`)
  - `delay` — Delay in milliseconds before playing (default: `0`)

```yaml
entries:
  - type: "minecraft:entity.experience_orb.pickup"
    volume: 1.0
    pitch: 0.69
    delay: 0
```

> **Tip**: Multiple entries can be added to play several sounds at once or in sequence.

```yaml
entries:
  - type: "minecraft:block.note_block.pling"
    volume: 1.0
    pitch: 1.2
    delay: 0
  - type: "minecraft:entity.experience_orb.pickup"
    volume: 0.5
    pitch: 1.5
    delay: 200
```

### Down Sound (`down`)

#### `enabled`
- **Type**: boolean
- **Default**: `true`

#### `broadcast`
- **Type**: boolean
- **Default**: `false`
- **Description**: `true` plays in-world, `false` plays only for the moved player.
- **Note**: `broadcast` cannot be changed via `/scelevator set`.

```yaml
enabled: true
```

#### `entries`

```yaml
entries:
  - type: "minecraft:entity.experience_orb.pickup"
    volume: 1.0
    pitch: 0.56
    delay: 0
```

---

## Command Sound Settings (`elevator.sound.command`)

Sounds played when using the `/scelevator move <floor>` command.
Same structure as the standard sound settings above.

### `elevator.sound.command.up`

#### `enabled`
- **Type**: boolean
- **Default**: `true`

#### `entries`

```yaml
command:
  up:
    enabled: true
    entries:
      - type: "minecraft:entity.experience_orb.pickup"
        volume: 1.0
        pitch: 0.69
        delay: 0
  down:
    enabled: true
    entries:
      - type: "minecraft:entity.experience_orb.pickup"
        volume: 1.0
        pitch: 0.56
        delay: 0
```

---

## Floor Bar Settings (`elevator.floorbar`)

### `enabled`
- **Type**: boolean
- **Default**: `true`

### `color`
- **Type**: string
- **Default**: `"BLUE"`
- **Available**: `BLUE`, `GREEN`, `PINK`, `PURPLE`, `RED`, `WHITE`, `YELLOW`

### `style`
- **Type**: string
- **Default**: `"SOLID"`
- **Available**: `SOLID`, `SEGMENTED_6`, `SEGMENTED_10`, `SEGMENTED_12`, `SEGMENTED_20`

### `format`
- **Type**: string
- **Default**: `"§e{current}F / {total}F"`

### `use-y-progress`
- **Type**: boolean
- **Default**: `false`
- **Description**: If enabled, BossBar progress is based on Y-coordinate range instead of floor count.

---

## Title Settings (`elevator.title`)

You can show title notifications for normal movement and `/scelevator move`.

### `up` / `down`
- `enabled`
- `title`
- `subtitle`
- `fade-in` / `stay` / `fade-out` (ticks)

### `command.up` / `command.down`
- Same structure as `up` / `down`.
- If not explicitly set, command title values fall back to normal movement title values.

```yaml
title:
  up:
    enabled: true
    title: "§e↑UP↑"
    subtitle: "§7{current}F / {total}F"
    fade-in: 10
    stay: 40
    fade-out: 10
```

---

## ActionBar Settings (`elevator.actionbar`)

Shows a short floor notification in the action bar when moving.

### `up` / `down` / `command.up` / `command.down`
- `enabled`
- `format` (`{current}` and `{total}` supported)

```yaml
actionbar:
  up:
    enabled: true
    format: "§e↑ {current}F / {total}F"
```

---

## Messages Settings (`messages`)

### `language`
- **Type**: string
- **Default**: `"ja"`
- **Available**: `ja`, `en`
- **Description**: Language used for in-game command messages.

```yaml
messages:
  language: en
```

> **Note**: Message text itself can be customized by editing `messages_ja.yml` or `messages_en.yml` in the `user-settings` folder.

---

## Special Notation

### Tags (`$`)

You can use Minecraft block tags by adding `$` before the tag name.
See available tags:
<https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Tag.html>

**Examples:**
- `$AIR`
- `$BUTTONS`
- `$CARPETS`
- `$WOOLS`
- `$SIGNS`

### Exclusion (`!`)

Prefix a block or tag with `!` to exclude it.

```yaml
passable-blocks:
  - $TRAPDOORS
  - !IRON_TRAPDOOR
```

```yaml
base-blocks:
  - $DOORS
  - !$WOODEN_DOORS
```

### Formatting Codes (`§`)

Messages can include Minecraft formatting codes (`§0`-`§f`, `§k`-`§r`).

---

## Configuration Examples

### Minimal Setup

```yaml
elevator:
  floor:
    base-blocks:
      - IRON_BLOCK
    passable-blocks:
      - $AIR
    required-air: 2
    tolerance-height: 0.2

  cooldown:
    enabled: false

  sound:
    up:
      enabled: true
      entries:
        - type: "minecraft:entity.experience_orb.pickup"
          volume: 1.0
          pitch: 0.69
          delay: 0
    down:
      enabled: true
      entries:
        - type: "minecraft:entity.experience_orb.pickup"
          volume: 1.0
          pitch: 0.56
          delay: 0

  floorbar:
    enabled: true
    color: BLUE
    style: SOLID
    format: "§e{current}F / {total}F"

messages:
  language: en
```

### English Display Example

```yaml
elevator:
  cooldown:
    format: "§cCooldown...: {time}sec"

  floorbar:
    format: "§aFloor {current} of {total}"

messages:
  language: en
```

### Tag-based Setup

```yaml
elevator:
  floor:
    base-blocks:
      - $WOOLS
      - IRON_BLOCK
    passable-blocks:
      - $AIR
      - $CARPETS
      - PRESSURE_PLATES
```

---

## Tips

- Keep `passable-blocks` as small as possible to reduce processing overhead.
- Use a small `tolerance-height` (recommended: under `0.5`) to avoid false detections.
- Use different sounds for up/down for better movement feedback.
- Use broad tags first, then refine with `!` exclusions.
- Avoid extremely high floor counts in one shaft; split by purpose when needed.

---

## Notes

1. After changing config, run `/simplecometelevator reload` (or `/scelevator reload`).
2. Invalid block/tag names are logged as warnings.
3. Sound IDs may vary by Minecraft version.
4. Available tags may vary by Minecraft version.

---

## Other

Plugin version: `1.1.0-1.21.x`
Author: [Hisui.A](https://github.com/stabery)
GitHub repository: https://github.com/stabery/SimpleCometElevator

- If you find bugs or have feature requests, please open an Issue on GitHub.
- Pull requests are welcome, especially performance improvements.

