/*
 * Pinata plugin - spawn pinata mob and kill it to get drops
 * Copyright (C)2018 Plajer
 *
 *  This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package pl.plajer.pinata.commands;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import pl.plajer.pinata.commands.arguments.ArgumentsRegistry;
import pl.plajer.pinata.commands.arguments.data.CommandArgument;

/**
 * @author Plajer
 * <p>
 * Created at 11.05.2018
 */
public class TabCompletion implements TabCompleter {

  private ArgumentsRegistry registry;

  public TabCompletion(ArgumentsRegistry registry) {
    this.registry = registry;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player)) {
      return null;
    }
    if (cmd.getName().equalsIgnoreCase("pinata")) {
      if (args.length == 1) {
        return registry.getMappedArguments().get(cmd.getName().toLowerCase()).stream().map(CommandArgument::getArgumentName).collect(Collectors.toList());
      }
    }
    return null;
  }
}
