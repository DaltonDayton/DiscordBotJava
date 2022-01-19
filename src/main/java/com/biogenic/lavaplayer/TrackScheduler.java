package com.biogenic.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Allows us to handle a queue of tracks
 */
public class TrackScheduler extends AudioEventAdapter {
  public final AudioPlayer player;
  public final BlockingQueue<AudioTrack> queue;
  public boolean repeating = false;

  /**
   * Constructor
   * @param player The audio player
   */
  public TrackScheduler(AudioPlayer player) {
    this.player = player;
    this.queue = new LinkedBlockingQueue<>();
  }

  /**
   * Queues a new track
   * @param track The track to be queued
   */
  public void queue(AudioTrack track) {
    if (!this.player.startTrack(track, true)) {
      this.queue.offer(track);
    }
  }

  /**
   * Switches to the next track
   */
  public void nextTrack() {
    this.player.startTrack(this.queue.poll(), false);
  }

  /**
   * Executes when the current track ends
   * @param player The audio player
   * @param track The current track
   * @param endReason The reason the track ended
   */
  @Override
  public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
    if (endReason.mayStartNext) {
      if (this.repeating) {
        this.player.startTrack(track.makeClone(), false);
        return;
      }
      nextTrack();
    }
  }

}
