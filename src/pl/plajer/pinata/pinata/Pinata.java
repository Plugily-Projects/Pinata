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

package pl.plajer.pinata.pinata;

import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.entity.EntityType;

/**
 * @author Plajer
 * <p>
 * Created at 02.06.2018
 */
public class Pinata {

  private String ID;
  private String name;
  private EntityType entityType;
  private DyeColor sheepColor;
  private PinataType pinataType;
  private DropType dropType;
  private double health;
  private int crateTime;
  private double price;
  private int dropViewTime;
  private String permission;
  private boolean blindnessEnabled;
  private int blindnessTime;
  private boolean fullBlindness;
  private List<PinataItem> drops;

  public Pinata(String ID, String name, EntityType entityType, DyeColor sheepColor, PinataType pinataType, DropType dropType, double health, int crateTime, double price, int dropViewTime, String permission, boolean blindnessEnabled, int blindnessTime, boolean fullBlindness, List<PinataItem> drops) {
    this.ID = ID;
    this.name = name;
    this.entityType = entityType;
    this.sheepColor = sheepColor;
    this.pinataType = pinataType;
    this.dropType = dropType;
    this.health = health;
    this.crateTime = crateTime;
    this.price = price;
    this.dropViewTime = dropViewTime;
    this.permission = permission;
    this.blindnessEnabled = blindnessEnabled;
    this.blindnessTime = blindnessTime;
    this.fullBlindness = fullBlindness;
    this.drops = drops;
  }

  public String getID() {
    return this.ID;
  }

  public void setID(String ID) {
    this.ID = ID;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public EntityType getEntityType() {
    return this.entityType;
  }

  public void setEntityType(EntityType entityType) {
    this.entityType = entityType;
  }

  public DyeColor getSheepColor() {
    return this.sheepColor;
  }

  public void setSheepColor(DyeColor sheepColor) {
    this.sheepColor = sheepColor;
  }

  public PinataType getPinataType() {
    return this.pinataType;
  }

  public void setPinataType(PinataType pinataType) {
    this.pinataType = pinataType;
  }

  public DropType getDropType() {
    return this.dropType;
  }

  public void setDropType(DropType dropType) {
    this.dropType = dropType;
  }

  public double getHealth() {
    return this.health;
  }

  public void setHealth(double health) {
    this.health = health;
  }

  public int getCrateTime() {
    return this.crateTime;
  }

  public void setCrateTime(int crateTime) {
    this.crateTime = crateTime;
  }

  public double getPrice() {
    return this.price;
  }

  public void setPrice(double price) {
    this.price = price;
  }

  public int getDropViewTime() {
    return this.dropViewTime;
  }

  public void setDropViewTime(int dropViewTime) {
    this.dropViewTime = dropViewTime;
  }

  public String getPermission() {
    return this.permission;
  }

  public void setPermission(String permission) {
    this.permission = permission;
  }

  public boolean isBlindnessEnabled() {
    return this.blindnessEnabled;
  }

  public void setBlindnessEnabled(boolean blindnessEnabled) {
    this.blindnessEnabled = blindnessEnabled;
  }

  public int getBlindnessTime() {
    return this.blindnessTime;
  }

  public void setBlindnessTime(int blindnessTime) {
    this.blindnessTime = blindnessTime;
  }

  public boolean isFullBlindness() {
    return this.fullBlindness;
  }

  public void setFullBlindness(boolean fullBlindness) {
    this.fullBlindness = fullBlindness;
  }

  public List<PinataItem> getDrops() {
    return this.drops;
  }

  public void setDrops(List<PinataItem> drops) {
    this.drops = drops;
  }

  public enum PinataType {
    PUBLIC, PRIVATE
  }

  public enum DropType {
    PUNCH, DEATH
  }

}
