package com.xraymod.client.mixin;

import com.xraymod.client.XRayState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class NoFallMixin {

    @Inject(method = "handleFallDamage", at = @At("HEAD"), cancellable = true)
    private void cancelFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if (XRayState.noFallEnabled) {
            cir.setReturnValue(false);
        }
    }
}
