/*
汎用プラグイン更新チェッカー！
HisuiPluginUpdateChecker
Version 1.0.0
*/

/*
  [自分用 HisuiPluginUpdateChecker 使い方メモ]
  [初期化]
  - メイン側で `new HisuiPluginUpdateChecker(this)` を生成
  - `PluginManager#registerEvents(updateChecker, this)` で登録
  - 起動時に `updateChecker.checkForUpdates()` を実行
  - 設定再読込時は `updateChecker.reload()` 後に再チェック

  [差し替える設定 (update-checker.yml)]
  - `plugin.display-name`
  - `sources.primary.page-url` / 必要なら `sources.primary.api-url`
  - `sources.secondary.page-url`
  - `permissions.notify`
  - `versioning.*` (separator と index)

  [使い回し時の確認]
  - `sources.primary.api-url` 未指定時は GitHub URL から API を自動解決
  - latest release が 404 の場合は tags API へフォールバック
  - プレイヤー通知は `permissions.notify` 保有者のみ
*/

package H_Amase.simpleCometElevator;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HisuiPluginUpdateChecker implements Listener {

	/*===== 設定キー・内部定数 =====*/
	private static final String CONFIG_FILE_NAME = "update-checker.yml";
	private static final String JSON_STRING_FIELD_REGEX = "\"%s\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"";
	private static final Pattern VERSION_TOKEN_PATTERN = Pattern.compile("\\d+|[A-Za-z]+");
	private static final String PRIMARY_LINK_PLACEHOLDER = "{primary_link}";
	private static final String SECONDARY_LINK_PLACEHOLDER = "{secondary_link}";
	private static final String LEGACY_DOWNLOAD_LINK_PLACEHOLDER = "{download_link}";
	private static final String LEGACY_MODRINTH_LINK_PLACEHOLDER = "{modrinth_link}";

	/*===== 依存オブジェクト =====*/
	private final JavaPlugin plugin;
	private final HttpClient httpClient;
	private final LegacyComponentSerializer legacySerializer;

	/*===== 設定値 =====*/
	private volatile UpdateCheckerConfig config;

	/*===== 更新状態管理 =====*/
	private volatile boolean updateAvailable;
	private volatile String latestVersion;
	private volatile String latestDownloadUrl;
	private volatile boolean preReleaseAvailable;
	private volatile String preReleaseVersion;

	/*===== Ko-fi URL =====*/
	private volatile String kofiUrl;

	public HisuiPluginUpdateChecker(JavaPlugin plugin) {
		this.plugin = Objects.requireNonNull(plugin, "plugin");
		this.httpClient = HttpClient.newBuilder()
				.followRedirects(HttpClient.Redirect.NORMAL)
				.connectTimeout(Duration.ofSeconds(10))
				.build();
		this.legacySerializer = LegacyComponentSerializer.legacyAmpersand();
		reload();
	}

	/*===== 設定再読み込み =====*/
	public void reload() {
		YamlConfiguration yaml = loadBundledConfig();

		boolean enabled = yaml.getBoolean("enabled", true);
		boolean notifyOnlinePlayersOnCheck = yaml.getBoolean("notify-online-players-on-check", true);
		String pluginDisplayName = firstNonBlank(
				trimToNull(yaml.getString("plugin.display-name")),
				plugin.getName()
		);
		this.kofiUrl = trimToNull(yaml.getString("plugin.kofi-url"));
		String primarySourceUrl = firstNonBlank(
				trimToNull(yaml.getString("sources.primary.page-url")),
				trimToNull(yaml.getString("repository-url"))
		);
		String primarySourceDisplayName = firstNonBlank(
				trimToNull(yaml.getString("sources.primary.display-name")),
				trimToNull(yaml.getString("repository-display-name")),
				"Primary"
		);
		String secondarySourceUrl = firstNonBlank(
				trimToNull(yaml.getString("sources.secondary.page-url")),
				trimToNull(yaml.getString("modrinth-url"))
		);
		String secondarySourceDisplayName = firstNonBlank(
				trimToNull(yaml.getString("sources.secondary.display-name")),
				trimToNull(yaml.getString("modrinth-display-name")),
				"Secondary"
		);
		String apiUrl = firstNonBlank(
				trimToNull(yaml.getString("sources.primary.api-url")),
				trimToNull(yaml.getString("api-url"))
		);
		String tagsApiUrl = trimToNull(yaml.getString("sources.primary.tags-api-url"));
		String tagsPageUrl = trimToNull(yaml.getString("sources.primary.tags-page-url"));
		String latestVersionField = firstNonBlank(
				trimToNull(yaml.getString("sources.primary.latest-version-field")),
				"tag_name"
		);
		String latestVersionFallbackField = firstNonBlank(
				trimToNull(yaml.getString("sources.primary.latest-version-fallback-field")),
				"name"
		);
		String latestDownloadUrlField = firstNonBlank(
				trimToNull(yaml.getString("sources.primary.download-url-field")),
				"html_url"
		);
		String tagsVersionField = firstNonBlank(
				trimToNull(yaml.getString("sources.primary.tags-version-field")),
				"name"
		);
		String notifyPermission = firstNonBlank(
				trimToNull(yaml.getString("permissions.notify")),
				trimToNull(yaml.getString("notify-permission")),
				defaultNotifyPermission()
		);

		int timeoutSeconds = Math.max(3, yaml.getInt("request.timeout-seconds", 10));
		String requestAcceptHeader = firstNonBlank(
				trimToNull(yaml.getString("request.accept-header")),
				"application/vnd.github+json"
		);
		String requestUserAgentTemplate = firstNonBlank(
				trimToNull(yaml.getString("request.user-agent")),
				"{plugin_id}/{current_version}"
		);
		String githubToken = trimToNull(yaml.getString("request.github-token"));
		String versionSeparator = yaml.contains("versioning.separator")
				? defaultString(yaml.getString("versioning.separator"))
				: "-";
		int pluginVersionPartIndex = yaml.getInt("versioning.plugin-version-index", 0);
		int minecraftVersionPartIndex = yaml.getInt("versioning.minecraft-version-index", 1);
		int releaseStatePartIndex = yaml.getInt("versioning.release-state-index", 2);
		boolean enforceMinecraftVersionMatch = yaml.getBoolean("versioning.enforce-minecraft-version-match", true);

		String consoleUpdateAvailableMessage = yaml.getString(
				"messages.console-update-available",
				"&e[UpdateChecker] A new version of {plugin_display_name} is available: &f{latest_version}&e (current: &f{current_version}&e) &7| {primary_source_name}: {primary_link} &7| {secondary_source_name}: {secondary_link}"
		);
		String consoleUpToDateMessage = yaml.getString(
				"messages.console-up-to-date",
				"&7[UpdateChecker] {plugin_display_name} is up to date. Current version: &f{current_version}"
		);
		String consoleCheckFailedMessage = yaml.getString(
				"messages.console-check-failed",
				"&c[UpdateChecker] Failed to check updates for {plugin_display_name}: {error}"
		);
		String playerUpdateAvailableMessage = yaml.getString(
				"messages.player-update-available",
				"&e[UpdateChecker] An update is available for {plugin_display_name}: &f{latest_version}&7 (current: {current_version}) &7| {primary_source_name}: {primary_link} &7| {secondary_source_name}: {secondary_link}"
		);
		String consolePreReleaseAvailableMessage = yaml.getString(
				"messages.console-pre-release-available",
				"&6[UpdateChecker] Pre-release version of {plugin_display_name} available: &f{latest_version}&6 (current: &f{current_version}&6) &7| {primary_source_name}: {primary_link} &7| {secondary_source_name}: {secondary_link}"
		);
		String playerPreReleaseAvailableMessage = yaml.getString(
				"messages.player-pre-release-available",
				"&6[UpdateChecker] Pre-release version available for {plugin_display_name}: &f{latest_version}&7 (current: {current_version}) &7| {primary_source_name}: {primary_link} &7| {secondary_source_name}: {secondary_link}"
		);
		String minecraftVersionMismatchMessage = yaml.getString(
				"messages.minecraft-version-mismatch",
				"&7[UpdateChecker] Update found for {plugin_display_name}, but Minecraft version mismatch. Required: {latest_minecraft_version}, Current: {current_minecraft_version}"
		);
		String consoleRateLimitedMessage = yaml.getString(
				"messages.console-rate-limited",
				"&c[UpdateChecker] GitHub API rate limit exceeded for {plugin_display_name}. Resets at: {rate_limit_reset}"
		);

		this.config = new UpdateCheckerConfig(
				enabled,
				notifyOnlinePlayersOnCheck,
				pluginDisplayName,
				primarySourceUrl,
				primarySourceDisplayName,
				secondarySourceUrl,
				secondarySourceDisplayName,
				apiUrl,
				tagsApiUrl,
				tagsPageUrl,
				latestVersionField,
				latestVersionFallbackField,
				latestDownloadUrlField,
				tagsVersionField,
				notifyPermission,
				timeoutSeconds,
				requestAcceptHeader,
				requestUserAgentTemplate,
				versionSeparator,
				pluginVersionPartIndex,
				minecraftVersionPartIndex,
				releaseStatePartIndex,
				enforceMinecraftVersionMatch,
				consoleUpdateAvailableMessage,
				consoleUpToDateMessage,
				consoleCheckFailedMessage,
				playerUpdateAvailableMessage,
				consolePreReleaseAvailableMessage,
				playerPreReleaseAvailableMessage,
				minecraftVersionMismatchMessage,
				consoleRateLimitedMessage,
				githubToken
		);

		resetStatus();
	}

	/*===== 公開ゲッター =====*/
	public String getPrimarySourceDisplayName() { return config.primarySourceDisplayName; }
	public String getPrimarySourceUrl() { return config.primarySourceUrl; }
	public String getSecondarySourceDisplayName() { return config.secondarySourceDisplayName; }
	public String getSecondarySourceUrl() { return config.secondarySourceUrl; }
	public String getKofiUrl() { return kofiUrl; }

	/*===== 更新チェック =====*/
	public void checkForUpdates() {
		UpdateCheckerConfig cfg = config;
		if (cfg == null || !cfg.enabled) {
			resetStatus();
			return;
		}

		String resolvedApiUrl = resolveApiUrl(cfg);
		if (resolvedApiUrl == null) {
			handleCheckFailure(cfg, "sources.primary.page-url or sources.primary.api-url is invalid");
			return;
		}

		boolean allowTagFallback = resolveTagsApiUrl(cfg) != null;
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> performUpdateCheck(cfg, resolvedApiUrl, allowTagFallback));
	}

	/*===== 参加時通知 =====*/
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (!updateAvailable || !plugin.isEnabled()) {
			return;
		}

		UpdateCheckerConfig cfg = config;
		if (cfg == null) {
			return;
		}

		Player player = event.getPlayer();
		if (!player.hasPermission(cfg.notifyPermission)) {
			return;
		}

		sendPlayerUpdateMessage(cfg, player, preReleaseAvailable);
	}

	/*===== 更新判定 =====*/
	private void performUpdateCheck(UpdateCheckerConfig cfg, String resolvedApiUrl, boolean allowTagFallback) {
		try {
			HttpResponse<String> response = sendRequest(cfg, resolvedApiUrl);
			ReleaseInfo release;

			if (isRateLimited(response)) {
				handleRateLimitExceeded(cfg, extractRateLimitResetTime(response));
				return;
			}

			if (response.statusCode() == 404 && allowTagFallback) {
				release = fetchLatestTag(cfg);
				if (release == null) {
					handleCheckFailure(cfg, "HTTP 404");
					return;
				}
			} else {
				if (response.statusCode() < 200 || response.statusCode() >= 300) {
					handleCheckFailure(cfg, "HTTP " + response.statusCode());
					return;
				}
				release = parseRelease(cfg, response.body());
			}

			if (release.version() == null || release.version().isBlank()) {
				handleCheckFailure(cfg, "Unable to retrieve the latest version information.");
				return;
			}

			String currentVersion = currentVersion();
			VersionInfo currentVersionInfo = parseVersionInfo(cfg, currentVersion);
			VersionInfo latestVersionInfo = parseVersionInfo(cfg, release.version());

			// Minecraftバージョンが一致するかチェック
			if (!isMinecraftVersionCompatible(cfg, currentVersionInfo, latestVersionInfo)) {
				plugin.getLogger().info(stripColors(formatMessage(
						cfg,
						cfg.minecraftVersionMismatchMessage,
						release.version(),
						firstNonBlank(release.downloadUrl(), cfg.primarySourceUrl),
						latestVersionInfo.minecraftVersion,
						currentVersionInfo.minecraftVersion
				)));
				resetStatus();
				return;
			}

			// プラグインバージョンのみで比較
			int versionComparison = compareVersions(latestVersionInfo.pluginVersion, currentVersionInfo.pluginVersion);
			
			if (versionComparison <= 0) {
				// 新しいバージョンがない
				if (!plugin.isEnabled()) {
					return;
				}

				Bukkit.getScheduler().runTask(plugin, () -> {
					if (!plugin.isEnabled()) {
						return;
					}
					plugin.getLogger().info(stripColors(formatMessage(cfg, cfg.consoleUpToDateMessage, currentVersion, cfg.primarySourceUrl, null)));
				});
				resetStatus();
				return;
			}

			// 新しいバージョンがある場合
			boolean isPreRelease = latestVersionInfo.releaseState != null && !latestVersionInfo.releaseState.isEmpty();
			latestVersion = release.version();
			latestDownloadUrl = firstNonBlank(release.downloadUrl(), cfg.primarySourceUrl);
			updateAvailable = true;
			preReleaseAvailable = isPreRelease;
			preReleaseVersion = isPreRelease ? release.version() : null;

			if (!plugin.isEnabled()) {
				return;
			}

			Bukkit.getScheduler().runTask(plugin, () -> {
				if (!plugin.isEnabled()) {
					return;
				}

				if (isPreRelease) {
					plugin.getLogger().warning(stripColors(formatMessage(cfg, cfg.consolePreReleaseAvailableMessage, latestVersion, latestDownloadUrl, null)));
				} else {
					plugin.getLogger().warning(stripColors(formatMessage(cfg, cfg.consoleUpdateAvailableMessage, latestVersion, latestDownloadUrl, null)));
				}
				
				if (cfg.notifyOnlinePlayersOnCheck) {
					notifyOnlineAdmins(cfg, isPreRelease);
				}
			});
		} catch (RateLimitException rle) {
			handleRateLimitExceeded(cfg, rle.resetTime);
		} catch (Exception exception) {
			handleCheckFailure(cfg, exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage());
		}
	}

	private void handleCheckFailure(UpdateCheckerConfig cfg, String errorMessage) {
		resetStatus();
		if (!plugin.isEnabled()) {
			return;
		}
		Bukkit.getScheduler().runTask(plugin, () -> {
			if (!plugin.isEnabled()) {
				return;
			}
			plugin.getLogger().warning(stripColors(formatMessage(cfg, cfg.consoleCheckFailedMessage, null, null, errorMessage)));
		});
	}

	private void notifyOnlineAdmins(UpdateCheckerConfig cfg, boolean isPreRelease) {
		for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			if (onlinePlayer.hasPermission(cfg.notifyPermission)) {
				sendPlayerUpdateMessage(cfg, onlinePlayer, isPreRelease);
			}
		}
	}

	private void sendPlayerUpdateMessage(UpdateCheckerConfig cfg, Player player, boolean isPreRelease) {
		String template = isPreRelease ? cfg.playerPreReleaseAvailableMessage : cfg.playerUpdateAvailableMessage;
		player.sendMessage(buildPlayerMessage(cfg, template, latestVersion, latestDownloadUrl));
	}

	private HttpResponse<String> sendRequest(UpdateCheckerConfig cfg, String url) throws Exception {
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(url))
				.timeout(Duration.ofSeconds(cfg.timeoutSeconds))
				.GET();

		String resolvedAcceptHeader = trimToNull(cfg.requestAcceptHeader);
		if (resolvedAcceptHeader != null) {
			requestBuilder.header("Accept", resolvedAcceptHeader);
		}

		String resolvedUserAgent = trimToNull(resolveRequestUserAgent(cfg));
		if (resolvedUserAgent != null) {
			requestBuilder.header("User-Agent", resolvedUserAgent);
		}

		String resolvedToken = trimToNull(cfg.githubToken);
		if (resolvedToken != null) {
			requestBuilder.header("Authorization", "Bearer " + resolvedToken);
		}

		HttpRequest request = requestBuilder.build();
		return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	}

	private ReleaseInfo parseRelease(UpdateCheckerConfig cfg, String json) {
		String version = firstNonBlank(
				unescapeJson(findJsonField(json, cfg.latestVersionField)),
				unescapeJson(findJsonField(json, cfg.latestVersionFallbackField))
		);
		String downloadUrl = unescapeJson(findJsonField(json, cfg.latestDownloadUrlField));
		return new ReleaseInfo(trimToNull(version), trimToNull(downloadUrl));
	}

	private ReleaseInfo fetchLatestTag(UpdateCheckerConfig cfg) throws Exception {
		String tagsApiUrl = resolveTagsApiUrl(cfg);
		if (tagsApiUrl == null) {
			return null;
		}

		HttpResponse<String> response = sendRequest(cfg, tagsApiUrl);

		if (isRateLimited(response)) {
			throw new RateLimitException(extractRateLimitResetTime(response));
		}

		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			return null;
		}

		String version = unescapeJson(findJsonField(response.body(), cfg.tagsVersionField));
		if (version == null || version.isBlank()) {
			return null;
		}

		return new ReleaseInfo(trimToNull(version), buildTagsPageUrl(cfg));
	}

	private String resolveApiUrl(UpdateCheckerConfig cfg) {
		if (cfg.apiUrl != null) {
			return cfg.apiUrl;
		}

		GitHubRepository repository = parseGitHubRepository(cfg.primarySourceUrl);
		if (repository == null) {
			return null;
		}
		return "https://api.github.com/repos/" + repository.owner() + "/" + repository.repo() + "/releases/latest";
	}

	private String resolveTagsApiUrl(UpdateCheckerConfig cfg) {
		if (cfg.tagsApiUrl != null) {
			return cfg.tagsApiUrl;
		}

		GitHubRepository repository = parseGitHubRepository(cfg.primarySourceUrl);
		if (repository == null) {
			return null;
		}
		return "https://api.github.com/repos/" + repository.owner() + "/" + repository.repo() + "/tags";
	}

	/*===== 通知メッセージ =====*/
	private String formatMessage(UpdateCheckerConfig cfg, String template, String resolvedLatestVersion, String resolvedDownloadUrl, String errorMessage) {
		String downloadLinkUrl = firstNonBlank(resolvedDownloadUrl, latestDownloadUrl, cfg.primarySourceUrl);
		String formatted = formatBaseMessage(cfg, template, resolvedLatestVersion, resolvedDownloadUrl, errorMessage);
		return applyConsoleLinkPlaceholders(cfg, formatted, downloadLinkUrl);
	}

	private String formatMessage(UpdateCheckerConfig cfg, String template, String resolvedLatestVersion, String resolvedDownloadUrl, String latestMcVersion, String currentMcVersion) {
		String downloadLinkUrl = firstNonBlank(resolvedDownloadUrl, latestDownloadUrl, cfg.primarySourceUrl);
		String formatted = formatBaseMessage(cfg, template, resolvedLatestVersion, resolvedDownloadUrl, null);
		formatted = formatted
				.replace("{latest_minecraft_version}", defaultString(latestMcVersion))
				.replace("{current_minecraft_version}", defaultString(currentMcVersion));
		return applyConsoleLinkPlaceholders(cfg, formatted, downloadLinkUrl);
	}

	private String formatBaseMessage(UpdateCheckerConfig cfg, String template, String resolvedLatestVersion, String resolvedDownloadUrl, String errorMessage) {
		String currentVersion = currentVersion();
		return normalizeLegacyPlaceholders(template)
				.replace("{plugin_name}", cfg.pluginDisplayName)
				.replace("{plugin_display_name}", cfg.pluginDisplayName)
				.replace("{plugin_id}", plugin.getName())
				.replace("{current_version}", currentVersion)
				.replace("{latest_version}", defaultString(firstNonBlank(resolvedLatestVersion, latestVersion, currentVersion)))
				.replace("{primary_source_name}", defaultString(cfg.primarySourceDisplayName))
				.replace("{secondary_source_name}", defaultString(cfg.secondarySourceDisplayName))
				.replace("{primary_source_url}", defaultString(cfg.primarySourceUrl))
				.replace("{secondary_source_url}", defaultString(cfg.secondarySourceUrl))
				.replace("{repository_url}", defaultString(cfg.primarySourceUrl))
				.replace("{modrinth_url}", defaultString(cfg.secondarySourceUrl))
				.replace("{download_url}", defaultString(firstNonBlank(resolvedDownloadUrl, latestDownloadUrl, cfg.primarySourceUrl)))
				.replace("{permission}", defaultString(cfg.notifyPermission))
				.replace("{error}", defaultString(errorMessage));
	}

	private Component buildPlayerMessage(UpdateCheckerConfig cfg, String template, String resolvedLatestVersion, String resolvedDownloadUrl) {
		String message = formatBaseMessage(cfg, template, resolvedLatestVersion, resolvedDownloadUrl, null);
		String downloadLinkUrl = firstNonBlank(resolvedDownloadUrl, latestDownloadUrl, cfg.primarySourceUrl);
		Component result = Component.empty();
		int cursor = 0;

		while (cursor < message.length()) {
			int downloadIndex = message.indexOf(PRIMARY_LINK_PLACEHOLDER, cursor);
			int modrinthIndex = message.indexOf(SECONDARY_LINK_PLACEHOLDER, cursor);
			int nextIndex = nextPlaceholderIndex(downloadIndex, modrinthIndex);

			if (nextIndex < 0) {
				return result.append(deserializeLegacy(message.substring(cursor)));
			}

			if (nextIndex > cursor) {
				result = result.append(deserializeLegacy(message.substring(cursor, nextIndex)));
			}

			if (nextIndex == downloadIndex) {
				result = result.append(buildLinkComponent(cfg.primarySourceDisplayName, downloadLinkUrl));
				cursor = downloadIndex + PRIMARY_LINK_PLACEHOLDER.length();
			} else {
				result = result.append(buildLinkComponent(cfg.secondarySourceDisplayName, cfg.secondarySourceUrl));
				cursor = modrinthIndex + SECONDARY_LINK_PLACEHOLDER.length();
			}
		}

		return result;
	}

	private String applyConsoleLinkPlaceholders(UpdateCheckerConfig cfg, String template, String resolvedPrimaryLinkUrl) {
		return template
				.replace(PRIMARY_LINK_PLACEHOLDER, formatConsoleLink(cfg.primarySourceDisplayName, resolvedPrimaryLinkUrl))
				.replace(SECONDARY_LINK_PLACEHOLDER, formatConsoleLink(cfg.secondarySourceDisplayName, cfg.secondarySourceUrl));
	}

	private String normalizeLegacyPlaceholders(String template) {
		return defaultString(template)
				.replace(LEGACY_DOWNLOAD_LINK_PLACEHOLDER, PRIMARY_LINK_PLACEHOLDER)
				.replace(LEGACY_MODRINTH_LINK_PLACEHOLDER, SECONDARY_LINK_PLACEHOLDER);
	}

	private int nextPlaceholderIndex(int first, int second) {
		if (first < 0) {
			return second;
		}
		if (second < 0) {
			return first;
		}
		return Math.min(first, second);
	}

	private Component buildLinkComponent(String label, String url) {
		Component component = deserializeLegacy(defaultString(label));
		if (url == null || url.isBlank()) {
			return component;
		}
		return component
				.clickEvent(ClickEvent.openUrl(url))
				.hoverEvent(HoverEvent.showText(deserializeLegacy("&7" + url)));
	}

	private Component deserializeLegacy(String text) {
		if (text == null || text.isEmpty()) {
			return Component.empty();
		}
		return legacySerializer.deserialize(text);
	}

	private String formatConsoleLink(String label, String url) {
		String resolvedLabel = defaultString(label);
		if (url == null || url.isBlank()) {
			return resolvedLabel;
		}
		return resolvedLabel + " <" + url + ">";
	}

	private String findJsonField(String json, String fieldName) {
		Pattern pattern = Pattern.compile(String.format(JSON_STRING_FIELD_REGEX, Pattern.quote(fieldName)), Pattern.DOTALL);
		Matcher matcher = pattern.matcher(json);
		if (!matcher.find()) {
			return null;
		}
		return matcher.group(1);
	}

	/*===== バージョン比較 =====*/
	private int compareVersions(String left, String right) {
		List<String> leftTokens = tokenizeVersion(left);
		List<String> rightTokens = tokenizeVersion(right);
		int maxSize = Math.max(leftTokens.size(), rightTokens.size());

		for (int index = 0; index < maxSize; index++) {
			String leftToken = index < leftTokens.size() ? leftTokens.get(index) : null;
			String rightToken = index < rightTokens.size() ? rightTokens.get(index) : null;
			int comparison = compareVersionToken(leftToken, rightToken);
			if (comparison != 0) {
				return comparison;
			}
		}

		return 0;
	}

	private VersionInfo parseVersionInfo(UpdateCheckerConfig cfg, String fullVersion) {
		if (fullVersion == null || fullVersion.isBlank()) {
			return new VersionInfo(null, null, null);
		}

		List<String> parts = splitVersionParts(cfg, fullVersion);
		String pluginVersion = extractVersionPart(parts, cfg.pluginVersionPartIndex, fullVersion);
		String minecraftVersion = extractVersionPart(parts, cfg.minecraftVersionPartIndex, null);
		String releaseState = extractVersionPart(parts, cfg.releaseStatePartIndex, null);

		return new VersionInfo(pluginVersion, minecraftVersion, releaseState);
	}

	private boolean isMinecraftVersionCompatible(UpdateCheckerConfig cfg, VersionInfo currentVersionInfo, VersionInfo latestVersionInfo) {
		if (!cfg.enforceMinecraftVersionMatch) {
			return true;
		}
		if (currentVersionInfo.minecraftVersion == null || latestVersionInfo.minecraftVersion == null) {
			return true;
		}
		return currentVersionInfo.minecraftVersion.equalsIgnoreCase(latestVersionInfo.minecraftVersion);
	}

	private List<String> splitVersionParts(UpdateCheckerConfig cfg, String fullVersion) {
		String separator = cfg.versionSeparator;
		if (separator == null || separator.isEmpty()) {
			return List.of(fullVersion);
		}

		String[] rawParts = fullVersion.split(Pattern.quote(separator), -1);
		List<String> parts = new ArrayList<>(rawParts.length);
        Collections.addAll(parts, rawParts);
		return parts;
	}

	private String extractVersionPart(List<String> parts, int partIndex, String fallbackValue) {
		if (partIndex < 0 || partIndex >= parts.size()) {
			return trimToNull(fallbackValue);
		}
		return trimToNull(parts.get(partIndex));
	}

	private List<String> tokenizeVersion(String version) {
		List<String> tokens = new ArrayList<>();
		Matcher matcher = VERSION_TOKEN_PATTERN.matcher(defaultString(version));
		while (matcher.find()) {
			tokens.add(matcher.group());
		}
		return tokens;
	}

	private int compareVersionToken(String leftToken, String rightToken) {
		if (Objects.equals(leftToken, rightToken)) {
			return 0;
		}
		if (leftToken == null) {
			return compareMissingAgainst(rightToken);
		}
		if (rightToken == null) {
			return -compareMissingAgainst(leftToken);
		}

		boolean leftNumeric = leftToken.chars().allMatch(Character::isDigit);
		boolean rightNumeric = rightToken.chars().allMatch(Character::isDigit);

		if (leftNumeric && rightNumeric) {
			return Integer.compare(Integer.parseInt(leftToken), Integer.parseInt(rightToken));
		}
		if (leftNumeric) {
			return 1;
		}
		if (rightNumeric) {
			return -1;
		}

		int leftQualifierRank = qualifierRank(leftToken);
		int rightQualifierRank = qualifierRank(rightToken);
		if (leftQualifierRank != rightQualifierRank) {
			return Integer.compare(leftQualifierRank, rightQualifierRank);
		}

		return leftToken.compareToIgnoreCase(rightToken);
	}

	private int compareMissingAgainst(String token) {
		if (token == null) {
			return 0;
		}
		if (token.chars().allMatch(Character::isDigit)) {
			return -1;
		}
		return isPreReleaseQualifier(token) ? 1 : -1;
	}

	private boolean isPreReleaseQualifier(String token) {
		String normalized = token.toLowerCase(Locale.ROOT);
		return normalized.equals("snapshot")
				|| normalized.equals("alpha")
				|| normalized.equals("beta")
				|| normalized.equals("rc")
				|| normalized.equals("pre");
	}

	private int qualifierRank(String token) {
		String normalized = token.toLowerCase(Locale.ROOT);
		return switch (normalized) {
			case "snapshot" -> 10;
			case "alpha" -> 20;
			case "beta" -> 30;
			case "rc", "pre" -> 40;
			default -> 25;
		};
	}

	/*===== 設定読込 (JAR内固定) =====*/
	private YamlConfiguration loadBundledConfig() {
		InputStream resourceStream = plugin.getResource(CONFIG_FILE_NAME);
		if (resourceStream == null) {
			plugin.getLogger().warning("Bundled resource not found: " + CONFIG_FILE_NAME + " (using defaults)");
			return new YamlConfiguration();
		}

		try (InputStream input = resourceStream;
			 InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
			return YamlConfiguration.loadConfiguration(reader);
		}
		catch (Exception exception) {
			plugin.getLogger().warning("Failed to load bundled resource " + CONFIG_FILE_NAME + ": " + exception.getMessage());
			return new YamlConfiguration();
		}
	}

	private void resetStatus() {
		updateAvailable = false;
		latestVersion = null;
		latestDownloadUrl = null;
		preReleaseAvailable = false;
		preReleaseVersion = null;
	}

	private String colorize(String message) {
		return defaultString(message).replaceAll("(?i)&([0-9A-FK-OR])", "§$1");
	}

	private String stripColors(String message) {
		return colorize(message).replaceAll("(?i)§[0-9A-FK-ORX]", "");
	}

	private String currentVersion() {
		return plugin.getPluginMeta().getVersion();
	}

	private String defaultString(String value) {
		return value == null ? "" : value;
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private String firstNonBlank(String... values) {
		for (String value : values) {
			if (value != null && !value.isBlank()) {
				return value;
			}
		}
		return null;
	}

	private String resolveRequestUserAgent(UpdateCheckerConfig cfg) {
		return defaultString(cfg.requestUserAgentTemplate)
				.replace("{plugin_name}", cfg.pluginDisplayName)
				.replace("{plugin_display_name}", cfg.pluginDisplayName)
				.replace("{plugin_id}", plugin.getName())
				.replace("{current_version}", currentVersion());
	}

	private String defaultNotifyPermission() {
		String normalizedPluginName = plugin.getName()
				.toLowerCase(Locale.ROOT)
				.replaceAll("[^a-z0-9._-]", "");
		return normalizedPluginName.isBlank() ? "plugin.admin" : normalizedPluginName + ".admin";
	}

	private GitHubRepository parseGitHubRepository(String repositoryPageUrl) {
		String resolvedUrl = trimToNull(repositoryPageUrl);
		if (resolvedUrl == null) {
			return null;
		}

		try {
			URI uri = URI.create(resolvedUrl);
			String host = uri.getHost();
			if (host == null || !host.toLowerCase(Locale.ROOT).endsWith("github.com")) {
				return null;
			}

			String[] segments = uri.getPath().split("/");
			List<String> pathParts = new ArrayList<>();
			for (String segment : segments) {
				if (!segment.isBlank()) {
					pathParts.add(segment);
				}
			}

			if (pathParts.size() < 2) {
				return null;
			}

			String owner = pathParts.get(0);
			String repo = pathParts.get(1).replaceFirst("\\.git$", "");
			return new GitHubRepository(owner, repo);
		} catch (IllegalArgumentException exception) {
			return null;
		}
	}

	private String buildTagsPageUrl(UpdateCheckerConfig cfg) {
		String base = firstNonBlank(cfg.tagsPageUrl, trimToNull(cfg.primarySourceUrl));
		if (base == null) {
			return null;
		}
		return base.endsWith("/") ? base.substring(0, base.length() - 1) + "/tags" : base + "/tags";
	}

	private String unescapeJson(String value) {
		if (value == null) {
			return null;
		}
		return value
				.replace("\\/", "/")
				.replace("\\\"", "\"")
				.replace("\\n", "\n")
				.replace("\\r", "\r")
				.replace("\\t", "\t")
				.replace("\\\\", "\\");
	}

	private record ReleaseInfo(String version, String downloadUrl) {
	}

	private record VersionInfo(String pluginVersion, String minecraftVersion, String releaseState) {
	}

	private record GitHubRepository(String owner, String repo) {
	}

	private record UpdateCheckerConfig(boolean enabled, boolean notifyOnlinePlayersOnCheck, String pluginDisplayName,
									   String primarySourceUrl, String primarySourceDisplayName,
									   String secondarySourceUrl, String secondarySourceDisplayName, String apiUrl,
									   String tagsApiUrl, String tagsPageUrl, String latestVersionField,
									   String latestVersionFallbackField, String latestDownloadUrlField,
									   String tagsVersionField, String notifyPermission, int timeoutSeconds,
									   String requestAcceptHeader, String requestUserAgentTemplate,
									   String versionSeparator, int pluginVersionPartIndex,
									   int minecraftVersionPartIndex, int releaseStatePartIndex,
									   boolean enforceMinecraftVersionMatch, String consoleUpdateAvailableMessage,
									   String consoleUpToDateMessage, String consoleCheckFailedMessage,
									   String playerUpdateAvailableMessage, String consolePreReleaseAvailableMessage,
									   String playerPreReleaseAvailableMessage, String minecraftVersionMismatchMessage,
									   String consoleRateLimitedMessage, String githubToken) {
	}

	private static final class RateLimitException extends Exception {
		final String resetTime;

		RateLimitException(String resetTime) {
			super("GitHub API rate limit exceeded");
			this.resetTime = resetTime;
		}
	}

	/*===== レート制限ユーティリティ =====*/
	private boolean isRateLimited(HttpResponse<String> response) {
		if (response.statusCode() == 429) {
			return true;
		}
		if (response.statusCode() == 403) {
			return response.headers().firstValue("X-RateLimit-Remaining")
					.map("0"::equals)
					.orElse(false);
		}
		return false;
	}

	private String extractRateLimitResetTime(HttpResponse<String> response) {
		// X-RateLimit-Reset ヘッダー (Unix タイムスタンプ) を優先
		return response.headers().firstValue("X-RateLimit-Reset")
				.map(v -> {
					try {
						long epochSeconds = Long.parseLong(v);
						long secondsLeft = epochSeconds - System.currentTimeMillis() / 1000;
						String timeStr = Instant.ofEpochSecond(epochSeconds)
								.atZone(ZoneId.systemDefault())
								.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
						if (secondsLeft > 0) {
							long m = secondsLeft / 60;
							long s = secondsLeft % 60;
							return timeStr + " (in " + (m > 0 ? m + "m " : "") + s + "s)";
						}
						return timeStr;
					} catch (NumberFormatException ignored) {
						return v;
					}
				})
				.or(() -> response.headers().firstValue("Retry-After").map(v -> {
					try {
						long secs = Long.parseLong(v);
						long m = secs / 60;
						long s = secs % 60;
						return "in " + (m > 0 ? m + "m " : "") + s + "s";
					} catch (NumberFormatException ignored) {
						return v;
					}
				}))
				.orElse("unknown");
	}

	private void handleRateLimitExceeded(UpdateCheckerConfig cfg, String resetTime) {
		resetStatus();
		if (!plugin.isEnabled()) {
			return;
		}
		Bukkit.getScheduler().runTask(plugin, () -> {
			if (!plugin.isEnabled()) {
				return;
			}
			String message = defaultString(cfg.consoleRateLimitedMessage)
					.replace("{plugin_name}", cfg.pluginDisplayName)
					.replace("{plugin_display_name}", cfg.pluginDisplayName)
					.replace("{plugin_id}", plugin.getName())
					.replace("{current_version}", currentVersion())
					.replace("{rate_limit_reset}", defaultString(resetTime));
			plugin.getLogger().warning(stripColors(message));
		});
	}
}
