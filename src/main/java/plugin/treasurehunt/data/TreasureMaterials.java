package plugin.treasurehunt.data;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import plugin.treasurehunt.Main;

/**
 * 宝探しゲームを実行する際の対象マテリアル情報を、ボーナススコアとセットで扱うオブジェクトです。
 * 日本語化を手動で行っているため、新しいマテリアルを追加する場合、config.yml内Materialセクションへも追加が必要です。
 */
@Getter
@Setter
public class TreasureMaterials {

  private final Main main;

  private List<TreasureData> easyTreasureList;
  private List<TreasureData> normalTreasureList;
  private List<TreasureData> hardTreasureList;

  public TreasureMaterials(Main main) {
    this.main = main;

    int initGameTime = main.getConfig().getInt("game.initGameTime");

    this.easyTreasureList = List.of(
        new TreasureData(Material.DIRT, 0),
        new TreasureData(Material.SAND, initGameTime / 4)
    );
    this.normalTreasureList = List.of(
        new TreasureData(Material.OAK_LOG, (initGameTime / 4) * 2),
        new TreasureData(Material.DARK_OAK_LOG, (initGameTime / 4) * 3)
    );
    this.hardTreasureList = List.of(
        new TreasureData(Material.DIAMOND, initGameTime)
    );
  }

  /**
   * 宝探しの対象マテリアルとそのボーナススコアをセットで扱うためのレコードです。
   *
   * @param material   対象マテリアル
   * @param bonusScore ボーナススコア
   */
  public record TreasureData(Material material, int bonusScore) {

    public Material getMaterial() {
      return material;
    }

    public int getBonusScore() {
      return bonusScore;
    }
  }
}