package info.dawns.bot.listeners;

import info.dawns.Constants;
import info.dawns.bot.Bot;
import info.dawns.bot.BotUtils;
import info.dawns.scheduling.ScheduleManager;
import info.dawns.scheduling.Shift;
import info.dawns.scheduling.ShiftType;
import info.dawns.scheduling.VerificationContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class BotStringSelectListener extends ListenerAdapter {

    public static Logger verificationLogger = LoggerFactory.getLogger("Verification");

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        switch(event.getComponentId()) {
            case "verification": {
                long id = event.getUser().getIdLong();
                VerificationContext ctx = Bot.verificationMemory.getOrDefault(event.getUser().getIdLong(), null);
                Shift selection = Shift.fromId(event.getValues().get(0));

                if (ctx == null) {
                    verificationLogger.warn("Null verification context recieved");
                }

                try {
                    ScheduleManager.verify(event.getUser().getIdLong(), selection);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                switch (ctx.verificationType()) {
                    case "pick-up": {
                        ScheduleManager.completePickupShiftFor(id, selection);
                        break;
                    }
                    case "quest": {
                        ScheduleManager.completeQuestShift(selection);
                        break;
                    }
                }

                String hoursString = BotUtils.hoursString(selection.getHours());
                verificationLogger.info(event.getUser().getEffectiveName() + " verified " + selection.getName() + " for " + hoursString);

                event.reply("**" + selection.getName() + "** successfully verified for " + hoursString + "!")
                        .setEphemeral(true)
                        .and(
                                event.getChannel().addReactionById(ctx.messageId(), Emoji.fromUnicode("\uD83E\uDD95")))
                        .queue();
                break;
            }

            case "sell": {
                Shift selection = Shift.fromId(event.getValues().get(0));
                long saleId = event.getIdLong();
                Bot.marketMemory.put(saleId, selection);

                event.replyEmbeds(new EmbedBuilder()
                    .setTitle(selection.getName())
                    .setDescription("")
                    .setColor(Constants.ACCENT_COLOR)
                    .setFooter("Sold by " + ScheduleManager.getName(event.getUser().getIdLong()), event.getMember().getEffectiveAvatarUrl())
                    .build())

                    .addActionRow(Button.of(ButtonStyle.SUCCESS, "sale-claim:" + String.valueOf(saleId), "Claim"))
                    .queue();
                break;
            }
        }
    }
}
