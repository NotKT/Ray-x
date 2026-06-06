package com.xraymod.client.mixin;

import com.xraymod.client.XRayState;
import com.xraymod.client.config.XRayConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class ReachMixin {

    @Inject(method = "getBlockInteractionRange", at = @At("HEAD"), cancellable = true)
    private void overrideBlockReach(CallbackInfoReturnable<Double> cir) {
        if (XRayState.reachEnabled) {
            cir.setReturnValue(XRayConfig.instance.utility.reachDistance);
        }
    }

    @Inject(method = "getEntityInteractionRange", at = @At("HEAD"), cancellable = true)
    private void overrideEntityReach(CallbackInfoReturnable<Double> cir) {
        if (XRayState.reachEnabled) {
            cir.setReturnValue(XRayConfig.instance.utility.reachDistance);
        }
    }
}
