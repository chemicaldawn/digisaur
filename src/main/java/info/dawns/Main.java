package info.dawns;

import com.google.gson.Gson;
import info.dawns.initialization.Discord;
import info.dawns.bot.BotCommandRegistry;
import info.dawns.initialization.scaffolding.Config;
import info.dawns.initialization.scaffolding.DiscordCredentials;
import info.dawns.scheduling.ScheduleManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.slf4j.Logger;

import java.io.InputStreamReader;
import java.io.Reader;

public class Main {
    public static JDA bot = Discord.initializeBot();
    public static SelfUser botUser = bot.getSelfUser();

    public static Config botConfig;

    public static void main(String... args) {
        ClassLoader loader = Main.class.getClassLoader();
        Reader reader = new InputStreamReader(loader.getResourceAsStream("config.json"));
        botConfig = (new Gson()).fromJson(reader, Config.class);

        CommandListUpdateAction commands = bot.updateCommands();
        BotCommandRegistry.initializeCommands();

        commands.addCommands(BotCommandRegistry.apiRegistry);
        commands.queue();

        ScheduleManager.updateCaches();
    }
}