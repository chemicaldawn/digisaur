package info.dawns.bot;

import info.dawns.Constants;
import info.dawns.scheduling.*;
import info.dawns.utils.Registry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

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

                Message verificationMessage = BotUtils.getLatestMessageFrom(id, e.getChannel());
                boolean validVerificationMessage = false;

                if (verificationMessage != null) {
                    if (!verificationMessage.getAttachments().isEmpty() && !BotUtils.isAcknowledged(verificationMessage)) {
                        validVerificationMessage = true;
                    }
                }

                if (validVerificationMessage) {
                    Schedule userSchedule = ScheduleManager.getSchedule(id);
                    Set<ShiftType> shiftTypeOptions = new HashSet<>();
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
                        case "quest":
                            shiftTypeOptions = ScheduleManager.getQuestShifts();
                            break;
                    }

                    Bot.verificationMemory.put(e.getUser().getIdLong(),
                            new VerificationContext(verificationMessage.getIdLong(), workshiftType));

                    if (shifts.size() != 0) {
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

        registerUserCommand(Commands.slash("sell", "Put your workshift on the market")
                        .addOption(OptionType.STRING, "message", "The message to attach to your workshift sale", true),
            (SlashCommandInteractionEvent e) -> {

                e.reply("Select the workshift you'd like to sell...")
                        .addActionRow(StringSelectMenu.create("sell")
                                .addOptions(BotUtils.selectOptionsFor(ScheduleManager.getSchedule(e.getUser().getIdLong()).getAllShifts()))
                                .build())
                        .setEphemeral(true)
                        .queue();
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

                    List<ShiftType> schedule = ScheduleManager.getSchedule(id).getShiftsFor(d);
                    for (ShiftType s : schedule) {
                        shifts += s.getName() + "\n";
                    }

                    if (!schedule.isEmpty()) {
                        builder.addField(new MessageEmbed.Field(d.toString(), shifts, false));
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
