package com.thragg.auctionmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class AuctionMod implements ClientModInitializer {
    private static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            AuctionManager.onChatMessage(message.getString());
        });

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.auctionmod.opengui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "category.auctionmod"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            AuctionManager.tick();
            while (openGuiKey.wasPressed()) {
                if (client.player != null) {
                    client.setScreen(new AuctionScreen());
                }
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("mika")
                .executes(context -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.send(() -> client.setScreen(new AuctionScreen()));
                    return 1;
                })
            );

            dispatcher.register(ClientCommandManager.literal("auktion")
                .executes(context -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.send(() -> client.setScreen(new AuctionScreen()));
                    return 1;
                })
            );
        });
    }
}
