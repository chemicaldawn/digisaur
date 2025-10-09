package info.dawns.bot;

import info.dawns.Main;
import info.dawns.scheduling.ShiftType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.Collection;
import java.util.List;

public class BotUtils {

    public static int DEFAULT_HISTORY_LIMIT = 15;

    public static Message getLatestMessageFrom(long userId, MessageChannel in) {
        return getLatestMessageFrom(userId, in, DEFAULT_HISTORY_LIMIT);
    }
    
    public static Message getLatestMessageFrom(long userId, MessageChannel in, int historyLimit) {
        for (Message m : in.getHistory().retrievePast(historyLimit).complete()) {
            if (m.getAuthor().getIdLong() == userId) {
                return m;
            }
        }

        return null;
    }

    public static boolean isAcknowledged(Message m) {
        for (MessageReaction r : m.getReactions()) {
            if (r.getEmoji().equals(Emoji.fromUnicode("\uD83E\uDD95"))) {
                if (r.retrieveUsers(MessageReaction.ReactionType.NORMAL).complete().contains(Main.botUser)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<SelectOption> selectOptionsFor(Collection<ShiftType> shiftTypes) {
        List<SelectOption> optionList;

        optionList = shiftTypes.stream().map((ShiftType s) -> {
            return SelectOption.of(s.getName(), s.getId());
        }).toList();

        return optionList;
    }

    public static String hoursString(double hours) {
        if (hours - Math.floor(hours) > 0D) {
            return String.valueOf(hours) + " hours";
        } else if (hours == 1D) {
            return "1 hour";
        } else {
            return String.valueOf((int) hours) + " hours";
        }
    }
}
