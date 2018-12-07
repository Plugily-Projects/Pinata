package pl.plajer.pinata.commands.arguments.admin;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import pl.plajer.pinata.Main;
import pl.plajer.pinata.commands.arguments.ArgumentsRegistry;
import pl.plajer.pinata.commands.arguments.data.CommandArgument;
import pl.plajer.pinata.commands.arguments.data.LabelData;
import pl.plajer.pinata.commands.arguments.data.LabeledCommandArgument;
import pl.plajer.pinata.creator.CreatorMenu;
import pl.plajer.pinata.utils.Utils;

public class EditArgument {

  public EditArgument(ArgumentsRegistry registry) {
    registry.mapArgument("pinata", new LabeledCommandArgument("edit", "pinata.admin.edit", CommandArgument.ExecutorType.PLAYER,
        new LabelData("/pinata edit", "/pinata edit", "&7Edit Pinata\n&6Permission: &7pinata.admin.edit")) {
        @Override
        public void execute(CommandSender sender, String[] args) {
          if (args.length == 1) {
            sender.sendMessage(Utils.colorRawMessage("%prefix% Please type pinata name!"));
          } else {
            if (registry.getPlugin().getPinataManager().getPinataByName(args[1]) == null) {
              sender.sendMessage(Utils.colorRawMessage("%prefix% Pinata doesn't exist!"));
              return;
            }
            new CreatorMenu(args[1]).openInventory((Player) sender);
          }
        }
      });
    }
}
