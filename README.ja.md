# SimpleCometElevator 使い方（詳細版）

> 最初に要点だけ確認したい場合は [`README.md`](README.md) を参照してください。

[English README](README.en.md)

SimpleCometElevator - Minecraft(Bukkit/Spigot/Paper)用プラグイン

最低限の機能を備えた至ってシンプルなエレベータープラグインです。

## 目次

- [ゲーム内での使い方](#ゲーム内での使い方)
- [設定ファイル](#設定ファイル)
  - [エレベーターフロア設定](#エレベーターフロア設定-elevatorfloor)
  - [クールダウン設定](#クールダウン設定-elevatorcooldown)
  - [サウンド設定](#サウンド設定-elevatorsound)
  - [フロアバー設定](#フロアバー設定-elevatorfloorbar)
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

`plugins/SympleCometElevator/config.yml`

## 管理コマンド

管理者権限（`simplecometelevator.admin`）を持つプレイヤーは、ゲーム内コマンドで設定を操作できます。

- `/simplecometelevator reload` (`/scelevator reload`)
  - `config.yml` を再読み込みし、現在の動作に即時反映します。
- `/simplecometelevator get <path>`
  - 現在の設定値を表示します。
- `/simplecometelevator set <path> <value>`
  - 設定値を更新して保存し、即時反映します。

**例**:
```mcfunction
/scelevator set elevator.cooldown.enabled true
/scelevator set elevator.cooldown.seconds 1.5
/scelevator set elevator.floor.base-blocks IRON_BLOCK,GOLD_BLOCK,$WOOLS
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

#### sound.up / sound.down
- `elevator.sound.up.enabled`（上昇音の有効化）
- `elevator.sound.up.type`（上昇音のサウンドID）
- `elevator.sound.up.volume`（上昇音の音量）
- `elevator.sound.up.pitch`（上昇音のピッチ）
- `elevator.sound.down.enabled`（下降音の有効化）
- `elevator.sound.down.type`（下降音のサウンドID）
- `elevator.sound.down.volume`（下降音の音量）
- `elevator.sound.down.pitch`（下降音のピッチ）

#### floorbar
- `elevator.floorbar.enabled`（BossBar表示の有効化）
- `elevator.floorbar.color`（BossBar色）
- `elevator.floorbar.style`（BossBarスタイル）
- `elevator.floorbar.format`（タイトルフォーマット）
- `elevator.floorbar.use-y-progress`（Y座標基準の進捗）

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

### 上昇音設定 (up)

#### enabled
- **型**: ブール値
- **デフォルト**: `true`
- **説明**: 上昇時のサウンドを有効にするかどうかを指定します。
- **使用例**:
  ```yaml
  enabled: true
  ```
>**Tips**: 無効にすると、エレベーターで上昇した時の音が再生されなくなります。

#### type
- **型**: 文字列
- **デフォルト**: `"minecraft:entity.experience_orb.pickup"`
- **説明**: 上昇時に再生するサウンドの種類を指定します。MinecraftのサウンドIDを使用します。
- **使用例**:
  ```yaml
  type: "minecraft:block.note_block.pling"
  ```
>**Tips**: サウンドIDは「minecraft:」から始まります。Tab補完が効かないので正確に入力してください。

#### volume
- **型**: 小数
- **デフォルト**: `1.0`
- **説明**: 上昇音のボリュームを指定します。1.0が通常の音量です。
- **使用例**:
  ```yaml
  volume: 0.8  # 80%の音量
  ```
>**Tips**: 0.0で無音、1.0で最大音量になります。それ以上に設定すると音量は上がらず聞こえる距離が伸びます。

#### pitch
- **型**: 小数
- **デフォルト**: `0.69`
- **説明**: 上昇音のピッチ（音の高さ）を指定します。1.0が通常のピッチです。
- **使用例**:
  ```yaml
  pitch: 1.2  # 高めの音
  ```
>**Tips**: 0.5で低音、2.0で高音になります。上昇音を高めに設定すると、方向がわかりやすくなります。

### 下降音設定 (down)

#### enabled
- **型**: ブール値
- **デフォルト**: `true`
- **説明**: 下降時のサウンドを有効にするかどうかを指定します。
- **使用例**:
  ```yaml
  enabled: true
  ```
>**Tips**: 上昇音と個別に設定できます。

#### type
- **型**: 文字列
- **デフォルト**: `"minecraft:entity.experience_orb.pickup"`
- **説明**: 下降時に再生するサウンドの種類を指定します。
- **使用例**:
  ```yaml
  type: "minecraft:block.note_block.bass"
  ```
>**Tips**: 上昇音とは異なるサウンドを設定しても、方向がわかりやすくなります。

#### volume
- **型**: 小数
- **デフォルト**: `1.0`
- **説明**: 下降音のボリュームを指定します。
- **使用例**:
  ```yaml
  volume: 0.8
  ```
>**Tips**: 上昇音と同じ音量にするのがおすすめです。

#### pitch
- **型**: 小数
- **デフォルト**: `0.56`
- **説明**: 下降音のピッチを指定します。
- **使用例**:
  ```yaml
  pitch: 0.8  # 低めの音
  ```
>**Tips**: 下降音を低めに設定すると、方向がわかりやすくなります。

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

### 日本語表示の設定
```yaml
elevator:
  cooldown:
    format: "§cクールダウン中: {time}秒"

  floorbar:
    format: "§a{current}階 / {total}階"
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

プラグインバージョン: 1.0.0
作成者: [Hisui.A](https://github.com/stabery)
githubリポジトリ: https://github.com/stabery/SimpleCometElevator

- プラグインのバグや要望がある場合は、GitHubリポジトリのIssueにてお知らせください。
- プルリクエストは大歓迎です。パフォーマンス改善などを目的としたコードの変更は特に歓迎します。