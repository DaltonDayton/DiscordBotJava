package com.biogenic;

import java.util.EnumSet;

import javax.security.auth.login.LoginException;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Bot {
    /**
     * Constructor
     */
    private Bot() throws LoginException {
        EventWaiter waiter = new EventWaiter();

        JDABuilder.createDefault(
                Config.get("TOKEN"),
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_VOICE_STATES)
                .disableCache(EnumSet.of(
                        CacheFlag.CLIENT_STATUS,
                        CacheFlag.ACTIVITY,
                        CacheFlag.EMOTE))
                .enableCache(CacheFlag.VOICE_STATE)
                .addEventListeners(new Listener(waiter), waiter)
                .setActivity(Activity.watching("Transcendence"))
                .build();
    }

    public static void main(String[] args) throws LoginException {
        // Creates a new instance of the bot
        new Bot();
    }

}
