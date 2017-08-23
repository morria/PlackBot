package com.etsy;

import com.ullink.slack.simpleslackapi.*;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackChannelJoined;
import com.ullink.slack.simpleslackapi.events.SlackChannelLeft;
import com.ullink.slack.simpleslackapi.events.SlackConnected;
import com.ullink.slack.simpleslackapi.events.SlackDisconnected;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.events.ReactionAdded;
import com.ullink.slack.simpleslackapi.events.ReactionRemoved;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackChannelJoinedListener;
import com.ullink.slack.simpleslackapi.listeners.SlackChannelLeftListener;
import com.ullink.slack.simpleslackapi.listeners.SlackConnectedListener;
import com.ullink.slack.simpleslackapi.listeners.SlackDisconnectedListener;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import com.ullink.slack.simpleslackapi.listeners.ReactionAddedListener;
import com.ullink.slack.simpleslackapi.listeners.ReactionRemovedListener;
import com.ullink.slack.simpleslackapi.replies.SlackChannelReply;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.*;

/**
*/
public class PlackBot extends AbstractPircBot
{
    private SlackSession session = null;

    /**
    * The API token given by Slack
    */
    private String apiToken = null;

    public PlackBot(String apiToken) {
        this.apiToken = apiToken;
    }

    /**
     * Gets the internal login of the PlackBot.
     */
    public String getLogin() {
        return this.session.sessionPersona().getUserName();
    }

    /**
     * Gets the name of the PlackBot
     */
    public String getName() {
        return this.session.sessionPersona().getRealName();
    }

    /**
     * Returns the current nick of hte bot
     */
    public String getNick() {
        return this.session.sessionPersona().getUserName();
    }

    public String getChannelTopic(String channelName) {
        try {
            SlackChannel channel = this.slackChannelFromName(channelName);
            return StringEscapeUtils.unescapeHtml4(channel.getTopic());
        } catch (ChannelNotFoundException exception) {
            System.err.println(exception.getMessage());
        }

        return "";
    }

    /**
     * Returns true if we're connected, else false
     */
    public boolean isConnected() {
        return (this.session != null && this.session.isConnected());
    }

    /**
     * Reconnects to the Slack server that we were previously connected to.
     */
    public void reconnect() throws IOException {
        if (!this.isConnected()) {
            this.connect();
        } else {
            this.disconnect();
            this.connect();
        }
    }

    /**
    * Attempts to connect to the server
    */
    public void connect() throws IOException {
        this.session =
            SlackSessionFactory.createWebSocketSlackSession(this.apiToken);

        // Listen for when we connect
        session.addSlackConnectedListener(new SlackConnectedListener() {
            @Override
            public void onEvent(SlackConnected event, SlackSession session) {
                PlackBot.this.onConnect();
            }
        });

        // Listen for when we're disconnected
        session.addSlackDisconnectedListener(new SlackDisconnectedListener() {
            @Override
            public void onEvent(SlackDisconnected event, SlackSession session) {
                PlackBot.this.onDisconnect();
            }
        });

        // Listen for when we join a channel
        session.addChannelJoinedListener(new SlackChannelJoinedListener() {
            @Override
            public void onEvent(SlackChannelJoined event, SlackSession session) {
                SlackChannel channel = event.getSlackChannel();
                PlackBot.this.onJoin(
                    channel.getName(),
                    PlackBot.this.getNick(),
                    PlackBot.this.getLogin(),
                    PlackBot.this.getName()
                );
            }
        });

        // Listen for when we leave a channel
        session.addChannelLeftListener(new SlackChannelLeftListener() {
          @Override
          public void onEvent(SlackChannelLeft event, SlackSession session) {
              SlackChannel channel = event.getSlackChannel();
              PlackBot.this.onPart(
                  channel.getName(),
                  PlackBot.this.getNick(),
                  PlackBot.this.getLogin(),
                  PlackBot.this.getName()
              );
          }
        });

        // Listen for new messages
        session.addMessagePostedListener(new SlackMessagePostedListener() {
            @Override
            public void onEvent(SlackMessagePosted event, SlackSession session) {
                SlackChannel channel = event.getChannel();
                String messageContent = event.getMessageContent();
                SlackUser user = event.getSender();

                SlackMessagePosted.MessageSubType messageSubType =
                event.getMessageSubType();

                if (messageSubType == SlackMessagePosted.MessageSubType.CHANNEL_TOPIC) {
                    // String topic = (String)event.getJsonSource().get("topic");
                    JSONObject jsonObject = new JSONObject(event.getJsonSource());

                    String topic = StringEscapeUtils.unescapeHtml4(
                        jsonObject.getString("topic")
                    );

                    PlackBot.this.onTopic(
                        channel.getName(),
                        topic,
                        user.getUserName(),
                        0,
                        true
                        );
                } else if (channel.isDirect()) {
                    PlackBot.this.onPrivateMessage(
                        user.getUserName(),
                        user.getUserMail(),
                        user.getRealName(),
                        messageContent
                    );
                } else if (messageSubType == SlackMessagePosted.MessageSubType.UNKNOWN) {
                    PlackBot.this.onMessage(
                        channel.getName(),
                        user.getUserName(),
                        user.getUserMail(),
                        user.getRealName(),
                        messageContent
                    );
                }
            }
        });

        // Listen for new reactions added
        session.addReactionAddedListener(new ReactionAddedListener() {
            @Override
            public void onEvent(ReactionAdded event, SlackSession session) {
                String emojiName = event.getEmojiName();
                SlackUser user = event.getUser();
                SlackUser itemUser = event.getItemUser();
                SlackChannel channel = event.getChannel();

                // reactions don't always have a channel or item user, set defaults
                String userName = (user != null) ? user.getUserName() : null;
                String itemUserName = (itemUser != null) ? itemUser.getUserName() : null;
                String channelName = (channel != null) ? channel.getName() : null;

                PlackBot.this.onReactionAdded(
                    channelName,
                    userName,
                    itemUserName,
                    emojiName
                );
            }
        });

        // Listen for new reactions removed
        session.addReactionRemovedListener(new ReactionRemovedListener() {
            @Override
            public void onEvent(ReactionRemoved event, SlackSession session) {
                String emojiName = event.getEmojiName();
                SlackUser user = event.getUser();
                SlackUser itemUser = event.getItemUser();
                SlackChannel channel = event.getChannel();

                // reactions don't always have a channel or item user, set defaults
                String userName = (user != null) ? user.getUserName() : null;
                String itemUserName = (itemUser != null) ? itemUser.getUserName() : null;
                String channelName = (channel != null) ? channel.getName() : null;

                PlackBot.this.onReactionRemoved(
                    channelName,
                    userName,
                    itemUserName,
                    emojiName
                );
            }
        });

        this.session.connect();
    }

