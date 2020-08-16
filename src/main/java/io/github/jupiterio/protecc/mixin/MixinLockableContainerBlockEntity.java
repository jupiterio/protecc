package io.github.jupiterio.protecc.mixin;

import net.minecraft.block.entity.LockableContainerBlockEntity;

import net.minecraft.inventory.ContainerLock;

import io.github.jupiterio.protecc.impl.LockGetter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LockableContainerBlockEntity.class)
public class MixinLockableContainerBlockEntity implements LockGetter {
    
    @Shadow
    private ContainerLock lock;
    
    @Override
    public ContainerLock protecc_getLock() {
		return this.lock;
	}
    
}
