package com.biogenic.command.commands;

import java.util.concurrent.TimeUnit;

import com.biogenic.command.CommandContext;
import com.biogenic.command.ICommand;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.vdurmont.emoji.EmojiParser;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;

public class EventWaiterCommand implements ICommand {
    String emoteOne = ":one:";
    private final String emote = EmojiParser.parseToUnicode(emoteOne);
    private final EventWaiter waiter;

    public EventWaiterCommand(EventWaiter waiter) {
        this.waiter = waiter;
    }

    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();

        channel.sendMessage("React with ")
                .append(emote)
                .queue((message -> {
                    message.addReaction(emote).queue();

                    this.waiter.waitForEvent(
                            GuildMessageReactionAddEvent.class,
                            e -> e.getMessageIdLong() == message.getIdLong() && !e.getUser().isBot(),
                            e -> channel.sendMessageFormat("%#s was the first to react", e.getUser()).queue(),
                            20L, TimeUnit.SECONDS,
                            () -> channel.sendMessage("You waited too long").queue());
                }));
    }

    @Override
    public String getName() {
        return "eventwaiter";
    }

    @Override
    public String getHelp() {
        return "An event waiter example";
    }
}
