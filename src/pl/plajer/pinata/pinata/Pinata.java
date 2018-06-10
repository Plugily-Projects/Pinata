package pl.plajer.pinata.pinata;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.DyeColor;
import org.bukkit.entity.EntityType;

import java.util.List;

/**
 * @author Plajer
 * <p>
 * Created at 02.06.2018
 */
@Data
@AllArgsConstructor
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

    public enum PinataType {
        PUBLIC, PRIVATE
    }

    public enum DropType {
        PUNCH, DEATH
    }

}
