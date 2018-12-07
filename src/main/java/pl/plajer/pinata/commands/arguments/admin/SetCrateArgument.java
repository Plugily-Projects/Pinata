package pl.plajer.pinata.commands.arguments.admin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import pl.plajer.pinata.Main;
import pl.plajer.pinata.commands.arguments.ArgumentsRegistry;
import pl.plajer.pinata.commands.arguments.data.CommandArgument;
import pl.plajer.pinata.commands.arguments.data.LabelData;
import pl.plajer.pinata.commands.arguments.data.LabeledCommandArgument;
import pl.plajer.pinata.utils.Utils;
import pl.plajerlair.core.utils.ConfigUtils;

public class SetCrateArgument {

  public SetCrateArgument(ArgumentsRegistry registry) {
    registry.mapArgument("pinata", new LabeledCommandArgument("setcrate", "pinata.admin.crate.set", CommandArgument.ExecutorType.PLAYER,
        new LabelData("/pinata setcrate", "/pinata setcrate", "&7Set chance of drops\n&6Permission: &7pinata.admin.crate.set")) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        if (args.length != 2) {
          sender.sendMessage(Utils.colorMessage("Pinata.Crate-Creation.Specify-Name"));
          return;
        }
        Player p = (Player) sender;
        Block l = p.getTargetBlock(null, 20);
        if (l.getType().equals(Material.CHEST)) {
          if (registry.getPlugin().getCrateManager().getCratesLocations().containsKey(l.getLocation())) {
            p.sendMessage(Utils.colorMessage("Pinata.Crate-Creation.Is-Set-Here"));
            return;
          }
          if (ConfigUtils.getConfig(registry.getPlugin(), "crates").isSet("crates." + args[1])) {
            p.sendMessage(Utils.colorMessage("Pinata.Crate-Creation.Already-Exists"));
            return;
          }

          FileConfiguration config = ConfigUtils.getConfig(registry.getPlugin(), "crates");
          config.set("crates." + args[1] + ".world", l.getWorld().getName());
          config.set("crates." + args[1] + ".x", l.getX());
          config.set("crates." + args[1] + ".y", l.getY());
          config.set("crates." + args[1] + ".z", l.getZ());
          config.set("crates." + args[1] + ".name", args[1]);
          ConfigUtils.saveConfig(registry.getPlugin(), config, "crates");

          registry.getPlugin().getCrateManager().getCratesLocations().put(new Location(l.getWorld(), l.getX(), l.getY(), l.getZ()), args[1]);
          p.sendMessage(Utils.colorMessage("Pinata.Crate-Creation.Create-Success").replace("%name%", args[1]));
        } else {
          p.sendMessage(Utils.colorMessage("Pinata.Crate-Creation.Target-Block-Not-Chest"));
        }
      }
    });
  }
}