package si.f5.hatosaba.autoupdate.config;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

public class MainConfig extends Config {

    private @Getter String accessToken;

    public MainConfig(){
        super("config.yml");
        load();
    }

    @Override
    public void load() {
        FileConfiguration config = config();

        this.accessToken = config.getString("accessToken");
    }

    public void saveAll() {
        update();
    }
}
