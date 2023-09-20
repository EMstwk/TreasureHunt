package plugin.treasurehunt.command;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.potion.PotionEffect;
import plugin.treasurehunt.Main;
import plugin.treasurehunt.PlayerScoreDao;
import plugin.treasurehunt.config.MaterialConfig;
import plugin.treasurehunt.data.ExecutingPlayer;
import plugin.treasurehunt.data.Treasure;
import plugin.treasurehunt.mapper.data.PlayerScore;
import plugin.treasurehunt.scheduler.Countdown;
import plugin.treasurehunt.scheduler.GameScheduler;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SplittableRandom;

/**
 * ランダムで指定されるマテリアルを制限時間内に入手し、スコアを獲得する【宝探しゲーム】を起動するコマンドです。
 * スコアはマテリアルの種類、入手までにかかった時間によって変動します。
 * 結果はプレイヤー名、点数、日時などで保存されます。
 */
public class TreasureHuntCommand extends BaseCommand implements Listener {

  public static final String LIST = "list";

  private final Main main;
  private final PlayerScoreDao playerScoreDao = new PlayerScoreDao();

  public List<ExecutingPlayer> executingPlayerList = new ArrayList<>();

  public TreasureHuntCommand(Main main) {
    this.main = main;
  }

  @Override
  public boolean onExecutePlayerCommand(Player player, Command command, String label,
                                        String[] args) {
    // 最初の引数が「list」の場合、スコアを一覧表示して処理を終了します。
    if (args.length == 1 && LIST.equals(args[0])) {
      sendPlayerScoreList(player);
      return false;
    }

    Difficulty difficulty = getDifficulty(player, args);
    if (difficulty.equals(Difficulty.NONE)) {
      return false;
    }

    ExecutingPlayer nowExecutingPlayer = getExecutingPlayer(player);

    startGame(player, difficulty, nowExecutingPlayer);

    return true;
  }

  @Override
  public boolean onExecuteNPCCommand(CommandSender sender, Command command, String label,
                                     String[] args) {
    return false;
  }

  /**
   * 現在登録されているスコアの一覧を、メッセージに表示します。
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
          + playerScore.getJpTreasureName() + " | "
          + playerScore.getRegisteredAt()
          .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
  }

  /**
   * 難易度をコマンド引数から取得します。
   *
   * @param player コマンドを実行したプレイヤー
   * @param args   コマンド引数
   * @return 難易度
   */
  private Difficulty getDifficulty(Player player, String[] args) {
    String firstArgs = args[0].toUpperCase();
    if (args.length == 1
        && (Difficulty.EASY.name().equals(firstArgs)
        || Difficulty.NORMAL.name().equals(firstArgs)
        || Difficulty.HARD.name().equals(firstArgs))) {

      return Arrays.stream(Difficulty.values())
          .filter(d -> d.name().equals(firstArgs))
          .findFirst()
          .orElse(Difficulty.NONE);
    }

    player.sendMessage(
        ChatColor.RED + "実行できません。コマンド引数の1つ目に難易度の指定が必要です。[easy, normal, hard]");
    return Difficulty.NONE;
  }

  /**
   * コマンドを実行したプレイヤー情報をリストで管理し、現在実行しているプレイヤーのスコア情報等を取得します。
   *
   * @param player コマンドを実行したプレイヤー
   * @return 現在実行しているプレイヤーのスコア情報等
   */
  private ExecutingPlayer getExecutingPlayer(Player player) {
    ExecutingPlayer nowExecutingPlayer = new ExecutingPlayer(player.getName());

    if (executingPlayerList.isEmpty()) {
      nowExecutingPlayer = addNewPlayer(player);
    } else {
      nowExecutingPlayer = executingPlayerList.stream()
          .findFirst()
          .map(p -> p.getPlayerName().equals(player.getName())
              ? p
              : addNewPlayer(player)).orElse(nowExecutingPlayer);
    }
    return nowExecutingPlayer;
  }

  /**
   * 新規のプレイヤー情報をリストに追加します。
   *
   * @param player コマンドを実行したプレイヤー
   * @return 新規プレイヤー
   */
  private ExecutingPlayer addNewPlayer(Player player) {
    ExecutingPlayer newPlayer = new ExecutingPlayer(player.getName());
    executingPlayerList.add(newPlayer);
    return newPlayer;
  }

