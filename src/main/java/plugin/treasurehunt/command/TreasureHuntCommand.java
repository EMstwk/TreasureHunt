package plugin.treasurehunt.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
import plugin.treasurehunt.data.ExecutingPlayer;
import plugin.treasurehunt.scheduler.GameScheduler;

public class TreasureHuntCommand implements Listener, CommandExecutor {

  private Material treasure;
  private Material foundMaterial;
  private int gameScore;
  private int bonusScore;
  private GameScheduler gameScheduler;
  private ExecutingPlayer executingPlayer;
  public Optional<ExecutingPlayer> nowExecutingPlayer;

  private final Main main;

  public List<ExecutingPlayer> executingPlayerList = new ArrayList<>();

  public TreasureHuntCommand(Main main) {
    this.main = main;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player player) {

      executingPlayer = new ExecutingPlayer(player.getName());

      // リストにコマンド実行者と同じ名前が入ってたらそのプレイヤーの情報を返して、
      // 空か一致しなければ新規でプレイヤーの情報追加して返し、Nullなら今の実行者情報を返す
      if (executingPlayerList.isEmpty()) {
        executingPlayer = addNewPlayer(player);
      } else {
        executingPlayer = executingPlayerList.stream()
            .findFirst()
            .map(p -> p.getPlayerName().equals(player.getName())
                ? p
                : addNewPlayer(player)).orElse(executingPlayer);
      }

      // コマンド実行者が実行中のスケジューラがあればキャンセル
      if (!Objects.isNull(executingPlayer.getGameScheduler())) {
        executingPlayerList.stream()
            .filter(p -> p.getPlayerName().equals(player.getName()))
            .findFirst()
            .ifPresent(p -> p.getGameScheduler().cancel());
      }

      // treasureを指定し、プレイヤー情報とリストに追加
      treasure = setTreasureMaterial();
      executingPlayer.setTreasure(treasure);

      // スケジューラを作成し、プレイヤー情報とリストに追加
      gameScheduler = new GameScheduler(main);
      executingPlayer.setGameScheduler(gameScheduler);

      gameScheduler.startTask();
      player.sendMessage("宝探しスタート！【" + treasure + "】を探しましょう！");
      player.sendMessage(treasure + " のボーナススコアは【" + getBonusScore() + "点】です！");
    }
    return false;
  }

  @EventHandler
  public void onEntityPickupItemEvent(EntityPickupItemEvent e) {
    if (!(e.getEntity() instanceof Player)) {
      return;
    }

    Player player = (Player) e.getEntity();
    foundMaterial = e.getItem().getItemStack().getType();

    nowExecutingPlayer = executingPlayerList.stream()
        .filter(p -> p.getPlayerName().equals(player.getName()))
        .findFirst();

    nowExecutingPlayer.ifPresent(executingPlayer -> {
      if (executingPlayer.getTreasure().equals(foundMaterial) && !executingPlayer.getGameScheduler()
          .isCancelled()) {
        executingPlayer.getGameScheduler().cancel();
        player.sendTitle(foundMaterial + " を発見！",
            player.getName() + "の合計スコアは【" + getTotalScore() + "点】です！", 0, 60, 10);
        player.sendMessage("宝探しゲームを終了しました");
        executingPlayerList.remove(executingPlayer);
      }
    });
  }

  // gameSchedulerクラスで使えないか検討中
  public void removeNowExecutingPlayer(Optional<ExecutingPlayer> nowExecutingPlayer) {
    nowExecutingPlayer.ifPresent(executingPlayer -> executingPlayerList.remove(executingPlayer));
  }

  private ExecutingPlayer addNewPlayer(Player player) {
    ExecutingPlayer newPlayer = new ExecutingPlayer(player.getName());
    executingPlayerList.add(newPlayer);
    return newPlayer;
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
      case SAND -> 10;
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
    int remainingTime = gameScheduler.getGameTime();

    if (remainingTime > 15) {
      gameScore = 20;
    } else if (remainingTime > 10) {
      gameScore = 15;
    } else if (remainingTime > 5) {
      gameScore = 10;
    } else if (remainingTime > 0) {
      gameScore = 5;
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