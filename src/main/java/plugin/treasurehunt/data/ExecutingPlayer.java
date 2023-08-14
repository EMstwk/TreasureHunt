package plugin.treasurehunt.data;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import plugin.treasurehunt.scheduler.GameScheduler;

/**
 * TreasureHuntのゲームを実行する際のプレイヤー情報を扱うオブジェクト。
 */
@Getter
@Setter
public class ExecutingPlayer {

  private String playerName;
  private int score;
  private int gameTime;
  private String difficulty;

  private Material treasure;

  private GameScheduler gameScheduler;

  private int countdown;

  public ExecutingPlayer(String playerName) {
    this.playerName = playerName;
  }
}
