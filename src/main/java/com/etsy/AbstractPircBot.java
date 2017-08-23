package com.etsy;

import java.io.IOException;

/**
 * A clone of the PircBot API
 */
abstract class AbstractPircBot
{
    /**
     */
    abstract public void connect() throws IOException;

    /**
     */
    abstract public void disconnect() throws IOException;

    /**
     * Returns an array of all channels that we are in.
     */
    abstract public String[] getChannels();

    /**
     * Gets the internal login of the SlackBot.
     */
    abstract public String getLogin();

    /**
     * Gets the name of the SlackBot
     */
    abstract public String getName();

    /**
     * Returns the current nick of hte bot
     */
    abstract public String getNick();

    /**
     * Returns whether or not the SlackBot is currently connected to a server.
     */
    abstract public boolean isConnected();

    /**
     * Joins a channel
     */
    abstract public void joinChannel(String channel);

    /**
     * This method is called once the SlackBot has successfully connected to the server.
     */
    abstract protected void onConnect();

    /**
     * This method carries out the actions to be performed when the PircBot gets disconnected.
     */
    abstract protected void onDisconnect();

    /**
     * Called when we are invited to a channel by a user.
     */
    // abstract protected void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String channel);

    /**
     * This method is called whenever someone (possibly us) joins a channel which we are on.
     */
    abstract protected void onJoin(String channel, String sender, String login, String hostname) ;

    /**
     * This method is called whenever a message is sent to a channel.
     */
    abstract protected void onMessage(String channel, String sender, String login, String realName, String message);

    /**
     * This method is called whenever someone (possibly us) parts a channel which we are on.
     */
    abstract protected void onPart(String channel, String sender, String login, String hostname);

    /**
     * This method is called whenever a private message is sent to the PircBot.
     */
    abstract protected void onPrivateMessage(String sender, String login, String hostname, String message);

    /**
     * This method is called whenever someone (possibly us) quits from the server.
     */
    // abstract protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason);

    /**
     * This method is called whenever a user sets the topic, or when PircBot joins a new channel and discovers its topic.
     */
    abstract protected void onTopic(String channel, String topic, String setBy, long date, boolean changed);

    /**
     * Reconnects to the Slack server that we were previously connected to.
     */
    abstract public void reconnect() throws IOException;

    /**
     * Sends a message to a channel or a private message to a user.
     */
    abstract public void sendMessage(String target, String message) throws ChannelNotFoundException;

    /**
     * Sets the topic for a channel
     */
    abstract public void setTopic(String channel, String topic);

}
