package plugin.treasurehunt.command;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plugin.treasurehunt.Main;
import plugin.treasurehunt.data.ExecutingPlayer;

/**
 * 宝探しゲームの実行中に、ゲームを強制終了させるためのコマンドです。
 * カウントダウンの実行中は、カウントダウンを強制終了させます。
 */
public class HuntEndCommand extends BaseCommand {

  private final Main main;
  private final TreasureHuntCommand treasureHuntCommand;

  public HuntEndCommand(Main main, TreasureHuntCommand treasureHuntCommand) {
    this.main = main;
    this.treasureHuntCommand = treasureHuntCommand;
  }


  @Override
  public boolean onExecutePlayerCommand(Player player, Command command, String label,
      String[] args) {

    List<ExecutingPlayer> executingPlayerList = treasureHuntCommand.getExecutingPlayerList();

    // コマンドを実行したプレイヤーが、カウントダウンか宝探しゲームを実行中であればtrueを返します。
    boolean isPlaying = executingPlayerList.stream()
        .filter(p -> p.getPlayerName().equals(player.getName()))
        .anyMatch(p -> ((p.getCountdown() != null && !p.getCountdown().isCancelled())
            || (p.getGameScheduler() != null && !p.getGameScheduler().isCancelled())));

    if (executingPlayerList.isEmpty() || !isPlaying) {
      player.sendMessage(ChatColor.RED + main.getConfig().getString("messages.invalidCommand"));
      return false;
    }

    Optional<ExecutingPlayer> nowExecutingPlayer = executingPlayerList.stream()
        .filter(p -> p.getPlayerName().equals(player.getName()))
        .findFirst();

    nowExecutingPlayer.ifPresent(p -> {
      if (p.getCountdown() != null && !p.getCountdown().isCancelled()) {
        p.getCountdown().cancel();
      } else if (p.getGameScheduler() != null && !p.getGameScheduler().isCancelled()) {
        p.getGameScheduler().cancel();
        p.getGameScheduler().getBossBar().removeAll();
      }

      player.sendMessage(Objects.requireNonNull(main.getConfig().getString("messages.endGame")));
      executingPlayerList.remove(p);
    });
    return false;
  }

  @Override
  public boolean onExecuteNPCCommand(CommandSender sender, Command command, String label,
      String[] args) {
    return false;
  }
}
