package plugin.treasurehunt.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import plugin.treasurehunt.Main;

public class TreasureHuntCommand implements Listener, CommandExecutor {

  private final Main main;

  public TreasureHuntCommand(Main main) {
    this.main = main;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player player) {
      player.sendMessage("コマンドが実行された！しかしゲームはまだ実行できないようだ…");
    }
    return false;
  }

  @EventHandler
  public void onEntityPickupItemEvent(EntityPickupItemEvent e) {
    if (e.getEntity() instanceof Player) {
      Player player = (Player) e.getEntity();
      Item item = e.getItem();
      player.sendMessage("おめでとう！ " + item.getName() + " を手に入れた！");
    }
  }
}
