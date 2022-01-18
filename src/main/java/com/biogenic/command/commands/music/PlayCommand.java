package com.biogenic.command.commands.music;

import com.biogenic.CommandManager;
import com.biogenic.Config;
import com.biogenic.command.CommandContext;
import com.biogenic.command.ICommand;
import com.biogenic.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Makes the bot play a song
 */
public class PlayCommand implements ICommand {
  @Override
  public void handle(CommandContext ctx) {
    final TextChannel channel = ctx.getChannel();

    if (ctx.getArgs().isEmpty()) {
      channel.sendMessage("Usage: `" + Config.get("PREFIX") + "play <link>`");
      return;
    }

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

    String link = String.join(" ", ctx.getArgs());

    if (!isUrl(link)) {
      link = "ytsearch:" + link;
    }

    PlayerManager.getInstance().loadAndPlay(channel, link);
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
