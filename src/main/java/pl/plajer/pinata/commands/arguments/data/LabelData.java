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
 */package pl.plajer.pinata.commands.arguments.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import pl.plajer.pinata.pinata.LivingPinata;
import pl.plajer.pinata.utils.Utils;

/**
 * @author Plajer
 * <p>
 * Created at 03.05.2018
 */
public class LabelData {
  private Map<Entity, LivingPinata> pinata = new HashMap<>();
  private List<Player> users = new ArrayList<>();
  private String text;
  private String command;
  private String description;

  public LabelData(String text, String command, String description) {
    this.text = Utils.colorRawMessage(text);
    this.command = command;
    this.description = Utils.colorRawMessage(description);
  }

  public String getText() {
    return text;
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Map<Entity, LivingPinata> getPinata() {
    return pinata;
  }

  public List<Player> getUsers() {
    return users;
  }

  public void setUsers(List<Player> users) {
    this.users = users;
  }
}
