package com.etsy;

import java.io.IOException;

public class DemoBot extends PlackBot {

    public DemoBot(String apiToken) {
        super(apiToken);
    }

    protected void onMessage(String channel, String sender, String login, String realName, String message) {
        System.out.println("Got message on #" + channel + " from @" + sender + ": " + message);
    }

    protected void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
        System.out.println("Got topic on #" + channel + " from @" + setBy + ": " + topic);
    }

    protected void onConnect() {
        System.out.println("Connected");
    }

    protected void onDisconnect() {
        System.out.println("Disconnect");
    }

    protected void onPrivateMessage(String sender, String login, String hostname, String message) {
        System.out.println("Got direct message from @" + sender + ": " + message);
    }

    protected void onJoin(String channel, String sender, String login, String hostname) {
        System.out.println("Joined #" + channel + " from @" + sender);
    }

    protected void onPart(String channel, String sender, String login, String hostname) {
        System.out.println("Left #" + channel + " from @" + sender);
    }

    public static void main (String[] argv) throws IOException, InterruptedException {
        DemoBot demoBot = new DemoBot(argv[0]);
        demoBot.connect();
    }

}
