package info.dawns.bot.listeners;

import info.dawns.bot.BotCommand;
import info.dawns.bot.BotCommandRegistry;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class BotSlashCommandListener extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        BotCommand targetCommand = BotCommandRegistry.registry.get(event.getName());

        if (targetCommand != null) {
            targetCommand.apply(event);
        }
    }
}
