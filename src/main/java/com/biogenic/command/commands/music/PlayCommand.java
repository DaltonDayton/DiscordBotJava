package com.biogenic.command.commands.music;

import com.biogenic.Config;
import com.biogenic.command.CommandContext;
import com.biogenic.command.ICommand;
import com.biogenic.lavaplayer.PlayerManager;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Makes the bot play a song
 */
public class PlayCommand implements ICommand {
    private final EventWaiter waiter;

    public PlayCommand(EventWaiter waiter) {
        this.waiter = waiter;
    }

    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();

        if (ctx.getArgs().isEmpty()) {
            channel.sendMessage("Usage: `" + Config.get("PREFIX") + "play <link>`");
            return;
        }

        final Member self = ctx.getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();
        VoiceChannel selfVoiceStateChannel = selfVoiceState.getChannel();

        final Member member = ctx.getMember();
        final GuildVoiceState memberVoiceState = member.getVoiceState();

        if (!selfVoiceState.inVoiceChannel()) {
            JoinCommand joinCommand = new JoinCommand();
            joinCommand.handle(ctx);
            selfVoiceStateChannel = memberVoiceState.getChannel();
        }

        if (!memberVoiceState.inVoiceChannel()) {
            channel.sendMessage("You need to be in a voice channel for this to work.").queue();
            return;
        }

        if (!memberVoiceState.getChannel().equals(selfVoiceStateChannel)) {
            channel.sendMessage("You need to be in the same voice channel as me for this to work.").queue();
            return;
        }

        String link = String.join(" ", ctx.getArgs());

        if (!isUrl(link)) {
            link = "ytsearch:" + link;
        }

        PlayerManager.getInstance().loadAndPlay(channel, link, waiter);
    }

    /**
     * @return The name of the command
     */
    @Override
    public String getName() {
        return "play";
    }

    /**
     * @return Command Description
     */
    @Override
    public String getHelp() {
        return "Plays a song\n" +
                "Usage: `" + Config.get("PREFIX") + "play <link>`";
    }

    private boolean isUrl(String url) {
        try {
            new URI(url);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

}
