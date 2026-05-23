package com.xraymod.client;

import com.xraymod.client.config.XRayConfig;
import com.xraymod.client.gui.XRayConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.KeyBinding.Category;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class XRayClient implements ClientModInitializer {

    public static KeyBinding xrayKey;
    public static KeyBinding configKey;
    public static KeyBinding entityGlowKey;
    private static boolean wasActive = false;
    private static boolean wasEntityGlowActive = false;

    @Override
    public void onInitializeClient() {
        XRayState.config = XRayConfig.load();

        Category cat = Category.create(Identifier.of("xraymod", "keys"));

        xrayKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.xraymod.xray", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, cat));

        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.xraymod.config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, cat));

        entityGlowKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.xraymod.entityglow", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, cat));

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

            // Entity glow toggle
            boolean isEntityGlowActive = entityGlowKey.isPressed();
            if (isEntityGlowActive != wasEntityGlowActive) {
                XRayState.entityGlowActive = isEntityGlowActive;
                wasEntityGlowActive = isEntityGlowActive;
            }


            // Fullbright via night vision
            if (client.player != null) {
                boolean hasnv = client.player.hasStatusEffect(StatusEffects.NIGHT_VISION);
                if (XRayState.config.isFullbright() && !hasnv) {
                    client.player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.NIGHT_VISION,
                        Integer.MAX_VALUE, 0, false, false, false
                    ));
                } else if (!XRayState.config.isFullbright() && hasnv) {
                    client.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
                }
            }

            // Config screen
            while (configKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new XRayConfigScreen(null));
                }
            }

            // Action bar
            if (client.player != null) {
                if (XRayState.active && XRayState.entityGlowActive) {
                    client.player.sendMessage(Text.literal(
                        "§bKPS+ §aXRay + EntityGlow ACTIVE §7— Range: §f"
                        + XRayState.config.getChunkRange() + " chunks"), true);
                } else if (XRayState.active) {
                    client.player.sendMessage(Text.literal(
                        "§bKPS+ §aXRay ACTIVE §7— §eX §7to disable | Range: §f"
                        + XRayState.config.getChunkRange() + " chunks"), true);
                } else if (XRayState.entityGlowActive) {
                    client.player.sendMessage(Text.literal(
                        "§bKPS+ §aEntity Glow ACTIVE §7— §eC §7to disable"), true);
                }
            }
        });
    }
}
