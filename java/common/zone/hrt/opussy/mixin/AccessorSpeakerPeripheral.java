// SPDX-FileCopyrightText: 2026 JackMacWindows
//
// SPDX-License-Identifier: MPL-2.0

package zone.hrt.opussy.mixin;

import dan200.computercraft.shared.peripheral.speaker.SpeakerPeripheral;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SpeakerPeripheral.class)
public interface AccessorSpeakerPeripheral {
    @Invoker
    static double callClampVolume(double volume) {throw new UnsupportedOperationException();}
}
