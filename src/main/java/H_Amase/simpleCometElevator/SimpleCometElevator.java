package H_Amase.simpleCometElevator;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

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
    private static final String CONFIG_SOUND_UP_ENABLED = "elevator.sound.up.enabled";
    private static final String CONFIG_SOUND_UP_TYPE = "elevator.sound.up.type";
    private static final String CONFIG_SOUND_UP_VOLUME = "elevator.sound.up.volume";
    private static final String CONFIG_SOUND_UP_PITCH = "elevator.sound.up.pitch";
    private static final String CONFIG_SOUND_DOWN_ENABLED = "elevator.sound.down.enabled";
    private static final String CONFIG_SOUND_DOWN_TYPE = "elevator.sound.down.type";
    private static final String CONFIG_SOUND_DOWN_VOLUME = "elevator.sound.down.volume";
    private static final String CONFIG_SOUND_DOWN_PITCH = "elevator.sound.down.pitch";
    private static final String CONFIG_BOSSBAR_ENABLED = "elevator.floorbar.enabled";
    private static final String CONFIG_BOSSBAR_COLOR = "elevator.floorbar.color";
    private static final String CONFIG_BOSSBAR_STYLE = "elevator.floorbar.style";
    private static final String CONFIG_BOSSBAR_FORMAT = "elevator.floorbar.format";
    private static final String CONFIG_BOSSBAR_USE_Y_PROGRESS = "elevator.floorbar.use-y-progress";

    /*===== プレイヤー状態管理 =====*/
    private final Set<UUID> elevatorPlayers = new HashSet<>();
    private final Map<UUID, org.bukkit.boss.BossBar> bossBars = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<FloorCacheKey, List<Integer>> floorCache = new HashMap<>();
    private final Map<UUID, EvaluatedBase> lastEvaluatedBases = new HashMap<>();

    /*===== 設定値 =====*/
    private Set<Material> baseBlocks;
    private Set<Material> passableBlocks;
    private int requiredAirBlocks;
    private double toleranceHeight;
    private boolean allowMixedBlocks;

    private boolean cooldownEnabled;
    private long cooldownMillis;
    private String cooldownFormat;

    private boolean bossBarEnabled;
    private org.bukkit.boss.BarColor bossBarColor;
    private org.bukkit.boss.BarStyle bossBarStyle;
    private String bossBarFormat;
    private boolean bossBarUseYProgress;

    private boolean upSoundEnabled;
    private boolean downSoundEnabled;
    private Sound upSound, downSound;
    private float upVolume, upPitch;
    private float downVolume, downPitch;

    private static final String ADMIN_PERMISSION = "simplecometelevator.admin";
    private static final List<String> SUB_COMMANDS = List.of("reload", "get", "set", "help");
    private static final List<String> EDITABLE_PATHS = List.of(
            CONFIG_BASE_BLOCKS,
            CONFIG_PASSABLE_BLOCKS,
            CONFIG_REQUIRED_AIR,
            CONFIG_TOLERANCE_HEIGHT,
            CONFIG_ALLOW_MIXED_BLOCKS,
            CONFIG_COOLDOWN_ENABLED,
            CONFIG_COOLDOWN_SECONDS,
            CONFIG_COOLDOWN_FORMAT,
            CONFIG_SOUND_UP_ENABLED,
            CONFIG_SOUND_UP_TYPE,
            CONFIG_SOUND_UP_VOLUME,
            CONFIG_SOUND_UP_PITCH,
            CONFIG_SOUND_DOWN_ENABLED,
            CONFIG_SOUND_DOWN_TYPE,
            CONFIG_SOUND_DOWN_VOLUME,
            CONFIG_SOUND_DOWN_PITCH,
            CONFIG_BOSSBAR_ENABLED,
            CONFIG_BOSSBAR_COLOR,
            CONFIG_BOSSBAR_STYLE,
            CONFIG_BOSSBAR_FORMAT,
            CONFIG_BOSSBAR_USE_Y_PROGRESS
    );

    /**
     * ===============================
     * プラグイン初期化
     * ===============================
     */
    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfigValues();
        getServer().getPluginManager().registerEvents(this, this);

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
     * ===============================
     * 設定読み込み
     * ===============================
     */
    private void loadConfigValues() {

        /*===== フロアブロック =====*/
        baseBlocks = loadMaterialSet(CONFIG_BASE_BLOCKS, "base-block");
        passableBlocks = loadMaterialSet(CONFIG_PASSABLE_BLOCKS, "passable-block");
        getLogger().info("Base blocks: " + baseBlocks);
        getLogger().info("Passable blocks: " + passableBlocks);
        requiredAirBlocks = getConfig().getInt(CONFIG_REQUIRED_AIR);
        toleranceHeight = getConfig().getDouble(CONFIG_TOLERANCE_HEIGHT);
        allowMixedBlocks = getConfig().getBoolean(CONFIG_ALLOW_MIXED_BLOCKS);

        /*===== クールダウン =====*/
        cooldownEnabled = getConfig().getBoolean(CONFIG_COOLDOWN_ENABLED);
        cooldownMillis = (long)(getConfig().getDouble(CONFIG_COOLDOWN_SECONDS) * 1000);
        cooldownFormat = getConfig().getString(CONFIG_COOLDOWN_FORMAT);

        /*===== サウンド =====*/
        upSoundEnabled = getConfig().getBoolean(CONFIG_SOUND_UP_ENABLED);
        upSound = getSound(CONFIG_SOUND_UP_TYPE);
        upVolume = (float)getConfig().getDouble(CONFIG_SOUND_UP_VOLUME);
        upPitch = (float)getConfig().getDouble(CONFIG_SOUND_UP_PITCH);

        downSoundEnabled = getConfig().getBoolean(CONFIG_SOUND_DOWN_ENABLED);
        downSound = getSound(CONFIG_SOUND_DOWN_TYPE);
        downVolume = (float)getConfig().getDouble(CONFIG_SOUND_DOWN_VOLUME);
        downPitch = (float)getConfig().getDouble(CONFIG_SOUND_DOWN_PITCH);

        /*===== BossBar =====*/
        bossBarEnabled = getConfig().getBoolean(CONFIG_BOSSBAR_ENABLED);
        bossBarColor = parseEnumSafely(
                org.bukkit.boss.BarColor.class,
                getConfig().getString(CONFIG_BOSSBAR_COLOR),
                org.bukkit.boss.BarColor.BLUE
        );
        bossBarStyle = parseEnumSafely(
                org.bukkit.boss.BarStyle.class,
                getConfig().getString(CONFIG_BOSSBAR_STYLE),
                org.bukkit.boss.BarStyle.SOLID
        );
        bossBarFormat = getConfig().getString(CONFIG_BOSSBAR_FORMAT);
        bossBarUseYProgress = getConfig().getBoolean(CONFIG_BOSSBAR_USE_Y_PROGRESS);
    }
    /*===== マテリアルパーサ =====*/
    private Set<Material> loadMaterialSet(String path, String logName) {
        Set<Material> set = new HashSet<>();
        Set<Material> excludeSet = new HashSet<>();

        for (String name : getConfig().getStringList(path)) {
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

        playSound(player, loc, up);
        player.teleport(createTeleportLocation(loc, targetY));
        updateBossBar(player, getBaseBlock(player.getLocation()));
        return true;
    }

    /* 方向に応じてサウンドを再生 */
    private void playSound(Player player, Location loc, boolean up) {
        if (up && upSoundEnabled && upSound != null) {
            player.playSound(loc, upSound, upVolume, upPitch);
        } else if (!up && downSoundEnabled && downSound != null) {
            player.playSound(loc, downSound, downVolume, downPitch);
        }
    }

    /* テレポート先の座標を作成 */
    private Location createTeleportLocation(Location loc, int targetY) {
        return new Location(
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

    /* サウンド取得 */
    private Sound getSound(String path) {
        String keyStr = getConfig().getString(path);
        if (keyStr == null) return null;

        try {
            NamespacedKey key = NamespacedKey.fromString(keyStr);
            return key != null ? Registry.SOUNDS.get(key) : null;
        } catch (Exception e) {
            getLogger().warning("Invalid sound key: " + keyStr);
            return null;
        }
    }

    /* Enum値を安全にパース（存在しない場合はデフォルト値を返す）*/
    private <T extends Enum<T>> T parseEnumSafely(Class<T> enumClass, String value, T defaultValue) {
        if (value == null) return defaultValue;
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            getLogger().warning("Invalid enum value '" + value + "' for " + enumClass.getSimpleName());
            return defaultValue;
        }
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

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!command.getName().equalsIgnoreCase("simplecometelevator")) {
            return false;
        }

        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage("§cこのコマンドを実行する権限がありません。(" + ADMIN_PERMISSION + ")");
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
                    sender.sendMessage("§c使い方: /" + label + " reload");
                    return true;
                }
                reloadConfig();
                applyConfigRuntime();
                sender.sendMessage("§aSimpleCometElevatorの設定をリロードしました。");
            }
            case "get" -> {
                if (args.length != 2) {
                    sender.sendMessage("§c使い方: /" + label + " get <path>");
                    return true;
                }
                String path = args[1];
                if (!EDITABLE_PATHS.contains(path)) {
                    sender.sendMessage("§c未対応のpathです: " + path);
                    return true;
                }
                Object value = getConfig().get(path);
                sender.sendMessage("§e" + path + "§7 = §b" + value);
            }
            case "set" -> {
                if (args.length < 3) {
                    sender.sendMessage("§c使い方: /" + label + " set <path> <value>");
                    return true;
                }

                String path = args[1];
                if (!EDITABLE_PATHS.contains(path)) {
                    sender.sendMessage("§c未対応のpathです: " + path);
                    return true;
                }

                String rawValue = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                Object oldValue = getConfig().get(path);

                try {
                    Object parsed = parseConfigValue(path, rawValue);
                    getConfig().set(path, parsed);
                    saveConfig();
                    applyConfigRuntime();
                    sender.sendMessage("§a設定を更新しました: §e" + path + "§7 = §b" + parsed + " §7(旧: " + oldValue + ")");
                } catch (IllegalArgumentException e) {
                    sender.sendMessage("§c" + e.getMessage());
                }
            }
            case "help" -> sendCommandHelp(sender, label);
            default -> sendCommandHelp(sender, label);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NotNull [] args) {
        if (!command.getName().equalsIgnoreCase("simplecometelevator")) {
            return Collections.emptyList();
        }

        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return filterByPrefix(SUB_COMMANDS, args[0]);
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (args.length == 2 && (sub.equals("get") || sub.equals("set"))) {
            return filterByPrefix(EDITABLE_PATHS, args[1]);
        }

        if (args.length == 3 && sub.equals("set")) {
            String path = args[1];
            if (isBooleanPath(path)) {
                return filterByPrefix(List.of("true", "false"), args[2]);
            }
            if (path.equals(CONFIG_BOSSBAR_COLOR)) {
                return filterByPrefix(Arrays.stream(org.bukkit.boss.BarColor.values())
                        .map(Enum::name)
                        .toList(), args[2]);
            }
            if (path.equals(CONFIG_BOSSBAR_STYLE)) {
                return filterByPrefix(Arrays.stream(org.bukkit.boss.BarStyle.values())
                        .map(Enum::name)
                        .toList(), args[2]);
            }
        }

        return Collections.emptyList();
    }

    private void applyConfigRuntime() {
        loadConfigValues();
        floorCache.clear();
        lastEvaluatedBases.clear();

        if (!bossBarEnabled) {
            bossBars.values().forEach(org.bukkit.boss.BossBar::removeAll);
            bossBars.clear();
        }

        if (!cooldownEnabled) {
            cooldowns.clear();
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            checkElevator(player, player.getLocation(), true);
        }
    }

    private void sendCommandHelp(CommandSender sender, String label) {
        sender.sendMessage("§6=== SimpleCometElevator Admin ===");
        sender.sendMessage("§e/" + label + " reload §7- 設定を再読み込み");
        sender.sendMessage("§e/" + label + " get <path> §7- 現在値を表示");
        sender.sendMessage("§e/" + label + " set <path> <value> §7- 設定値を変更して保存");
    }

    private List<String> filterByPrefix(List<String> candidates, String prefix) {
        String normalized = prefix.toLowerCase(Locale.ROOT);
        return candidates.stream()
                .filter(candidate -> candidate.toLowerCase(Locale.ROOT).startsWith(normalized))
                .toList();
    }

    private boolean isBooleanPath(String path) {
        return path.equals(CONFIG_ALLOW_MIXED_BLOCKS)
                || path.equals(CONFIG_COOLDOWN_ENABLED)
                || path.equals(CONFIG_SOUND_UP_ENABLED)
                || path.equals(CONFIG_SOUND_DOWN_ENABLED)
                || path.equals(CONFIG_BOSSBAR_ENABLED)
                || path.equals(CONFIG_BOSSBAR_USE_Y_PROGRESS);
    }

    private Object parseConfigValue(String path, String rawValue) {
        if (path.equals(CONFIG_BASE_BLOCKS) || path.equals(CONFIG_PASSABLE_BLOCKS)) {
            List<String> values = Arrays.stream(rawValue.split(","))
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .toList();
            if (values.isEmpty()) {
                throw new IllegalArgumentException("値が空です。カンマ区切りで1つ以上指定してください。");
            }
            return values;
        }

        if (isBooleanPath(path)) {
            if (!rawValue.equalsIgnoreCase("true") && !rawValue.equalsIgnoreCase("false")) {
                throw new IllegalArgumentException("boolean値を指定してください: true or false");
            }
            return Boolean.parseBoolean(rawValue);
        }

        switch (path) {
            case CONFIG_REQUIRED_AIR -> {
                try {
                    int value = Integer.parseInt(rawValue);
                    if (value < 1) {
                        throw new IllegalArgumentException("required-airは1以上を指定してください。");
                    }
                    return value;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("整数値を指定してください。");
                }
            }
            case CONFIG_TOLERANCE_HEIGHT, CONFIG_COOLDOWN_SECONDS, CONFIG_SOUND_UP_VOLUME, CONFIG_SOUND_UP_PITCH,
                 CONFIG_SOUND_DOWN_VOLUME, CONFIG_SOUND_DOWN_PITCH -> {
                try {
                    return Double.parseDouble(rawValue);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("小数値を指定してください。");
                }
            }
            case CONFIG_BOSSBAR_COLOR -> {
                String normalized = rawValue.toUpperCase(Locale.ROOT);
                try {
                    org.bukkit.boss.BarColor.valueOf(normalized);
                    return normalized;
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("BossBar colorが不正です。例: BLUE, GREEN, RED");
                }
            }
            case CONFIG_BOSSBAR_STYLE -> {
                String normalized = rawValue.toUpperCase(Locale.ROOT);
                try {
                    org.bukkit.boss.BarStyle.valueOf(normalized);
                    return normalized;
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("BossBar styleが不正です。例: SOLID, SEGMENTED_10");
                }
            }
        }

        return rawValue;
    }
}
