package com.biogenic.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton - only has one instance
 */
public class PlayerManager {
  private static PlayerManager INSTANCE;

  private final Map<Long, GuildMusicManager> musicManagers; // Maps guild ids to guild music managers
  private final AudioPlayerManager audioPlayerManager;

  /**
   * Constructor
   */
  public PlayerManager() {
    this.musicManagers = new HashMap<>();
    this.audioPlayerManager = new DefaultAudioPlayerManager();

    // 'teaches' audioPlayerManager what external video players are
    AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
    AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
  }

  /**
   * Gets a Music Manager for a guild
   * @param guild The guild
   * @return guildMusicManager
   */
  public GuildMusicManager getMusicManager(Guild guild) {
    return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
      final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager);

      // Tell JDA what to use for sending the audio. In this case: GuildMusicManagerSendingHandler
      guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());

      return guildMusicManager;
    });
  }

  /**
   * Loads tracks
   * @param channel TextChannel
   * @param trackUrl Track URL
   */
  public void loadAndPlay(TextChannel channel, String trackUrl) {
    final GuildMusicManager musicManager = this.getMusicManager(channel.getGuild());

    this.audioPlayerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
      // AudioLoadResultHandler is an interface

      /**
       * Append the audio track to the queue, or play if nothing is currently playing
       * @param track The Audio Track
       */
      @Override
      public void trackLoaded(AudioTrack track) {
        musicManager.scheduler.queue(track);

        channel.sendMessage("Adding to queue: `")
            .append(track.getInfo().title)
            .append("` by `")
            .append(track.getInfo().author)
            .queue();
      }

      @Override
      public void playlistLoaded(AudioPlaylist playlist) {

      }

      @Override
      public void noMatches() {

      }

      @Override
      public void loadFailed(FriendlyException exception) {

      }
    });
  }

  /**
   * Only assign if we need it
   * @return The Instance of PlayerManager
   */
  public static PlayerManager getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new PlayerManager();
    }

    return INSTANCE;
  }

}
