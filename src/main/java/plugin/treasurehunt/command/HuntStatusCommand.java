package plugin.treasurehunt.command;

import java.util.List;
import java.util.Optional;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import plugin.treasurehunt.Main;
import plugin.treasurehunt.data.ExecutingPlayer;

/**
 * 宝探しゲームの実行中に、コマンド入力時点での取得可能スコアを表示するためのコマンドです。
 * 残り時間によるスコアと、対象マテリアルによるボーナススコアも分けて表示します。
 */
public class HuntStatusCommand extends BaseCommand implements Listener {

  private final Main main;
  private final TreasureHuntCommand treasureHuntCommand;

  public HuntStatusCommand(Main main, TreasureHuntCommand treasureHuntCommand) {
    this.main = main;
    this.treasureHuntCommand = treasureHuntCommand;
  }

  @Override
  public boolean onExecutePlayerCommand(Player player, Command command, String label,
      String[] args) {

    List<ExecutingPlayer> executingPlayerList = treasureHuntCommand.getExecutingPlayerList();

    // コマンドを実行したプレイヤーが、宝探しゲームを実行中であればtrueを返します。
    boolean isExecutingPlayer = executingPlayerList.stream()
        .filter(p -> p.getPlayerName().equals(player.getName()))
        .anyMatch(p -> p.getGameScheduler() != null && !p.getGameScheduler().isCancelled());

    if (executingPlayerList.isEmpty() || !isExecutingPlayer) {
      player.sendMessage(ChatColor.RED + main.getConfig().getString("messages.invalidCommand"));
      return false;
    }

    Optional<ExecutingPlayer> nowExecutingPlayer = executingPlayerList.stream()
        .filter(p -> p.getPlayerName().equals(player.getName()))
        .findFirst();

    nowExecutingPlayer.ifPresent(p -> {
      int nowTotalScore = treasureHuntCommand.getTotalScore(p);
      int nowGameScore = treasureHuntCommand.getGameScore(p.getGameScheduler());
      int nowBonusScore = p.getBonusScore();
      int nowGameTime = p.getGameScheduler().getGameTime();

      player.sendMessage("現在の取得可能スコアは" + nowTotalScore + "点です。\n"
          + ChatColor.GRAY + "(残り時間" + nowGameTime / 60 + "分" + nowGameTime % 60 + "秒："
          + nowGameScore + "点、ボーナススコア：" + nowBonusScore + "点)");
    });
    return false;
  }

  @Override
  public boolean onExecuteNPCCommand(CommandSender sender, Command command, String label,
      String[] args) {
    return false;
  }
}
