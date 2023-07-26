package plugin.treasurehunt.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class GameScheduler extends BukkitRunnable {

  private int gameTime;
  private BukkitTask task;

  public GameScheduler(int initialGameTime) {
    this.gameTime = initialGameTime;
  }

  @Override
  public void run() {
    if (gameTime <= 0) {
      cancel();
      Bukkit.broadcastMessage("時間切れです");
      return;
    }
    Bukkit.broadcastMessage("残り" + gameTime + "秒");
    gameTime -= 5;
  }

  public void cancelTask() {
    if (task != null) {
      task.cancel();
    }
  }
}
