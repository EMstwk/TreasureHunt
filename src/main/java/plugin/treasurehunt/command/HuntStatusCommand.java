package plugin.treasurehunt.command;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import plugin.treasurehunt.Main;
import plugin.treasurehunt.data.ExecutingPlayer;

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

    if (executingPlayerList.isEmpty()) {
      player.sendMessage(ChatColor.RED + "このコマンドは宝探しゲーム実行中のみ使用できます。");
      return false;
    }

    boolean isExecutingPlayer = executingPlayerList.stream()
        .filter(p -> p.getPlayerName().equals(player.getName()))
        .anyMatch(p -> p.getGameScheduler() != null && !p.getGameScheduler().isCancelled());

    if (isExecutingPlayer) {
      player.sendMessage("現時点での取得可能スコア？表示予定");
    } else {
      player.sendMessage(ChatColor.RED + "このコマンドは宝探しゲーム実行中のみ使用できます。");
    }
    return false;
  }

  @Override
  public boolean onExecuteNPCCommand(CommandSender sender, Command command, String label,
      String[] args) {
    return false;
  }
}