  /**
   * ゲームを開始します。入手すべき対象マテリアルを指定し、ゲームのスケジューラを実行します。
   * コマンドを実行したプレイヤーが既に実行中のスケジューラがあれば、重複しないよう既存のものをキャンセルします。
   *
   * @param player             コマンドを実行したプレイヤー
   * @param difficulty         難易度
   * @param nowExecutingPlayer 現在実行中のプレイヤー情報
   */
  private void startGame(Player player, Difficulty difficulty, ExecutingPlayer nowExecutingPlayer) {
    // コマンド実行者が実行中のカウントダウンがあればキャンセルします。
    if (Objects.nonNull(nowExecutingPlayer.getCountdown())) {
      executingPlayerList.stream()
          .filter(p -> p.getPlayerName().equals(player.getName()))
          .findFirst()
          .ifPresent(p -> p.getCountdown().cancel());
    }

    // コマンド実行者が実行中のゲームスケジューラがあればキャンセルします。
    if (Objects.nonNull(nowExecutingPlayer.getGameScheduler())) {
      executingPlayerList.stream()
          .filter(p -> p.getPlayerName().equals(player.getName()))
          .findFirst()
          .ifPresent(p -> {
            p.getGameScheduler().cancel();
            p.getGameScheduler().getBossBar().removeAll();
          });
    }

    // カウントダウン実行後にゲームを開始します。
    Countdown countdown = new Countdown(main, player);
    nowExecutingPlayer.setCountdown(countdown);
    countdown.setCompletionCallback(() -> {
      initPlayerStatus(player);

      Treasure treasureData = getTreasureData(difficulty);
      Material treasureMaterial = treasureData.getTreasureMaterial();
      int initGameTime = main.getConfig().getInt("game.initGameTime");
      int bonusScore =
          (initGameTime / 10) * treasureData.getBonusScore();
      String jpName = treasureData.getJpName();

      GameScheduler gameScheduler = new GameScheduler(main, player);

      setExecutingPlayerData(difficulty, nowExecutingPlayer, treasureMaterial, bonusScore, jpName,
          gameScheduler);

      int nowBonusScore = nowExecutingPlayer.getBonusScore();
      String nowJpTreasureName = nowExecutingPlayer.getJpTreasureName();

      gameScheduler.startTask();

      player.sendTitle(
          "【" + ChatColor.AQUA + nowJpTreasureName + ChatColor.RESET + "】",
          ChatColor.BOLD + "(ボーナススコア：" + ChatColor.AQUA + nowBonusScore
              + ChatColor.RESET + ChatColor.BOLD + "点）を探そう！",
          0, 70, 30);

      // スコア確認コマンドを実行しなくても対象マテリアル等が確認できるよう、タイトルと合わせメッセージも送信します。
      String message = """
          宝探しを開始しました！
          【%s%s%s(ボーナススコア：%s%d%s点)】を探しましょう！
          """.formatted(ChatColor.AQUA, nowJpTreasureName, ChatColor.RESET,
          ChatColor.AQUA, nowBonusScore, ChatColor.RESET);
      player.sendMessage(message);

    });
    countdown.startCountdown();
  }

  /**
   * ゲーム開始時に、プレイヤーの状態を設定します。体力と満腹度を最大にし、プレイヤーに設定されている特殊効果は削除します。
   *
   * @param player コマンドを実行したプレイヤー
   */
  private void initPlayerStatus(Player player) {
    player.setHealth(20);
    player.setFoodLevel(20);

    player.getActivePotionEffects().stream()
        .map(PotionEffect::getType)
        .forEach(player::removePotionEffect);
  }

  /**
   * ゲームで探す対象マテリアル情報を、該当する難易度のリストからランダムに取得します。
   *
   * @param difficulty 難易度
   * @return 対象マテリアル情報
   */
  private Treasure getTreasureData(Difficulty difficulty) {
    // TODO: 本当はconfigはTreasureHuntCommandクラスのフィールドに持たせたい
    MaterialConfig config = MaterialConfig.loadMaterialConfig();
    List<Treasure> treasureMaterialList = switch (difficulty) {
      case NORMAL -> config.getNormalTreasureList();
      case HARD -> config.getHardTreasureList();
      default -> config.getEasyTreasureList();
    };

    int random = new SplittableRandom().nextInt(treasureMaterialList.size());
    return treasureMaterialList.get(random);
  }

