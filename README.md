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
/scelevator get elevator.cooldown.enabled
/scelevator set elevator.cooldown.enabled true
/scelevator set elevator.cooldown.seconds 1.5
...
```

### Main Config Keys

- `elevator.floor.base-blocks`: elevator floor blocks
- `elevator.floor.passable-blocks`: blocks treated as passable space
- `elevator.cooldown.enabled` / `seconds`: anti-spam cooldown
- `elevator.sound.up.*` / `elevator.sound.down.*`: movement sounds
- `elevator.floorbar.format`: floor display format

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

- ジャンプで上階へ
- スニークで下階へ
- BossBar表示、サウンド、クールダウンを設定可能

### 3分で使い始める

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
/scelevator get elevator.cooldown.enabled
/scelevator set elevator.cooldown.enabled true
/scelevator set elevator.cooldown.seconds 1.5
...
```

### 調整できる主な項目

- `elevator.floor.base-blocks`: エレベーターの床に使えるブロック
- `elevator.floor.passable-blocks`: 空きスペースとみなすブロック
- `elevator.cooldown.enabled` / `seconds`: 連打防止
- `elevator.sound.up.*` / `elevator.sound.down.*`: 上下の音
- `elevator.floorbar.format`: 階数表示フォーマット

### うまく動かないとき

- 床ブロックが `base-blocks` に入っているものか
- 床の上が `required-air` 分空いているか
- 上のブロックが `passable-blocks` に含まれているか
- 設定変更後に `/scelevator reload` したか

## 詳細ドキュメント

- 日本語で全設定を確認する: [`README.ja.md`](README.ja.md)
- English full guide: [`README.en.md`](README.en.md)
