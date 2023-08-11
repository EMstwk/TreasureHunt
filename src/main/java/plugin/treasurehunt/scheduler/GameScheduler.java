package plugin.treasurehunt.scheduler;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import plugin.treasurehunt.Main;

@Getter
@Setter
public class GameScheduler extends BukkitRunnable {

  private static final int initGameTime = 30;
  private int gameTime = initGameTime;
  private BossBar bossBar;
  private Double progress = 1.0;

  private final Main main;
  private Player player;

  public GameScheduler(Main main, Player player) {
    this.main = main;
    this.player = player;

    // 模索中
    bossBar = Bukkit.createBossBar("Countdown", BarColor.GREEN, BarStyle.SOLID);
    bossBar.addPlayer(player);
  }

  @Override
  public void run() {
    if (gameTime <= 0) {
      cancel();
      player.sendTitle("残念！時間切れです", "", 0, 60, 10);

      // 模索中
      bossBar.removeAll();
      return;
    } else if (!(gameTime == initGameTime) && (gameTime % 10 == 0 || gameTime <= 10)) {
      player.sendTitle("", "残り" + gameTime + "秒", 0, 20, 5);
    }

    //模索中
    bossBar.setProgress(progress);
    bossBar.setTitle("残り" + gameTime + "秒");
    progress -= (double) 1 / initGameTime;

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
