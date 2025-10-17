package info.dawns.bot.listeners;

import info.dawns.scheduling.ScheduleManager;
import info.dawns.scheduling.ShiftType;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;

public class BotAutocompleteListener extends ListenerAdapter {

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (event.getFocusedOption().getName().equals("shift")) {
            event.replyChoices(ShiftType.shiftTypeRegistry.values().stream().map(
                    (ShiftType s) -> {
                        return new Command.Choice(s.getName(), s.getId());
                    }
            ).toList());
        }
    }
}
