package com.xraymod.client.mixin;

import com.xraymod.client.XRayState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityGlowMixin {

    @Inject(method = "isGlowing", at = @At("RETURN"), cancellable = true)
    private void xray_forceGlow(CallbackInfoReturnable<Boolean> cir) {
        if (!XRayState.entityGlowActive) return;

        Entity self = (Entity) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) return;
        if (self == client.player) return;

        // Use separate entity glow range
        int rangeBlocks = XRayState.config != null
            ? XRayState.config.getEntityGlowRange() * 16
            : 96;

        double dist = client.player.squaredDistanceTo(self);
        if (dist > rangeBlocks * rangeBlocks) return;

        String entityId = Registries.ENTITY_TYPE.getId(self.getType()).toString();
        if (XRayState.config != null && XRayState.config.isEntityExcluded(entityId)) return;

        cir.setReturnValue(true);
    }
}
