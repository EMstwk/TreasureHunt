package plugin.treasurehunt.command;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SplittableRandom;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import plugin.treasurehunt.Main;
import plugin.treasurehunt.PlayerScoreDao;
import plugin.treasurehunt.data.ExecutingPlayer;
import plugin.treasurehunt.mapper.data.PlayerScore;
import plugin.treasurehunt.scheduler.GameScheduler;

public class TreasureHuntCommand extends BaseCommand implements Listener {

  public static final String EASY = "easy";
  public static final String NORMAL = "normal";
  public static final String HARD = "hard";
  public static final String NONE = "none";
  public static final String LIST = "list";

  private Material treasure;
  private Material foundMaterial;
  private int gameScore;
  private int bonusScore;
  private GameScheduler gameScheduler;

  private final Main main;
  private PlayerScoreDao playerScoreDao = new PlayerScoreDao();

  public List<ExecutingPlayer> executingPlayerList = new ArrayList<>();

  public TreasureHuntCommand(Main main) {
    this.main = main;
  }

  @Override
  public boolean onExecutePlayerCommand(Player player, Command command, String label,
      String[] args) {
    // 最初の引数が「list」だったらスコアを一覧表示して処理を終了する。
    if (args.length == 1 && LIST.equals(args[0])) {
      sendPlayerScoreList(player);
      return false;
    }

    String difficulty = getDifficulty(player, args);
    if (difficulty.equals(NONE)) {
      return false;
    }

    ExecutingPlayer nowExecutingPlayer = new ExecutingPlayer(player.getName());

    // リストにコマンド実行者と同じ名前が入ってたらそのプレイヤーの情報を返して、
    // 空か一致しなければ新規でプレイヤーの情報追加して返し、Nullなら今の実行者情報を返す
    if (executingPlayerList.isEmpty()) {
      nowExecutingPlayer = addNewPlayer(player);
    } else {
      nowExecutingPlayer = executingPlayerList.stream()
          .findFirst()
          .map(p -> p.getPlayerName().equals(player.getName())
              ? p
              : addNewPlayer(player)).orElse(nowExecutingPlayer);
    }

    // コマンド実行者が実行中のスケジューラがあればキャンセル
    // 同時に2人のプレイヤーが実行したときに正しく処理できてるか微妙？
    if (!Objects.isNull(nowExecutingPlayer.getGameScheduler())) {
      executingPlayerList.stream()
          .filter(p -> p.getPlayerName().equals(player.getName()))
          .findFirst()
          .ifPresent(p -> p.getGameScheduler().cancel());
    }

    // treasureを指定し、プレイヤー情報とリストに追加
    treasure = setTreasureMaterial(difficulty);
    nowExecutingPlayer.setDifficulty(difficulty);
    nowExecutingPlayer.setTreasure(treasure);

    // スケジューラを作成し、プレイヤー情報とリストに追加
    gameScheduler = new GameScheduler(main);
    nowExecutingPlayer.setGameScheduler(gameScheduler);

    gameScheduler.startTask();
    player.sendMessage("宝探しスタート！【" + nowExecutingPlayer.getTreasure() + "】を探しましょう！");
    player.sendMessage(
        nowExecutingPlayer.getTreasure() + " のボーナススコアは【" + getBonusScore(
            nowExecutingPlayer.getTreasure())
            + "点】です！");

    return true;
  }

  @Override
  public boolean onExecuteNPCCommand(CommandSender sender, Command command, String label,
      String[] args) {
    return false;
  }

  /**
   * 現在登録されているスコアの一覧をメッセージに送る。
   *
   * @param player プレイヤー
   */
  private void sendPlayerScoreList(Player player) {
    List<PlayerScore> playerScoreList = playerScoreDao.selectList();
    for (PlayerScore playerScore : playerScoreList) {
      player.sendMessage(playerScore.getId() + " | "
          + playerScore.getPlayerName() + " | "
          + playerScore.getScore() + " | "
          + playerScore.getDifficulty() + " | "
          + playerScore.getTreasure() + " | "
          + playerScore.getRegisteredAt()
          .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
  }

  @EventHandler
  public void onEntityPickupItemEvent(EntityPickupItemEvent e) {
    if (!(e.getEntity() instanceof Player)) {
      return;
    }

    Player player = (Player) e.getEntity();
    foundMaterial = e.getItem().getItemStack().getType();

    Optional<ExecutingPlayer> nowExecutingPlayer = executingPlayerList.stream()
        .filter(p -> p.getPlayerName().equals(player.getName()))
        .findFirst();

    nowExecutingPlayer.ifPresent(executingPlayer -> {
      if (executingPlayer.getTreasure().equals(foundMaterial) && !executingPlayer.getGameScheduler()
          .isCancelled()) {
        executingPlayer.getGameScheduler().cancel();

        int totalScore = getTotalScore(executingPlayer.getTreasure(),
            executingPlayer.getGameScheduler());
        executingPlayer.setScore(totalScore);

        player.sendTitle(foundMaterial + " を発見！",
            player.getName() + "の合計スコアは【" + totalScore + "点】です！", 0, 60, 10);
        player.sendMessage("宝探しゲームを終了しました");

        // スコア登録処理
        playerScoreDao.insert(
            new PlayerScore(executingPlayer.getPlayerName(),
                executingPlayer.getScore(),
                executingPlayer.getDifficulty(),
                executingPlayer.getTreasure()));
      }

      executingPlayerList.remove(executingPlayer);
    });
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
  private Material setTreasureMaterial(String difficulty) {
    List<Material> materialList = switch (difficulty) {
      case NORMAL -> List.of(Material.DIRT, Material.SAND);
      case HARD -> List.of(Material.DIRT, Material.SAND, Material.OAK_LOG);
      default -> List.of(Material.DIRT);
    };

    int random = new SplittableRandom().nextInt(materialList.size());
    return materialList.get(random);
  }

  /**
   * 難易度をコマンド引数から取得します。
   *
   * @param player コマンドを実行したプレイヤー
   * @param args   コマンド引数
   * @return 難易度
   */
  private String getDifficulty(Player player, String args[]) {
    if (args.length == 1 && (EASY.equals(args[0]) || NORMAL.equals(args[0]) || HARD.equals(
        args[0]))) {
      return args[0];
    }
    player.sendMessage(
        ChatColor.RED + "実行できません。コマンド引数の1つ目に難易度指定が必要です。[easy, normal, hard]");
    return NONE;
  }

  /**
   * ゲームで探す対象Materialの種類により、ボーナススコアの設定をします。
   *
   * @return ボーナススコア
   */
  private int getBonusScore(Material treasure) {
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
  private int getGameScore(GameScheduler gameScheduler) {
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
  private int getTotalScore(Material treasure, GameScheduler gameScheduler) {
    return getBonusScore(treasure) + getGameScore(gameScheduler);
  }
}