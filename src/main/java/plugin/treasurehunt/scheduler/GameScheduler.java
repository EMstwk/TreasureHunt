package plugin.treasurehunt.scheduler;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import plugin.treasurehunt.Main;

/**
 * ゲームの制限時間を管理するスケジューラです。
 * ボスバーで残り時間を常時表示します。1分単位と残り10秒では、残り時間をタイトル表示もします。
 */
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

    initGameTime = main.getConfig().getInt("Game.initGameTime");
    gameTime = initGameTime;

    bossBar = Bukkit.createBossBar("Countdown", BarColor.GREEN, BarStyle.SOLID);
    bossBar.addPlayer(player);
  }

  @Override
  public void run() {
    if (gameTime <= 0) {
      cancel();
      bossBar.removeAll();

      player.sendTitle("残念！時間切れです", "", 0, 60, 10);
      player.sendMessage(Objects.requireNonNull(main.getConfig().getString("Messages.endGame")));
      return;
    } else if (gameTime <= 10) {
      player.sendTitle(String.valueOf(gameTime), "", 0, 20, 5);
    } else if (!(gameTime == initGameTime) && gameTime % 60 == 0) {
      player.sendTitle("", "残り" + gameTime / 60 + "分", 0, 20, 5);
    }

    bossBar.setProgress(progress);
    bossBar.setTitle("残り" + gameTime / 60 + "分" + gameTime % 60 + "秒");
    progress -= (double) 1 / initGameTime;

    gameTime -= 1;
  }

  public void startTask() {
    runTaskTimer(main, 0, 20);
  }
}
