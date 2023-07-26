package plugin.treasurehunt.command;

import java.util.List;
import java.util.SplittableRandom;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import plugin.treasurehunt.Main;
import plugin.treasurehunt.scheduler.GameScheduler;

public class TreasureHuntCommand implements Listener, CommandExecutor {

  private Material treasure;
  private Material foundMaterial;
  private GameScheduler gameScheduler;

  private final Main main;

  public TreasureHuntCommand(Main main) {
    this.main = main;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player player) {

      treasure = setTreasureMaterial();

      int point = switch (treasure) {
        case DIRT -> 10;
        case SAND -> 15;
        case OAK_LOG -> 20;
        default -> 0;
      };

      player.sendMessage("コマンドが実行された！【" + treasure + "】を探してみよう！");
      player.sendMessage(treasure + " のボーナススコアは【" + point + "点】！");

      gameScheduler = new GameScheduler(20);
      gameScheduler.cancelTask();
      gameScheduler.runTaskTimer(main, 0, 5 * 20);
    }

    return false;
  }

  @EventHandler
  public void onEntityPickupItemEvent(EntityPickupItemEvent e) {
    if (e.getEntity() instanceof Player) {
      Player player = (Player) e.getEntity();
      foundMaterial = e.getItem().getItemStack().getType();

      if (treasure.equals(foundMaterial)) {
        gameScheduler.cancel();
        player.sendMessage("おめでとう！ " + foundMaterial + " を手に入れた！");
      }
    }
  }

  private Material setTreasureMaterial() {
    List<Material> materialList = List.of(Material.SAND, Material.DIRT, Material.OAK_LOG);

    int random = new SplittableRandom().nextInt(materialList.size());
    return materialList.get(random);
  }
}