package plugin.treasurehunt.config;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import plugin.treasurehunt.data.Treasure;

/**
 * 宝探しゲームの対象マテリアル情報を設定ファイルから取得し、リストとして扱うためのConfigクラスです。
 */
public class MaterialConfig {

  @SuppressWarnings("unchecked")
  public List<List<Treasure>> loadTreasures() {
    Yaml yaml = new Yaml();
    Map<String, Object> yamlData = yaml.load(loadConfigFile());

    if (yamlData == null) {
      throw new YAMLException("YAMLデータの読み込みに失敗しました。");
    }

    Map<String, List<Map<String, Object>>> treasuresData
        = (Map<String, List<Map<String, Object>>>) yamlData.get("treasures");

    if (treasuresData == null) {
      throw new YAMLException("YAMLデータ内に'treasures'セクションが見つかりません。");
    }

    List<List<Treasure>> allTreasuresList = new ArrayList<>();

    for (List<Map<String, Object>> treasuresList : treasuresData.values()) {
      List<Treasure> treasureList = getTreasureList(treasuresList);
      allTreasuresList.add(treasureList);
    }

    return allTreasuresList;
  }

  /**
   * resourcesから設定ファイルを取得します。
   * @return 設定ファイル
   */
  private InputStream loadConfigFile() {
    ClassLoader classLoader = getClass().getClassLoader();
    return classLoader.getResourceAsStream("config.yml");
  }

  /**
   * 設定ファイルから対象マテリアルの情報を取得し、各マテリアルごとにリストに登録します。
   * @param treasuresList 各マテリアルごとの情報を含んだリスト
   * @return 各マテリアルごとの情報を含んだリスト
   */
  private List<Treasure> getTreasureList(List<Map<String, Object>> treasuresList) {
    List<Treasure> treasureList = new ArrayList<>();

    for (Map<String, Object> treasureData : treasuresList) {
      Treasure treasure = new Treasure();
      treasure.setMaterialName((String) treasureData.get("material"));
      treasure.setBonusScore((int) treasureData.get("bonusScore"));
      treasure.setJpName((String) treasureData.get("jpName"));
      treasureList.add(treasure);
    }
    return treasureList;
  }
}