    /**
     * Disconnects from the server
     */
    public void disconnect() throws IOException {
        this.session.disconnect();
        this.session = null;
    }

    /**
     * Returns an array of all channels that we are in.
     */
    public String[] getChannels() {
        Collection<SlackChannel> slackChannelCollection =
            this.session.getChannels();

        List<String> channelNameList = new ArrayList<String>();

        for (SlackChannel slackChannel : slackChannelCollection) {
            if (slackChannel.isMember()) {
                channelNameList.add(slackChannel.getName());
            }
        }

        return channelNameList.toArray(new String[channelNameList.size()]);
    }

    /**
     * Joins a channel
     */
    public void joinChannel(String channel) {
        this.session.joinChannel(channel);
    }

    /**
     * This method is called once the PlackBot has successfully connected to the server.
     */
    protected void onConnect() {}

    /**
     * This method carries out the actions to be performed when the PircBot gets disconnected.
     */
    protected void onDisconnect() {}

    /**
     * This method is called whenever someone (possibly us) joins a channel which we are on.
     */
    protected void onJoin(String channel, String sender, String login, String hostname) {}

    /**
     * This method is called whenever a message is sent to a channel.
     */
    protected void onMessage(String channel, String sender, String login, String realName, String message) {}

    /**
     * This method is called whenever a reaction is added in a channel.
     */
    protected void onReactionAdded(String channel, String sender, String receiver, String emojiName) {}

    /**
     * This method is called whenever a reaction is removed in a channel.
     */
    protected void onReactionRemoved(String channel, String sender, String receiver, String emojiName) {}

    /**
     * This method is called whenever someone (possibly us) joins a channel which we are on.
     */
    protected void onPart(String channel, String sender, String login, String hostname) {}

    /**
     * This method is called whenever a private message is sent to the PircBot.
     */
    protected void onPrivateMessage(String sender, String login, String hostname, String message) {}

    /**
     * This method is called whenever a user sets the topic, or when PircBot joins a new channel and discovers its topic.
     */
    protected void onTopic(String channel, String topic, String setBy, long date, boolean changed) {}

    /**
     * Sends a message to a channel or a private message to a user.
     */
    public void sendMessage(String target, String message) throws ChannelNotFoundException {
        if (target.startsWith("@")) {
            this.session.sendMessageToUser(target, message, null);
        }

        try {
            SlackChannel channel = this.slackChannelFromName(target);
            this.session.sendMessage(channel, message);
        } catch (ChannelNotFoundException exception) {
            SlackUser user = this.session.findUserByUserName(target);
            if (user != null) {
                this.session.sendMessageToUser(user, message, null);
            } else {
                throw exception;
            }
        }
    }

    /**
     * Sets the topic for a channel
     */
    public void setTopic(String channelName, String topic) {
        try {
            SlackChannel channel = this.slackChannelFromName(channelName);
            this.session.setChannelTopic(channel, topic);
        } catch (ChannelNotFoundException exception) {
            System.err.println(exception.getMessage());
        }
    }

    /**
     * Get a user object from a userID or null if not found
     */
    protected SlackUser slackUserFromId(String userId) {
        try {
            SlackUser user = this.session.findUserById(userId);
            return user;
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            return null;
        }
    }

    /**
     * Get a nick from a userId
     */
    public String nickFromUserId(String userId) {
        SlackUser user = this.slackUserFromId(userId);
        return user == null ? null : user.getUserName();
    }

    /**
     * @param String channelName
     * The name of a channel.
     *
     * @return SlackChannel
     * If the channel can be found, it'll be returned. Otherwise, null
     * will be returned.
     *
     * @throws ChannelNotFoundException
     * If the channel cannot be found, an exception will be thrown
     */
    private SlackChannel slackChannelFromName(
      String channelName
    ) throws ChannelNotFoundException {

        if (channelName == null) {
          channelName = "";
        }

        // Strip out any leading channel characters
        if ('#' == channelName.charAt(0)) {
          channelName = channelName.substring(1);
        }

        // Look up the name on the known list of channels
        SlackChannel slackChannel =
          this.session.findChannelByName(channelName);

        if (slackChannel == null) {
          throw new ChannelNotFoundException("No such channel " + channelName);
        }

        return slackChannel;
    }

}
