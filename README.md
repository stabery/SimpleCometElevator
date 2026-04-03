# SimpleCometElevator Quick Guide / クイックガイド
https://github.com/stabery/SimpleCometElevator

[日本語版クイックガイド](#日本語クイックガイド)

---

## English (Quick Start)

This is a compact guide for first-time users.
For full settings and detailed explanations:

- English detailed guide: [`README.en.md`](README.en.md)

### What Is This Plugin?

A simple elevator plugin for Minecraft (Paper).

Supported version: **Paper / Minecraft 1.21.x**

- Jump to go up
- Sneak to go down
- Configurable BossBar, sounds, and cooldown

### Start in 3 Minutes

1. Put the plugin JAR in your server and start it
2. Stack floor blocks vertically (at least 2 air/passable blocks above each floor)
3. Jump or sneak on a floor block to move

### Minimum Placement Rules

- Floor blocks must be included in `elevator.floor.base-blocks`
- Space above each floor must satisfy `required-air` (default: `2`)
- Blocks in that space must be included in `passable-blocks`

### Admin Commands

Permission: `simplecometelevator.admin`

```mcfunction
/scelevator reload
/scelevator help
/scelevator get elevator.cooldown.enabled
/scelevator set elevator.cooldown.enabled true
/scelevator set elevator.cooldown.seconds 1.5
/scelevator set elevator.sound.up.entries {minecraft:entity.experience_orb.pickup,1.0,0.69,0}
/scelevator set elevator.sound.up.entries {minecraft:block.note_block.pling,1.0,1.2,0}, {minecraft:entity.experience_orb.pickup,0.5,1.5,200}
/scelevator reset elevator.cooldown.enabled
/scelevator reset all
/scelevator reset all confirm
/scelevator info
...
```

**`entries` path format**: `{soundId,volume,pitch,delayMs}` — separate multiple entries with `, ` for simultaneous or sequential playback.

`/scelevator reset all` requires a second confirmation command (`/scelevator reset all confirm`) within 30 seconds.

### Move Command

Permission: `simplecometelevator.move`

```mcfunction
/scelevator move <floor>
```

Move to a specific floor while standing on an elevator.

### Main Config Keys

- `elevator.floor.base-blocks`: elevator floor blocks
- `elevator.floor.passable-blocks`: blocks treated as passable space
- `elevator.cooldown.enabled` / `seconds`: anti-spam cooldown
- `elevator.command.move.enabled`: enable/disable `/scelevator move`
- `elevator.sound.up.entries` / `elevator.sound.down.entries`: movement sounds (entries list with `type`, `volume`, `pitch`, `delay`)
- `elevator.sound.command.up.entries` / `elevator.sound.command.down.entries`: sounds for `/scelevator move`
- `elevator.title.*` / `elevator.actionbar.*`: title and actionbar notifications on movement
- `elevator.floorbar.format`: floor display format
- `messages.language`: message language (`ja` / `en`)

### If It Does Not Work

- Confirm your floor block is in `base-blocks`
- Confirm enough space above floor (`required-air`)
- Confirm above blocks are in `passable-blocks`
- Run `/scelevator reload` after config changes

---

## 日本語（クイックガイド）

初見ユーザー向けの簡易版です。詳細な仕様や全設定は以下を参照してください。

- 日本語詳細ガイド: [`README.ja.md`](README.ja.md)

### どんなプラグイン？

Minecraft (Paper) 向けの、シンプルなエレベータープラグインです。

対応バージョン: **Paper / Minecraft 1.21.x**

- ジャンプで上階へ
- スニークで下階へ
- BossBar表示、サウンド、クールダウンを設定可能

### 3分スタート

1. サーバーにプラグインJARを配置して起動
2. 床ブロックを縦に複数配置（各床の上に空き2マス以上）
3. 床上でジャンプ/スニークして移動

### 最低限の設置ルール

- 床ブロックは `elevator.floor.base-blocks` に含まれるもの
- 各床の上に `required-air` 分の空間が必要（既定値 `2`）
- 空間内のブロックは `passable-blocks` に含まれる必要あり

### 設定用管理コマンド

権限: `simplecometelevator.admin`

```mcfunction
/scelevator reload
/scelevator help
/scelevator get elevator.cooldown.enabled
/scelevator set elevator.cooldown.enabled true
/scelevator set elevator.cooldown.seconds 1.5
/scelevator set elevator.sound.up.entries {minecraft:entity.experience_orb.pickup,1.0,0.69,0}
/scelevator set elevator.sound.up.entries {minecraft:block.note_block.pling,1.0,1.2,0}, {minecraft:entity.experience_orb.pickup,0.5,1.5,200}
/scelevator reset elevator.cooldown.enabled
/scelevator reset all
/scelevator reset all confirm
/scelevator info
...
```

**`entries` パスの書式**: `{サウンドID,volume,pitch,遅延ms}` — `, ` 区切りで複数指定すると同時再生・逐次再生に対応。

`/scelevator reset all` は 30 秒以内に `/scelevator reset all confirm` を実行した場合のみ確定します。

### 移動コマンド

権限: `simplecometelevator.move`

```mcfunction
/scelevator move <階数>
```

エレベーターに乗っている間、指定した階数に直接テレポートします。

### 調整できる主な項目

- `elevator.floor.base-blocks`: エレベーターの床に使えるブロック
- `elevator.floor.passable-blocks`: 空きスペースとみなすブロック
- `elevator.cooldown.enabled` / `seconds`: 連打防止
- `elevator.command.move.enabled`: `/scelevator move` の有効/無効
- `elevator.sound.up.entries` / `elevator.sound.down.entries`: 上下の音（`type` / `volume` / `pitch` / `delay` のリスト形式）
- `elevator.sound.command.up.entries` / `elevator.sound.command.down.entries`: `/scelevator move` 用の音
- `elevator.title.*` / `elevator.actionbar.*`: 移動時のタイトル/アクションバー通知
- `elevator.floorbar.format`: 階数表示フォーマット
- `messages.language`: メッセージ言語（`ja` / `en`）

### うまく動かないとき

- 床ブロックが `base-blocks` に入っているものか
- 床の上が `required-air` 分空いているか
- 上のブロックが `passable-blocks` に含まれているか
- 設定変更後にプラグインのリロードをしたか

## 詳細ドキュメント

- 日本語で全設定を確認する: [`README.ja.md`](README.ja.md)
- English full guide: [`README.en.md`](README.en.md)
