package com.xraymod.client;

import com.xraymod.client.config.XRayConfig;
import com.xraymod.client.gui.XRayConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.KeyBinding.Category;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class XRayClient implements ClientModInitializer {

    public static KeyBinding xrayKey;
    public static KeyBinding configKey;
    public static KeyBinding entityGlowKey;
    private static boolean wasXrayKeyPressed = false;
    private static boolean wasEntityGlowKeyPressed = false;

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

            // XRay — hold or toggle mode
            boolean xrayKeyDown = xrayKey.isPressed();
            if (XRayState.config.isXrayToggleMode()) {
                if (xrayKeyDown && !wasXrayKeyPressed) {
                    XRayState.active = !XRayState.active;
                    if (client.worldRenderer != null) client.worldRenderer.reload();
                }
            } else {
                if (xrayKeyDown != XRayState.active) {
                    XRayState.active = xrayKeyDown;
                    if (client.worldRenderer != null) client.worldRenderer.reload();
                }
            }
            wasXrayKeyPressed = xrayKeyDown;

            // Entity glow — hold or toggle mode
            boolean entityKeyDown = entityGlowKey.isPressed();
            if (XRayState.config.isEntityGlowToggleMode()) {
                if (entityKeyDown && !wasEntityGlowKeyPressed) {
                    XRayState.entityGlowActive = !XRayState.entityGlowActive;
                    if (!XRayState.entityGlowActive && client.world != null) {
                        for (net.minecraft.entity.Entity entity : client.world.getEntities()) {
                            entity.setGlowing(false);
                        }
                    }
                }
            } else {
                if (entityKeyDown != XRayState.entityGlowActive) {
                    XRayState.entityGlowActive = entityKeyDown;
                    if (!XRayState.entityGlowActive && client.world != null) {
                        for (net.minecraft.entity.Entity entity : client.world.getEntities()) {
                            entity.setGlowing(false);
                        }
                    }
                }
            }
            wasEntityGlowKeyPressed = entityKeyDown;

            // Fullbright
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
                String xrayMode = XRayState.config.isXrayToggleMode() ? "Toggle" : "Hold";
                String glowMode = XRayState.config.isEntityGlowToggleMode() ? "Toggle" : "Hold";
                if (XRayState.active && XRayState.entityGlowActive) {
                    client.player.sendMessage(Text.literal(
                        "§bKPS+ §aXRay + Glow ACTIVE §7| Range: §f"
                        + XRayState.config.getChunkRange() + "c"), true);
                } else if (XRayState.active) {
                    client.player.sendMessage(Text.literal(
                        "§bKPS+ §aXRay ACTIVE §7[" + xrayMode + "] Range: §f"
                        + XRayState.config.getChunkRange() + "c"), true);
                } else if (XRayState.entityGlowActive) {
                    client.player.sendMessage(Text.literal(
                        "§bKPS+ §aGlow ACTIVE §7[" + glowMode + "] Range: §f"
                        + XRayState.config.getEntityGlowRange() + "c"), true);
                }
            }
        });
    }
}
