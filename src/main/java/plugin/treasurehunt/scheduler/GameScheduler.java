package plugin.treasurehunt.scheduler;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import plugin.treasurehunt.Main;
import plugin.treasurehunt.command.TreasureHuntCommand;

@Getter
@Setter
public class GameScheduler extends BukkitRunnable {

  private int gameTime = 20;

  private static Main main;
  private static TreasureHuntCommand command;

  public GameScheduler(Main main) {
    this.main = main;
  }

  @Override
  public void run() {
    if (gameTime <= 0) {
      cancel();
      // command.removeNowExecutingPlayer(command.nowExecutingPlayer);
      Bukkit.broadcastMessage("残念！時間切れです");
      return;
    } else if (gameTime % 5 == 0 || gameTime <= 10) {
      Bukkit.broadcastMessage("残り" + gameTime + "秒");
    }
    gameTime -= 1;
  }

  public void startTask() {
    setGameTime(gameTime);
    runTaskTimer(main, 0, 20);
  }

  public void cancelTask() {
    cancel();
  }
}
