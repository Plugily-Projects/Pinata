package pl.plajer.pinata.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.EntityType;

import java.util.List;

/**
 * @author Plajer
 * <p>
 * Created at 29.03.2018
 */
@Getter
@AllArgsConstructor
public class Pinata {

    private String ID;
    private String name;
    private EntityType entityType;
    private PinataType pinataType;
    private DropType dropType;
    private double health;
    private int crateTime;
    private double price;
    private int dropViewTime;
    private String permission;
    private List<PinataItem> drops;

    public enum PinataType {
        PUBLIC, PRIVATE
    }

    public enum DropType {
        PUNCH, DEATH
    }

}
