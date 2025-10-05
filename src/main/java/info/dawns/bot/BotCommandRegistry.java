package info.dawns.bot;

import info.dawns.Constants;
import info.dawns.Main;
import info.dawns.scheduling.Schedule;
import info.dawns.scheduling.Shift;
import info.dawns.scheduling.VerificationContext;
import info.dawns.utils.Registry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.cef.browser.CefDevToolsClient;

import java.util.*;
import java.util.function.Consumer;

public class BotCommandRegistry {

    public static Registry<BotCommand> registry = new Registry<>();
    public static List<CommandData> apiRegistry = new ArrayList<>();

    public static void initializeCommands() {

        registerUserCommand(
            Commands.slash("verify", "Verify your workshift automatically!")
                    .addOptions(new OptionData(OptionType.STRING, "type", "The type of workshift you're verifying", true)
                            .addChoice("routine", "routine")
                            .addChoice("pick-up", "pickup")
                            .addChoice("bonus", "bonus")
                            .addChoice("quest", "quest")),

            (SlashCommandInteractionEvent e) -> {
                long id = e.getUser().getIdLong();
                String workshiftType = e.getOption("type").getAsString();

                Message verificationMessage = null;
                boolean validVerificationMessage = false;

                List<Message> recentHistory = e.getChannel().getHistory().retrievePast(7).complete();
                for (Message m : recentHistory) {

                    if (m.getAuthor().getIdLong() == e.getUser().getIdLong() && !m.getAttachments().isEmpty()) {

                        verificationMessage = m;
                        validVerificationMessage = true;

                        List<MessageReaction> reactions = m.getReactions();

                        for (MessageReaction r : reactions) {
                            if (r.getEmoji().equals(Emoji.fromUnicode("\uD83E\uDD95"))) {
                                if (r.retrieveUsers(MessageReaction.ReactionType.NORMAL).complete().contains(Main.bot.getSelfUser())) {
                                    verificationMessage = null;
                                    validVerificationMessage = false;
                                }
                            }
                        }

                        break;
                    }
                }

                if (validVerificationMessage) {
                    Schedule userSchedule = Main.sheet.getSchedule(id);
                    Set<Shift> shiftOptions = new HashSet<>();
                    Set<SelectOption> shifts = new HashSet<>();

                    switch (workshiftType) {
                        case "routine":
                            shiftOptions = userSchedule.getAllShifts();
                            break;

                        case "pickup":
                            shiftOptions = Main.sheet.getPickupShiftsFor(id);
                            break;

                        case "bonus":
                            shiftOptions = Main.sheet.getBonusShifts();
                            break;

                        case "quest":
                            shiftOptions = Main.sheet.getQuestShifts();
                            break;
                    }

                    for (Shift s : shiftOptions) {
                        shifts.add(SelectOption.of(s.internalName, s.internalName));
                    }

                    BotCommandListener.verificationMemory.put(e.getUser().getIdLong(),
                            new VerificationContext(verificationMessage.getIdLong(), workshiftType));

                    if (shifts.size() != 0) {
                        e.reply("Choose the shift you would like to verify...")
                                .addActionRow(StringSelectMenu.create("verification")
                                        .addOptions(shifts)
                                        .build())
                                .setEphemeral(true)
                                .queue();
                    } else {
                        String message;
                        if (workshiftType.equals("routine")) {
                            message = "It looks like you don't have any shifts assigned! If you're a manager, this is to be expected.";
                        } else {
                            message = "It looks like there aren't any available shifts of that type.";
                        }

                        e.reply(message)
                                .setEphemeral(true)
                                .queue();
                    }
                } else {
                    e.reply("Looks like you haven't sent verification photos recently! Please send your photos and retry `/verify`.")
                            .setEphemeral(true)
                            .queue();
                }
            }
        );

        registerAdminCommand(
            Commands.slash("approve", "Approve workshift to be verified")
                    .addOptions(new OptionData(OptionType.STRING, "type", "The type of workshift to approve")
                            .addChoice("pick-up", "pickup")
                            .addChoice("quest", "quest"))
                    .addOption(OptionType.USER, "member", "The member to approve this workshift for", false),

            (SlashCommandInteractionEvent e) -> {
                String workshiftType = e.getOption("type").getAsString();
                User member = e.getOption("member").getAsUser();
                Set<SelectOption> options = new HashSet<>();

                for (Shift s : Shift.shiftRegistry.values()) {
                    options.add(SelectOption.of(s.internalName, s.internalName));
                }

                if (workshiftType.equals("pick-up") && member == null) {
                    e.reply("You need to specify a member for pick-up shifts!")
                            .setEphemeral(true)
                            .queue();

                    return;
                }

                VerificationContext ctx;
                if (member != null) {
                    ctx = new VerificationContext(member.getIdLong(), workshiftType);
                } else {
                    ctx = new VerificationContext(0L, workshiftType);
                }
                BotCommandListener.approvalMemory.put(e.getUser().getIdLong(), ctx);

                e.reply("Choose the shift you would like to approve...")
                        .addActionRow(StringSelectMenu.create("approval")
                                .addOptions(options)
                                .build())
                        .setEphemeral(true)
                        .queue();
            }
        );

        registerAdminCommand(
                Commands.slash("update", "Re-read the Workshift Tracker sheet and update caches"),
                (SlashCommandInteractionEvent e) -> {

                    Main.sheet.updateCaches();

                    e.reply("Caches updated!")
                            .setEphemeral(true)
                            .queue();
                }
        );

        registerUserCommand(Commands.slash("schedule", "View your current workshift assignments"),
            (SlashCommandInteractionEvent e) -> {
                EmbedBuilder builder = new EmbedBuilder()
                        .setTitle("Workshift Schedule")
                        .setColor(Constants.ACCENT_COLOR);

                for (Schedule.Day d : Schedule.Day.values()) {
                    long id = e.getUser().getIdLong();
                    String shifts = "";

                    List<Shift> schedule = Main.sheet.getSchedule(id).getShiftsFor(d);
                    for (Shift s : schedule) {
                        shifts += s.internalName + "\n";
                    }

                    if (!schedule.isEmpty()) {
                        builder.addField(new MessageEmbed.Field(d.name().charAt(0) + d.name().toLowerCase(Locale.ROOT).replace('_',' ').substring(1), shifts, false));
                    }
                }

                MessageEmbed embed = builder.build();
                e.replyEmbeds(embed)
                        .setEphemeral(true)
                        .queue();
            }
        );

        registerAdminCommand(Commands.slash("speak","Speak your truth queen")
                        .addOption(OptionType.STRING, "message","#truth", true),
            (SlashCommandInteractionEvent event) -> {
                event.getChannel().sendMessage(event.getOption("message").getAsString())
                        .and(event.deferReply(true)).queue();
            }
        );

        registerAdminCommand(Commands.slash("reply","Reply your truth queen")
                .addOption(OptionType.USER, "member", "Who u callin out", true)
                .addOption(OptionType.STRING, "message","#truth", true),

                (SlashCommandInteractionEvent event) -> {
                    User member = event.getOption("member").getAsUser();

                    List<Message> messages = event.getChannel().getHistory().retrievePast(15).complete();

                    for (Message m : messages) {
                        if (m.getAuthor().getIdLong() == member.getIdLong()) {
                            m.reply(event.getOption("message").getAsString())
                                    .and(event.deferReply(true))
                                    .queue();
                            return;
                        }
                    }

                    event.reply("I have been silenced.")
                            .setEphemeral(true)
                            .queue();
                }
        );
    }

    public static void registerUserCommand(SlashCommandData data, Consumer<SlashCommandInteractionEvent> consumer) {
        data.setContexts(InteractionContextType.GUILD);
        data.setDefaultPermissions(DefaultMemberPermissions.ENABLED);

        registry.add(new BotCommand(data, consumer));
        apiRegistry.add(data);
    }

    public static void registerAdminCommand(SlashCommandData data, Consumer<SlashCommandInteractionEvent> consumer) {
        data.setContexts(InteractionContextType.GUILD);
        data.setDefaultPermissions(DefaultMemberPermissions.DISABLED);

        registry.add(new BotCommand(data, consumer));
        apiRegistry.add(data);
    }
}
