package info.dawns.initialization;

import com.google.gson.Gson;
import info.dawns.initialization.scaffolding.DiscordCredentials;
import info.dawns.bot.listeners.BotButtonListener;
import info.dawns.bot.listeners.BotSlashCommandListener;
import info.dawns.bot.listeners.BotStringSelectListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;

public class Discord {

    public static Logger discordInitializationLogger = LoggerFactory.getLogger(Discord.class);

    public static JDA initializeBot() {
        discordInitializationLogger.info("Reading Discord credentials");

        ClassLoader loader = Discord.class.getClassLoader();
        Reader reader = new InputStreamReader(loader.getResourceAsStream(".env/discord/credentials.json"));
        String discordToken = (new Gson()).fromJson(reader, DiscordCredentials.class).getToken();

        discordInitializationLogger.info("Attempting login");

        return JDABuilder.createLight(discordToken, Collections.emptyList())
                .enableIntents(
                        GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .addEventListeners(new BotSlashCommandListener(), new BotButtonListener(), new BotStringSelectListener())
                .build();
    }
}
