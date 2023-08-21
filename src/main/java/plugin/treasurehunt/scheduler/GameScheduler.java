package plugin.treasurehunt.scheduler;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import plugin.treasurehunt.Main;

@Getter
@Setter
public class GameScheduler extends BukkitRunnable {

  private int initGameTime;
  private int gameTime;
  private BossBar bossBar;
  private Double progress = 1.0;

  private final Main main;
  private Player player;

  public GameScheduler(Main main, Player player) {
    this.main = main;
    this.player = player;

    FileConfiguration config = main.getConfig();
    initGameTime = config.getInt("game.initGameTime");
    gameTime = initGameTime;

    bossBar = Bukkit.createBossBar("Countdown", BarColor.GREEN, BarStyle.SOLID);
    bossBar.addPlayer(player);
  }

  @Override
  public void run() {
    if (gameTime <= 0) {
      cancel();
      player.sendTitle("残念！時間切れです", "", 0, 60, 10);
      player.sendMessage("宝探しゲームを終了しました。");

      // 模索中
      bossBar.removeAll();
      return;
    } else if (gameTime <= 10) {
      player.sendTitle(String.valueOf(gameTime), "", 0, 20, 5);
    } else if (!(gameTime == initGameTime) && gameTime % 10 == 0) {
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
}
