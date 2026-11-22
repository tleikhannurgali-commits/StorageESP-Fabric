package com.example.storageesp.mixin;

import com.example.storageesp.TracesTracker;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Capture sound positions after the client receives and resolves the sound. */
@Mixin(SoundManager.class)
public abstract class SoundManagerMixin {
    @Inject(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V", at = @At("HEAD"), require = 0)
    private void storageesp$onPlay(SoundInstance sound, CallbackInfo ci) {
        TracesTracker.onSound(sound);
    }
}
