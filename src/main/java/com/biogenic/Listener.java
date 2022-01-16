package com.biogenic;

import me.duncte123.botcommons.BotCommons;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * Listens for events
 */
public class Listener extends ListenerAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(Listener.class);
  private final CommandManager manager = new CommandManager();

  /**
   * Executes when the bot is ready
   * @param event The Ready event
   */
  @Override
  public void onReady(@Nonnull ReadyEvent event) {
    LOGGER.info("{} is ready", event.getJDA().getSelfUser().getAsTag());
  }

  /**
   * Executes when a message is sent to a text channel
   * @param event The GuildMessageReceived event
   */
  @Override
  public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
    User user = event.getAuthor();

    if (user.isBot() || event.isWebhookMessage()) {
      return;
    }

    String prefix = Config.get("PREFIX");
    String raw = event.getMessage().getContentRaw();

    if (raw.equalsIgnoreCase(prefix + "shutdown")
        && user.getId().equals(Config.get("OWNER_ID"))) {
      LOGGER.info("Shutting down");
      event.getJDA().shutdown();
      BotCommons.shutdown(event.getJDA());

      return;
    }

    if (raw.startsWith(prefix)) {
      manager.handle(event);
    }
  }

}
