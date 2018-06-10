package pl.plajer.pinata.creator;

import lombok.AllArgsConstructor;
import lombok.Data;
import pl.plajer.pinata.pinata.Pinata;

/**
 * @author Plajer
 * <p>
 * Created at 09.06.2018
 */
@Data
@AllArgsConstructor
public class ChatReaction {

    private ReactionType reactionType;
    private Pinata pinata;

    public enum ReactionType {
        SET_MOB_TYPE, SET_HEALTH, SET_CRATE_TIME, SET_DROP_VIEW_TIME, SET_BLINDNESS_DURATION, SET_BLINDNESS, SET_FULL_BLINDNESS
    }

}
