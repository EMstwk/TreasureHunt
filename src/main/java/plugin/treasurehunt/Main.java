package plugin.treasurehunt;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.treasurehunt.command.HuntEndCommand;
import plugin.treasurehunt.command.HuntStatusCommand;
import plugin.treasurehunt.command.TreasureHuntCommand;

public final class Main extends JavaPlugin {

  @Override
  public void onEnable() {
    saveDefaultConfig();

    TreasureHuntCommand treasureHuntCommand = new TreasureHuntCommand(this);
    Bukkit.getPluginManager().registerEvents(treasureHuntCommand, this);
    getCommand("treasurehunt").setExecutor(treasureHuntCommand);

    HuntEndCommand huntEndCommand = new HuntEndCommand(this, treasureHuntCommand);
    getCommand("huntend").setExecutor(huntEndCommand);

    HuntStatusCommand huntStatusCommand = new HuntStatusCommand(this, treasureHuntCommand);
    Bukkit.getPluginManager().registerEvents(huntStatusCommand, this);
    getCommand("huntstatus").setExecutor(huntStatusCommand);
  }
}
