package si.f5.hatosaba.autoupdate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import si.f5.hatosaba.autoupdate.config.MainConfig;
import si.f5.hatosaba.autoupdate.utils.FormatUtil;

import java.util.logging.Level;

public final class AutoUpdate extends JavaPlugin {

    private static AutoUpdate instance;

    @Getter
    private  final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private String pluginTag;

    private MainConfig config;
    private Updater updater;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        pluginTag = ChatColor.GRAY + "[" + ChatColor.DARK_GRAY + getDescription().getName() + ChatColor.GRAY + "]" + ChatColor.RESET + " ";
        config = new MainConfig();
        updater = new Updater();

        new JoinListener();

        getCommand("update").setExecutor(new UpdateCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        config.saveAll();
    }

    public static AutoUpdate getInstance() {
        return instance;
    }

    public Updater getUpdater() {
        return updater;
    }

    public MainConfig getMainConfig(){
        return config;
    }

    public String getPluginTag() {
        return pluginTag;
    }
}
