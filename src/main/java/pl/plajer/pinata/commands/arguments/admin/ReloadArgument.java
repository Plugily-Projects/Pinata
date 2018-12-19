package pl.plajer.pinata.commands.arguments.admin;

import org.bukkit.command.CommandSender;

import pl.plajer.pinata.Main;
import pl.plajer.pinata.commands.arguments.ArgumentsRegistry;
import pl.plajer.pinata.commands.arguments.data.CommandArgument;
import pl.plajer.pinata.commands.arguments.data.LabelData;
import pl.plajer.pinata.commands.arguments.data.LabeledCommandArgument;
import pl.plajer.pinata.handlers.language.LanguageManager;
import pl.plajer.pinata.utils.Utils;

public class ReloadArgument {

  public ReloadArgument(ArgumentsRegistry registry) {
    registry.mapArgument("pinata", new LabeledCommandArgument("reloadconfig", "pinata.admin.reload", CommandArgument.ExecutorType.BOTH,
        new LabelData("/pinata reload", "/pinata reload", "&7Reload whole plugin\n&7&lThat will reload Pinata!\n" +
            "&c&lNot recommended!\n&6Permission: &7pinata.admin.reload")) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        try {
          registry.getPlugin().reloadConfig();
          registry.getPlugin().getPinataManager().getPinataList().clear();
          registry.getPlugin().getPinataManager().loadPinatas();
          LanguageManager.init(registry.getPlugin());
        } catch (Exception e) {
          sender.sendMessage(Utils.colorMessage("Pinata.Config.Reload-Fail"));
          return;
        }
        registry.getPlugin().getDisabledWorlds().clear();
        for (String world : registry.getPlugin().getConfig().getStringList("disabled-worlds")) {
          registry.getPlugin().getDisabledWorlds().add(world);
          registry.getPlugin().getLogger().info("Pinata creation blocked at world " + world + "!");
        }
        if (registry.getPlugin().isNormalUpdate()) {
          sender.sendMessage(Utils.colorMessage("Other.Plugin-Up-To-Date").replace("%old%", registry.getPlugin().getDescription().getVersion()).replace("%new%", registry.getPlugin().getNewestVersion()));
        }
        sender.sendMessage(Utils.colorMessage("Pinata.Config.Reload-Success"));
      }
    });
  }

}