# SimpleCometElevator 使い方（詳細版）

> 最初に要点だけ確認したい場合は [`README.md`](README.md) を参照してください。

[English README](README.en.md)

SimpleCometElevator - Minecraft(Paper)用プラグイン

対応バージョン: **Paper / Minecraft 1.21.x**

最低限の機能を備えた至ってシンプルなエレベータープラグインです。

## 目次

- [ゲーム内での使い方](#ゲーム内での使い方)
- [設定ファイル](#設定ファイル)
  - [エレベーターフロア設定](#エレベーターフロア設定-elevatorfloor)
  - [クールダウン設定](#クールダウン設定-elevatorcooldown)
  - [サウンド設定](#サウンド設定-elevatorsound)
  - [コマンドサウンド設定](#コマンドサウンド設定-elevatorsoundcommand)
  - [フロアバー設定](#フロアバー設定-elevatorfloorbar)
  - [タイトル設定](#タイトル設定-elevatortitle)
  - [アクションバー設定](#アクションバー設定-elevatoractionbar)
  - [メッセージ設定](#メッセージ設定-messages)
- [特殊記法](#特殊記法)
- [設定例](#設定例)
- [Tips集](#tips集)
- [注意事項](#注意事項)
- [その他](#その他)

---

# ゲーム内での使い方

## エレベーターの設置

床ブロックを十分な空間を開けて縦に複数個配置することで、エレベーターを作成できます。

**エレベーターの設置条件**:
- **同じ種類**の床ブロック（base-blocks）が縦に複数個配置されていること
- 各床ブロックの上に、プレイヤーが立つための空きスペース（required-air）が空いていること
- 空きスペースには、通過可能なブロック（passable-blocks）だけが配置されていること

**例：**
```
[AIR]
[AIR]
[IRON_BLOCK] （床）
[AIR]
[AIR]
[IRON_BLOCK] （床）
```
```
[AIR]
[PRESSURE_PLATE]
[LAPIS_BLOCK] （床）
[OAK_PLANKS]  (天井など任意のブロック)
[AIR]
[PRESSURE_PLATE]
[LAPIS_BLOCK] （床）
```
>**Tips**:異なる種類の床ブロックを複数個縦に配置した場合、それぞれが**別のエレベーターとして認識**されます。
> <br>
> 例えば、1階と3階にIRON_BLOCK、2階と4階にGOLD_BLOCKを配置した場合、IRON_BLOCKの床は1階と3階で繋がったエレベーターになりますが、GOLD_BLOCKの床は2階と4階につながる独立したエレベーターになります。
> <br>
>configから別の種類でも同じエレベーターとして認識するようにできます。

## エレベーターの操作

| 動作 | 方法          | 効果        |
|----|-------------|-----------|
| 上昇 | 床ブロック上でジャンプ | 上の階にテレポート |
| 下降 | 床ブロック上でスニーク | 下の階にテレポート |

>**Tips**:config.ymlから任意のブロックをエレベーターの床や通過可能なブロックとして利用できるように変更できます。

**搭載機能**:
- 上昇/下降時に音が鳴ります
- BossBar（画面上部）に現在の階数が表示されます
- クールダウンが有効な場合、連続使用時にあと何秒で使用できるようになるかが表示されます

>**Tips**:これらはすべてconfig.ymlで無効化したり挙動を変更できます。

---

# 設定ファイル

`plugins/SimpleCometElevator/user-settings/config.yml`

## 管理コマンド

管理者権限（`simplecometelevator.admin`）を持つプレイヤーは、ゲーム内コマンドで設定を操作できます。

- `/simplecometelevator reload` (`/scelevator reload`)
  - `config.yml` を再読み込みし、現在の動作に即時反映します。
- `/simplecometelevator help` (`/scelevator help`)
  - 管理コマンドのヘルプを表示します。
- `/simplecometelevator get <path>`
  - 現在の設定値を表示します。
- `/simplecometelevator set <path> <value>`
  - 設定値を更新して保存し、即時反映します。
- `/simplecometelevator reset <path>`
  - 指定した設定値をデフォルト値へ戻して即時反映します。
- `/simplecometelevator reset all` -> `/simplecometelevator reset all confirm`
  - 編集可能な全pathを一括でデフォルト値に戻します（30秒以内のconfirmが必要）。
- `/simplecometelevator info` (`/scelevator info`)
  - プラグインのバージョン・作者・リポジトリリンクを表示します。

## 移動コマンド

移動権限（`simplecometelevator.move`）を持つプレイヤーは、エレベーターの特定の階に直接テレポートできます。

- `/simplecometelevator move <階数>` (`/scelevator move <階数>`)
  - エレベーターに乗っている間、指定した階数へテレポートします。
  - Tab補完で利用可能な階数が表示されます。

**例**:
```mcfunction
/scelevator set elevator.cooldown.enabled true
/scelevator set elevator.cooldown.seconds 1.5
/scelevator set elevator.floor.base-blocks IRON_BLOCK,GOLD_BLOCK,$WOOLS
/scelevator set elevator.sound.up.entries {minecraft:entity.experience_orb.pickup,1.0,0.69,0}
/scelevator set elevator.sound.up.entries {minecraft:block.note_block.pling,1.0,1.2,0}, {minecraft:entity.experience_orb.pickup,0.5,1.5,200}
/scelevator move 3
/scelevator reload
```

### setで編集できるpath（機能別）

`/simplecometelevator set <path> <value>` で編集できる `path` は以下です。

#### floor
- `elevator.floor.base-blocks`（床ブロック一覧）
- `elevator.floor.passable-blocks`（通過可能ブロック一覧）
- `elevator.floor.required-air`（必要な空きマス数）
- `elevator.floor.tolerance-height`（床判定の高さ許容）
- `elevator.floor.allow-mixed-blocks`（異種床ブロック混在許可）

#### cooldown
- `elevator.cooldown.enabled`（クールダウン有効化）
- `elevator.cooldown.seconds`（クールダウン秒数）
- `elevator.cooldown.format`（表示フォーマット）

#### command
- `elevator.command.move.enabled`（`/scelevator move` の有効化）

#### sound.up / sound.down
- `elevator.sound.up.enabled`（上昇音の有効化）
- `elevator.sound.up.entries`（上昇音エントリリスト）
- `elevator.sound.down.enabled`（下降音の有効化）
- `elevator.sound.down.entries`（下降音エントリリスト）
- `elevator.sound.command.up.enabled`（moveコマンド上昇音の有効化）
- `elevator.sound.command.up.entries`（moveコマンド上昇音エントリリスト）
- `elevator.sound.command.down.enabled`（moveコマンド下降音の有効化）
- `elevator.sound.command.down.entries`（moveコマンド下降音エントリリスト）

#### entries パスのコマンド書式

`entries` パスには `{サウンドID,volume,pitch,delayMs}` の形式でエントリを指定します。  
カンマ + スペース区切りで複数エントリを並べると、**複数のサウンドを同時または時間差で再生**できます。

```
/scelevator set <path> {type,volume,pitch,delay} [, {type,volume,pitch,delay} ...]
```

| フィールド | 型 | 説明 |
|---|---|---|
| `type` | 文字列 | MinecraftのサウンドID（例: `minecraft:entity.experience_orb.pickup`） |
| `volume` | 小数 | 音量（例: `1.0`） |
| `pitch` | 小数 | ピッチ（例: `0.69`） |
| `delay` | 整数 | 再生遅延（ミリ秒、`0` で即時） |

**コマンド使用例**:
```mcfunction
# 上昇音を1エントリに設定
/scelevator set elevator.sound.up.entries {minecraft:entity.experience_orb.pickup,1.0,0.69,0}

# 上昇音を2エントリに設定（同時再生）
/scelevator set elevator.sound.up.entries {minecraft:block.note_block.pling,1.0,1.2,0}, {minecraft:ui.button.click,0.8,1.0,0}

# 上昇音を2エントリに設定（200ms後に逐次再生）
/scelevator set elevator.sound.up.entries {minecraft:block.note_block.pling,1.0,1.2,0}, {minecraft:entity.experience_orb.pickup,0.5,1.5,200}

# 下降音を設定
/scelevator set elevator.sound.down.entries {minecraft:entity.experience_orb.pickup,1.0,0.56,0}

# コマンド移動音（上）を設定
/scelevator set elevator.sound.command.up.entries {minecraft:ui.button.click,1.0,1.0,0}

# 現在の上昇音エントリを確認
/scelevator get elevator.sound.up.entries
```

> **Tip**: `delay` の値が異なるエントリを並べると逐次再生に、同じ値（または両方 `0`）にすると同時再生になります。  
> サウンドIDの一覧は [Minecraft Wiki](https://minecraft.wiki/w/Sounds.json) で確認できます。

#### floorbar
- `elevator.floorbar.enabled`（BossBar表示の有効化）
- `elevator.floorbar.color`（BossBar色）
- `elevator.floorbar.style`（BossBarスタイル）
- `elevator.floorbar.format`（タイトルフォーマット）
- `elevator.floorbar.use-y-progress`（Y座標基準の進捗）

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
- `messages.language`（メッセージ言語）

詳細な型・デフォルト値・使用例は、この後の各設定セクションを参照してください。

## エレベーターフロア設定 (elevator.floor)

#### base-blocks
- **型**: 文字列リスト
- **デフォルト**: `[IRON_BLOCK, GOLD_BLOCK, REDSTONE_BLOCK, EMERALD_BLOCK, LAPIS_BLOCK, DIAMOND_BLOCK, NETHERITE_BLOCK, $WOOLS]`
- **説明**: エレベーターの床となるブロックを指定します。これらのブロックの上にプレイヤーが乗るとエレベーターとして機能します。
- **使用例**:
  ```yaml
  base-blocks:
    - IRON_BLOCK
    - GOLD_BLOCK
    - $WOOLS  # 羊毛タグを使用
  ```
>**Tips**: 個別のブロック名（IRON_BLOCKなど）またはMinecraftのブロックタグ（$WOOLSなど）を指定できます。タグを使用すると複数の関連ブロックを一度に指定できます。

#### passable-blocks
- **型**: 文字列リスト
- **デフォルト**: `[$AIR, WATER, $BUTTONS, $SIGNS, $BANNERS, $CARPETS, $FLOWERS, $SMALL_FLOWERS, $TALL_FLOWERS, $SAPLINGS, $CROPS, $RAILS, $PRESSURE_PLATES, $TRAPDOORS, $DOORS, $CORAL_FANS, $SEA_GRASS, $KELP]`
- **説明**: エレベーターの床ブロックの上に配置しても通過可能とみなすブロックを指定します。
- **使用例**:
  ```yaml
  passable-blocks:
    - $AIR
    - WATER
    - $CARPETS  # カーペットタグを使用
  ```
>**Tips**: ここで指定したブロックが床ブロックの上にあってもエレベーターとしての機能を失うことはありません。カーペットや感圧版などのブロックを指定すると便利です。

#### required-air
- **型**: 整数
- **デフォルト**: `2`
- **説明**: エレベーターの床ブロックの上に必要なプレイヤーが立つための空きスペース（ブロック）の最低必要数を指定します。この数のブロック分、空間が空いている必要があります。
- **使用例**:
  ```yaml
  required-air: 2  # 床ブロックの上に2ブロック分の空きが必要
  ```
>**Tips**: この値が小さいほど、エレベーターの「天井」の低さが制限されます。プレイヤーの身長より大きい値を設定することをおすすめします。

#### tolerance-height
- **型**: 小数
- **デフォルト**: `0.2`
- **説明**: エレベーターに立つプレイヤーの床ブロックからの高さの許容範囲をブロック単位で指定します。
- **使用例**:
  ```yaml
  tolerance-height: 0.2  # ±0.2ブロックの範囲で床ブロックを探す
  ```
>**Tips**: 床ブロックの上に設置されたカーペットや雪などの薄いブロックの上に乗っている場合にもエレベーターが動作するようにできます。0に設定すると厳密な高さチェックになります。

#### allow-mixed-blocks
- **型**: ブール値
- **デフォルト**: `false`
- **説明**: 異なる種類の床ブロックを同じエレベーターとして認識するかどうかを指定します。
- **使用例**:
  ```yaml  
  allow-mixed-blocks: true
  ```
>**Tips**: これを有効にすると、base-blocksで指定した複数のブロックが混在していても、それらが同じエレベーターの床ブロックとして認識されるようになります。例えば、IRON_BLOCKとGOLD_BLOCKの両方がbase-blocksに指定されている場合、IRON_BLOCKとGOLD_BLOCKのフロアが混在していても同じエレベーターとして機能します。


## クールダウン設定 (elevator.cooldown)

#### enabled
- **型**: ブール値
- **デフォルト**: `false`
- **説明**: エレベーター使用時のクールダウンを有効にするかどうかを指定します。
- **使用例**:
  ```yaml
  enabled: true  # クールダウンを有効にする
  ```
>**Tips**: クールダウンを有効にするとエレベーターの連続使用を制限できます。

#### seconds
- **型**: 小数
- **デフォルト**: `1`
- **説明**: クールダウンの時間を秒単位で指定します。
- **使用例**:
  ```yaml
  seconds: 1.5  # 1.5秒のクールダウン
  ```
>**Tips**: 小数点以下の値も指定可能です。短すぎると効果が薄く、長すぎると使い勝手が悪くなります。

#### format
- **型**: 文字列
- **デフォルト**: `"§cCooldown: {time}s"`
- **説明**: クールダウン中のメッセージフォーマットを指定します。{time}は残り時間を表します。
- **使用例**:
  ```yaml
  format: "§eクールダウン中: {time}秒"  # 日本語表示
  ```
>**Tips**: Minecraftのカラーコード（§）を使用できます。{time}は自動的に残り時間に置き換えられます。

## サウンド設定 (elevator.sound)

サウンド設定は **entriesリスト形式** を使用します。上昇・下降それぞれに複数のサウンドエントリを設定でき、同時または遅延させて再生できます。

### 上昇音設定 (up)

#### enabled
- **型**: ブール値
- **デフォルト**: `true`
- **説明**: 上昇時のサウンドを有効にするかどうかを指定します。
- **使用例**:
  ```yaml
  enabled: true
  ```

#### broadcast
- **型**: ブール値
- **デフォルト**: `false`
- **説明**: `true` でワールド再生（周囲プレイヤーにも聞こえる）、`false` で移動したプレイヤーのみに再生します。
- **補足**: `broadcast` は `/scelevator set` 対象外のため、`config.yml` を直接編集してください。

#### entries
- **型**: サウンドエントリのリスト
- **説明**: 各エントリには以下のフィールドがあります。
  - `type` — MinecraftのサウンドID（例: `"minecraft:entity.experience_orb.pickup"`）
  - `volume` — 音量（デフォルト: `1.0`）
  - `pitch` — ピッチ（デフォルト: `1.0`）
  - `delay` — 再生までの遅延（ミリ秒、デフォルト: `0`）
- **使用例**:
  ```yaml
  entries:
    - type: "minecraft:entity.experience_orb.pickup"
      volume: 1.0
      pitch: 0.69
      delay: 0
  ```

>**Tips**: 複数のエントリを追加することで、複数のサウンドを同時または時間差で再生できます。
> ```yaml
> entries:
>   - type: "minecraft:block.note_block.pling"
>     volume: 1.0
>     pitch: 1.2
>     delay: 0
>   - type: "minecraft:entity.experience_orb.pickup"
>     volume: 0.5
>     pitch: 1.5
>     delay: 200
> ```

### 下降音設定 (down)

#### enabled
- **型**: ブール値
- **デフォルト**: `true`
- **説明**: 下降時のサウンドを有効にするかどうかを指定します。
- **使用例**:
  ```yaml
  enabled: true
  ```

#### entries
- **使用例**:
  ```yaml
  entries:
    - type: "minecraft:entity.experience_orb.pickup"
      volume: 1.0
      pitch: 0.56
      delay: 0
  ```

>**Tips**: 上昇音とは異なるピッチや音を設定すると、移動方向がわかりやすくなります。

## コマンドサウンド設定 (elevator.sound.command)

`/scelevator move <階数>` コマンド使用時に再生されるサウンドです。  
上昇・下降のサウンドとは独立して設定できます。構造は通常のサウンド設定と同じです。

#### enabled
- **型**: ブール値
- **デフォルト**: `true`
- **説明**: コマンドによる移動時のサウンドを有効にするかどうかを指定します。

#### broadcast
- **型**: ブール値
- **デフォルト**: `false`
- **説明**: `true` でワールド再生、`false` で移動プレイヤーのみに再生します。
- **補足**: `broadcast` は `/scelevator set` では変更できません。

#### entries
- **説明**: 通常のサウンドと同じ形式（`type` / `volume` / `pitch` / `delay`）

- **使用例**:
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

## フロアバー設定 (elevator.floorbar)

#### enabled
- **型**: ブール値
- **デフォルト**: `true`
- **説明**: エレベーター使用時のBossBarによる階数表示を有効にするかどうかを指定します。
- **使用例**:
  ```yaml
  enabled: true
  ```
>**Tips**: BossBarは画面上部に現在の階数と総階数を表示します。無効にするとよりクリーンな画面になりますが、階数がわからなくなります。

#### color
- **型**: 文字列
- **デフォルト**: `"BLUE"`
- **説明**: BossBarの色を指定します。利用可能な値: BLUE, GREEN, PINK, PURPLE, RED, WHITE, YELLOW
- **使用例**:
  ```yaml
  color: "GREEN"
  ```
>**Tips**: サーバーのテーマに合った色など、お好みのカラーを選択してください。

#### style
- **型**: 文字列
- **デフォルト**: `"SOLID"`
- **説明**: BossBarのスタイルを指定します。利用可能な値: SOLID, SEGMENTED_6, SEGMENTED_10, SEGMENTED_12, SEGMENTED_20
- **使用例**:
  ```yaml
  style: "SEGMENTED_10"
  ```
>**Tips**: SEGMENTEDはBossBarを分割表示します。数字は分割数を表します。分割数を階数と同期させることはできないので、あまり使用はおすすめしません。

#### format
- **型**: 文字列
- **デフォルト**: `"§e{current}F / {total}F"`
- **説明**: BossBarのタイトルフォーマットを指定します。{current}は現在の階数、{total}は総階数を表します。
- **使用例**:
  ```yaml
  format: "§a階数: {current}/{total}階"
  ```
>**Tips**: {current}と{total}は自動的に数字に置き換えられます。カラーコードも使用可能です。

#### use-y-progress
- **型**: ブール値
- **デフォルト**: `false`
- **説明**: 有効にするとBossBarの進捗が階数基準ではなく、フロアのy座標基準になります。

## タイトル設定 (elevator.title)

上昇/下降時と、`/scelevator move` 使用時にタイトル通知を表示できます。

### `up` / `down`
- `enabled`（有効化）
- `title`（タイトル文字列）
- `subtitle`（サブタイトル文字列）
- `fade-in` / `stay` / `fade-out`（tick単位）

### `command.up` / `command.down`
- 基本構造は `up` / `down` と同じです。
- 未設定時は通常移動側の値を引き継ぎます。

**使用例**:
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

## アクションバー設定 (elevator.actionbar)

移動時に画面下部へ短文通知を表示します。

### `up` / `down` / `command.up` / `command.down`
- `enabled`（有効化）
- `format`（`{current}` と `{total}` を使用可能）

**使用例**:
```yaml
actionbar:
  up:
    enabled: true
    format: "§e↑ {current}F / {total}F"
```

---

## メッセージ設定 (messages)

#### language
- **型**: 文字列
- **デフォルト**: `"ja"`
- **利用可能**: `ja`（日本語）、`en`（英語）
- **説明**: ゲーム内コマンドのメッセージに使用する言語を指定します。
- **使用例**:
  ```yaml
  messages:
    language: en
  ```
>**Tips**: メッセージのテキスト自体は `user-settings` フォルダ内の `messages_ja.yml` または `messages_en.yml` を直接編集することでカスタマイズできます。

---

## 特殊記法

### タグの使用 ($)
設定項目ではMinecraftのブロックタグを使用できます。タグを使用するには、タグ名の前に`$`を付けます。
タグの一覧は、[こちら](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Tag.html)から閲覧できます。

**使用可能なタグ一例**:
- `$AIR` - 空気ブロック
- `$BUTTONS` - ボタン
- `$CARPETS` - カーペット
- `$WOOLS` - 羊毛
- `$SIGNS` - 看板

**例**:
```yaml
base-blocks:
  - IRON_BLOCK
  - $WOOLS  # すべての羊毛ブロック
```

>**Tips**: タグを使用すると、Minecraftのバージョンアップ時に自動的に新しいブロックが追加されます。例えば`$WOOLS`はすべての色の羊毛を含みます。

### ブロックの除外 (!)
ブロック名やタグ名の前に`!`を付けると、そのブロック/タグをフィルタから除外できます。

**使用例**:
```yaml
passable-blocks:
  - $TRAPDOORS     # すべてのトラップドアを許可
  - !IRON_TRAPDOOR    # ただし、鉄のトラップドアは除外
```

この例では、すべてのトラップドアが通過可能になりますが、IRON_TRAPDOORだけは通過不可能になります。

**タグの除外**:
```yaml
base-blocks:
  - $DOORS       # すべてのドアを許可
  - !$WOODEN_DOORS  # 木製のドアは除外
```

>**Tips**: 除外を使用する際は、より広いタグやカテゴリーを指定してから、不要な要素を除外するのが効果的です。

### 装飾コード (§)
メッセージやフォーマットではMinecraftの装飾コードを使用することができます。

**主な装飾コード**:
- `§0` - 黒
- `§1` - 濃い青
- `§2` - 濃い緑
- `§3` - 濃い水色
- `§4` - 濃い赤
- `§5` - 濃い紫
- `§6` - 金色
- `§7` - 灰色
- `§8` - 濃い灰色
- `§9` - 青
- `§a` - 緑
- `§b` - 水色
- `§c` - 赤
- `§d` - ピンク
- `§e` - 黄色
- `§f` - 白
- `§k` - ランダム文字
- `§l` - 太字
- `§m` - 打ち消し線
- `§n` - 下線
- `§o` - 斜体
- `§r` - リセット（装飾をリセット）

>**Tips**: 装飾コードはメッセージを読みやすくします。BossBarやクールダウンメッセージで効果的です。

---

## 設定例

### 最低限な設定
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
  language: ja
```

### 日本語表示の設定
```yaml
elevator:
  cooldown:
    format: "§cクールダウン中: {time}秒"

  floorbar:
    format: "§a{current}階 / {total}階"

messages:
  language: ja
```

### タグを活用した設定
```yaml
elevator:
  floor:
    base-blocks:
      - $WOOLS  # すべての羊毛
      - IRON_BLOCK  # 鉄ブロック（個別指定）
    passable-blocks:
      - $AIR
      - $CARPETS  # すべてのカーペット
      - PRESSURE_PLATES  # すべての感圧版
```

---

## Tips集

### パフォーマンスに関するTips
- **passable-blocksは必要最小限に**: 多くのブロックを指定するほど処理が重くなります。また、衝突判定のあるブロックを指定した場合プレイヤーがブロックに埋まる恐れがあります。
- **tolerance-heightは小さめに**: 大きい値を設定すると誤作動が発生する恐れがあります。0.5未満の高さに設定することをおすすめします。

### 使いやすさに関するTips
- **サウンドを方向で変える**: 上昇音と下降音を異なる音にすると、どちらの方向に移動したかがわかりやすくなります。
- **ブロック除外で柔軟に設定**: タグで大まかなカテゴリーを指定してから、不要なブロックを`!`で除外すると効率的です。
- **階数の多すぎるエレベーターに注意**: 階数が多すぎると移動が大変です。高層/低層用エレベーターや高速移動用エレベーターなど、用途に応じて複数のエレベーターを設置するのがおすすめです。

### トラブルシューティング
- **エレベーターが動作しない**: base-blocksとpassable-blocksの設定を確認してください。また、required-airが大きすぎる可能性があります。
- **音が鳴らない**: サウンドIDや音量、有効化設定などが正しいか確認してください。
- **タグが認識されない**: タグ名の前に$を付けることを忘れないでください。また、Minecraftのバージョンによって利用可能なタグが変わります。
- **除外が機能しない**: 除外記号`!`が正しく付いているか確認してください。タグと組み合わせる場合、タグが存在することを確認してください。

----

## 注意事項

1. 設定を変更した後は、`/simplecometelevator reload`（`/scelevator reload`）を実行してください。
2. 無効なブロック名やタグ名を指定すると、ログに警告が表示されます。
3. サウンドIDはMinecraftのバージョンによって異なる場合があります。
4. タグはMinecraftのバージョンによって利用可能なものが異なります。

----
## その他

プラグインバージョン: 1.1.0-1.21.x
作成者: [Hisui.A](https://github.com/stabery)
GitHubリポジトリ: https://github.com/stabery/SimpleCometElevator

- プラグインのバグや要望がある場合は、GitHubリポジトリのIssueにてお知らせください。
- プルリクエストは大歓迎です。パフォーマンス改善などを目的としたコードの変更は特に歓迎します。