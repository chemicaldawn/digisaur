package info.dawns.bot;

import info.dawns.Constants;
import info.dawns.scheduling.*;
import info.dawns.utils.Registry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class BotCommandRegistry {

    public static Registry<BotCommand> slashRegistry = new Registry<>();
    public static List<CommandData> apiRegistry = new ArrayList<>();

    public static void initializeCommands() {

        registerUserCommand(
            Commands.slash("verify", "Verify your workshift automatically!")
                    .addOptions(new OptionData(OptionType.STRING, "type", "The type of workshift you're verifying", true)
                            .addChoice("routine", "routine")
                            .addChoice("pick-up", "pickup")
                            .addChoice("bonus", "bonus")),
            (SlashCommandInteractionEvent e) -> {
                long id = e.getUser().getIdLong();
                String workshiftType = e.getOption("type").getAsString();

                Message verificationMessage = BotUtils.getLatestMessageFrom(id, e.getChannel());
                boolean validVerificationMessage = false;

                if (verificationMessage != null) {
                    if (!verificationMessage.getAttachments().isEmpty() && !BotUtils.isAcknowledged(verificationMessage)) {
                        validVerificationMessage = true;
                    }
                }

                if (validVerificationMessage) {
                    Schedule userSchedule = ScheduleManager.getScheduleFor(id);
                    Set<Shift> shiftTypeOptions = new HashSet<>();
                    Set<SelectOption> shifts = new HashSet<>();

                    switch (workshiftType) {
                        case "routine":
                            shiftTypeOptions = userSchedule.getAllShifts();
                            break;
                        case "pickup":
                            shiftTypeOptions = ScheduleManager.getPickupShiftsFor(id);
                            break;
                        case "bonus":
                            shiftTypeOptions = ScheduleManager.getBonusShifts();
                            break;
                    }

                    Bot.verificationMemory.put(e.getUser().getIdLong(),
                            new VerificationContext(verificationMessage.getIdLong(), workshiftType));

                    if (shiftTypeOptions.size() != 0) {
                        e.reply("Choose the shift you would like to verify...")
                                .addActionRow(StringSelectMenu.create("verification")
                                        .addOptions(BotUtils.selectOptionsFor(shiftTypeOptions))
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
                Commands.slash("award", "Award someone a specific number of hours")
                        .addOption(OptionType.USER, "member", "The member of the house to award hours to", true)
                        .addOption(OptionType.INTEGER, "hours", "The number of hours to award", true)
                        .addOption(OptionType.STRING, "description", "The reason for awarding hours", true),
                (SlashCommandInteractionEvent e) -> {
                    User user = e.getOption("member").getAsUser();
                    Integer hours = e.getOption("hours").getAsInt();
                    String description = e.getOption("description").getAsString();
                    try {
                        ScheduleManager.verify(user.getIdLong(), new Shift(new ShiftType(description, hours, "", Emoji.fromUnicode("\uD83E\uDD95"), "bonus"), Day.ANY_DAY));
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
        );

        registerAdminCommand(
                Commands.slash("approve", "Approve a pick-up shift for someone")
                        .addOption(OptionType.USER, "member", "The member of the house to approve a pick-up shift for", true)
                        .addOption(OptionType.STRING, "shift", "The shift to approve", true, true)
                        .addOption(OptionType.INTEGER, "hours", "The hours awarded for this specific shift, if applicable", false),
                (SlashCommandInteractionEvent e) -> {
                    User member = e.getOption("member").getAsUser();
                    String shiftId = e.getOption("shift").getAsString();
                    Integer hoursAdjustment = e.getOption("hours").getAsInt();

                    Bot.approvalMemory.put(e.getUser().getIdLong(), new ApprovalContext(member.getIdLong(), ShiftType.fromId(shiftId), 0));
                }
        );

        registerUserCommand(Commands.slash("sell", "Put your workshift on the market"),
            (SlashCommandInteractionEvent e) -> {

                e.reply("Select the workshift you'd like to sell...")
                        .addActionRow(StringSelectMenu.create("sell")
                                .addOptions(BotUtils.selectOptionsFor(ScheduleManager.getScheduleFor(e.getUser().getIdLong()).getAllShifts()))
                                .build())
                        .setEphemeral(true)
                        .queue();
            }
        );

        registerUserCommand(Commands.slash("reserve", "Reserve a room of the house")
                .addOptions(new OptionData(OptionType.STRING, "room", "The type of workshift you're verifying", true)
                        .addChoice("guest-room", "guest-room")
                        .addChoice("projector-room", "projector-room")
                        .addChoice("patio", "patio"))
                .addOption(OptionType.STRING, "description", "Event description", true)
                .addOption(OptionType.STRING, "starting", "The starting date and time and date to reserve for", true)
                .addOption(OptionType.STRING, "duration", "The duration, in hours, of the event", false)
                .addOption(OptionType.STRING, "ending", "The starting date and time and date to reserve for", false),
                (SlashCommandInteractionEvent e) -> {

            }
        );

        registerUserCommand(Commands.slash("stats", "Get your workshift standings for this week"),
            (SlashCommandInteractionEvent e) -> {

            }
        );

        registerUserCommand(Commands.slash("schedule", "View your current workshift assignments"),
            (SlashCommandInteractionEvent e) -> {
                EmbedBuilder builder = new EmbedBuilder()
                        .setTitle("Workshift Schedule")
                        .setColor(Constants.ACCENT_COLOR);

                for (Day d : Day.values()) {
                    long id = e.getUser().getIdLong();
                    String shifts = "";

                    Set<Shift> schedule = ScheduleManager.getScheduleFor(id).getShiftsFor(d);
                    for (Shift s : schedule) {
                        shifts += s.getName() + "\n";
                    }

                    if (!schedule.isEmpty()) {
                        builder.addField(new MessageEmbed.Field(d.toString(), shifts, false));
                    }
                }

                builder.setFooter("Generated for " + ScheduleManager.getName(e.getUser().getIdLong()), e.getMember().getEffectiveAvatarUrl());

                MessageEmbed embed = builder.build();
                e.replyEmbeds(embed)
                        .setEphemeral(true)
                        .queue();
            }
        );

        registerSpeakingCommand(Commands.slash("speak","Speak your truth queen")
                        .addOption(OptionType.STRING, "message","#truth", true),
            (SlashCommandInteractionEvent event) -> {
                event.deferReply(true).complete().deleteOriginal().queue();
                event.getChannel().sendTyping().queue();
                event.getChannel().sendMessage(event.getOption("message").getAsString()).queueAfter(2250, TimeUnit.MILLISECONDS);
            }
        );

        registerSpeakingCommand(Commands.slash("image","Image your truth queen")
                        .addOption(OptionType.ATTACHMENT, "image","#truth", true),
                (SlashCommandInteractionEvent event) -> {
                    event.deferReply(true).complete().deleteOriginal().queue();
                    event.getChannel().sendMessage(event.getOption("image").getAsAttachment().getUrl()).queue();
                }
        );

        registerSpeakingCommand(Commands.slash("react","React your truth queen")
                    .addOption(OptionType.USER, "member", "Who u callin out", true)
                    .addOption(OptionType.STRING, "emoji","#truth", true),
                (SlashCommandInteractionEvent event) -> {
                    User member = event.getOption("member").getAsUser();

                    List<Message> messages = event.getChannel().getHistory().retrievePast(15).complete();

                    for (Message m : messages) {
                        if (m.getAuthor().getIdLong() == member.getIdLong()) {
                            event.deferReply(true).complete().deleteOriginal().queue();

                            String emojiText = event.getOption("emoji").getAsString();
                            ScheduleManager.scheduleManagerLogger.info(emojiText);
                            Emoji emoji = Emoji.fromFormatted(emojiText);

                            m.addReaction(emoji).queue();
                            return;
                        }
                    }
                }
        );

        registerSpeakingCommand(Commands.slash("reply","Reply your truth queen")
                .addOption(OptionType.USER, "member", "Who u callin out", true)
                .addOption(OptionType.STRING, "message","#truth", true),
            (SlashCommandInteractionEvent event) -> {
                    User member = event.getOption("member").getAsUser();

                    List<Message> messages = event.getChannel().getHistory().retrievePast(15).complete();

                    for (Message m : messages) {
                        if (m.getAuthor().getIdLong() == member.getIdLong()) {
                            event.deferReply(true).complete().deleteOriginal().queue();
                            event.getChannel().sendTyping().queue();
                            m.reply(event.getOption("message").getAsString()).queueAfter(2250, TimeUnit.MILLISECONDS);
                            return;
                        }
                    }

                    event.reply("I have been silenced.")
                            .setEphemeral(true)
                            .queue();
                }
        );

        registerAdminCommand(
                Commands.slash("update", "Re-read the Workshift Tracker sheet and update caches"),
                (SlashCommandInteractionEvent e) -> {

                    ScheduleManager.updateCaches();

                    e.reply("Caches updated!")
                            .setEphemeral(true)
                            .queue();
                }
        );
    }

    public static void registerUserCommand(SlashCommandData data, Consumer<SlashCommandInteractionEvent> consumer) {
        data.setContexts(InteractionContextType.GUILD);
        data.setDefaultPermissions(DefaultMemberPermissions.ENABLED);

        slashRegistry.add(new BotCommand(data, consumer));
        apiRegistry.add(data);
    }

    public static void registerOtherCommand(CommandData data) {
        data.setContexts(InteractionContextType.GUILD);
        data.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));

        apiRegistry.add(data);
    }

    public static void registerSpeakingCommand(SlashCommandData data, Consumer<SlashCommandInteractionEvent> consumer) {
        data.setContexts(InteractionContextType.GUILD);
        data.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_GUILD_EXPRESSIONS));

        slashRegistry.add(new BotCommand(data, consumer));
        apiRegistry.add(data);
    }

    public static void registerAdminCommand(SlashCommandData data, Consumer<SlashCommandInteractionEvent> consumer) {
        data.setContexts(InteractionContextType.GUILD);
        data.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));

        slashRegistry.add(new BotCommand(data, consumer));
        apiRegistry.add(data);
    }
}
