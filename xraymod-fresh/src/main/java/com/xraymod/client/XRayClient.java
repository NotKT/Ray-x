package com.xraymod.client;

import com.xraymod.client.config.XRayConfig;
import com.xraymod.client.gui.XRayConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class XRayClient implements ClientModInitializer {

    public static KeyBinding xrayKey;
    public static KeyBinding configKey;

    @Override
    public void onInitializeClient() {
        XRayState.config = XRayConfig.load();

        xrayKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.xraymod.xray",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            KeyBinding.MISC_CATEGORY
        ));

        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.xraymod.config",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            KeyBinding.MISC_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            XRayState.active = xrayKey.isPressed();
            while (configKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new XRayConfigScreen(null));
                }
            }
            if (client.player != null && XRayState.active) {
                client.player.sendMessage(
                    Text.literal("§bXRay §aACTIVE §7— release §eX §7to disable"), true);
            }
        });
    }
}
