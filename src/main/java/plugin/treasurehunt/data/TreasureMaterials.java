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

    // ボーナススコアは0+10段階（easy:0~2、normal:3~6、hard:7~10）で設定しています。
    this.easyTreasureList = List.of(
        new TreasureData(Material.PUMPKIN, 0),
        new TreasureData(Material.DANDELION, initGameTime / 10),
        new TreasureData(Material.WHITE_WOOL, initGameTime / 10),
        new TreasureData(Material.BEEF, (initGameTime / 10) * 2),
        new TreasureData(Material.PORKCHOP, (initGameTime / 10) * 2)
    );
    this.normalTreasureList = List.of(
        new TreasureData(Material.BONE, (initGameTime / 10) * 3),
        new TreasureData(Material.INK_SAC, (initGameTime / 10) * 4),
        new TreasureData(Material.LILY_OF_THE_VALLEY, (initGameTime / 10) * 5),
        new TreasureData(Material.LAPIS_LAZULI, (initGameTime / 10) * 6),
        new TreasureData(Material.RAW_GOLD, (initGameTime / 10) * 6)
    );
    this.hardTreasureList = List.of(
        new TreasureData(Material.EMERALD, (initGameTime / 10) * 7),
        new TreasureData(Material.DIAMOND, (initGameTime / 10) * 8),
        new TreasureData(Material.ENDER_PEARL, (initGameTime / 10) * 9),
        new TreasureData(Material.ANCIENT_DEBRIS, (initGameTime / 10) * 9),
        new TreasureData(Material.WITHER_ROSE, initGameTime)
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