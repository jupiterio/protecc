package io.github.jupiterio.protecc.mixin;

import net.minecraft.inventory.ContainerLock;

import io.github.jupiterio.protecc.impl.KeyGetter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ContainerLock.class)
public class MixinContainerLock implements KeyGetter {
    
    @Shadow
    @Final
    private String key;
    
    @Override
    public String protecc_getKey() {
		return this.key;
	}
    
}