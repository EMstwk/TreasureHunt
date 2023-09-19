package plugin.treasurehunt.data;

import java.util.List;
import plugin.treasurehunt.config.MaterialConfig;

/**
 * 宝探しゲームの対象マテリアル情報を、難易度ごとにリストとして扱うためのオブジェクトです。
 */
public class TreasureLists {

  MaterialConfig materialConfig = new MaterialConfig();
  List<List<Treasure>> allTreasuresList = materialConfig.loadTreasures();

  public List<Treasure> getEasyTreasureList() {
    return allTreasuresList.get(0);
  }

  public List<Treasure> getNormalTreasureList() {
    return allTreasuresList.get(1);
  }

  public List<Treasure> getHardTreasureList() {
    return allTreasuresList.get(2);
  }
}
