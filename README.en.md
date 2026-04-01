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
  - [Floor Bar Settings (`elevator.floorbar`)](#floor-bar-settings-elevatorfloorbar)
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

| Action | Input | Effect |
|---|---|---|
| Up | Jump on floor block | Teleport to upper floor |
| Down | Sneak on floor block | Teleport to lower floor |

> **Tip**: You can customize floor and passable blocks in `config.yml`.

**Built-in Features**:
- Plays sound on up/down movement
- Shows current floor in BossBar
- Shows remaining cooldown time when cooldown is enabled

> **Tip**: All of these can be disabled or adjusted in `config.yml`.

---

## Configuration File

`plugins/SympleCometElevator/config.yml`

### Admin Commands

Players with admin permission (`simplecometelevator.admin`) can manage settings in-game.

- `/simplecometelevator reload` (`/scelevator reload`)
  - Reloads `config.yml` and applies changes immediately.
- `/simplecometelevator get <path>`
  - Displays the current value for a config path.
- `/simplecometelevator set <path> <value>`
  - Updates and saves the value, then applies it immediately.

**Examples**:

```mcfunction
/scelevator set elevator.cooldown.enabled true
/scelevator set elevator.cooldown.seconds 1.5
/scelevator set elevator.floor.base-blocks IRON_BLOCK,GOLD_BLOCK,$WOOLS
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

#### sound.up / sound.down
- `elevator.sound.up.enabled`
- `elevator.sound.up.type`
- `elevator.sound.up.volume`
- `elevator.sound.up.pitch`
- `elevator.sound.down.enabled`
- `elevator.sound.down.type`
- `elevator.sound.down.volume`
- `elevator.sound.down.pitch`

#### floorbar
- `elevator.floorbar.enabled`
- `elevator.floorbar.color`
- `elevator.floorbar.style`
- `elevator.floorbar.format`
- `elevator.floorbar.use-y-progress`

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

### Up Sound (`up`)

#### `enabled`
- **Type**: boolean
- **Default**: `true`

```yaml
enabled: true
```

#### `type`
- **Type**: string
- **Default**: `"minecraft:entity.experience_orb.pickup"`

```yaml
type: "minecraft:block.note_block.pling"
```

#### `volume`
- **Type**: decimal
- **Default**: `1.0`

```yaml
volume: 0.8
```

#### `pitch`
- **Type**: decimal
- **Default**: `0.69`

```yaml
pitch: 1.2
```

### Down Sound (`down`)

#### `enabled`
- **Type**: boolean
- **Default**: `true`

```yaml
enabled: true
```

#### `type`
- **Type**: string
- **Default**: `"minecraft:entity.experience_orb.pickup"`

```yaml
type: "minecraft:block.note_block.bass"
```

#### `volume`
- **Type**: decimal
- **Default**: `1.0`

```yaml
volume: 0.8
```

#### `pitch`
- **Type**: decimal
- **Default**: `0.56`

```yaml
pitch: 0.8
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
      type: "minecraft:entity.IRON_GOLEM_ATTACK"
      volume: 1.0
      pitch: 1.0
    down:
      enabled: true
      type: "minecraft:entity.IRON_GOLEM_ATTACK"
      volume: 1.0
      pitch: 1.0

  floorbar:
    enabled: true
    color: BLUE
    style: SOLID
    format: "§e{current}F / {total}F"
```

### Japanese Display Example

```yaml
elevator:
  cooldown:
    format: "§cクールダウン中: {time}秒"

  floorbar:
    format: "§a{current}階 / {total}階"
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

Plugin version: `1.0.0`
Author: [Hisui.A](https://github.com/stabery)
github repository: githubリポジトリ: https://github.com/stabery/SimpleCometElevator

- If you find bugs or have feature requests, please open an Issue on GitHub.
- Pull requests are welcome, especially performance improvements.

