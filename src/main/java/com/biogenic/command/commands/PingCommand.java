package com.biogenic.command.commands;

import com.biogenic.command.CommandContext;
import com.biogenic.command.ICommand;
import net.dv8tion.jda.api.JDA;

/**
 * Responds with the user's ping
 */
public class PingCommand implements ICommand {
  @Override
  public void handle(CommandContext ctx) {
    JDA jda = ctx.getJDA();

    jda.getRestPing().queue(
        (ping) -> ctx.getChannel()
            .sendMessageFormat("Reset ping: %sms\nWS ping: %sms", ping, jda.getGatewayPing()).queue()
    );
  }

  /**
   * @return The name of the command
   */
  @Override
  public String getName() {
    return "ping";
  }

}
