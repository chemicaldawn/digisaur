package info.dawns.bot;

import info.dawns.utils.RegistryItem;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class BotCommand implements RegistryItem {

    private CommandData commandData;
    private Consumer<SlashCommandInteractionEvent> consumer;

    public BotCommand(CommandData commandData, Consumer<SlashCommandInteractionEvent> consumer) {
        this.commandData = commandData;
        this.consumer = consumer;
    }

    public void apply(SlashCommandInteractionEvent e) {
        consumer.accept(e);
    }

    @Override
    public String getKey() {
        return commandData.getName();
    }
}
