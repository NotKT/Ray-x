package com.xraymod.client.mixin;

import com.xraymod.client.XRayState;
import com.xraymod.client.config.XRayConfig;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class MaceWindBurstMixin {

    @Inject(method = "attack", at = @At("HEAD"))
    private void onAttack(Entity target, CallbackInfo ci) {
        ClientPlayerEntity self = (ClientPlayerEntity)(Object)this;

        if (!XRayState.maceWindBurstEnabled) return;

        boolean holdingMace = self.getMainHandStack().isOf(Items.MACE)
                           || self.getOffHandStack().isOf(Items.MACE);

        if (holdingMace) {
            self.fallDistance = XRayConfig.instance.utility.maceWindBurstFallDistance;
        }
    }
}
