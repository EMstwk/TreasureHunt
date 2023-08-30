package plugin.treasurehunt.mapper.data;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * プレイヤーのスコア情報を扱うオブジェクトです。 DBに存在するテーブルと連動します。
 */
@Getter
@Setter
@NoArgsConstructor
public class PlayerScore {

  private int id;
  private String playerName;
  private int score;
  private String difficulty;
  private String jpTreasureName;
  private LocalDateTime registeredAt;

  public PlayerScore(String playerName, int score, String difficulty, String jpTreasureName) {
    this.playerName = playerName;
    this.score = score;
    this.difficulty = difficulty;
    this.jpTreasureName = jpTreasureName;
  }
}
