package com.thragg.auctionmod;

import net.minecraft.client.MinecraftClient;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuctionManager {
    private static boolean active = false;
    private static String itemName = "Item";
    private static int minBid = 0;
    private static int highestBid = 0;
    private static String highestBidder = "Niemand";
    private static long endTime = 0;

    private static final Pattern PAY_PATTERN = Pattern.compile("(?i)\\bDu hast\\s*(\\d+)\\s*\\$?\\s*von\\s*([a-zA-Z0-9_]+)\\s*erhalten");

    public static void startAuction(String item, int startingBid, int durationSeconds) {
        active = true;
        itemName = item;
        minBid = startingBid;
        highestBid = startingBid - 1;
        highestBidder = "Niemand";
        endTime = System.currentTimeMillis() + (durationSeconds * 1000L);

        sendPublicChat(">>> AUKTION: [" + itemName + "] gestartet! Mindestgebot: " + startingBid + "$ | Dauer: " + durationSeconds + "s (/pay) <<<");
    }

    public static void onChatMessage(String message) {
        if (!active) return;

        Matcher matcher = PAY_PATTERN.matcher(message);
        if (matcher.find()) {
            int amount = Integer.parseInt(matcher.group(1));
            String player = matcher.group(2);

            if (amount > highestBid && amount >= minBid) {
                highestBid = amount;
                highestBidder = player;
                sendPublicChat("Neues Hoechstgebot fuer " + itemName + ": " + amount + "$ von " + player + "!");
            }
        }
    }

    public static void tick() {
        if (active && System.currentTimeMillis() >= endTime) {
            endAuction();
        }
    }

    private static void endAuction() {
        active = false;
        if (highestBidder.equals("Niemand")) {
            sendPublicChat("Auktion beendet! Fuer " + itemName + " gab es kein gueltiges Gebot.");
        } else {
            sendPublicChat("GEWONNEN! " + highestBidder + " erhaelt " + itemName + " fuer " + highestBid + "$!");
        }
    }

    public static void cancelAuction() {
        if (active) {
            active = false;
            sendPublicChat("Auktion fuer " + itemName + " wurde abgebrochen!");
        }
    }

    private static void sendPublicChat(String text) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.networkHandler.sendChatMessage(text);
        }
    }

    public static boolean isActive() { return active; }
    public static String getItemName() { return itemName; }
    public static int getHighestBid() { return highestBid; }
    public static String getHighestBidder() { return highestBidder; }
    public static int getSecondsLeft() {
        return Math.max(0, (int)((endTime - System.currentTimeMillis()) / 1000));
    }
}
