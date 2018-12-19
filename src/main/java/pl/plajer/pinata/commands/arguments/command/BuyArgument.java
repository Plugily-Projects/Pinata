package pl.plajer.pinata.commands.arguments.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import pl.plajer.pinata.Main;
import pl.plajer.pinata.commands.arguments.ArgumentsRegistry;
import pl.plajer.pinata.commands.arguments.data.CommandArgument;
import pl.plajer.pinata.commands.arguments.data.LabelData;
import pl.plajer.pinata.commands.arguments.data.LabeledCommandArgument;
import pl.plajer.pinata.pinata.Pinata;
import pl.plajer.pinata.utils.PinataUtils;
import pl.plajer.pinata.utils.Utils;

public class BuyArgument {

  public BuyArgument(ArgumentsRegistry registry) {
    registry.mapArgument("pinata", new LabeledCommandArgument("buy", "pinata.command.buy", CommandArgument.ExecutorType.PLAYER,
        new LabelData("/pinata buy", "/pinata buy", "&7Buy an Pinata\n&6Permission: &7pinata.command.buy")) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        final Player p = (Player) sender;
        if (!registry.getPlugin().isPluginEnabled("Vault")) {
          p.sendMessage(Utils.colorMessage("Pinata.Command.Vault-Not-Detected"));
          return;
        }
        if (registry.getPlugin().getDisabledWorlds().contains(p.getWorld().getName())) {
          p.sendMessage(Utils.colorMessage("Pinata.Create.Disabled-World"));
          return;
        }
        if (args.length == 1) {
          Utils.createPinatasGUI("Menus.List-Menu.Inventory-Name", p);
          return;
        }
        Pinata pinata = registry.getPlugin().getPinataManager().getPinataByName(args[1]);
        if (pinata == null) {
          p.sendMessage(Utils.colorMessage("Pinata.Not-Found"));
          return;
        }
        if (!PinataUtils.checkForSale(pinata, p)) {
          return;
        }
        if (!PinataUtils.checkPermissions(pinata, p)) {
          return;
        }
        if (p.hasPermission("pinata.admin.freeall")) {
          Utils.createPinataAtPlayer(p, p.getLocation(), pinata);
        } else if (registry.getPlugin().getEco().getBalance(Bukkit.getOfflinePlayer(p.getUniqueId())) >= pinata.getPrice()) {
          if (Utils.createPinataAtPlayer(p, p.getLocation(), pinata)) {
            registry.getPlugin().getEco().withdrawPlayer(Bukkit.getOfflinePlayer(p.getUniqueId()), pinata.getPrice());
          }
        } else {
          sender.sendMessage(Utils.colorMessage("Pinata.Selling.Cannot-Afford"));
        }
      }
    });
  }
}
