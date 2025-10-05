package info.dawns.bot;

import com.google.gson.Gson;
import info.dawns.authorization.Google;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collections;

public class BotInitializer {
    public static JDA initializeBot() {
        try {
            Reader reader = new FileReader(BotInitializer.class.getResource("/.env/discord/credentials.json").getFile());
            String discordToken = (new Gson()).fromJson(reader, DiscordCredentials.class).getToken();
            return JDABuilder.createLight(discordToken, Collections.emptyList())
                    .enableIntents(
                            GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS)
                    .addEventListeners(new BotCommandListener())
                    .build();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
