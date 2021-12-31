package si.f5.hatosaba.autoupdate;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class UpdateCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final Updater updater = AutoUpdate.getInstance().getUpdater();
        updater.update(sender);
        return true;
    }

}
