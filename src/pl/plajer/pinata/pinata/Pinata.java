package pl.plajer.pinata.pinata;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author Plajer
 * <p>
 * Created at 02.06.2018
 */
@Data
@AllArgsConstructor
public class Pinata {

    private String name;
    private PinataData deepData;
    private List<PinataItem> items;

}
