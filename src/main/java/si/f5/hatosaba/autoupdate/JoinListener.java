package si.f5.hatosaba.autoupdate;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    public JoinListener() {
        Bukkit.getPluginManager().registerEvents(this, AutoUpdate.getInstance());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if(player.hasPermission("autoupdate.update")) {
            AutoUpdate.getInstance().getUpdater().sendUpdateNotification(event.getPlayer());
        }
    }
}
