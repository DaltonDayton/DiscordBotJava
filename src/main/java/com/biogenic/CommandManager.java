package com.biogenic;

import com.biogenic.command.CommandContext;
import com.biogenic.command.ICommand;
import com.biogenic.command.commands.HelpCommand;
import com.biogenic.command.commands.PingCommand;
import com.biogenic.command.commands.music.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Manages a list of, and handles, commands
 */
public class CommandManager {
  private final List<ICommand> commands = new ArrayList<>();

  /**
   * Constructor
   */
  public CommandManager() {
    // General
    addCommand(new PingCommand());
    addCommand(new HelpCommand(this));

    // Music Commands
    addCommand(new JoinCommand());
    addCommand(new PlayCommand());
    addCommand(new StopCommand());
    addCommand(new SkipCommand());
    addCommand(new NowPlayingCommand());
    addCommand(new QueueCommand());
  }

  /**
   * Adds a new command to the command list after checking to see if it exists already
   * @param cmd The command
   */
  private void addCommand(ICommand cmd) {
    boolean nameFound = this.commands.stream().anyMatch((it) -> it.getName().equalsIgnoreCase(cmd.getName()));

    if (nameFound) {
      throw new IllegalArgumentException("The command: " + cmd.getName() + " already exists.");
    }

    commands.add(cmd);
  }

  /**
   * @return A list of all commands.
   */
  public List<ICommand> getCommands() {
    return commands;
  }

  /**
   * Searches for a command in the command list
   * @param search The command to search for
   * @return The command if it exists, otherwise null
   */
  @Nullable
  public ICommand getCommand(String search) {
    String searchLower = search.toLowerCase();

    for (ICommand cmd : this.commands) {
      if (cmd.getName().equals(searchLower) || cmd.getAliases().contains(searchLower)) {
        return cmd;
      }
    }

    return null;
  }

  /**
   * Handles the command entered into a text channel
   * @param event GuildMessageReceived event
   */
  void handle(GuildMessageReceivedEvent event) {
    // Gets the message content, removes the prefix, and splits on whitespace
    String[] split = event.getMessage().getContentRaw()
        .replaceFirst("(?i)" + Pattern.quote(Config.get("PREFIX")), "")
        .split("\\s+");

    String invoke = split[0].toLowerCase();
    ICommand cmd = this.getCommand(invoke);

    if (cmd != null) {
      event.getChannel().sendTyping().queue();
      List<String> args = Arrays.asList(split).subList(1, split.length);

      CommandContext ctx = new CommandContext(event, args);

      cmd.handle(ctx);
    } else {
      // Functionality for telling the user a command doesn't exist
    }
  }

}
