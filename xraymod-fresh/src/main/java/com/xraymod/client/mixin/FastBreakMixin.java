package com.xraymod.client.mixin;

import com.xraymod.client.XRayState;
import com.xraymod.client.config.XRayConfig;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerEntity.class)
public class FastBreakMixin {

    @ModifyVariable(
        method = "getBlockBreakingSpeed",
        at = @At("RETURN"),
        ordinal = 0
    )
    private float multiplyBreakSpeed(float original) {
        if (XRayState.fastBreakEnabled) {
            return original * XRayConfig.instance.utility.fastBreakMultiplier;
        }
        return original;
    }
}
