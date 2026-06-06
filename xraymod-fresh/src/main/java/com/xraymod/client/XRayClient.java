package com.xraymod.client;

import com.xraymod.client.config.XRayConfig;
import com.xraymod.client.gui.XRayConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class XRayClient implements ClientModInitializer {

    // XRay
    private static KeyBinding xrayKey;
    private static boolean xrayHeld = false;

    // Entity Glow
    private static KeyBinding entityGlowKey;
    private static boolean entityGlowHeld = false;

    // Config Screen
    private static KeyBinding configKey;

    // Utility keys
    private static KeyBinding flyKey;
    private static KeyBinding fastBreakKey;
    private static KeyBinding reachKey;
    private static KeyBinding noFallKey;
    private static KeyBinding maceWindBurstKey;

    @Override
    public void onInitializeClient() {
        XRayConfig.load();

        KeyBinding.Category cat = KeyBinding.Category.create(
            net.minecraft.util.Identifier.of("xraymod", "keys"));

        xrayKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.xraymod.xray", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, cat));
        entityGlowKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.xraymod.entityglow", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, cat));
        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.xraymod.config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, cat));

        flyKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.xraymod.fly", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, cat));
        fastBreakKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.xraymod.fastbreak", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, cat));
        reachKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.xraymod.reach", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_N, cat));
        noFallKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.xraymod.nofall", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M, cat));
        maceWindBurstKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.xraymod.macewindburst", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, cat));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            // Update player chunk position for XRay range
            XRayState.playerChunkX = client.player.getChunkPos().x;
            XRayState.playerChunkZ = client.player.getChunkPos().z;

            // --- XRay ---
            if (XRayConfig.instance.xrayToggleMode) {
                if (xrayKey.wasPressed()) {
                    XRayState.xrayActive = !XRayState.xrayActive;
                    if (client.worldRenderer != null) client.worldRenderer.reload();
                }
            } else {
                boolean nowHeld = xrayKey.isPressed();
                if (nowHeld != xrayHeld) {
                    xrayHeld = nowHeld;
                    XRayState.xrayActive = nowHeld;
                    if (client.worldRenderer != null) client.worldRenderer.reload();
                }
            }

            // --- Entity Glow ---
            if (XRayConfig.instance.entityGlowToggleMode) {
                if (entityGlowKey.wasPressed()) {
                    XRayState.entityGlowActive = !XRayState.entityGlowActive;
                    if (!XRayState.entityGlowActive) {
                        for (net.minecraft.entity.Entity e : client.world.getEntities()) {
                            e.setGlowing(false);
                        }
                    }
                }
            } else {
                boolean nowHeld = entityGlowKey.isPressed();
                if (nowHeld != entityGlowHeld) {
                    entityGlowHeld = nowHeld;
                    XRayState.entityGlowActive = nowHeld;
                    if (!nowHeld) {
                        for (net.minecraft.entity.Entity e : client.world.getEntities()) {
                            e.setGlowing(false);
                        }
                    }
                }
            }

            // --- Fullbright ---
            if (XRayState.fullbrightActive != XRayConfig.instance.fullbright) {
                XRayState.fullbrightActive = XRayConfig.instance.fullbright;
                if (XRayState.fullbrightActive) {
                    client.player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, false));
                } else {
                    client.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
                }
            }

            // --- Config Screen ---
            if (configKey.wasPressed()) {
                client.setScreen(new XRayConfigScreen(client.currentScreen));
            }

            // --- Utility Toggles ---
            if (flyKey.wasPressed()) {
                XRayState.flyEnabled = !XRayState.flyEnabled;
                XRayConfig.instance.utility.flyEnabled = XRayState.flyEnabled;
                XRayConfig.save();
                // Apply or remove flying ability
                client.player.getAbilities().allowFlying = XRayState.flyEnabled;
                if (!XRayState.flyEnabled) {
                    client.player.getAbilities().flying = false;
                }
                client.player.sendAbilitiesUpdate();
                sendHudMessage(client, "Fly: " + (XRayState.flyEnabled ? "ON" : "OFF"));
            }

            if (fastBreakKey.wasPressed()) {
                XRayState.fastBreakEnabled = !XRayState.fastBreakEnabled;
                XRayConfig.instance.utility.fastBreakEnabled = XRayState.fastBreakEnabled;
                XRayConfig.save();
                sendHudMessage(client, "Fast Break: " + (XRayState.fastBreakEnabled ? "ON" : "OFF"));
            }

            if (reachKey.wasPressed()) {
                XRayState.reachEnabled = !XRayState.reachEnabled;
                XRayConfig.instance.utility.reachEnabled = XRayState.reachEnabled;
                XRayConfig.save();
                sendHudMessage(client, "Reach: " + (XRayState.reachEnabled ? "ON" : "OFF"));
            }

            if (noFallKey.wasPressed()) {
                XRayState.noFallEnabled = !XRayState.noFallEnabled;
                XRayConfig.instance.utility.noFallEnabled = XRayState.noFallEnabled;
                XRayConfig.save();
                sendHudMessage(client, "No Fall: " + (XRayState.noFallEnabled ? "ON" : "OFF"));
            }

            if (maceWindBurstKey.wasPressed()) {
                XRayState.maceWindBurstEnabled = !XRayState.maceWindBurstEnabled;
                XRayConfig.instance.utility.maceWindBurstEnabled = XRayState.maceWindBurstEnabled;
                XRayConfig.save();
                sendHudMessage(client, "Mace Wind Burst: " + (XRayState.maceWindBurstEnabled ? "ON" : "OFF"));
            }
        });
    }

    private void sendHudMessage(MinecraftClient client, String msg) {
        if (client.player != null) {
            client.player.sendMessage(Text.literal("[KPS+] " + msg), true);
        }
    }
}
