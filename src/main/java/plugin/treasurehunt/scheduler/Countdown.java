package plugin.treasurehunt.scheduler;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import plugin.treasurehunt.Main;

@Getter
@Setter
public class Countdown extends BukkitRunnable {

  private int remainingTime;
  private final Main main;
  private Player player;

  private Runnable completionCallback;

  public Countdown(Main main, Player player) {
    this.main = main;
    this.player = player;
  }

  public void setCompletionCallback(Runnable callback) {
    this.completionCallback = callback;
  }

  @Override
  public void run() {
    if (remainingTime <= 0) {
      cancel();
      if (completionCallback != null) {
        completionCallback.run(); // カウントダウン終了時にコールバックを実行
      }
      return;
    }
    player.sendTitle("ゲーム開始まで： " + ChatColor.AQUA + remainingTime,
        "", 0, 25, 0);
    remainingTime -= 1;
  }

  public void startCountdown() {
    setRemainingTime(5);
    runTaskTimer(main, 0, 20);
  }
}
