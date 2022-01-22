package com.biogenic.lavaplayer;

import java.nio.ByteBuffer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;

import net.dv8tion.jda.api.audio.AudioSendHandler;

/**
 * Sends the audio returned from Lavaplayer to Discord via JDA
 */
public class AudioPlayerSendHandler implements AudioSendHandler {
    private final AudioPlayer audioPlayer; // Lavaplayer AudioPlayer
    private final ByteBuffer buffer; // Used to store a few bytes to send to Discord
    private final MutableAudioFrame frame; // What the player is writing the audio to

    /**
     * Constructor
     */
    public AudioPlayerSendHandler(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        this.buffer = ByteBuffer.allocate(1024); // How much Lavaplayer will provide every 20ms
        // Everything written to the frame will be written to the buffer
        this.frame = new MutableAudioFrame();
        this.frame.setBuffer(buffer);
    }

    /**
     * Write to the Mutable Audio Frame which will write to the ByteBuffer
     */
    @Override
    public boolean canProvide() {
        return this.audioPlayer.provide(this.frame);
    }

    /**
     * Flip the buffer and set the position to 0, so JDA can restart
     */
    @Override
    public ByteBuffer provide20MsAudio() {
        return this.buffer.flip();
    }

    /**
     * Lavaplayer will always return Opus unless explicitly configured.
     * If false, JDA will try to re-encode the audio after Lavaplayer has already
     * done so.
     * 
     * @return true
     */
    @Override
    public boolean isOpus() {
        return true;
    }

}
