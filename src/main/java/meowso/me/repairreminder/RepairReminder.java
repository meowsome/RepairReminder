package meowso.me.repairreminder;

import org.bukkit.plugin.java.JavaPlugin;

public final class RepairReminder extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new Listeners(this), this);
        saveDefaultConfig();
    }
}
