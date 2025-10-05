package info.dawns.bot;

import info.dawns.Main;
import info.dawns.scheduling.Shift;
import info.dawns.scheduling.VerificationContext;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class BotCommandListener extends ListenerAdapter {

    public static Map<Long, VerificationContext> verificationMemory = new TreeMap<>();
    public static Map<Long, VerificationContext> approvalMemory = new TreeMap<>();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        BotCommand targetCommand = BotCommandRegistry.registry.get(event.getName());

        if (targetCommand != null) {
            targetCommand.apply(event);
        }
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        if (event.getComponentId().equals("verification")) {

            long id = event.getUser().getIdLong();
            VerificationContext ctx = verificationMemory.getOrDefault(event.getUser().getIdLong(), null);

            if (ctx == null) {
                event.reply("Dawn fucked up. Send her a screenshot of this message RN.")
                        .queue();
                return;
            }

            Shift selection = Shift.shiftRegistry.get(event.getValues().get(0));
            try {
                Main.sheet.verify(event.getUser().getIdLong(), selection);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            switch(ctx.verificationType) {
                case "pick-up": {
                    Main.sheet.completePickupShiftFor(id, selection);
                    break;
                }
                case "quest": {
                    Main.sheet.completeQuestShift(selection);
                    break;
                }
            }

            event.reply("**" + selection.internalName + "** successfully verified for " + String.valueOf(selection.defaultHoursAwarded) + " hour(s)!")
                    .setEphemeral(true)
                    .and(
            event.getChannel().addReactionById(ctx.messageId, Emoji.fromUnicode("\uD83E\uDD95")))
                    .queue();
        }

        else if (event.getComponentId().equals("approval")) {
            VerificationContext ctx = verificationMemory.getOrDefault(event.getUser().getIdLong(), null);
            Shift selection = Shift.shiftRegistry.get(event.getValues().get(0));

            if (ctx == null) {
                event.reply("Dawn fucked up. Send her a screenshot of this message RN.")
                        .queue();
                return;
            }

            switch(ctx.verificationType) {
                case "pick-up": {
                    Main.sheet.addPickupShiftFor(ctx.messageId, selection);

                    event.reply("**" + selection.internalName + "** officially approved for this member!")
                            .queue();
                    break;
                }
                case "quest": {
                    Main.sheet.addQuestShift(selection);

                    event.reply("**" + selection.internalName + "** officially added to the quest board!")
                            .queue();
                    break;
                }
            }
        }
    }
}
