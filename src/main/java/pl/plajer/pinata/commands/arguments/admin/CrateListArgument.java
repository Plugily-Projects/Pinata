package pl.plajer.pinata.commands.arguments.admin;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import pl.plajer.pinata.commands.arguments.ArgumentsRegistry;
import pl.plajer.pinata.commands.arguments.data.CommandArgument;
import pl.plajer.pinata.commands.arguments.data.LabelData;
import pl.plajer.pinata.commands.arguments.data.LabeledCommandArgument;
import pl.plajer.pinata.utils.Utils;

public class CrateListArgument {
  public CrateListArgument(ArgumentsRegistry registry) {
    registry.mapArgument("pinata", new LabeledCommandArgument("cratelist", "pinata.admin.cratelist", CommandArgument.ExecutorType.PLAYER,
        new LabelData("/pinata cratelist", "/pinata cratelist", "&7See list of crates\n&6Permission: &7pinata.admin.cratelist")) {
        @Override
        public void execute(CommandSender sender, String[] args) {
          int num = 0;
          sender.sendMessage(Utils.colorMessage("Pinata.Crate-Creation.List"));
          for (Location l : registry.getPlugin().getCrateManager().getCratesLocations().keySet()) {
            sender.sendMessage(Utils.colorRawMessage("&a" + registry.getPlugin().getCrateManager().getCratesLocations().get(l) + " - X: " + l.getX() + " Y: " + l.getY() + " Z: " + l.getZ()));
            num++;
          }
          if (num == 0) {
            sender.sendMessage(Utils.colorMessage("Pinata.Crate-Creation.List-Empty"));
          }
        }
      });
    }
}
