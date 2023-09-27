package plugin.treasurehunt.config;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import plugin.treasurehunt.data.Treasure;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 宝探しゲームの対象マテリアル情報を設定ファイルから取得し、リストとして扱うためのConfigクラスです。
 */
public class MaterialConfig {
  private List<Treasure> easyTreasureList;
  private List<Treasure> normalTreasureList;
  private List<Treasure> hardTreasureList;

  private static MaterialConfig instance;

  private MaterialConfig() {
  }

  /**
   * MaterialConfigクラス内で唯一のインスタンスを生成し、初回のみ設定ファイルからの情報取得処理を行います。
   *
   * @return クラス内で唯一のインスタンス
   */
  public static synchronized MaterialConfig getInstance() {
    if (instance == null) {
      instance = new MaterialConfig();
      instance.loadMaterialConfig();
    }
    return instance;
  }

  /**
   * 設定ファイルから難易度ごとの対象マテリアル情報を取得し、リストに格納します。
   */
  @SuppressWarnings("unchecked")
  private void loadMaterialConfig() {
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

    if (treasuresData.size() != 3) {
      throw new YAMLException("YAMLデータ内の'treasures'セクションには、3つの難易度の情報が必要です。");
    }

    easyTreasureList = getTreasureList(treasuresData.get("easy"));
    normalTreasureList = getTreasureList(treasuresData.get("normal"));
    hardTreasureList = getTreasureList(treasuresData.get("hard"));
  }

  public List<Treasure> getEasyTreasureList() {
    return easyTreasureList;
  }

  public List<Treasure> getNormalTreasureList() {
    return normalTreasureList;
  }

  public List<Treasure> getHardTreasureList() {
    return hardTreasureList;
  }

  /**
   * resourcesから設定ファイルを取得します。
   *
   * @return 設定ファイル
   */
  private static InputStream loadConfigFile() {
    ClassLoader classLoader = MaterialConfig.class.getClassLoader();
    return classLoader.getResourceAsStream("config.yml");
  }

  /**
   * 設定ファイルから対象マテリアルの情報を取得し、各マテリアルごとにリストに登録します。
   *
   * @param treasuresList 各マテリアルごとの情報を含んだリスト
   * @return 各マテリアルごとの情報を含んだリスト
   */
  private static List<Treasure> getTreasureList(List<Map<String, Object>> treasuresList) {
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
