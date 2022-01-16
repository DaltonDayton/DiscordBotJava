package com.biogenic;

import me.duncte123.botcommons.BotCommons;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class Listener extends ListenerAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(Listener.class);

  @Override
  public void onReady(@Nonnull ReadyEvent event) {
    LOGGER.info("{} is ready", event.getJDA().getSelfUser().getAsTag());
  }

  @Override
  public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
    String prefix = Config.get("PREFIX");
    String raw = event.getMessage().getContentRaw();

    if (raw.equalsIgnoreCase(prefix + "shutdown")
        && event.getAuthor().getId().equals(Config.get("OWNER_ID"))) {
      LOGGER.info("Shutting down");
      event.getJDA().shutdown();
      BotCommons.shutdown(event.getJDA());
    }
  }
}
