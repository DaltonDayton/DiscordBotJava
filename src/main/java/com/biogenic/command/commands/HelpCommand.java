package com.biogenic.command.commands;

import com.biogenic.CommandManager;
import com.biogenic.Config;
import com.biogenic.command.CommandContext;
import com.biogenic.command.ICommand;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

/**
 * Shows the description of a given command
 */
public class HelpCommand implements ICommand {
  private final CommandManager manager; // Gives access to our commands

  public HelpCommand(CommandManager manager) {
    this.manager = manager;
  }

  @Override
  public void handle(CommandContext ctx) {
    List<String> args = ctx.getArgs();
    TextChannel channel = ctx.getChannel();

    if (args.isEmpty()) {
      StringBuilder builder = new StringBuilder();

      builder.append("List of commands\n");

      manager.getCommands().stream().map(ICommand::getName).forEach(
          (it) -> builder.append('`').append(Config.get("PREFIX")).append(it).append("`\n")
      );

      channel.sendMessage(builder.toString()).queue();
      return;
    }

    String search = args.get(0);
    ICommand command = manager.getCommand(search);

    if (command == null) {
      channel.sendMessage("Nothing found for " + search).queue();
      return;
    }

    channel.sendMessage(command.getHelp()).queue();
  }

  /**
   * @return The name of the command
   */
  @Override
  public String getName() {
    return "help";
  }

  /**
   * @return Command Description
   */
  @Override
  public String getHelp() {
    return "Shows the list with commands in the bot\n" +
        "Usage: `" + Config.get("PREFIX") + "help [command]`";
  }

  @Override
  public List<String> getAliases() {
    return List.of("commands", "cmds", "commandlist");
  }
}
