package info.dawns.bot.listeners;

import info.dawns.bot.Bot;
import info.dawns.scheduling.ScheduleManager;
import info.dawns.scheduling.Shift;
import info.dawns.scheduling.ShiftType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

public class BotButtonListener extends ListenerAdapter {
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.getComponentId().contains("sale-claim")) {
            long id = Long.valueOf(event.getComponentId().substring("sale-claim-".length()));
            Shift soldShift = Bot.marketMemory.get(id);

            MessageEmbed embed = event.getMessage().getEmbeds().get(0);
            event.editButton(Button.secondary("claimed", "Claimed by " + ScheduleManager.getName(event.getUser().getIdLong())).asDisabled()).and(
            event.getMessage().editMessageEmbeds(EmbedBuilder.fromData(embed.toData())
                    .setColor(5001809)
                    .build()))
                    .queue();
        }
    }
}
