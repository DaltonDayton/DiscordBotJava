package com.biogenic.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

/**
 * Music Manager
 */
public class GuildMusicManager {
    public final AudioPlayer audioPlayer;
    public final TrackScheduler scheduler;

    private final AudioPlayerSendHandler sendHandler;

    /**
     * Constructor
     * 
     * @param manager Helps create the audio player
     */
    public GuildMusicManager(AudioPlayerManager manager) {
        this.audioPlayer = manager.createPlayer();
        this.scheduler = new TrackScheduler(this.audioPlayer);
        this.audioPlayer.addListener(this.scheduler);
        this.sendHandler = new AudioPlayerSendHandler(this.audioPlayer);
    }

    /**
     * Getter for sendHandler
     */
    public AudioPlayerSendHandler getSendHandler() {
        return sendHandler;
    }

}
