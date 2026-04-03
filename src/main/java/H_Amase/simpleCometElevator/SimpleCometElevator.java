/**
* SimpleCometElevator 1.1.0-1.21.x
*/

package H_Amase.simpleCometElevator;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import java.time.Duration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public final class SimpleCometElevator extends JavaPlugin implements Listener {

    /*===== 設定キー定数 =====*/
    private static final String CONFIG_BASE_BLOCKS = "elevator.floor.base-blocks";
    private static final String CONFIG_PASSABLE_BLOCKS = "elevator.floor.passable-blocks";
    private static final String CONFIG_REQUIRED_AIR = "elevator.floor.required-air";
    private static final String CONFIG_TOLERANCE_HEIGHT = "elevator.floor.tolerance-height";
    private static final String CONFIG_ALLOW_MIXED_BLOCKS = "elevator.floor.allow-mixed-blocks";
    private static final String CONFIG_COOLDOWN_ENABLED = "elevator.cooldown.enabled";
    private static final String CONFIG_COOLDOWN_SECONDS = "elevator.cooldown.seconds";
    private static final String CONFIG_COOLDOWN_FORMAT = "elevator.cooldown.format";
    private static final String CONFIG_COMMAND_MOVE_ENABLED = "elevator.command.move.enabled";
    private static final String CONFIG_SOUND_UP_ENABLED = "elevator.sound.up.enabled";
    private static final String CONFIG_SOUND_UP_BROADCAST = "elevator.sound.up.broadcast";
    private static final String CONFIG_SOUND_UP_ENTRIES = "elevator.sound.up.entries";
    private static final String CONFIG_SOUND_DOWN_ENABLED = "elevator.sound.down.enabled";
    private static final String CONFIG_SOUND_DOWN_BROADCAST = "elevator.sound.down.broadcast";
    private static final String CONFIG_SOUND_DOWN_ENTRIES = "elevator.sound.down.entries";
    private static final String CONFIG_SOUND_CMD_UP_ENABLED = "elevator.sound.command.up.enabled";
    private static final String CONFIG_SOUND_CMD_UP_BROADCAST = "elevator.sound.command.up.broadcast";
    private static final String CONFIG_SOUND_CMD_UP_ENTRIES = "elevator.sound.command.up.entries";
    private static final String CONFIG_SOUND_CMD_DOWN_ENABLED = "elevator.sound.command.down.enabled";
    private static final String CONFIG_SOUND_CMD_DOWN_BROADCAST = "elevator.sound.command.down.broadcast";
    private static final String CONFIG_SOUND_CMD_DOWN_ENTRIES = "elevator.sound.command.down.entries";
    private static final String CONFIG_BOSSBAR_ENABLED = "elevator.floorbar.enabled";
    private static final String CONFIG_BOSSBAR_COLOR = "elevator.floorbar.color";
    private static final String CONFIG_BOSSBAR_STYLE = "elevator.floorbar.style";
    private static final String CONFIG_BOSSBAR_FORMAT = "elevator.floorbar.format";
    private static final String CONFIG_BOSSBAR_USE_Y_PROGRESS = "elevator.floorbar.use-y-progress";
    private static final String CONFIG_TITLE_UP_ENABLED = "elevator.title.up.enabled";
    private static final String CONFIG_TITLE_UP_TITLE = "elevator.title.up.title";
    private static final String CONFIG_TITLE_UP_SUBTITLE = "elevator.title.up.subtitle";
    private static final String CONFIG_TITLE_UP_FADE_IN = "elevator.title.up.fade-in";
    private static final String CONFIG_TITLE_UP_STAY = "elevator.title.up.stay";
    private static final String CONFIG_TITLE_UP_FADE_OUT = "elevator.title.up.fade-out";
    private static final String CONFIG_TITLE_DOWN_ENABLED = "elevator.title.down.enabled";
    private static final String CONFIG_TITLE_DOWN_TITLE = "elevator.title.down.title";
    private static final String CONFIG_TITLE_DOWN_SUBTITLE = "elevator.title.down.subtitle";
    private static final String CONFIG_TITLE_DOWN_FADE_IN = "elevator.title.down.fade-in";
    private static final String CONFIG_TITLE_DOWN_STAY = "elevator.title.down.stay";
    private static final String CONFIG_TITLE_DOWN_FADE_OUT = "elevator.title.down.fade-out";
    private static final String CONFIG_TITLE_CMD_UP_ENABLED = "elevator.title.command.up.enabled";
    private static final String CONFIG_TITLE_CMD_UP_TITLE = "elevator.title.command.up.title";
    private static final String CONFIG_TITLE_CMD_UP_SUBTITLE = "elevator.title.command.up.subtitle";
    private static final String CONFIG_TITLE_CMD_UP_FADE_IN = "elevator.title.command.up.fade-in";
    private static final String CONFIG_TITLE_CMD_UP_STAY = "elevator.title.command.up.stay";
    private static final String CONFIG_TITLE_CMD_UP_FADE_OUT = "elevator.title.command.up.fade-out";
    private static final String CONFIG_TITLE_CMD_DOWN_ENABLED = "elevator.title.command.down.enabled";
    private static final String CONFIG_TITLE_CMD_DOWN_TITLE = "elevator.title.command.down.title";
    private static final String CONFIG_TITLE_CMD_DOWN_SUBTITLE = "elevator.title.command.down.subtitle";
    private static final String CONFIG_TITLE_CMD_DOWN_FADE_IN = "elevator.title.command.down.fade-in";
    private static final String CONFIG_TITLE_CMD_DOWN_STAY = "elevator.title.command.down.stay";
    private static final String CONFIG_TITLE_CMD_DOWN_FADE_OUT = "elevator.title.command.down.fade-out";
    private static final String CONFIG_ACTIONBAR_UP_ENABLED = "elevator.actionbar.up.enabled";
    private static final String CONFIG_ACTIONBAR_UP_FORMAT = "elevator.actionbar.up.format";
    private static final String CONFIG_ACTIONBAR_DOWN_ENABLED = "elevator.actionbar.down.enabled";
    private static final String CONFIG_ACTIONBAR_DOWN_FORMAT = "elevator.actionbar.down.format";
    private static final String CONFIG_ACTIONBAR_CMD_UP_ENABLED = "elevator.actionbar.command.up.enabled";
    private static final String CONFIG_ACTIONBAR_CMD_UP_FORMAT = "elevator.actionbar.command.up.format";
    private static final String CONFIG_ACTIONBAR_CMD_DOWN_ENABLED = "elevator.actionbar.command.down.enabled";
    private static final String CONFIG_ACTIONBAR_CMD_DOWN_FORMAT = "elevator.actionbar.command.down.format";
    private static final String CONFIG_MESSAGES_LANGUAGE = "messages.language";

    /*===== プレイヤー状態管理 =====*/
    private final Set<UUID> elevatorPlayers = new HashSet<>();
    private final Map<UUID, org.bukkit.boss.BossBar> bossBars = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<FloorCacheKey, List<Integer>> floorCache = new HashMap<>();
    private final Map<UUID, EvaluatedBase> lastEvaluatedBases = new HashMap<>();
    private final Map<String, PendingReset> pendingResets = new HashMap<>();

    /*===== 設定値 =====*/
    private Set<Material> baseBlocks;
    private Set<Material> passableBlocks;
    private int requiredAirBlocks;
    private double toleranceHeight;
    private boolean allowMixedBlocks;

    private boolean cooldownEnabled;
    private long cooldownMillis;
    private String cooldownFormat;
    private boolean moveCommandEnabled;

    private boolean bossBarEnabled;
    private org.bukkit.boss.BarColor bossBarColor;
    private org.bukkit.boss.BarStyle bossBarStyle;
    private String bossBarFormat;
    private boolean bossBarUseYProgress;

    private boolean titleUpEnabled;
    private String titleUpTitle;
    private String titleUpSubtitle;
    private int titleUpFadeIn;
    private int titleUpStay;
    private int titleUpFadeOut;
    private boolean titleDownEnabled;
    private String titleDownTitle;
    private String titleDownSubtitle;
    private int titleDownFadeIn;
    private int titleDownStay;
    private int titleDownFadeOut;
    private boolean titleCmdUpEnabled;
    private String titleCmdUpTitle;
    private String titleCmdUpSubtitle;
    private int titleCmdUpFadeIn;
    private int titleCmdUpStay;
    private int titleCmdUpFadeOut;
    private boolean titleCmdDownEnabled;
    private String titleCmdDownTitle;
    private String titleCmdDownSubtitle;
    private int titleCmdDownFadeIn;
    private int titleCmdDownStay;
    private int titleCmdDownFadeOut;
    private boolean actionBarUpEnabled;
    private String actionBarUpFormat;
    private boolean actionBarDownEnabled;
    private String actionBarDownFormat;
    private boolean actionBarCmdUpEnabled;
    private String actionBarCmdUpFormat;
    private boolean actionBarCmdDownEnabled;
    private String actionBarCmdDownFormat;

    private boolean upSoundsEnabled;
    private boolean upSoundsBroadcast;
    private boolean downSoundsEnabled;
    private boolean downSoundsBroadcast;
    private boolean cmdUpSoundsEnabled;
    private boolean cmdUpSoundsBroadcast;
    private boolean cmdDownSoundsEnabled;
    private boolean cmdDownSoundsBroadcast;
    private List<SoundEntry> upSounds = new ArrayList<>();
    private List<SoundEntry> downSounds = new ArrayList<>();
    private List<SoundEntry> cmdUpSounds = new ArrayList<>();
    private List<SoundEntry> cmdDownSounds = new ArrayList<>();

    private String messageLanguage;
    private File settingsFolder;
    private File configFile;
    private FileConfiguration userConfig;
    private FileConfiguration jaMessages;
    private FileConfiguration enMessages;

    /*===== HisuiPluginUpdateChecker連携 =====*/
    private HisuiPluginUpdateChecker updateChecker;

    private static final String ADMIN_PERMISSION = "simplecometelevator.admin";
    private static final String MOVE_PERMISSION = "simplecometelevator.move";
    private static final String MESSAGE_PREFIX = "§8[§bSimpleCometElevator§8] §7";
    private static final String SETTINGS_DIR_NAME = "user-settings";
    private static final String CONFIG_FILE_NAME = "config.yml";
    private static final String MESSAGES_JA_FILE_NAME = "messages_ja.yml";
    private static final String MESSAGES_EN_FILE_NAME = "messages_en.yml";
    private static final String RESET_ALL_KEYWORD = "all";
    private static final String RESET_CONFIRM_KEYWORD = "confirm";
    private static final long RESET_CONFIRM_TIMEOUT_MILLIS = 30_000L;
    private static final List<String> ADMIN_SUB_COMMANDS = List.of("reload", "check", "get", "set", "reset", "help", "info");
    private static final List<String> EDITABLE_PATHS = List.of(
            CONFIG_BASE_BLOCKS,
            CONFIG_PASSABLE_BLOCKS,
            CONFIG_REQUIRED_AIR,
            CONFIG_TOLERANCE_HEIGHT,
            CONFIG_ALLOW_MIXED_BLOCKS,
            CONFIG_COOLDOWN_ENABLED,
            CONFIG_COOLDOWN_SECONDS,
            CONFIG_COOLDOWN_FORMAT,
            CONFIG_COMMAND_MOVE_ENABLED,
            CONFIG_SOUND_UP_ENABLED,
            CONFIG_SOUND_UP_ENTRIES,
            CONFIG_SOUND_DOWN_ENABLED,
            CONFIG_SOUND_DOWN_ENTRIES,
            CONFIG_SOUND_CMD_UP_ENABLED,
            CONFIG_SOUND_CMD_UP_ENTRIES,
            CONFIG_SOUND_CMD_DOWN_ENABLED,
            CONFIG_SOUND_CMD_DOWN_ENTRIES,
            CONFIG_BOSSBAR_ENABLED,
            CONFIG_BOSSBAR_COLOR,
            CONFIG_BOSSBAR_STYLE,
            CONFIG_BOSSBAR_FORMAT,
            CONFIG_BOSSBAR_USE_Y_PROGRESS,
            CONFIG_TITLE_UP_ENABLED,
            CONFIG_TITLE_UP_TITLE,
            CONFIG_TITLE_UP_SUBTITLE,
            CONFIG_TITLE_UP_FADE_IN,
            CONFIG_TITLE_UP_STAY,
            CONFIG_TITLE_UP_FADE_OUT,
            CONFIG_TITLE_DOWN_ENABLED,
            CONFIG_TITLE_DOWN_TITLE,
            CONFIG_TITLE_DOWN_SUBTITLE,
            CONFIG_TITLE_DOWN_FADE_IN,
            CONFIG_TITLE_DOWN_STAY,
            CONFIG_TITLE_DOWN_FADE_OUT,
            CONFIG_TITLE_CMD_UP_ENABLED,
            CONFIG_TITLE_CMD_UP_TITLE,
            CONFIG_TITLE_CMD_UP_SUBTITLE,
            CONFIG_TITLE_CMD_UP_FADE_IN,
            CONFIG_TITLE_CMD_UP_STAY,
            CONFIG_TITLE_CMD_UP_FADE_OUT,
            CONFIG_TITLE_CMD_DOWN_ENABLED,
            CONFIG_TITLE_CMD_DOWN_TITLE,
            CONFIG_TITLE_CMD_DOWN_SUBTITLE,
            CONFIG_TITLE_CMD_DOWN_FADE_IN,
            CONFIG_TITLE_CMD_DOWN_STAY,
            CONFIG_TITLE_CMD_DOWN_FADE_OUT,
            CONFIG_ACTIONBAR_UP_ENABLED,
            CONFIG_ACTIONBAR_UP_FORMAT,
            CONFIG_ACTIONBAR_DOWN_ENABLED,
            CONFIG_ACTIONBAR_DOWN_FORMAT,
            CONFIG_ACTIONBAR_CMD_UP_ENABLED,
            CONFIG_ACTIONBAR_CMD_UP_FORMAT,
            CONFIG_ACTIONBAR_CMD_DOWN_ENABLED,
            CONFIG_ACTIONBAR_CMD_DOWN_FORMAT,
            CONFIG_MESSAGES_LANGUAGE
    );

    /**
     * ===============================
     * プラグイン初期化
     * ===============================
     */
    
    @Override
    public void onEnable() {
        initializeUserSettings();
        loadConfigValues();
        getServer().getPluginManager().registerEvents(this, this);

        /*===== HisuiPluginUpdateChecker連携: 初期化・イベント登録・初回更新確認 =====*/
        updateChecker = new HisuiPluginUpdateChecker(this);
        getServer().getPluginManager().registerEvents(updateChecker, this);
        /*====================================================================*/

        refreshOnlinePlayersElevatorState();

        /*===== HisuiPluginUpdateChecker連携: 起動時更新確認 =====*/
        updateChecker.checkForUpdates();
        /*================================================*/

        startJumpSuppressTask();
    }

    @Override
    public void onDisable() {
        bossBars.values().forEach(org.bukkit.boss.BossBar::removeAll);
        bossBars.clear();
        elevatorPlayers.clear();
        cooldowns.clear();
        floorCache.clear();
        lastEvaluatedBases.clear();
        pendingResets.clear();
        Bukkit.getScheduler().cancelTasks(this);
    }

    private record FloorCacheKey(UUID worldUid, int x, int z, Material baseType, boolean mixed) {
    }

    private record EvaluatedBase(UUID worldUid, int x, int y, int z, Material type, boolean elevator) {
        static EvaluatedBase from(Block block, boolean elevator) {
            return new EvaluatedBase(
                    block.getWorld().getUID(),
                    block.getX(),
                    block.getY(),
                    block.getZ(),
                    block.getType(),
                    elevator
            );
        }

        boolean matches(Block block) {
            return worldUid.equals(block.getWorld().getUID())
                    && x == block.getX()
                    && y == block.getY()
                    && z == block.getZ()
                    && type == block.getType();
        }
    }

    /**
     * 1エントリ分のサウンド設定（type・volume・pitch・遅延ms）
     */
    private record SoundEntry(Sound sound, float volume, float pitch, long delayMs) {}

    /**
     * reset確認待ち状態（pathと有効期限）
     */
    private record PendingReset(String path, long expiresAt) {}

    /**
     * ===============================
     * 設定読み込み
     * ===============================
     */
    private void loadConfigValues() {
        FileConfiguration config = getUserConfig();

        /*===== フロアブロック =====*/
        baseBlocks = loadMaterialSet(CONFIG_BASE_BLOCKS, "base-block");
        passableBlocks = loadMaterialSet(CONFIG_PASSABLE_BLOCKS, "passable-block");
        getLogger().info("Base blocks: " + baseBlocks);
        getLogger().info("Passable blocks: " + passableBlocks);
        requiredAirBlocks = config.getInt(CONFIG_REQUIRED_AIR);
        toleranceHeight = config.getDouble(CONFIG_TOLERANCE_HEIGHT);
        allowMixedBlocks = config.getBoolean(CONFIG_ALLOW_MIXED_BLOCKS);

        /*===== クールダウン =====*/
        cooldownEnabled = config.getBoolean(CONFIG_COOLDOWN_ENABLED);
        cooldownMillis = (long)(config.getDouble(CONFIG_COOLDOWN_SECONDS) * 1000);
        cooldownFormat = config.getString(CONFIG_COOLDOWN_FORMAT);

        /*===== コマンド =====*/
        moveCommandEnabled = config.getBoolean(CONFIG_COMMAND_MOVE_ENABLED, true);

        /*===== サウンド =====*/
        upSoundsEnabled   = config.getBoolean(CONFIG_SOUND_UP_ENABLED, true);
        upSoundsBroadcast = config.getBoolean(CONFIG_SOUND_UP_BROADCAST, false);
        upSounds          = loadSoundList(CONFIG_SOUND_UP_ENTRIES);
        downSoundsEnabled   = config.getBoolean(CONFIG_SOUND_DOWN_ENABLED, true);
        downSoundsBroadcast = config.getBoolean(CONFIG_SOUND_DOWN_BROADCAST, false);
        downSounds          = loadSoundList(CONFIG_SOUND_DOWN_ENTRIES);

        /*===== コマンドサウンド =====*/
        cmdUpSoundsEnabled   = config.getBoolean(CONFIG_SOUND_CMD_UP_ENABLED, true);
        cmdUpSoundsBroadcast = config.getBoolean(CONFIG_SOUND_CMD_UP_BROADCAST, false);
        cmdUpSounds          = loadSoundList(CONFIG_SOUND_CMD_UP_ENTRIES);
        cmdDownSoundsEnabled   = config.getBoolean(CONFIG_SOUND_CMD_DOWN_ENABLED, true);
        cmdDownSoundsBroadcast = config.getBoolean(CONFIG_SOUND_CMD_DOWN_BROADCAST, false);
        cmdDownSounds          = loadSoundList(CONFIG_SOUND_CMD_DOWN_ENTRIES);

        /*===== BossBar =====*/
        bossBarEnabled = config.getBoolean(CONFIG_BOSSBAR_ENABLED);
        bossBarColor = parseEnumSafely(
                org.bukkit.boss.BarColor.class,
                config.getString(CONFIG_BOSSBAR_COLOR),
                org.bukkit.boss.BarColor.BLUE
        );
        bossBarStyle = parseEnumSafely(
                org.bukkit.boss.BarStyle.class,
                config.getString(CONFIG_BOSSBAR_STYLE),
                org.bukkit.boss.BarStyle.SOLID
        );
        bossBarFormat = config.getString(CONFIG_BOSSBAR_FORMAT);
        bossBarUseYProgress = config.getBoolean(CONFIG_BOSSBAR_USE_Y_PROGRESS);

        /*===== タイトル =====*/
        titleUpEnabled  = config.getBoolean(CONFIG_TITLE_UP_ENABLED, false);
        titleUpTitle    = config.getString(CONFIG_TITLE_UP_TITLE, "");
        titleUpSubtitle = config.getString(CONFIG_TITLE_UP_SUBTITLE, "");
        titleUpFadeIn   = config.getInt(CONFIG_TITLE_UP_FADE_IN, 10);
        titleUpStay     = config.getInt(CONFIG_TITLE_UP_STAY, 40);
        titleUpFadeOut  = config.getInt(CONFIG_TITLE_UP_FADE_OUT, 10);
        titleDownEnabled  = config.getBoolean(CONFIG_TITLE_DOWN_ENABLED, false);
        titleDownTitle    = config.getString(CONFIG_TITLE_DOWN_TITLE, "");
        titleDownSubtitle = config.getString(CONFIG_TITLE_DOWN_SUBTITLE, "");
        titleDownFadeIn   = config.getInt(CONFIG_TITLE_DOWN_FADE_IN, 10);
        titleDownStay     = config.getInt(CONFIG_TITLE_DOWN_STAY, 40);
        titleDownFadeOut  = config.getInt(CONFIG_TITLE_DOWN_FADE_OUT, 10);

        titleCmdUpEnabled  = config.getBoolean(CONFIG_TITLE_CMD_UP_ENABLED, titleUpEnabled);
        titleCmdUpTitle    = config.getString(CONFIG_TITLE_CMD_UP_TITLE, titleUpTitle);
        titleCmdUpSubtitle = config.getString(CONFIG_TITLE_CMD_UP_SUBTITLE, titleUpSubtitle);
        titleCmdUpFadeIn   = config.getInt(CONFIG_TITLE_CMD_UP_FADE_IN, titleUpFadeIn);
        titleCmdUpStay     = config.getInt(CONFIG_TITLE_CMD_UP_STAY, titleUpStay);
        titleCmdUpFadeOut  = config.getInt(CONFIG_TITLE_CMD_UP_FADE_OUT, titleUpFadeOut);
        titleCmdDownEnabled  = config.getBoolean(CONFIG_TITLE_CMD_DOWN_ENABLED, titleDownEnabled);
        titleCmdDownTitle    = config.getString(CONFIG_TITLE_CMD_DOWN_TITLE, titleDownTitle);
        titleCmdDownSubtitle = config.getString(CONFIG_TITLE_CMD_DOWN_SUBTITLE, titleDownSubtitle);
        titleCmdDownFadeIn   = config.getInt(CONFIG_TITLE_CMD_DOWN_FADE_IN, titleDownFadeIn);
        titleCmdDownStay     = config.getInt(CONFIG_TITLE_CMD_DOWN_STAY, titleDownStay);
        titleCmdDownFadeOut  = config.getInt(CONFIG_TITLE_CMD_DOWN_FADE_OUT, titleDownFadeOut);

        /*===== アクションバー =====*/
        actionBarUpEnabled  = config.getBoolean(CONFIG_ACTIONBAR_UP_ENABLED, false);
        actionBarUpFormat   = config.getString(CONFIG_ACTIONBAR_UP_FORMAT, "");
        actionBarDownEnabled  = config.getBoolean(CONFIG_ACTIONBAR_DOWN_ENABLED, false);
        actionBarDownFormat   = config.getString(CONFIG_ACTIONBAR_DOWN_FORMAT, "");
        actionBarCmdUpEnabled  = config.getBoolean(CONFIG_ACTIONBAR_CMD_UP_ENABLED, actionBarUpEnabled);
        actionBarCmdUpFormat   = config.getString(CONFIG_ACTIONBAR_CMD_UP_FORMAT, actionBarUpFormat);
        actionBarCmdDownEnabled  = config.getBoolean(CONFIG_ACTIONBAR_CMD_DOWN_ENABLED, actionBarDownEnabled);
        actionBarCmdDownFormat   = config.getString(CONFIG_ACTIONBAR_CMD_DOWN_FORMAT, actionBarDownFormat);

        /*===== メッセージ =====*/
        messageLanguage = resolveLanguage(config.getString(CONFIG_MESSAGES_LANGUAGE));
    }
    /*===== マテリアルパーサ =====*/
    private Set<Material> loadMaterialSet(String path, String logName) {
        FileConfiguration config = getUserConfig();
        Set<Material> set = new HashSet<>();
        Set<Material> excludeSet = new HashSet<>();

        for (String name : config.getStringList(path)) {
            boolean isExclude = name.startsWith("!");
            String materialName = isExclude ? name.substring(1) : name;

            if (materialName.startsWith("$")) {
                loadTagMaterial(materialName, isExclude ? excludeSet : set, logName);
            } else {
                loadNormalMaterial(materialName, isExclude ? excludeSet : set, logName);
            }
        }

        /* 除外マテリアルをメインのセットから削除 */
        set.removeAll(excludeSet);

        return set;
    }

    private void loadTagMaterial(String tagName, Set<Material> set, String logName) {
        String name = tagName.substring(1).toLowerCase(Locale.ROOT);
        NamespacedKey key = NamespacedKey.minecraft(name);
        Tag<Material> tag = Bukkit.getTag(Tag.REGISTRY_BLOCKS, key, Material.class);

        if (tag != null) {
            set.addAll(tag.getValues());
        } else {
            getLogger().warning("Invalid tag for " + logName + ": " + tagName);
        }
    }

    private void loadNormalMaterial(String name, Set<Material> set, String logName) {
        Material mat = Material.matchMaterial(name);
        if (mat != null) {
            set.add(mat);
        } else {
            getLogger().warning("Invalid " + logName + ": " + name);
        }
    }

    /**
     * ===============================
     * 乗車判定
     * ===============================
     */
    private void checkElevator(Player player, Location loc) {
        checkElevator(player, loc, false);
    }

    private void checkElevator(Player player, Location loc, boolean forceRecheck) {
        Block block = getBaseBlock(loc);
        UUID uuid = player.getUniqueId();

        if (!forceRecheck) {
            EvaluatedBase previous = lastEvaluatedBases.get(uuid);
            if (previous != null && previous.matches(block)) {
                if (!bossBarEnabled) removeBossBar(player);
                return;
            }
        }

        boolean elevatorBase = isElevatorBase(block);
        lastEvaluatedBases.put(uuid, EvaluatedBase.from(block, elevatorBase));

        if (!elevatorBase) {
            removePlayer(uuid);
            return;
        }

        elevatorPlayers.add(uuid);
        updateBossBar(player, block);

        if (!bossBarEnabled) removeBossBar(player);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Location to = event.getTo();

        Location from = event.getFrom();
        if (from.getWorld() == to.getWorld()
                && from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        checkElevator(event.getPlayer(), to);
    }

    /* プレイヤーの位置から足元のブロックを取得（高さ許容範囲を考慮）*/
    private Block getBaseBlock(Location loc) {
        double playerY = loc.getY();
        int baseY = (int) Math.floor(playerY - 1);

        for (int offset = (int) Math.floor(-toleranceHeight); offset <= (int) Math.ceil(toleranceHeight); offset++) {
            int checkY = baseY + offset;
            Block block = loc.getWorld().getBlockAt(loc.getBlockX(), checkY, loc.getBlockZ());

            if (baseBlocks.contains(block.getType())) {
                return block;
            }
        }

        return loc.clone().subtract(0, 1, 0).getBlock();
    }

    /* プレイヤーをエレベータから削除 */
    private void removePlayer(UUID uuid) {
        elevatorPlayers.remove(uuid);
        lastEvaluatedBases.remove(uuid);
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) removeBossBar(player);
    }

    /**
     * ===============================
     * 上昇
     * ===============================
     */
    @EventHandler
    public void onJump(PlayerJumpEvent event) {
        handleTeleport(event.getPlayer(), true);
    }

    /**
     * ===============================
     * 下降
     * ===============================
     */
    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;
        handleTeleport(event.getPlayer(), false);
    }

    /**
     * ===============================
     * テレポート実行
     * ===============================
     */
    private void handleTeleport(Player player, boolean up) {
        if (!elevatorPlayers.contains(player.getUniqueId())) return;

        if (cooldownEnabled && isOnCooldown(player)) {
            showCooldown(player);
            return;
        }

        boolean success = teleportToNextFloor(player, up);

        if (success && cooldownEnabled) startCooldown(player);
    }

    /**
     * ===============================
     * エレベーターフロア取得
     * ===============================
     */
    private List<Integer> getFloors(World world, int x, int z, Material baseType) {
        FloorCacheKey key = new FloorCacheKey(world.getUID(), x, z, baseType, allowMixedBlocks);
        List<Integer> cached = floorCache.get(key);
        if (cached != null) return cached;

        List<Integer> floors = new ArrayList<>();

        for (int y = world.getMinHeight(); y < world.getMaxHeight(); y++) {
            Block b = world.getBlockAt(x, y, z);

            if (!baseBlocks.contains(b.getType())) continue;
            if (!allowMixedBlocks && b.getType() != baseType) continue;

            boolean valid = true;
            for (int i = 1; i <= requiredAirBlocks; i++) {
                Material mat = b.getRelative(0, i, 0).getType();
                if (mat != Material.AIR && !passableBlocks.contains(mat)) {
                    valid = false;
                    break;
                }
            }

            if (valid) {
                floors.add(y);
            }
        }

        List<Integer> immutableFloors = Collections.unmodifiableList(floors);
        floorCache.put(key, immutableFloors);
        return immutableFloors;
    }

    /**
     * ===============================
     * 次のフロア取得
     * ===============================
     */
    private Integer getNextFloor(List<Integer> floors, int currentY, boolean up) {
        if (up) {
            for (int y : floors) if (y > currentY) return y;
        } else {
            for (int i = floors.size()-1; i >= 0; i--) {
                int y = floors.get(i);
                if (y < currentY) return y;
            }
        }
        return null;
    }

    /**
     * ===============================
     * エレベーター判定
     * ===============================
     */
    private boolean isElevatorBase(Block block) {
        if (!baseBlocks.contains(block.getType())) return false;

        for (int i = 1; i <= requiredAirBlocks; i++) {
            Material mat = block.getRelative(0, i, 0).getType();
            if (mat != Material.AIR && !passableBlocks.contains(mat)) return false;
        }

        return getFloors(block.getWorld(), block.getX(), block.getZ(), block.getType()).size() >= 2;
    }

    private boolean hasRequiredSpace(Block block) {
        for (int i = 1; i <= requiredAirBlocks; i++) {
            Material mat = block.getRelative(0, i, 0).getType();
            if (mat != Material.AIR && !passableBlocks.contains(mat)) return false;
        }
        return true;
    }

    /**
     * ===============================
     * テレポート処理
     * ===============================
     */
    private boolean teleportToNextFloor(Player player, boolean up) {
        Location loc = player.getLocation();
        Block base = getBaseBlock(loc);

        List<Integer> floors = getFloors(loc.getWorld(), loc.getBlockX(), loc.getBlockZ(), base.getType());
        Integer targetY = getNextFloor(floors, base.getY(), up);

        if (targetY == null) return false;

        player.teleport(createTeleportLocation(loc, targetY));
        playSound(player, up, false);
        Block newBase = getBaseBlock(player.getLocation());
        updateBossBar(player, newBase);

        int currentFloor = floors.indexOf(targetY) + 1;
        sendElevatorNotification(player, up, false, currentFloor, floors.size());

        return true;
    }

    /* 方向と操作種別に応じてサウンドを再生（遅延・複数同時対応）*/
    private void playSound(Player player, boolean up, boolean isCommand) {
        boolean enabled = isCommand
                ? (up ? cmdUpSoundsEnabled : cmdDownSoundsEnabled)
                : (up ? upSoundsEnabled : downSoundsEnabled);

        if (!enabled) return;

        boolean broadcast = isCommand
                ? (up ? cmdUpSoundsBroadcast : cmdDownSoundsBroadcast)
                : (up ? upSoundsBroadcast : downSoundsBroadcast);

        List<SoundEntry> entries = isCommand
                ? (up ? cmdUpSounds : cmdDownSounds)
                : (up ? upSounds : downSounds);

        for (SoundEntry entry : entries) {
            /* delay: 0 でもテレポート完了パケットの後に確実に届くよう最低1tick待つ */
            long ticks = entry.delayMs() <= 0 ? 1L : Math.max(1L, entry.delayMs() / 50L);
            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (player.isOnline()) {
                    if (broadcast) {
                        /* broadcast: true → ワールドで再生（周囲プレイヤーにも聞こえる）*/
                        player.getWorld().playSound(
                                player.getLocation(),
                                entry.sound(), entry.volume(), entry.pitch());
                    } else {
                        /* broadcast: false → 移動したプレイヤーのみに再生 */
                        player.playSound(player, entry.sound(), entry.volume(), entry.pitch());
                    }
                }
            }, ticks);
        }
    }

    /**
     * ===============================
     * タイトル / アクションバー通知
     * ===============================
     */
    private void sendElevatorNotification(Player player, boolean up, boolean isCommand, int currentFloor, int totalFloors) {
        boolean titleEnabled;
        String titleTemplate;
        String subtitleTemplate;
        int fadeIn;
        int stay;
        int fadeOut;

        if (isCommand) {
            titleEnabled = up ? titleCmdUpEnabled : titleCmdDownEnabled;
            titleTemplate = up ? titleCmdUpTitle : titleCmdDownTitle;
            subtitleTemplate = up ? titleCmdUpSubtitle : titleCmdDownSubtitle;
            fadeIn = up ? titleCmdUpFadeIn : titleCmdDownFadeIn;
            stay = up ? titleCmdUpStay : titleCmdDownStay;
            fadeOut = up ? titleCmdUpFadeOut : titleCmdDownFadeOut;
        } else {
            titleEnabled = up ? titleUpEnabled : titleDownEnabled;
            titleTemplate = up ? titleUpTitle : titleDownTitle;
            subtitleTemplate = up ? titleUpSubtitle : titleDownSubtitle;
            fadeIn = up ? titleUpFadeIn : titleDownFadeIn;
            stay = up ? titleUpStay : titleDownStay;
            fadeOut = up ? titleUpFadeOut : titleDownFadeOut;
        }

        if (titleEnabled) {
            String titleText = titleTemplate
                    .replace("{current}", String.valueOf(currentFloor))
                    .replace("{total}", String.valueOf(totalFloors));
            String subtitleText = subtitleTemplate
                    .replace("{current}", String.valueOf(currentFloor))
                    .replace("{total}", String.valueOf(totalFloors));

            player.showTitle(Title.title(
                    LegacyComponentSerializer.legacySection().deserialize(titleText),
                    LegacyComponentSerializer.legacySection().deserialize(subtitleText),
                    Title.Times.times(
                            Duration.ofMillis(fadeIn * 50L),
                            Duration.ofMillis(stay * 50L),
                            Duration.ofMillis(fadeOut * 50L)
                    )
            ));
        }

        boolean actionBarEnabled;
        String actionBarTemplate;
        if (isCommand) {
            actionBarEnabled = up ? actionBarCmdUpEnabled : actionBarCmdDownEnabled;
            actionBarTemplate = up ? actionBarCmdUpFormat : actionBarCmdDownFormat;
        } else {
            actionBarEnabled = up ? actionBarUpEnabled : actionBarDownEnabled;
            actionBarTemplate = up ? actionBarUpFormat : actionBarDownFormat;
        }

        if (actionBarEnabled) {
            String format = actionBarTemplate
                    .replace("{current}", String.valueOf(currentFloor))
                    .replace("{total}", String.valueOf(totalFloors));
            player.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(format));
        }
    }

    /* テレポート先の座標を作成 */
    private Location createTeleportLocation(Location loc, int targetY) {        return new Location(
                loc.getWorld(),
                loc.getBlockX() + 0.5,
                targetY + 1,
                loc.getBlockZ() + 0.5,
                loc.getYaw(),
                loc.getPitch()
        );
    }
    /**
     * ===============================
     * ジャンプ抑制
     * ===============================
     */
    private void startJumpSuppressTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {

            for (UUID uuid : elevatorPlayers) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) continue;

                if (cooldownEnabled && isOnCooldown(player)) continue;

                Location loc = player.getLocation();
                Block base = getBaseBlock(loc);

                if (!baseBlocks.contains(base.getType()) || !hasRequiredSpace(base)) continue;

                List<Integer> floors = getFloors(loc.getWorld(), loc.getBlockX(), loc.getBlockZ(), base.getType());
                if (floors.size() < 2) continue;
                Integer next = getNextFloor(floors, base.getY(), true);

                if (next == null) continue;

                if (player.getVelocity().getY() > 0) {
                    player.setVelocity(player.getVelocity().setY(0));
                }

            }

        }, 0L, 2L); /* 2tick毎 */
    }

    /**
     * ===============================
     * BossBar更新
     * ===============================
     */
    private void updateBossBar(Player player, Block baseBlock) {
        if (!bossBarEnabled) return;

        UUID uuid = player.getUniqueId();
        List<Integer> floors = getFloors(baseBlock.getWorld(), baseBlock.getX(), baseBlock.getZ(), baseBlock.getType());

        int index = floors.indexOf(baseBlock.getY());
        if (index == -1) return;

        int current = index + 1;
        int total = floors.size();

        org.bukkit.boss.BossBar bar = bossBars.computeIfAbsent(uuid, k -> {
            org.bukkit.boss.BossBar b = Bukkit.createBossBar("", bossBarColor, bossBarStyle);
            b.addPlayer(player);
            return b;
        });

        bar.setColor(bossBarColor);
        bar.setStyle(bossBarStyle);

        String title = bossBarFormat
                .replace("{current}", String.valueOf(current))
                .replace("{total}", String.valueOf(total));

        double progress;

        if (bossBarUseYProgress) {
            /* ===== Y座標ベース ===== */
            int minY = floors.getFirst();
            int maxY = floors.getLast();
            int currentY = baseBlock.getY();

            if (maxY == minY) {
                progress = 1.0;
            } else {
                progress = (double)(currentY - minY) / (maxY - minY);
            }
        } else {
            /* ===== 階数ベース ===== */
            progress = (double)(current - 1) / (total - 1);
        }

        bar.setTitle(title);
        bar.setProgress(Math.min(Math.max(progress, 0), 1));
    }

    private void removeBossBar(Player player) {
        org.bukkit.boss.BossBar bar = bossBars.remove(player.getUniqueId());
        if (bar != null) bar.removeAll();
    }

    /**
     * ===============================
     * クールダウン管理
     * ===============================
     */
    private boolean isOnCooldown(Player player) {
        return System.currentTimeMillis() < cooldowns.getOrDefault(player.getUniqueId(), 0L);
    }

    private void startCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldownMillis);
    }

    private void showCooldown(Player player) {
        double sec = Math.max(0,
                (cooldowns.getOrDefault(player.getUniqueId(), 0L) - System.currentTimeMillis()) / 1000.0);

        player.sendActionBar(
                Component.text(cooldownFormat.replace("{time}", String.format("%.1f", sec)))
        );
    }

    /**
     * コマンド入力 "{type,volume,pitch,delay}, ..." をパースして List<Map> に変換
     * 形式例: {minecraft:entity.experience_orb.pickup,1.0,0.69,0}, {minecraft:block.note_block.pling,0.8,1.2,200}
     */
    private List<Map<String, Object>> parseSoundEntries(String raw) {
        List<Map<String, Object>> result = new ArrayList<>();
        int pos = 0;
        while (pos < raw.length()) {
            int start = raw.indexOf('{', pos);
            if (start == -1) break;
            int end = raw.indexOf('}', start);
            if (end == -1) throw new IllegalArgumentException(msg("parse-sound-unclosed-brace"));

            String[] parts = raw.substring(start + 1, end).split(",", 4);
            if (parts.length != 4) throw new IllegalArgumentException(msg("parse-sound-entry-format"));

            String type    = parts[0].trim();
            String volStr  = parts[1].trim();
            String pitStr  = parts[2].trim();
            String delStr  = parts[3].trim();

            NamespacedKey key = NamespacedKey.fromString(type);
            if (key == null || Registry.SOUNDS.get(key) == null) {
                throw new IllegalArgumentException(msg("parse-sound-invalid-type", "type", type));
            }

            float volume, pitch;
            long  delay;
            try { volume = Float.parseFloat(volStr); }
            catch (NumberFormatException e) { throw new IllegalArgumentException(msg("parse-sound-invalid-volume", "val", volStr)); }
            try { pitch  = Float.parseFloat(pitStr); }
            catch (NumberFormatException e) { throw new IllegalArgumentException(msg("parse-sound-invalid-pitch",  "val", pitStr)); }
            try { delay  = Long.parseLong(delStr); }
            catch (NumberFormatException e) { throw new IllegalArgumentException(msg("parse-sound-invalid-delay",  "val", delStr)); }
            if (delay < 0) throw new IllegalArgumentException(msg("parse-sound-negative-delay"));

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("type",   type);
            map.put("volume", volume);
            map.put("pitch",  pitch);
            map.put("delay",  delay);
            result.add(map);
            pos = end + 1;
        }
        if (result.isEmpty()) throw new IllegalArgumentException(msg("parse-sound-entry-format"));
        return result;
    }

    /* entries パスの値を {type,volume,pitch,delay}, ... 形式で表示 */
    private String formatSoundEntriesDisplay(Object value) {
        if (!(value instanceof List<?> list)) return String.valueOf(value);
        StringBuilder sb = new StringBuilder();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) continue;
            if (!sb.isEmpty()) sb.append(", ");
            Object t = map.get("type");
            Object v = map.get("volume");
            Object p = map.get("pitch");
            Object d = map.get("delay");
            sb.append("{")
              .append(t != null ? t : "?").append(",")
              .append(v != null ? v : "1.0").append(",")
              .append(p != null ? p : "1.0").append(",")
              .append(d != null ? d : "0")
              .append("}");
        }
        return sb.isEmpty() ? "(empty)" : sb.toString();
    }

    /* サウンドエントリリスト読み込み（type / volume / pitch / delay ms）*/
    private List<SoundEntry> loadSoundList(String path) {        FileConfiguration config = getUserConfig();
        List<SoundEntry> entries = new ArrayList<>();
        for (Map<?, ?> map : config.getMapList(path)) {
            String typeStr = map.containsKey("type") ? String.valueOf(map.get("type")) : null;
            if (typeStr == null) continue;
            try {
                NamespacedKey key = NamespacedKey.fromString(typeStr);
                Sound sound = key != null ? Registry.SOUNDS.get(key) : null;
                if (sound == null) {
                    getLogger().warning("Invalid sound type at '" + path + "': " + typeStr);
                    continue;
                }
                float volume    = map.containsKey("volume")    ? ((Number) map.get("volume")).floatValue()  : 1.0f;
                float pitch     = map.containsKey("pitch")     ? ((Number) map.get("pitch")).floatValue()   : 1.0f;
                long  delayMs   = map.containsKey("delay")     ? ((Number) map.get("delay")).longValue()    : 0L;
                entries.add(new SoundEntry(sound, volume, pitch, delayMs));
            } catch (Exception e) {
                getLogger().warning("Invalid sound entry at '" + path + "': " + e.getMessage());
            }
        }
        return entries;
    }

    /* Enum値を安全にパース（存在しない場合はデフォルト値を返す）*/
    private <T extends Enum<T>> T parseEnumSafely(Class<T> enumClass, String value, T defaultValue) {
        if (value == null) return defaultValue;
        try {
            return Enum.valueOf(enumClass, value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            getLogger().warning("Invalid enum value '" + value + "' for " + enumClass.getSimpleName());
            return defaultValue;
        }
    }

    /**
     * ===============================
     * ログイン処理
     * ===============================
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTask(this, () -> checkElevator(player, player.getLocation(), true));
    }

    /**
     * ===============================
     * ログアウト処理
     * ===============================
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        elevatorPlayers.remove(player.getUniqueId());
        lastEvaluatedBases.remove(player.getUniqueId());
        cooldowns.remove(player.getUniqueId());
        pendingResets.remove(getSenderKey(player));
        removeBossBar(player);
    }

    /**
     * ===============================
     * ブロック変更イベント（キャッシュクリア）
     * ===============================
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        clearCache(event.getBlock());
        Bukkit.getScheduler().runTask(this, () -> refreshColumnPlayers(event.getBlock()));
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        clearCache(event.getBlock());
        Bukkit.getScheduler().runTask(this, () -> refreshColumnPlayers(event.getBlock()));
    }

    private void refreshColumnPlayers(Block block) {
        World world = block.getWorld();
        int x = block.getX();
        int z = block.getZ();

        for (Player player : world.getPlayers()) {
            Location location = player.getLocation();
            if (location.getBlockX() == x && location.getBlockZ() == z) {
                checkElevator(player, location, true);
            }
        }
    }

    private void clearCache(Block block) {
        UUID worldUid = block.getWorld().getUID();
        int x = block.getX();
        int z = block.getZ();
        floorCache.keySet().removeIf(key -> key.worldUid().equals(worldUid) && key.x() == x && key.z() == z);
    }

    private void refreshOnlinePlayersElevatorState() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            checkElevator(player, player.getLocation(), true);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!command.getName().equalsIgnoreCase("simplecometelevator")
                && !label.equalsIgnoreCase("sce")
                && !label.equalsIgnoreCase("scelevator")) {
            return false;
        }

        /* move コマンドは管理者権限不要（MOVE_PERMISSION で制御）*/
        if (args.length >= 1 && args[0].equalsIgnoreCase("move")) {
            handleMoveCommand(sender, args, label);
            return true;
        }

        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sendErrorMessage(sender, msg("no-permission", "permission", ADMIN_PERMISSION));
            return true;
        }

        if (args.length == 0) {
            sendCommandHelp(sender, label);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "reload" -> {
                if (args.length != 1) {
                    sendErrorMessage(sender, msg("usage-reload", "label", label));
                    return true;
                }
                sendInfoMessage(sender, msg("configs-reloaded"));
                Bukkit.getScheduler().runTask(this, () -> {
                    Bukkit.getPluginManager().disablePlugin(this);
                    Bukkit.getPluginManager().enablePlugin(this);
                });
            }
            case "check" -> {
                if (args.length != 1) {
                    sendErrorMessage(sender, msg("usage-check", "label", label));
                    return true;
                }
                if (updateChecker == null) {
                    sendErrorMessage(sender, msg("update-checker-unavailable"));
                    return true;
                }
                updateChecker.checkForUpdates();
                sendInfoMessage(sender, msg("update-check-started"));
            }
            case "get" -> {
                if (args.length != 2) {
                    sendErrorMessage(sender, msg("usage-get", "label", label));
                    return true;
                }
                String path = args[1];
                if (!EDITABLE_PATHS.contains(path)) {
                    sendErrorMessage(sender, msg("unsupported-path", "path", path));
                    return true;
                }
                Object value = getUserConfig().get(path);
                String displayValue = isEntriesPath(path)
                        ? formatSoundEntriesDisplay(value)
                        : String.valueOf(value);
                sendInfoMessage(sender, msg("value-display", "path", path, "value", displayValue));
            }
            case "set" -> {
                if (args.length < 3) {
                    sendErrorMessage(sender, msg("usage-set", "label", label));
                    return true;
                }

                String path = args[1];
                if (!EDITABLE_PATHS.contains(path)) {
                    sendErrorMessage(sender, msg("unsupported-path", "path", path));
                    return true;
                }

                String rawValue = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                FileConfiguration config = getUserConfig();
                Object oldValue = config.get(path);

                try {
                    Object parsed = parseConfigValue(path, rawValue);
                    config.set(path, parsed);
                    saveUserConfig();
                    applyConfigRuntime();
                    sendInfoMessage(sender, msg(
                            "updated-value",
                            "path", path,
                            "new", String.valueOf(parsed),
                            "old", String.valueOf(oldValue)
                    ));
                } catch (IllegalArgumentException e) {
                    sendErrorMessage(sender, e.getMessage());
                }
            }
            case "reset" -> {
                if (args.length < 2 || args.length > 3) {
                    sendErrorMessage(sender, msg("usage-reset", "label", label));
                    return true;
                }

                String path = args[1];
                boolean resetAll = path.equalsIgnoreCase(RESET_ALL_KEYWORD);
                if (!resetAll && !EDITABLE_PATHS.contains(path)) {
                    sendErrorMessage(sender, msg("unsupported-path", "path", path));
                    return true;
                }

                if (!resetAll) {
                    if (args.length != 2) {
                        sendErrorMessage(sender, msg("usage-reset", "label", label));
                        return true;
                    }

                    FileConfiguration config = getUserConfig();
                    Object oldValue = config.get(path);
                    config.set(path, null);
                    saveUserConfig();
                    applyConfigRuntime();

                    Object newValue = getUserConfig().get(path);
                    String oldDisplay = isEntriesPath(path)
                            ? formatSoundEntriesDisplay(oldValue)
                            : String.valueOf(oldValue);
                    String newDisplay = isEntriesPath(path)
                            ? formatSoundEntriesDisplay(newValue)
                            : String.valueOf(newValue);

                    sendInfoMessage(sender, msg(
                            "reset-completed",
                            "path", path,
                            "old", oldDisplay,
                            "new", newDisplay
                    ));
                    return true;
                }

                String senderKey = getSenderKey(sender);
                long now = System.currentTimeMillis();

                if (args.length == 2) {
                    pendingResets.put(senderKey, new PendingReset(path, now + RESET_CONFIRM_TIMEOUT_MILLIS));
                    long timeoutSec = RESET_CONFIRM_TIMEOUT_MILLIS / 1000L;
                    sendInfoMessage(sender, msg(
                            "reset-confirm-prompt",
                            "path", RESET_ALL_KEYWORD,
                            "label", label,
                            "keyword", RESET_CONFIRM_KEYWORD,
                            "seconds", String.valueOf(timeoutSec)
                    ));
                    return true;
                }

                if (!args[2].equalsIgnoreCase(RESET_CONFIRM_KEYWORD)) {
                    sendErrorMessage(sender, msg("usage-reset", "label", label));
                    return true;
                }

                PendingReset pending = pendingResets.get(senderKey);
                if (pending == null) {
                    sendErrorMessage(sender, msg("reset-no-pending", "label", label));
                    return true;
                }
                if (pending.expiresAt() < now) {
                    pendingResets.remove(senderKey);
                    sendErrorMessage(sender, msg("reset-expired", "label", label));
                    return true;
                }
                if (!pending.path().equals(path)) {
                    sendErrorMessage(sender, msg("reset-path-mismatch", "pending", pending.path()));
                    return true;
                }

                int resetCount = resetAllEditablePaths();
                pendingResets.remove(senderKey);
                applyConfigRuntime();
                sendInfoMessage(sender, msg("reset-all-completed", "count", String.valueOf(resetCount)));
            }
            case "info" -> sendPluginInfo(sender);
            default -> sendCommandHelp(sender, label);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NotNull [] args) {
        if (!command.getName().equalsIgnoreCase("simplecometelevator")
                && !alias.equalsIgnoreCase("sce")
                && !alias.equalsIgnoreCase("scelevator")) {
            return Collections.emptyList();
        }

        /* 1引数目: 権限に応じてサブコマンド候補を組み立てる */
        if (args.length == 1) {
            List<String> available = new ArrayList<>();
            if (moveCommandEnabled && sender instanceof Player p && p.hasPermission(MOVE_PERMISSION)) {
                available.add("move");
            }
            if (sender.hasPermission(ADMIN_PERMISSION)) {
                available.addAll(ADMIN_SUB_COMMANDS);
            }
            return filterByPrefix(available, args[0]);
        }

        /* move コマンドの階数タブ補完 */
        if (args.length == 2 && args[0].equalsIgnoreCase("move")) {
            if (moveCommandEnabled
                    && sender instanceof Player player
                    && player.hasPermission(MOVE_PERMISSION)
                    && elevatorPlayers.contains(player.getUniqueId())) {
                Location loc = player.getLocation();
                Block base = getBaseBlock(loc);
                List<Integer> floors = getFloors(loc.getWorld(), loc.getBlockX(), loc.getBlockZ(), base.getType());
                List<String> floorNums = new ArrayList<>();
                for (int i = 1; i <= floors.size(); i++) {
                    floorNums.add(String.valueOf(i));
                }
                return filterByPrefix(floorNums, args[1]);
            }
            return Collections.emptyList();
        }

        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            return Collections.emptyList();
        }


        String sub = args[0].toLowerCase(Locale.ROOT);

        if (args.length == 2 && (sub.equals("get") || sub.equals("set") || sub.equals("reset"))) {
            List<String> candidates = new ArrayList<>(EDITABLE_PATHS);
            if (sub.equals("reset")) {
                candidates.add(RESET_ALL_KEYWORD);
            }
            return filterByPrefix(candidates, args[1]);
        }

        if (args.length == 3 && sub.equals("reset") && args[1].equalsIgnoreCase(RESET_ALL_KEYWORD)) {
            return filterByPrefix(List.of(RESET_CONFIRM_KEYWORD), args[2]);
        }

        if (args.length == 3 && sub.equals("set")) {
            String path = args[1];
            if (isEntriesPath(path)) {
                String hint = "{minecraft:entity.experience_orb.pickup,1.0,1.0,0}";
                return args[2].isEmpty() ? List.of(hint) : Collections.emptyList();
            }
            if (isBooleanPath(path)) {
                return filterByPrefix(List.of("true", "false"), args[2]);
            }
            switch (path) {
                case CONFIG_MESSAGES_LANGUAGE -> {
                    return filterByPrefix(List.of("ja", "en"), args[2]);
                }
                case CONFIG_BOSSBAR_COLOR -> {
                    return filterByPrefix(Arrays.stream(org.bukkit.boss.BarColor.values())
                            .map(Enum::name)
                            .toList(), args[2]);
                }
                case CONFIG_BOSSBAR_STYLE -> {
                    return filterByPrefix(Arrays.stream(org.bukkit.boss.BarStyle.values())
                            .map(Enum::name)
                            .toList(), args[2]);
                }
            }
        }

        return Collections.emptyList();
    }

    private void applyConfigRuntime() {
        initializeUserSettings();
        loadConfigValues();
        floorCache.clear();
        lastEvaluatedBases.clear();

        bossBars.values().forEach(org.bukkit.boss.BossBar::removeAll);
        bossBars.clear();

        if (!cooldownEnabled) {
            cooldowns.clear();
        }

        /*===== HisuiPluginUpdateChecker連携: リロード後の再読込・更新確認 =====*/
        if (updateChecker != null) {
            updateChecker.reload();
            updateChecker.checkForUpdates();
        }
        /*===============================================================*/

        refreshOnlinePlayersElevatorState();
    }

    private void sendCommandHelp(CommandSender sender, String label) {
        sendInfoMessage(sender, msg("admin-commands"));
        sender.sendMessage(MESSAGE_PREFIX + msg("help-reload", "label", label));
        sender.sendMessage(MESSAGE_PREFIX + msg("help-check", "label", label));
        sender.sendMessage(MESSAGE_PREFIX + msg("help-get", "label", label));
        sender.sendMessage(MESSAGE_PREFIX + msg("help-set", "label", label));
        sender.sendMessage(MESSAGE_PREFIX + msg("help-reset", "label", label));
        sender.sendMessage(MESSAGE_PREFIX + msg("help-info", "label", label));
        if (moveCommandEnabled) {
            sender.sendMessage(MESSAGE_PREFIX + msg("help-move", "label", label));
        }

        // 著者 → ウェブサイトリンク（右に Ko-fi リンクを配置）
        List<String> authors = getPluginMeta().getAuthors();
        String website = getPluginMeta().getWebsite();
        String kofiUrl = updateChecker != null ? updateChecker.getKofiUrl() : null;
        if (!authors.isEmpty() && website != null && !website.isBlank()) {
            String authorText = String.join(", ", authors);
            sender.sendMessage(buildHelpAuthorLine(authorText, website, kofiUrl));
        }
    }

    private Component buildHelpAuthorLine(String authorText, String websiteUrl, String kofiUrl) {
        Component prefix = LegacyComponentSerializer.legacySection()
                .deserialize(MESSAGE_PREFIX + msg("help-author-label"));
        Component link = Component.text(authorText, NamedTextColor.WHITE)
                .clickEvent(ClickEvent.openUrl(websiteUrl))
                .hoverEvent(HoverEvent.showText(
                        Component.text(msg("info-link-hover") + "\n" + websiteUrl, NamedTextColor.GRAY)));
        Component result = prefix.append(link);

        if (kofiUrl != null && !kofiUrl.isBlank()) {
            Component separator = LegacyComponentSerializer.legacySection().deserialize(" §7| ");
            Component kofiLink = Component.text("Ko-fi", NamedTextColor.GOLD)
                    .clickEvent(ClickEvent.openUrl(kofiUrl))
                    .hoverEvent(HoverEvent.showText(
                            Component.text(msg("info-link-hover") + "\n" + kofiUrl, NamedTextColor.GRAY)));
            result = result.append(separator).append(kofiLink);
        }

        return result;
    }

    private String getSenderKey(CommandSender sender) {
        if (sender instanceof Player player) {
            return "player:" + player.getUniqueId();
        }
        return sender.getClass().getName() + ":" + sender.getName();
    }

    private int resetAllEditablePaths() {
        FileConfiguration config = getUserConfig();
        for (String editablePath : EDITABLE_PATHS) {
            config.set(editablePath, null);
        }
        saveUserConfig();
        return EDITABLE_PATHS.size();
    }

    /**
     * ===============================
     * /sce move <階数> コマンド処理
     * ===============================
     */
    private void handleMoveCommand(CommandSender sender, String[] args, String label) {
        if (!moveCommandEnabled) {
            sendErrorMessage(sender, msg("move-disabled"));
            return;
        }

        if (!(sender instanceof Player player)) {
            sendErrorMessage(sender, msg("move-player-only"));
            return;
        }

        if (!player.hasPermission(MOVE_PERMISSION)) {
            sendErrorMessage(sender, msg("move-no-permission"));
            return;
        }

        if (args.length != 2) {
            sendErrorMessage(sender, msg("usage-move", "label", label));
            return;
        }

        if (!elevatorPlayers.contains(player.getUniqueId())) {
            sendErrorMessage(sender, msg("move-not-in-elevator"));
            return;
        }

        int floorNum;
        try {
            floorNum = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sendErrorMessage(sender, msg("move-floor-invalid"));
            return;
        }

        Location loc = player.getLocation();
        Block base = getBaseBlock(loc);
        List<Integer> floors = getFloors(loc.getWorld(), loc.getBlockX(), loc.getBlockZ(), base.getType());

        if (floorNum < 1 || floorNum > floors.size()) {
            sendErrorMessage(sender, msg("move-floor-out-of-range", "total", String.valueOf(floors.size())));
            return;
        }

        if (cooldownEnabled && isOnCooldown(player)) {
            showCooldown(player);
            return;
        }

        int currentFloor = floors.indexOf(base.getY()) + 1;
        int targetY = floors.get(floorNum - 1);
        boolean goingUp = floorNum >= currentFloor;

        player.teleport(createTeleportLocation(loc, targetY));
        playSound(player, goingUp, true);
        updateBossBar(player, getBaseBlock(player.getLocation()));
        sendElevatorNotification(player, goingUp, true, floorNum, floors.size());

        if (cooldownEnabled) startCooldown(player);
    }

    private void sendInfoMessage(CommandSender sender, String message) {
        sender.sendMessage(MESSAGE_PREFIX + message);
    }

    private void sendErrorMessage(CommandSender sender, String message) {
        sender.sendMessage("§8[§bSimpleCometElevator§8] §c" + message);
    }

    private void sendPluginInfo(CommandSender sender) {
        String version = getPluginMeta().getVersion();
        List<String> authors = getPluginMeta().getAuthors();
        String authorText = authors.isEmpty() ? null : String.join(", ", authors);

        String primaryName = updateChecker != null ? updateChecker.getPrimarySourceDisplayName() : "GitHub";
        String primaryUrl = updateChecker != null ? updateChecker.getPrimarySourceUrl() : null;
        String secondaryName = updateChecker != null ? updateChecker.getSecondarySourceDisplayName() : "Modrinth";
        String secondaryUrl = updateChecker != null ? updateChecker.getSecondarySourceUrl() : null;

        sendInfoMessage(sender, msg("info-title"));
        sendInfoMessage(sender, msg("info-version", "version", version));
        sendInfoMessage(sender, msg("info-description"));
        if (authorText != null) {
            sendInfoMessage(sender, msg("info-author", "author", authorText));
        }
        if (primaryUrl != null && !primaryUrl.isBlank()) {
            sender.sendMessage(buildInfoLinkLine(primaryName, primaryUrl));
        }
        if (secondaryUrl != null && !secondaryUrl.isBlank()) {
            sender.sendMessage(buildInfoLinkLine(secondaryName, secondaryUrl));
        }
    }

    private Component buildInfoLinkLine(String name, String url) {
        Component prefix = LegacyComponentSerializer.legacySection()
                .deserialize(MESSAGE_PREFIX + name + ": ");
        Component link = Component.text(url, NamedTextColor.AQUA)
                .clickEvent(ClickEvent.openUrl(url))
                .hoverEvent(HoverEvent.showText(
                        Component.text(msg("info-link-hover"), NamedTextColor.GRAY)));
        return prefix.append(link);
    }

    private List<String> filterByPrefix(List<String> candidates, String prefix) {
        String normalized = prefix.toLowerCase(Locale.ROOT);
        return candidates.stream()
                .filter(candidate -> candidate.toLowerCase(Locale.ROOT).startsWith(normalized))
                .toList();
    }

    private boolean isEntriesPath(String path) {
        return path.equals(CONFIG_SOUND_UP_ENTRIES)
                || path.equals(CONFIG_SOUND_DOWN_ENTRIES)
                || path.equals(CONFIG_SOUND_CMD_UP_ENTRIES)
                || path.equals(CONFIG_SOUND_CMD_DOWN_ENTRIES);
    }

    private boolean isTitleTimingPath(String path) {
        return path.equals(CONFIG_TITLE_UP_FADE_IN)
                || path.equals(CONFIG_TITLE_UP_STAY)
                || path.equals(CONFIG_TITLE_UP_FADE_OUT)
                || path.equals(CONFIG_TITLE_DOWN_FADE_IN)
                || path.equals(CONFIG_TITLE_DOWN_STAY)
                || path.equals(CONFIG_TITLE_DOWN_FADE_OUT)
                || path.equals(CONFIG_TITLE_CMD_UP_FADE_IN)
                || path.equals(CONFIG_TITLE_CMD_UP_STAY)
                || path.equals(CONFIG_TITLE_CMD_UP_FADE_OUT)
                || path.equals(CONFIG_TITLE_CMD_DOWN_FADE_IN)
                || path.equals(CONFIG_TITLE_CMD_DOWN_STAY)
                || path.equals(CONFIG_TITLE_CMD_DOWN_FADE_OUT);
    }

    private boolean isBooleanPath(String path) {
        return path.equals(CONFIG_ALLOW_MIXED_BLOCKS)
                || path.equals(CONFIG_COOLDOWN_ENABLED)
                || path.equals(CONFIG_COMMAND_MOVE_ENABLED)
                || path.equals(CONFIG_SOUND_UP_ENABLED)
                || path.equals(CONFIG_SOUND_DOWN_ENABLED)
                || path.equals(CONFIG_SOUND_CMD_UP_ENABLED)
                || path.equals(CONFIG_SOUND_CMD_DOWN_ENABLED)
                || path.equals(CONFIG_BOSSBAR_ENABLED)
                || path.equals(CONFIG_BOSSBAR_USE_Y_PROGRESS)
                || path.equals(CONFIG_TITLE_UP_ENABLED)
                || path.equals(CONFIG_TITLE_DOWN_ENABLED)
                || path.equals(CONFIG_TITLE_CMD_UP_ENABLED)
                || path.equals(CONFIG_TITLE_CMD_DOWN_ENABLED)
                || path.equals(CONFIG_ACTIONBAR_UP_ENABLED)
                || path.equals(CONFIG_ACTIONBAR_DOWN_ENABLED)
                || path.equals(CONFIG_ACTIONBAR_CMD_UP_ENABLED)
                || path.equals(CONFIG_ACTIONBAR_CMD_DOWN_ENABLED);
    }

    private Object parseConfigValue(String path, String rawValue) {
        if (isEntriesPath(path)) {
            return parseSoundEntries(rawValue);
        }

        if (path.equals(CONFIG_BASE_BLOCKS) || path.equals(CONFIG_PASSABLE_BLOCKS)) {            List<String> values = Arrays.stream(rawValue.split(","))
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .toList();
            if (values.isEmpty()) {
                throw new IllegalArgumentException(msg("parse-at-least-one-value"));
            }
            return values;
        }

        if (path.equals(CONFIG_MESSAGES_LANGUAGE)) {
            String normalized = rawValue.toLowerCase(Locale.ROOT);
            if (!normalized.equals("ja") && !normalized.equals("en")) {
                throw new IllegalArgumentException(msg("parse-language"));
            }
            return normalized;
        }

        if (isBooleanPath(path)) {
            if (!rawValue.equalsIgnoreCase("true") && !rawValue.equalsIgnoreCase("false")) {
                throw new IllegalArgumentException(msg("parse-boolean"));
            }
            return Boolean.parseBoolean(rawValue);
        }

        if (isTitleTimingPath(path)) {
            try {
                int value = Integer.parseInt(rawValue);
                if (value < 0) throw new IllegalArgumentException(msg("parse-title-timing-min"));
                return value;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(msg("parse-integer"));
            }
        }

        switch (path) {
            case CONFIG_REQUIRED_AIR -> {
                try {
                    int value = Integer.parseInt(rawValue);
                    if (value < 1) {
                        throw new IllegalArgumentException(msg("parse-required-air-min"));
                    }
                    return value;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(msg("parse-integer"));
                }
            }
            case CONFIG_TOLERANCE_HEIGHT, CONFIG_COOLDOWN_SECONDS -> {
                try {
                    return Double.parseDouble(rawValue);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(msg("parse-decimal"));
                }
            }
            case CONFIG_BOSSBAR_COLOR -> {
                String normalized = rawValue.toUpperCase(Locale.ROOT);
                try {
                    org.bukkit.boss.BarColor.valueOf(normalized);
                    return normalized;
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(msg("parse-bossbar-color"));
                }
            }
            case CONFIG_BOSSBAR_STYLE -> {
                String normalized = rawValue.toUpperCase(Locale.ROOT);
                try {
                    org.bukkit.boss.BarStyle.valueOf(normalized);
                    return normalized;
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(msg("parse-bossbar-style"));
                }
            }
        }

        return rawValue;
    }

    private String resolveLanguage(@Nullable String value) {
        if (value == null) return "ja";
        String normalized = value.toLowerCase(Locale.ROOT);
        return normalized.equals("ja") || normalized.equals("en") ? normalized : "ja";
    }

    private String msg(String key, String... replacements) {
        String language = messageLanguage != null ? messageLanguage : "ja";
        String text = getMessageByLanguage(language, key);

        if (text == null) {
            text = getMessageByLanguage("en", key);
        }
        if (text == null) {
            text = getMessageByLanguage("ja", key);
        }
        if (text == null) {
            text = key;
        }

        for (int i = 0; i + 1 < replacements.length; i += 2) {
            text = text.replace("{" + replacements[i] + "}", replacements[i + 1]);
        }
        return text;
    }

    private void initializeUserSettings() {
        settingsFolder = new File(getDataFolder(), SETTINGS_DIR_NAME);
        if (!settingsFolder.exists() && !settingsFolder.mkdirs()) {
            getLogger().warning("Could not create settings folder: " + settingsFolder.getPath());
        }

        ensureSettingsFileExists(CONFIG_FILE_NAME);
        ensureSettingsFileExists(MESSAGES_JA_FILE_NAME);
        ensureSettingsFileExists(MESSAGES_EN_FILE_NAME);
        syncSettingsWithDefaults();

        configFile = new File(settingsFolder, CONFIG_FILE_NAME);
        userConfig = YamlConfiguration.loadConfiguration(configFile);
        loadMessageConfigs();
    }

    private void syncSettingsWithDefaults() {
        mergeMissingKeysFromDefaults(CONFIG_FILE_NAME);
        mergeMissingKeysFromDefaults(MESSAGES_JA_FILE_NAME);
        mergeMissingKeysFromDefaults(MESSAGES_EN_FILE_NAME);
    }

    private void mergeMissingKeysFromDefaults(String resourceFileName) {
        File targetFile = new File(settingsFolder, resourceFileName);
        if (!targetFile.exists()) {
            return;
        }

        YamlConfiguration current = YamlConfiguration.loadConfiguration(targetFile);
        String before = current.saveToString();

        try (InputStream inputStream = getResource(resourceFileName)) {
            if (inputStream == null) {
                return;
            }

            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8)
            );
            current.setDefaults(defaults);
            current.options().copyDefaults(true);

            if (!before.equals(current.saveToString())) {
                current.save(targetFile);
                getLogger().info("Added missing settings entries to: " + targetFile.getPath());
            }
        } catch (IOException e) {
            getLogger().warning("Failed to merge missing settings entries: " + targetFile.getPath() + " (" + e.getMessage() + ")");
        }
    }

    private void ensureSettingsFileExists(String resourceFileName) {
        File targetFile = new File(settingsFolder, resourceFileName);
        if (targetFile.exists()) {
            return;
        }

        try (InputStream inputStream = getResource(resourceFileName)) {
            if (inputStream != null) {
                Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                getLogger().info("Generated missing settings file: " + targetFile.getPath());
                return;
            }

            if (targetFile.createNewFile()) {
                getLogger().warning("Default resource not found. Generated empty file: " + targetFile.getPath());
            }
        } catch (IOException e) {
            getLogger().warning("Failed to generate settings file: " + targetFile.getPath() + " (" + e.getMessage() + ")");
        }
    }

    private FileConfiguration getUserConfig() {
        if (userConfig == null) {
            userConfig = new YamlConfiguration();
        }
        return userConfig;
    }

    private void saveUserConfig() {
        try {
            getUserConfig().save(configFile);
        } catch (IOException e) {
            getLogger().warning("Failed to save settings file: " + configFile.getPath() + " (" + e.getMessage() + ")");
        }
    }

    private void loadMessageConfigs() {
        jaMessages = YamlConfiguration.loadConfiguration(new File(settingsFolder, MESSAGES_JA_FILE_NAME));
        enMessages = YamlConfiguration.loadConfiguration(new File(settingsFolder, MESSAGES_EN_FILE_NAME));
    }

    private @Nullable String getMessageByLanguage(String language, String key) {
        FileConfiguration config = switch (language) {
            case "ja" -> jaMessages;
            case "en" -> enMessages;
            default -> null;
        };
        return config != null ? config.getString(key) : null;
    }
}
