package plugin.treasurehunt.data;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import plugin.treasurehunt.scheduler.Countdown;
import plugin.treasurehunt.scheduler.GameScheduler;

/**
 * 宝探しゲームを実行する際の、プレイヤー情報を扱うオブジェクトです。
 */
@Getter
@Setter
public class ExecutingPlayer {

  private String playerName;
  private int score;
  private String difficulty;
  private Material treasure;
  private int bonusScore;

  private Countdown countdown;
  private GameScheduler gameScheduler;

  public ExecutingPlayer(String playerName) {
    this.playerName = playerName;
  }
}
