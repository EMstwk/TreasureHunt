package plugin.treasurehunt.command;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import plugin.treasurehunt.Main;
import plugin.treasurehunt.scheduler.GameScheduler;

public class TreasureHuntCommand implements Listener, CommandExecutor {

  private Material treasure;
  private Material foundMaterial;
  private int gameScore;
  private int bonusScore;
  private GameScheduler gameScheduler;

  private final Main main;

  private List<GameScheduler> gameSchedulerList = new ArrayList<>();

  public TreasureHuntCommand(Main main) {
    this.main = main;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player player) {
      treasure = setTreasureMaterial();
      player.sendMessage("宝探しスタート！【" + treasure + "】を探しましょう！");
      player.sendMessage(treasure + " のボーナススコアは【" + getBonusScore() + "点】です！");

      // gameSchedulerList内で実行中の処理があればキャンセルすることで、カウントが二重になるのを防ぎます。
      if (!gameSchedulerList.isEmpty()) {
        for (GameScheduler existingGameScheduler : gameSchedulerList) {
          existingGameScheduler.cancel();
        }
      }
      gameScheduler = new GameScheduler(main);
      gameScheduler.startTask();
      gameSchedulerList.add(gameScheduler);
    }
    return false;
  }

  @EventHandler
  public void onEntityPickupItemEvent(EntityPickupItemEvent e) {
    if (e.getEntity() instanceof Player) {
      Player player = (Player) e.getEntity();
      foundMaterial = e.getItem().getItemStack().getType();

      if (treasure.equals(foundMaterial)) {
        gameScheduler.cancel();
        player.sendMessage("おめでとう！ " + foundMaterial + " を入手しました！");
        player.sendMessage(
            player.getName() + "の合計スコアは【" + getTotalScore() + "点】です！");

        // Task終了後でもイベント発生する度に処理されるのを防ぎたい。けどnullはエラー出るから対処法要検討。
        treasure = null;
      }
    }
  }

  /**
   * ゲームで探すMaterialを、対象のリストからランダムに設定します。
   *
   * @return ゲームで探すMaterial
   */
  private Material setTreasureMaterial() {
    List<Material> materialList = List.of(Material.SAND, Material.DIRT, Material.OAK_LOG);

    int random = new SplittableRandom().nextInt(materialList.size());
    return materialList.get(random);
  }

  /**
   * ゲームで探す対象Materialの種類により、ボーナススコアの設定をします。
   *
   * @return ボーナススコア
   */
  private int getBonusScore() {
    bonusScore = switch (treasure) {
      case DIRT -> 10;
      case SAND -> 15;
      case OAK_LOG -> 20;
      default -> 0;
    };
    return bonusScore;
  }

  /**
   * ゲームの経過時間により、獲得できるゲームスコアを設定します。
   *
   * @return ゲームスコア
   */
  private int getGameScore() {
    gameScore = 30;
    if (gameScheduler.getGameTime() <= 15 && gameScheduler.getGameTime() > 10) {
      gameScore -= 10;
    } else if (gameScheduler.getGameTime() > 5) {
      gameScore -= 15;
    } else if (gameScheduler.getGameTime() > 0) {
      gameScore -= 20;
    }
    return gameScore;
  }

  /**
   * ボーナススコアとゲームスコアを合算し、合計スコアを返します。
   *
   * @return 合計スコア
   */
  private int getTotalScore() {
    return getBonusScore() + getGameScore();
  }
}