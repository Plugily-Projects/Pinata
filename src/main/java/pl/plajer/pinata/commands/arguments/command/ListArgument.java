package pl.plajer.pinata.commands.arguments.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import pl.plajer.pinata.commands.arguments.ArgumentsRegistry;
import pl.plajer.pinata.commands.arguments.data.CommandArgument;
import pl.plajer.pinata.commands.arguments.data.LabelData;
import pl.plajer.pinata.commands.arguments.data.LabeledCommandArgument;
import pl.plajer.pinata.utils.Utils;

public class ListArgument {

  public ListArgument(ArgumentsRegistry registry) {
    registry.mapArgument("pinata", new LabeledCommandArgument("list", "pinata.command.list", CommandArgument.ExecutorType.PLAYER,
        new LabelData("/pinata list", "/pinata list", "&7List of all Pinatas\n&6Permission: &7pinata.command.list")) {
        @Override
        public void execute(CommandSender sender, String[] args) {
          Utils.createPinatasGUI("Menus.List-Menu.Inventory-Name", (Player) sender);
        }
      });
    }
}