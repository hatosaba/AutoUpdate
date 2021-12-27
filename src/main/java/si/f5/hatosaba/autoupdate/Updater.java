package si.f5.hatosaba.autoupdate;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import si.f5.hatosaba.autoupdate.utils.HttpUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Logger;

public class Updater {

    private static HashMap<String, Boolean> lists = new HashMap<>();
    private final Logger LOG = AutoUpdate.getInstance().getLogger();

    private final Map<UUID, Long> lastNotification = new HashMap<>();

    private String updateNotification;

    private long lastCheck;

    private static final long CHECK_DELAY = 1000 * 60 * 10;

    private static final long NOTIFICATION_DELAY = 1000 * 60 * 60 * 20;

    private boolean isUpdateAvailable;

    public Updater() {
        checkUpdate();
    }

    public boolean checkUpdate() {
        lists.put(
                "Aooni", true
        );
        lists.forEach((s, v) -> searchUpdate(s, v));
        return true;
    }

    private String getUpdateNotification() {
        final String version = "新しいバージョンが見つかりました";
        final String automatic = " 次回の再起動時に自動的にインストールされます";
        final String automaticProgress = "ダウンロードを開始します、" + automatic;
        final String automaticDone = "ダウンロードが完了しています! " + automatic;
        final String command = "'/update'と実行すると、インストールされます。";

        updateNotification = version + (true ? automaticDone : command);
        return version + (true ? automaticProgress : command);
    }

    public boolean isUpdateAvailable() {
        return isUpdateAvailable;
    }

    public final void searchUpdate() {
        final long currentTime = new Date().getTime();
        if (lastCheck + CHECK_DELAY > currentTime) {
            return;
        }
        lastCheck = currentTime;

        Bukkit.getScheduler().runTaskAsynchronously(AutoUpdate.getInstance(), () -> {
            LOG.info(getUpdateNotification());
            if (isUpdateAvailable()) {
                update(Bukkit.getConsoleSender());
            }
        });
    }

