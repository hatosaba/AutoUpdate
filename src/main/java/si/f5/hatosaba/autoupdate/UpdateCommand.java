package si.f5.hatosaba.autoupdate;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UpdateCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final Updater updater = AutoUpdate.getInstance().getUpdater();
        if(updater.isUpdateAvailable()) {
            updater.update(sender);
        }else {
            sender.sendMessage(AutoUpdate.getInstance().getPluginTag() + "最近版です");
        }

        return true;
    }

}
