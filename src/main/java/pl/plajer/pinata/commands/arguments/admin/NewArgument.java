package pl.plajer.pinata.commands.arguments.admin;

import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import pl.plajer.pinata.commands.arguments.ArgumentsRegistry;
import pl.plajer.pinata.commands.arguments.data.CommandArgument;
import pl.plajer.pinata.commands.arguments.data.LabelData;
import pl.plajer.pinata.commands.arguments.data.LabeledCommandArgument;
import pl.plajer.pinata.pinata.Pinata;
import pl.plajer.pinata.pinata.PinataItem;
import pl.plajer.pinata.utils.Utils;
import pl.plajerlair.core.utils.ConfigUtils;

public class NewArgument {

  public NewArgument(ArgumentsRegistry registry) {
    registry.mapArgument("pinata", new LabeledCommandArgument("new", "pinata.admin.newpinata", CommandArgument.ExecutorType.PLAYER,
        new LabelData("/pinata new", "/pinata new", "&7Create new Pinata\n&6Permission: &7pinata.admin.newpinata")) {
        @Override
        public void execute(CommandSender sender, String[] args) {
          if (args.length == 1) {
            sender.sendMessage(Utils.colorRawMessage("%prefix% Please type pinata name!"));
          } else {
            if (registry.getPlugin().getPinataManager().getPinataByName(args[1]) != null) {
              sender.sendMessage(Utils.colorRawMessage("%prefix% This pinata already exists!"));
              return;
            }
            PinataItem item = new PinataItem(new ItemStack(Material.PAPER, 1), 100.0);

            Pinata pinata = new Pinata(args[1], args[1], EntityType.SHEEP, DyeColor.WHITE, Pinata.PinataType.PRIVATE,
                Pinata.DropType.DEATH, 20.0, 10, -1, 5, "pinata.use." + args[1],
                true, 15, false, Collections.singletonList(item));
            registry.getPlugin().getPinataManager().getPinataList().add(pinata);

            FileConfiguration config = ConfigUtils.getConfig(registry.getPlugin(), "pinata_storage");
            config.set("storage." + args[1] + ".display-name", args[1]);
            config.set("storage." + args[1] + ".timer-display", 5);
            config.set("storage." + args[1] + ".color", "WHITE");
            config.set("storage." + args[1] + ".permission-string", "pinata.use." + args[1]);
            config.set("storage." + args[1] + ".crate-buy-cost", -1);
            config.set("storage." + args[1] + ".pinata-access-type", "PRIVATE");
            config.set("storage." + args[1] + ".blindness-activated", true);
            config.set("storage." + args[1] + ".blindness-duration", 15);
            config.set("storage." + args[1] + ".full-blindness-activated", false);
            config.set("storage." + args[1] + ".crate-display-time-alive", 10);
            config.set("storage." + args[1] + ".health-amount", 5.0);
            config.set("storage." + args[1] + ".mob-entity-type", "SHEEP");
            config.set("storage." + args[1] + ".items-drop-type", "DEATH");
            config.set("storage." + args[1] + ".drops",
                new ArrayList<ItemStack>() {{
                  add(item.getItem());
                }});
            ConfigUtils.saveConfig(registry.getPlugin(), config, "pinata_storage");
            sender.sendMessage(Utils.colorRawMessage("%prefix% New pinata with ID " + args[1] + " created!"));
          }
        }
      });
  }
}
