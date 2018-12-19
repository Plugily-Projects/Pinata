package pl.plajer.pinata.commands.arguments.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import pl.plajer.pinata.Main;
import pl.plajer.pinata.commands.arguments.ArgumentsRegistry;
import pl.plajer.pinata.commands.arguments.data.CommandArgument;
import pl.plajer.pinata.commands.arguments.data.LabelData;
import pl.plajer.pinata.commands.arguments.data.LabeledCommandArgument;
import pl.plajer.pinata.pinata.Pinata;
import pl.plajer.pinata.pinata.PinataItem;
import pl.plajer.pinata.utils.Utils;
import pl.plajerlair.core.utils.MinigameUtils;

public class PreviewArgument {
  public PreviewArgument(ArgumentsRegistry registry) {
    registry.mapArgument("pinata", new LabeledCommandArgument("preview", "pinata.command.preview", CommandArgument.ExecutorType.PLAYER,
        new LabelData("/pinata preview", "/pinata preview", "&7Preview Pinata\n&6Permission: &7pinata.command.preview")) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        if (args.length == 1) {
          sender.sendMessage(Utils.colorMessage("Pinata.Specify-Name"));
          return;
        }
        Pinata pinata = registry.getPlugin().getPinataManager().getPinataByName(args[1]);
        if (pinata == null) {
          sender.sendMessage(Utils.colorMessage("Pinata.Not-Found"));
          return;
        }
        int rows = MinigameUtils.serializeInt(pinata.getDrops().size());
        Inventory previewMenu = Bukkit.createInventory(null, rows, Utils.colorMessage("Menus.Preview-Menu.Inventory-Name"));
        int i = 0;
        //todo cmd
        for (PinataItem item : pinata.getDrops()) {
          ItemMeta meta = item.getItem().getItemMeta();
          List<String> lore = new ArrayList<>();
          String dropLore = Utils.colorMessage("Menus.Preview-Menu.Drop-Chance").replace("%chance%", String.valueOf(item.getDropChance()));
          lore.add(dropLore);
          meta.setLore(lore);
          ItemStack stack = item.getItem();
          stack.setItemMeta(meta);
          previewMenu.setItem(i, stack);
          i++;
        }
        ((Player) sender).openInventory(previewMenu);
      }
    });
  }
}