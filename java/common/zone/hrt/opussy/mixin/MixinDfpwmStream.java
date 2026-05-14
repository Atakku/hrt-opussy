// SPDX-FileCopyrightText: 2026 JackMacWindows
//
// SPDX-License-Identifier: MPL-2.0

package zone.hrt.opussy.mixin;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Queue;

import dan200.computercraft.shared.peripheral.speaker.EncodedAudio;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zone.hrt.opussy.OPUSsy;

@Mixin(targets = "dan200.computercraft.client.sound.DfpwmStream")
public abstract class MixinDfpwmStream {
  @Accessor("buffers")
  abstract Queue<ByteBuffer> opussy$getBuffers();

  @Inject(method = "push", at = @At("HEAD"), cancellable = true)
  void push(EncodedAudio audio, CallbackInfo ci) {
    var input = audio.audio();
    if (!input.hasRemaining()) {
      OPUSsy.LOGGER.warn("Received empty audio. Skipping audio chunk.");
      ci.cancel();
      return;
    }
    byte[] bytes = new byte[input.remaining()];
    short[] samples = new short[audio.charge()];
    input.get(bytes);
    OPUSsy.decode(samples, bytes);
    try {
      ByteBuffer samples8 = ByteBuffer.allocate(samples.length).order(ByteOrder.nativeOrder());
      for (short sample : samples)
        samples8.put((byte) ((sample >> 8) ^ 0x80));
      samples8.flip();
      synchronized (this) {
        opussy$getBuffers().add(samples8);
      }
    } catch (RuntimeException e) {
      OPUSsy.LOGGER.error("Threw error while decoding audio. Skipping audio chunk.", e);
    } finally {
      ci.cancel();
    }
  }
}
