package plugin.treasurehunt.data;

import java.util.List;
import java.util.Map;
import plugin.treasurehunt.Main;

/**
 * 宝探しゲームを実行する際の、対象マテリアル情報を扱うオブジェクトです。
 * 難易度ごとに、設定ファイルのマテリアル、ボーナススコア、日本語名のマップ情報のリストを取得します。
 */
public class TreasureMaterials {

  private final Main main;

  public TreasureMaterials(Main main) {
    this.main = main;
  }

  public List<Map<?, ?>> getEasyTreasureList() {
    return main.getConfig().getMapList("treasures.easy");
  }

  public List<Map<?, ?>> getNormalTreasureList() {
    return main.getConfig().getMapList("treasures.normal");
  }

  public List<Map<?, ?>> getHardTreasureList() {
    return main.getConfig().getMapList("treasures.hard");
  }
}