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

  @java.beans.ConstructorProperties({"ID", "name", "entityType", "sheepColor", "pinataType", "dropType", "health", "crateTime", "price", "dropViewTime", "permission", "blindnessEnabled", "blindnessTime", "fullBlindness", "drops"})
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

  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof Pinata)) return false;
    final Pinata other = (Pinata) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$ID = this.getID();
    final Object other$ID = other.getID();
    if (this$ID == null ? other$ID != null : !this$ID.equals(other$ID)) return false;
    final Object this$name = this.getName();
    final Object other$name = other.getName();
    if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
    final Object this$entityType = this.getEntityType();
    final Object other$entityType = other.getEntityType();
    if (this$entityType == null ? other$entityType != null : !this$entityType.equals(other$entityType)) return false;
    final Object this$sheepColor = this.getSheepColor();
    final Object other$sheepColor = other.getSheepColor();
    if (this$sheepColor == null ? other$sheepColor != null : !this$sheepColor.equals(other$sheepColor)) return false;
    final Object this$pinataType = this.getPinataType();
    final Object other$pinataType = other.getPinataType();
    if (this$pinataType == null ? other$pinataType != null : !this$pinataType.equals(other$pinataType)) return false;
    final Object this$dropType = this.getDropType();
    final Object other$dropType = other.getDropType();
    if (this$dropType == null ? other$dropType != null : !this$dropType.equals(other$dropType)) return false;
    if (Double.compare(this.getHealth(), other.getHealth()) != 0) return false;
    if (this.getCrateTime() != other.getCrateTime()) return false;
    if (Double.compare(this.getPrice(), other.getPrice()) != 0) return false;
    if (this.getDropViewTime() != other.getDropViewTime()) return false;
    final Object this$permission = this.getPermission();
    final Object other$permission = other.getPermission();
    if (this$permission == null ? other$permission != null : !this$permission.equals(other$permission)) return false;
    if (this.isBlindnessEnabled() != other.isBlindnessEnabled()) return false;
    if (this.getBlindnessTime() != other.getBlindnessTime()) return false;
    if (this.isFullBlindness() != other.isFullBlindness()) return false;
    final Object this$drops = this.getDrops();
    final Object other$drops = other.getDrops();
    if (this$drops == null ? other$drops != null : !this$drops.equals(other$drops)) return false;
    return true;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $ID = this.getID();
    result = result * PRIME + ($ID == null ? 43 : $ID.hashCode());
    final Object $name = this.getName();
    result = result * PRIME + ($name == null ? 43 : $name.hashCode());
    final Object $entityType = this.getEntityType();
    result = result * PRIME + ($entityType == null ? 43 : $entityType.hashCode());
    final Object $sheepColor = this.getSheepColor();
    result = result * PRIME + ($sheepColor == null ? 43 : $sheepColor.hashCode());
    final Object $pinataType = this.getPinataType();
    result = result * PRIME + ($pinataType == null ? 43 : $pinataType.hashCode());
    final Object $dropType = this.getDropType();
    result = result * PRIME + ($dropType == null ? 43 : $dropType.hashCode());
    final long $health = Double.doubleToLongBits(this.getHealth());
    result = result * PRIME + (int) ($health >>> 32 ^ $health);
    result = result * PRIME + this.getCrateTime();
    final long $price = Double.doubleToLongBits(this.getPrice());
    result = result * PRIME + (int) ($price >>> 32 ^ $price);
    result = result * PRIME + this.getDropViewTime();
    final Object $permission = this.getPermission();
    result = result * PRIME + ($permission == null ? 43 : $permission.hashCode());
    result = result * PRIME + (this.isBlindnessEnabled() ? 79 : 97);
    result = result * PRIME + this.getBlindnessTime();
    result = result * PRIME + (this.isFullBlindness() ? 79 : 97);
    final Object $drops = this.getDrops();
    result = result * PRIME + ($drops == null ? 43 : $drops.hashCode());
    return result;
  }

  protected boolean canEqual(Object other) {
    return other instanceof Pinata;
  }

  public String toString() {
    return "Pinata(ID=" + this.getID() + ", name=" + this.getName() + ", entityType=" + this.getEntityType() + ", sheepColor=" + this.getSheepColor() + ", pinataType=" + this.getPinataType() + ", dropType=" + this.getDropType() + ", health=" + this.getHealth() + ", crateTime=" + this.getCrateTime() + ", price=" + this.getPrice() + ", dropViewTime=" + this.getDropViewTime() + ", permission=" + this.getPermission() + ", blindnessEnabled=" + this.isBlindnessEnabled() + ", blindnessTime=" + this.getBlindnessTime() + ", fullBlindness=" + this.isFullBlindness() + ", drops=" + this.getDrops() + ")";
  }

  public enum PinataType {
    PUBLIC, PRIVATE
  }

  public enum DropType {
    PUNCH, DEATH
  }

}