    public void searchUpdate(String repoName, boolean isPrivate) {
        try {
            Plugin target = Bukkit.getPluginManager().getPlugin(repoName);

            //プラグインが存在しない場合
            if (target == null) {
                update(Bukkit.getConsoleSender());
                return;
            }

            final String buildHash = getManifestValue(target, "Git-Revision");

            if (buildHash == null || buildHash.equalsIgnoreCase("unknown") || buildHash.length() != 40) {
                LOG.warning(ChatColor.DARK_GREEN + "Git-Revisionが利用できませんでした。");
                LOG.warning(ChatColor.DARK_GREEN + "このプラグインバージョンでは、サポートは受けられません。");
                return;
            }

            String masterHash = AutoUpdate.getInstance().getGson().fromJson(HttpUtil.requestHttp("https://api.github.com/repos/hatosaba/" + repoName + "/git/refs/heads/main", isPrivate), JsonObject.class).getAsJsonObject("object").get("sha").getAsString();
            JsonObject masterComparisonResult = AutoUpdate.getInstance().getGson().fromJson(HttpUtil.requestHttp("https://api.github.com/repos/hatosaba/" + repoName + "/compare/" + masterHash + "..." + buildHash + "?per_page=1", isPrivate), JsonObject.class);
            String masterStatus = masterComparisonResult.get("status").getAsString();
            switch (masterStatus.toLowerCase()) {
                case "behind":
                    isUpdateAvailable = true;
                    searchUpdate();
                    return;
                case "identical":
                    isUpdateAvailable= false;
                    return;
                default:
                    LOG.info("Got weird build comparison status from GitHub: " + masterStatus + ". Assuming plugin is up-to-date.");
                    return;
            }

        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    public void sendUpdateNotification(final Player player) {
        searchUpdate();
        if (updateNotification != null && isUpdateAvailable()) {
            final long currentTime = new Date().getTime();
            if (lastNotification.getOrDefault(player.getUniqueId(), 0L) + NOTIFICATION_DELAY > currentTime) {
                return;
            }
            lastNotification.put(player.getUniqueId(), currentTime);
            player.sendMessage(AutoUpdate.getInstance().getPluginTag() + ChatColor.DARK_GREEN + updateNotification);
        }
    }

    public void update(final CommandSender sender) {
        Bukkit.getScheduler().runTaskAsynchronously(AutoUpdate.getInstance(), () -> {
            try {
                sendMessage(sender, ChatColor.DARK_GREEN + "バージョンアップを開始します");
                final File folder = Bukkit.getUpdateFolderFile();
                if (!folder.exists() && !folder.mkdirs()) {
                    throw new Exception("アップデータは '" + folder.getName() + "' フォルダを作成できませんでした !");
                }

                updateDownloadToFile(folder, "Aooni", true);
                sendMessage(sender, ChatColor.DARK_GREEN + "...ダウンロードが終了しました。サーバーを再起動すると、プラグインが更新されます。");
            } catch (final Exception e) {
                sendMessage(sender, ChatColor.RED + e.getMessage());
                LOG.warning("アップデートの実行中にエラーが発生しました" + e.getMessage());
            }
        });
    }

    private void updateDownloadToFileFromURL(final File file, final String pluginName, final boolean isPrivate) {
        String url = AutoUpdate.getInstance().getGson().fromJson(HttpUtil.requestHttp("https://api.github.com/repos/hatosaba/" + pluginName + "/releases/latest", isPrivate), JsonObject.class).getAsJsonArray("assets").get(0).getAsJsonObject().get("url").getAsString();
        if (isPrivate) {
            HttpUtil.downloadFile(url, file, true);
        } else {
            HttpUtil.downloadFile(url, file, false);
        }
    }

    private void updateDownloadToFile(final File folder, final String pluginName, final boolean isPrivate) {
        final File file = new File(folder, pluginName + ".tmp");
        file.deleteOnExit();
        try {
            if (file.exists()) {
                throw new Exception("The file '" + file.getName() + "' already exists!" +
                        " Please wait for the currently running update to finish. If no update is running delete the file manually.");
            }
            if (!file.createNewFile()) {
                throw new Exception("The updater could not create the file '" + file.getName() + "'!");
            }
            updateDownloadToFileFromURL(file, pluginName, isPrivate);
            if (!file.renameTo(new File(folder, pluginName + ".jar"))) {
                throw new Exception("Could not rename the downloaded file."
                        + " Try running '/update' again. If it still does not work use a manual download.");
            }
        } catch (final Exception e) {
            if (file.exists()) {
                final boolean deleted = file.delete();
                if (!deleted) {
                    try {
                        throw new Exception("Download was interrupted! A broken file is in '/plugins/update'."
                                + " Delete this file or the updater will not work anymore. Afterwards you can try running"
                                + " '/update' again. If it still does not work use a manual download.", e);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }
            try {
                throw new Exception("Could not download the file. Try running '/update' again."
                        + " If it still does not work use a manual download.", e);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    private void sendMessage(final CommandSender sender, final String message) {
        LOG.info(message);
        if (sender != null && !(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(AutoUpdate.getInstance().getPluginTag() + message);
        }
    }

    static String getManifestValue(Plugin plugin, String reversion) {
        Map<String, String> attributes = new HashMap<>();
        try {
            Enumeration<URL> resources = plugin.getClass().getClassLoader().getResources(JarFile.MANIFEST_NAME);

            while (resources.hasMoreElements()) {
                InputStream inputStream = resources.nextElement().openStream();
                Manifest manifest = new Manifest(inputStream);
                manifest.getMainAttributes().forEach((key, value) -> attributes.put(key.toString(), (String) value));
                inputStream.close();
            }
        } catch (IOException ignored) {}

        return attributes.get(reversion);
    }
}
