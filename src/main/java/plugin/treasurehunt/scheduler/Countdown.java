package plugin.treasurehunt.scheduler;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import plugin.treasurehunt.Main;

/**
 * 宝探しゲームのコマンド実行時、ゲーム開始前に5秒のカウントダウンを表示させるスケジューラです。
 * カウントダウン実行中にゲームの制限時間も表示し、カウントダウン終了後にゲームを開始します。
 */
@Getter
@Setter
public class Countdown extends BukkitRunnable {

  private int remainingTime;
  private Runnable completionCallback;

  private final Main main;
  private Player player;

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
        // カウントダウン終了時にコールバックを実行します。
        completionCallback.run();
      }
      return;
    }

    int initGameTime = main.getConfig().getInt("game.initGameTime");
    player.sendTitle("ゲーム開始まで： " + ChatColor.AQUA + remainingTime,
        ChatColor.RESET + "制限時間：" + initGameTime / 60 + "分" + initGameTime % 60 + "秒",
        0, 25, 0);
    remainingTime -= 1;
  }

  public void startCountdown() {
    setRemainingTime(5);
    runTaskTimer(main, 0, 20);
  }
}
