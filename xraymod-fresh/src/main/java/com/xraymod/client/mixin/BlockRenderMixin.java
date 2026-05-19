package com.xraymod.client.mixin;

import com.xraymod.client.XRayState;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkRendererRegion.class)
public abstract class BlockRenderMixin {

    @Inject(
        method = "getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;",
        at = @At("RETURN"),
        cancellable = true
    )
    private void xray_filterBlockState(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if (!XRayState.active) return;
        BlockState state = cir.getReturnValue();
        if (state == null || state.isAir()) return;
        String blockId = Registries.BLOCK.getId(state.getBlock()).toString();
        if (XRayState.config != null && XRayState.config.isVisible(blockId)) return;
        cir.setReturnValue(Blocks.AIR.getDefaultState());
    }
}
