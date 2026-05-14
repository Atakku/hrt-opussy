// SPDX-FileCopyrightText: 2026 JackMacWindows
//
// SPDX-License-Identifier: MPL-2.0

package zone.hrt.opussy.mixin;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaTable;
import dan200.computercraft.api.lua.LuaValues;
import dan200.computercraft.shared.peripheral.speaker.EncodedAudio;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zone.hrt.opussy.OPUSsy;

import java.nio.ByteBuffer;
import java.util.Optional;

@Mixin(targets = "dan200.computercraft.shared.peripheral.speaker.DfpwmState")
abstract class MixinDfpwmState {
  @Accessor("pendingAudio")
  abstract EncodedAudio opussy$get_pendingAudio();

  @Accessor("pendingAudio")
  abstract void opussy$set_pendingAudio(EncodedAudio pendingAudio);

  @Accessor("pendingVolume")
  abstract float opussy$get_pendingVolume();

  @Accessor("pendingVolume")
  abstract void opussy$set_pendingVolume(float pendingVolume);

  @Inject(method = "pushBuffer", at = @At("HEAD"), cancellable = true)
  void pushBuffer(LuaTable<?, ?> table, int size, Optional<Double> volume, CallbackInfoReturnable<Boolean> cir)
      throws LuaException {
    if (opussy$get_pendingAudio() != null) {
      cir.setReturnValue(false);
      cir.cancel();
      return;
    }

    short[] samples = new short[size];
    for (int i = 0; i < size; i++) {
      Object value = table.get(i + 1);
      if (!(value instanceof Number number))
        throw LuaValues.badTableItem(i + 1, "number", LuaValues.getType(value));
      var level = number.doubleValue();
      if (level < -128.0 || level >= 128.0) {
        throw new LuaException("table item #" + i + " must be between -128 and 127");
      }
      samples[i] = (short) (level * 256.0);
    }
    byte[] buffer = new byte[size/8];
    OPUSsy.encode(buffer, samples);
    opussy$set_pendingAudio(new EncodedAudio(samples.length,
        0, false, ByteBuffer.wrap(buffer)));
    opussy$set_pendingVolume(
        (float) AccessorSpeakerPeripheral.callClampVolume(volume.orElse((double) opussy$get_pendingVolume())));
    cir.setReturnValue(true);
    cir.cancel();
  }
}
