package info.dawns.bot.listeners;

import info.dawns.bot.Bot;
import info.dawns.scheduling.ShiftType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class BotButtonListener extends ListenerAdapter {
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.getComponentId().contains("sale-claim")) {
            long id = Long.valueOf(event.getComponentId().substring("sale-claim-".length()));
            ShiftType soldShiftType = Bot.marketMemory.get(id);

            MessageEmbed embed = event.getMessage().getEmbeds().get(0);
            event.editMessageEmbeds(EmbedBuilder.fromData(embed.toData())
                    .setColor(0)
                    .build())
                    .and(
            event.editButton(event.getButton().asDisabled().withLabel("Claimed")))
                    .queue();
        }
    }
}
