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

    @Inject(method = "extract(Lnet/minecraft/block/entity/Hopper;Lnet/minecraft/inventory/Inventory;ILnet/minecraft/util/math/Direction;)Z", at = @At("HEAD"), cancellable = true)
    private static void onExtract(Hopper hopper, Inventory inventory, int slot, Direction side, CallbackInfoReturnable<Boolean> cir) {
        // Get block on top of the hopper
        World world = hopper.getWorld();
        BlockPos blockPos = new BlockPos(hopper.getHopperX(), hopper.getHopperY() + 1.0D, hopper.getHopperZ());
        BlockState blockState = world.getBlockState(blockPos);
        Block block = blockState.getBlock();

        // check if it's a lockable container and get its block entity
        if (block.hasBlockEntity()) {
            BlockEntity blockEntity = world.getBlockEntity(blockPos);
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
    private void onInsert(CallbackInfoReturnable<Boolean> cir) {
        HopperBlockEntity hopper = (HopperBlockEntity)((Object)this);
        // Get output block
        World world = hopper.getWorld();
        Direction direction = (Direction)hopper.getCachedState().get(HopperBlock.FACING);
        BlockPos blockPos = hopper.getPos().offset(direction);
        BlockState blockState = world.getBlockState(blockPos);
        Block block = blockState.getBlock();

        // check if it's a lockable container and get its block entity
        if (block.hasBlockEntity()) {
            BlockEntity blockEntity = world.getBlockEntity(blockPos);
            if (blockEntity instanceof LockableContainerBlockEntity) {
                if (!emptyLock((LockableContainerBlockEntity)hopper) && !sameLock((LockableContainerBlockEntity)hopper, (LockableContainerBlockEntity)blockEntity)) {
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