  /**
   * ゲームを実行中のプレイヤーの、対象マテリアル情報等を個別に保存します。
   *
   * @param difficulty         難易度
   * @param nowExecutingPlayer 現在実行中のプレイヤー
   * @param treasureMaterial   対象マテリアル
   * @param bonusScore         ボーナススコア
   * @param jpName             対象マテリアルの日本語名
   * @param gameScheduler      ゲームスケジューラ
   */
  private static void setExecutingPlayerData(Difficulty difficulty, ExecutingPlayer nowExecutingPlayer,
                                             Material treasureMaterial, int bonusScore, String jpName, GameScheduler gameScheduler) {
    nowExecutingPlayer.setDifficulty(difficulty);
    nowExecutingPlayer.setTreasureMaterial(treasureMaterial);
    nowExecutingPlayer.setBonusScore(bonusScore);
    nowExecutingPlayer.setJpTreasureName(jpName);
    nowExecutingPlayer.setGameScheduler(gameScheduler);
  }

  /**
   * プレイヤーがゲームの対象マテリアルを入手した場合、実行中のゲームを終了し、合計スコアを表示します。
   * プレイヤー名、スコア等の情報はDBに記録します。
   *
   * @param e エンティティがアイテムを入手するイベント
   */
  @EventHandler
  public void onEntityPickupItemEvent(EntityPickupItemEvent e) {
    if (!(e.getEntity() instanceof Player player)) {
      return;
    }

    Material foundMaterial = e.getItem().getItemStack().getType();

    Optional<ExecutingPlayer> nowExecutingPlayer = executingPlayerList.stream()
        .filter(p -> p.getPlayerName().equals(player.getName()))
        .findFirst();

    nowExecutingPlayer.ifPresent(p -> {
      Material nowTreasure = p.getTreasureMaterial();
      GameScheduler nowGameScheduler = p.getGameScheduler();

      if (nowTreasure.equals(foundMaterial) && !nowGameScheduler.isCancelled()) {
        nowGameScheduler.cancel();
        nowGameScheduler.getBossBar().removeAll();

        int totalScore = getTotalScore(p);

        player.sendTitle(p.getJpTreasureName() + " を発見！",
            player.getName() + "の合計スコアは【" + ChatColor.AQUA + totalScore
                + ChatColor.RESET + "点】です！", 0, 60, 10);

        // ゲーム開始時もメッセージを送信しているので、履歴がおかしくならないよう終了時にもメッセージを送信します。
        player.sendMessage(Objects.requireNonNull(main.getConfig().getString("messages.endGame"))
            + "（合計スコア：" + totalScore + "点）");

        playerScoreDao.insert(
            new PlayerScore(p.getPlayerName(), totalScore, p.getDifficulty().name().toLowerCase(),
                p.getJpTreasureName()));

        executingPlayerList.remove(p);
      }
    });
  }

  /**
   * ゲームの経過時間により、獲得できるゲームスコアを設定します。
   *
   * @return ゲームスコア
   */
  int getGameScore(GameScheduler gameScheduler) {
    int remainingTime = gameScheduler.getGameTime();
    return (remainingTime / 30) * 30 + 30;
  }

  /**
   * ボーナススコアとゲームスコアを合算し、合計スコアを返します。
   *
   * @return 合計スコア
   */
  int getTotalScore(ExecutingPlayer executingPlayer) {
    return executingPlayer.getBonusScore() + getGameScore(executingPlayer.getGameScheduler());
  }

  /**
   * 他クラスからでも、executingPlayerListの情報を取得できるようにします。
   * （※IntelliJを2023.2.1にアップデートした際lombokのプラグインが必要となり、その結果当メソッドは不要となりましたが、
   * 　念の為プラグインの無い環境でも動くようそのまま残しています。）
   *
   * @return executingPlayerList
   */
  public List<ExecutingPlayer> getExecutingPlayerList() {
    return executingPlayerList;
  }
}
