package com.biogenic;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.util.EnumSet;

public class Bot {

  public static JDA jda;

  private Bot() throws LoginException {
    jda = JDABuilder.create(EnumSet.allOf(GatewayIntent.class))
        .setToken(Config.get("TOKEN"))
        .addEventListeners(new Listener())
        .setActivity(Activity.playing("Transcendence"))
        .build();
  }

  public static void main(String[] args) throws LoginException {
    new Bot();
  }
}
