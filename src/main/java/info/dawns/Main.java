package info.dawns;

import info.dawns.bot.BotCommand;
import info.dawns.bot.BotCommandRegistry;
import info.dawns.bot.BotInitializer;
import info.dawns.scheduling.ScheduleManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class Main {

    public static JDA bot = BotInitializer.initializeBot();
    public static ScheduleManager sheet = new ScheduleManager();

    public static void main(String... args) {
        CommandListUpdateAction commands = bot.updateCommands();
        BotCommandRegistry.initializeCommands();

        commands.addCommands(BotCommandRegistry.apiRegistry);
        commands.queue();
    }
}