package pl.plajer.pinata.commands.arguments.admin;

import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import pl.plajer.pinata.commands.arguments.ArgumentsRegistry;
import pl.plajer.pinata.commands.arguments.data.CommandArgument;
import pl.plajer.pinata.commands.arguments.data.LabelData;
import pl.plajer.pinata.commands.arguments.data.LabeledCommandArgument;
import pl.plajer.pinata.utils.Utils;
import pl.plajerlair.core.utils.MinigameUtils;

public class SetChanceArgument {
  public SetChanceArgument(ArgumentsRegistry registry) {
    registry.mapArgument("pinata", new LabeledCommandArgument("setchance", "pinata.admin.setchance", CommandArgument.ExecutorType.PLAYER,
        new LabelData("/pinata setchance", "/pinata setchance", "&7Set chance of drops\n&6Permission: &7pinata.admin.setchance")) {
        @Override
        public void execute(CommandSender sender, String[] args) {
          if (args.length == 1) {
            sender.sendMessage(Utils.colorRawMessage("%prefix% Please type chance!"));
          } else {
            Player p = (Player) sender;
            if (p.getInventory().getItemInMainHand() == null || p.getInventory().getItemInMainHand().getType() == Material.AIR) {
              sender.sendMessage(Utils.colorRawMessage("%prefix% You must hold any item!"));
              return;
            }
            if (!NumberUtils.isNumber(args[1])) {
              sender.sendMessage(Utils.colorRawMessage("%prefix% Chance argument isn't a number!"));
              return;
            }
            double chance = Double.parseDouble(args[1]);
            ItemStack item = p.getInventory().getItemInMainHand();
            if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
              ItemMeta meta = item.getItemMeta();
              List<String> lore = item.getItemMeta().getLore();
              for (String search : lore) {
                if (search.contains("#!Chance")) {
                  lore.remove(search);
                  break;
                }
              }
              lore.add(0, "#!Chance:" + chance);
              meta.setLore(lore);
              item.setItemMeta(meta);
              p.sendMessage(Utils.colorRawMessage("%prefix% Chance updated to " + chance));
            } else {
              MinigameUtils.addLore(item, "#!Chance:" + chance);
              p.sendMessage(Utils.colorRawMessage("%prefix% Chance set to " + chance));
            }
          }
        }
      });
    }
}
