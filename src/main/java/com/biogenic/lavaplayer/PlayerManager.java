package com.biogenic.lavaplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.vdurmont.emoji.EmojiParser;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;

/**
 * Singleton - only has one instance
 */
public class PlayerManager {
    private static PlayerManager instance;

    private final Map<Long, GuildMusicManager> musicManagers; // Maps guild ids to guild music managers
    private final AudioPlayerManager audioPlayerManager;
    private ArrayList<String> emojiList = new ArrayList<>();

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
     * 
     * @param guild The guild
     * @return guildMusicManager
     */
    public GuildMusicManager getMusicManager(Guild guild) {
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), guildId -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager);

            // Tell JDA what to use for sending the audio. In this case:
            // GuildMusicManagerSendingHandler
            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());

            return guildMusicManager;
        });
    }

    /**
     * Loads tracks
     * 
     * @param channel  TextChannel
     * @param trackUrl Track URL
     */
    public void loadAndPlay(TextChannel channel, String trackUrl, EventWaiter waiter) {
        final GuildMusicManager musicManager = this.getMusicManager(channel.getGuild());

        this.audioPlayerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            // AudioLoadResultHandler is an interface

            /**
             * Append the audio track to the queue, or play if nothing is currently playing
             * 
             * @param track The Audio Track
             */
            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.scheduler.queue(track);

                addedToQueueMessage(channel, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                final List<AudioTrack> tracks = playlist.getTracks();

                // Create EmojiList for song selection
                emojiList.add(EmojiParser.parseToUnicode(":one:"));
                emojiList.add(EmojiParser.parseToUnicode(":two:"));
                emojiList.add(EmojiParser.parseToUnicode(":three:"));
                emojiList.add(EmojiParser.parseToUnicode(":four:"));
                emojiList.add(EmojiParser.parseToUnicode(":five:"));

                if (playlist.getName().contains("Search results")) {
                    // Handles ytsearch results
                    sendMessageGetReaction(channel, tracks, waiter, musicManager);
                } else {
                    // Add full playlist of songs
                    channel.sendMessage("Adding to queue: `")
                            .append(String.valueOf(tracks.size()))
                            .append("` tracks from playlist `")
                            .append(playlist.getName())
                            .append("`")
                            .queue();

                    for (final AudioTrack track : tracks) {
                        musicManager.scheduler.queue(track);
                    }
                }

            }

            @Override
            public void noMatches() {
                channel.sendMessage("No matches found. Try adding more information (title, author, etc)").queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("loadFailed").queue();
            }

        });

    }

    /**
     * Sends a menu with the top five tracks to the text channel, gets the reaction,
     * and adds the selected song.
     * 
     * @param channel      The text channel
     * @param tracks       The list of tracks
     * @param waiter       The EventWaiter
     * @param musicManager The Music Manager
     */
    private void sendMessageGetReaction(TextChannel channel, List<AudioTrack> tracks, EventWaiter waiter,
            GuildMusicManager musicManager) {
        AtomicInteger selectedEmojiIndex = new AtomicInteger();

        // Send Menu to chat and get reaction
        channel.sendMessage(topTrackList(tracks))
                .queue((message -> {
                    for (String emoji : emojiList) {
                        message.addReaction(emoji).queue();
                    }

                    waiter.waitForEvent(
                            GuildMessageReactionAddEvent.class,
                            e -> e.getMessageIdLong() == message.getIdLong() && !e.getUser().isBot(),
                            e -> {
                                String selectedEmoji = EmojiParser
                                        .parseToAliases(e.getReactionEmote().getEmoji());
                                switch (selectedEmoji) {
                                    case ":one:":
                                        selectedEmojiIndex.set(0);
                                        break;
                                    case ":two:":
                                        selectedEmojiIndex.set(1);
                                        break;
                                    case ":three:":
                                        selectedEmojiIndex.set(2);
                                        break;
                                    case ":four:":
                                        selectedEmojiIndex.set(3);
                                        break;
                                    case ":five:":
                                        selectedEmojiIndex.set(4);
                                        break;
                                    default:
                                        break;
                                }

                                AudioTrack selectedSong = tracks.get(selectedEmojiIndex.get());
                                addedToQueueMessage(channel, selectedSong);

                                // Add single song from menu
                                musicManager.scheduler.queue(tracks.get(selectedEmojiIndex.get()));
                            },
                            20L, TimeUnit.SECONDS,
                            () -> channel.sendMessage("Timed out.").queue());
                })); // end sendMessage queue
    }

    /**
     * Creates the menu for song selection
     * 
     * @param tracks A list of audio tracks
     * @return The top five tracks in a menu
     */
    private String topTrackList(List<AudioTrack> tracks) {
        StringBuilder topTrackListMenu = new StringBuilder();
        for (int i = 0; i < tracks.size() && i < 5; i++) {
            String trackDuration;
            long millis = tracks.get(i).getDuration();
            long hours = TimeUnit.MILLISECONDS.toHours(millis);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(millis)
                    - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));

            // Remove extra minutes
            if (minutes >= 60) {
                minutes -= minutes / 60 * 60;
            }
            // Remove extra seconds
            if (seconds >= 60) {
                seconds -= seconds / 60 * 60;
            }

            // Change formatting based on duration
            if (hours >= 1) {
                trackDuration = String.format("%02d:%02d:%02d",
                        hours,
                        minutes,
                        seconds);
            } else {
                trackDuration = String.format("%02d:%02d",
                        minutes,
                        seconds);
            }

            topTrackListMenu
                    .append("> " + emojiList.get(i) + " - ")
                    .append("__" + tracks.get(i).getInfo().title)
                    .append("__ - `" + trackDuration)
                    .append("` by **" + tracks.get(i).getInfo().author + "**\n");
        }

        return topTrackListMenu.toString();
    }

    /**
     * Sends an 'Adding to queue: ' message to the TextChannel
     * 
     * @param channel The TextChannel the message is sent to
     * @param track   The track being added
     */
    private void addedToQueueMessage(TextChannel channel, AudioTrack track) {
        channel.sendMessage("Adding to queue: `")
                .append(track.getInfo().title)
                .append("` by `")
                .append(track.getInfo().author)
                .append("`")
                .queue();
    }

    /**
     * Only assign if we need it
     * 
     * @return The Instance of PlayerManager
     */
    public static PlayerManager getInstance() {
        if (instance == null) {
            instance = new PlayerManager();
        }

        return instance;
    }

}
