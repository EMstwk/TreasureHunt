package plugin.treasurehunt.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import plugin.treasurehunt.mapper.data.PlayerScore;

/**
 * ゲームのプレイヤースコアに関連するDB操作を定義するためのインターフェースです。
 * プレイヤースコアには、スコア、難易度、対象マテリアル、日時などの情報が含まれます。
 */
public interface PlayerScoreMapper {

  @Select("select * from player_score order by score desc, id desc limit 5")
  List<PlayerScore> selectList();

  @Insert("insert player_score(player_name, score, difficulty, jpTreasureName, registered_at)"
      + "values (#{playerName}, #{score}, #{difficulty}, #{jpTreasureName}, now())")
  void insert(PlayerScore playerScore);
}
