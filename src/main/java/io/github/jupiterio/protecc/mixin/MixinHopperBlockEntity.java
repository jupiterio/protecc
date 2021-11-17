package io.github.jupiterio.protecc.mixin;

import net.minecraft.block.entity.HopperBlockEntity;

import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.BlockState;
import net.minecraft.world.World;
import net.minecraft.inventory.ContainerLock;

import io.github.jupiterio.protecc.impl.LockGetter;
import io.github.jupiterio.protecc.impl.KeyGetter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = HopperBlockEntity.class)
public class MixinHopperBlockEntity {

    @Inject(method = "extract(Lnet/minecraft/world/World;Lnet/minecraft/block/entity/Hopper;)Z", at = @At("HEAD"), cancellable = true)
    private static void onExtract(World world, Hopper hopper, CallbackInfoReturnable<Boolean> cir) {
        // Get block on top of the hopper
        var pos = new BlockPos(hopper.getHopperX(), hopper.getHopperY() + 1.0D, hopper.getHopperZ());
        var state = world.getBlockState(pos);

        // check if it's a lockable container and get its block entity
        if (state.hasBlockEntity()) {
            var blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof LockableContainerBlockEntity) {

                // lockable container, check if it's locked. If it's not, let it continue. If it is, check all locks
                if (!emptyLock((LockableContainerBlockEntity)blockEntity)) {
                    // if a hopper block, check if the locks are the same. if a hopper minecart, just disallow extraction
                    if (hopper instanceof LockableContainerBlockEntity) {
                        if (!sameLock((LockableContainerBlockEntity)hopper, (LockableContainerBlockEntity)blockEntity)) {
                            cir.setReturnValue(false);
                        }
                    } else {
                        cir.setReturnValue(false);
                    }
                }
            }
        }
    }

    @Inject(method = "insert", at = @At("HEAD"), cancellable = true)
    private static void onInsert(World world, BlockPos pos, BlockState state, Inventory inventory, CallbackInfoReturnable<Boolean> cir) {
        var hopper = (HopperBlockEntity)world.getBlockEntity(pos);
        // Get output block
        var direction = (Direction)state.get(HopperBlock.FACING);
        var outputPos = hopper.getPos().offset(direction);
        var outputState = world.getBlockState(outputPos);

        // check if it's a lockable container and get its block entity
        if (outputState.hasBlockEntity()) {
            var outputBEntity = world.getBlockEntity(outputPos);
            if (outputBEntity instanceof LockableContainerBlockEntity) {
                if (!emptyLock(hopper) && !sameLock(hopper, (LockableContainerBlockEntity) outputBEntity)) {
                    cir.setReturnValue(false);
                }
            }
        }
    }

    private static boolean sameLock(LockableContainerBlockEntity be1, LockableContainerBlockEntity be2) {
        ContainerLock be1Lock = ((LockGetter) be1).protecc_getLock();
        ContainerLock be2Lock = ((LockGetter) be2).protecc_getLock();

        String be1Key = ((KeyGetter) be1Lock).protecc_getKey();
        String be2Key = ((KeyGetter) be2Lock).protecc_getKey();

        return be1Key.equals(be2Key);
    }

    private static boolean emptyLock(LockableContainerBlockEntity be) {
        ContainerLock beLock = ((LockGetter) be).protecc_getLock();
        String beKey = ((KeyGetter) beLock).protecc_getKey();

        return "".equals(beKey);
    }

}
