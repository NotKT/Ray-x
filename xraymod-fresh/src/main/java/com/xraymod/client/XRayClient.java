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

    private static KeyBinding xrayKey;
    private static boolean xrayHeld = false;

    private static KeyBinding entityGlowKey;
    private static boolean entityGlowHeld = false;

    private static KeyBinding configKey;
    private static KeyBinding flyKey;

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

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

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

            // --- Fly toggle (V key) ---
            if (flyKey.wasPressed()) {
                XRayState.flyEnabled = !XRayState.flyEnabled;
                XRayConfig.instance.utility.flyEnabled = XRayState.flyEnabled;
                XRayConfig.save();
                applyFly(client);
                sendHudMessage(client, "Fly: " + (XRayState.flyEnabled ? "ON" : "OFF"));
            }

            // --- Apply fly every tick ---
            if (XRayState.flyEnabled) {
                applyFly(client);
            }
        });
    }

    private void applyFly(MinecraftClient client) {
        if (client.player == null) return;
        String module = XRayConfig.instance.utility.flyModule;
        float speed = XRayConfig.instance.utility.flySpeed;

        if (!XRayState.flyEnabled) {
            client.player.getAbilities().allowFlying = false;
            client.player.getAbilities().flying = false;
            client.player.getAbilities().setFlySpeed(0.05f);
            client.player.sendAbilitiesUpdate();
            return;
        }

        switch (module) {
            case "Elytra":
                client.player.getAbilities().allowFlying = true;
                client.player.getAbilities().setFlySpeed(speed * 0.5f);
                client.player.sendAbilitiesUpdate();
                break;
            case "Jetpack":
                client.player.getAbilities().allowFlying = true;
                client.player.getAbilities().setFlySpeed(speed * 1.5f);
                client.player.sendAbilitiesUpdate();
                break;
            case "Glide":
                client.player.getAbilities().allowFlying = true;
                client.player.getAbilities().setFlySpeed(speed * 0.3f);
                client.player.sendAbilitiesUpdate();
                break;
            default: // Vanilla
                client.player.getAbilities().allowFlying = true;
                client.player.getAbilities().setFlySpeed(speed);
                client.player.sendAbilitiesUpdate();
                break;
        }
    }

    private void sendHudMessage(MinecraftClient client, String msg) {
        if (client.player != null) {
            client.player.sendMessage(Text.literal("[KPS+] " + msg), true);
        }
    }
}
