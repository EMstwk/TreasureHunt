package plugin.treasurehunt.data;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

/**
 * 宝探しゲームの対象マテリアル情報を扱うためのオブジェクトです。
 */
@Getter
@Setter
public class Treasure {

  private String materialName;
  private int bonusScore;
  private String jpName;

  public Material getTreasureMaterial() {
    return Material.getMaterial(materialName);
  }
}
