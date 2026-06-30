package com.xraymod.client.mixin;

import com.xraymod.client.XRayState;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class FlyMixin {

    @Inject(method = "isFlying", at = @At("HEAD"), cancellable = true)
    private void overrideFly(CallbackInfoReturnable<Boolean> cir) {
        if (XRayState.flyEnabled) {
            cir.setReturnValue(true);
        }
    }
}
