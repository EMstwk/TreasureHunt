package plugin.treasurehunt.data;

import lombok.Getter;
import lombok.Setter;
import plugin.treasurehunt.scheduler.GameScheduler;

@Getter
@Setter
public class ExecutingPlayer {

  private String playerName;
  private int score;
  private int gameTime;

  private GameScheduler gameScheduler;

  public ExecutingPlayer(String playerName) {
    this.playerName = playerName;
  }
}
