package com.xraymod.client;

import com.xraymod.client.config.XRayConfig;
import com.xraymod.client.gui.XRayConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.KeyBinding.Category;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class XRayClient implements ClientModInitializer {

    public static KeyBinding xrayKey;
    public static KeyBinding configKey;
    private static boolean wasActive = false;

    @Override
    public void onInitializeClient() {
        XRayState.config = XRayConfig.load();

        Category cat = Category.create(Identifier.of("xraymod", "keys"));

        xrayKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.xraymod.xray", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, cat));

        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.xraymod.config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, cat));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                XRayState.playerChunkX = client.player.getBlockPos().getX() >> 4;
                XRayState.playerChunkZ = client.player.getBlockPos().getZ() >> 4;
            }

            // XRay toggle
            boolean isActive = xrayKey.isPressed();
            if (isActive != wasActive) {
                XRayState.active = isActive;
                wasActive = isActive;
                if (client.worldRenderer != null) {
                    client.worldRenderer.reload();
                }
            }

           // Fullbright using night vision effect
            if (client.player != null) {
                if (XRayState.config.isFullbright()) {
                    client.player.addStatusEffect(
                        new net.minecraft.entity.effect.StatusEffectInstance(
                            net.minecraft.entity.effect.StatusEffects.NIGHT_VISION,
                            Integer.MAX_VALUE, 0, false, false, false
                        )
                    );
                } else {
                    client.player.removeStatusEffect(
                        net.minecraft.registry.entry.RegistryEntry.of(
                            net.minecraft.entity.effect.StatusEffects.NIGHT_VISION
                        )
                    );
                }
            }

            // Config screen
            while (configKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new XRayConfigScreen(null));
                }
            }

            // Action bar
            if (client.player != null && XRayState.active) {
                client.player.sendMessage(
                    Text.literal("§bKPS+ §aXRay ACTIVE §7— §eX §7to disable | Range: §f"
                        + XRayState.config.getChunkRange() + " chunks"), true);
            }
        });
    }
}
