package com.biogenic.command.commands.music;

import com.biogenic.command.CommandContext;
import com.biogenic.command.ICommand;
import com.biogenic.lavaplayer.GuildMusicManager;
import com.biogenic.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

public class RepeatCommand implements ICommand {
  @Override
  public void handle(CommandContext ctx) {
    final TextChannel channel = ctx.getChannel();
    final Member self = ctx.getSelfMember();
    final GuildVoiceState selfVoiceState = self.getVoiceState();

    if (!selfVoiceState.inVoiceChannel()) {
      channel.sendMessage("I need to be in a voice channel for this to work.").queue();
//      new JoinCommand().handle(ctx);
      return;
    }

    final Member member = ctx.getMember();
    final GuildVoiceState memberVoiceState = member.getVoiceState();

    if (!memberVoiceState.inVoiceChannel()) {
      channel.sendMessage("You need to be in a voice channel for this to work.").queue();
      return;
    }

    if (!memberVoiceState.getChannel().equals(selfVoiceState.getChannel())) {
      channel.sendMessage("You need to be in the same voice channel as me for this to work.").queue();
      return;
    }

    final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
    final boolean newRepeating = !musicManager.scheduler.repeating;

    musicManager.scheduler.repeating = newRepeating;

    channel.sendMessageFormat("The player has been set to **%s**", newRepeating ? "repeating" : "not repeating").queue();
  }

  @Override
  public String getName() {
    return "repeat";
  }

  @Override
  public String getHelp() {
    return "Loops the current song.";
  }
}