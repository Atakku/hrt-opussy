// Copyright 2026 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package zone.hrt.opussy;

import java.nio.ByteBuffer;

import io.github.jaredmdobson.concentus.OpusApplication;
import io.github.jaredmdobson.concentus.OpusDecoder;
import io.github.jaredmdobson.concentus.OpusEncoder;
import io.github.jaredmdobson.concentus.OpusException;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(OPUSsy.MOD_ID)
public class OPUSsy {
  public static final String MOD_ID = "hrt_opussy";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  public static OpusEncoder ENCODER = null;
  public static OpusDecoder DECODER = null;

  public static final int FRAME_SIZE = 960;

  static {
    try {
      ENCODER = new OpusEncoder(48000, 1, OpusApplication.OPUS_APPLICATION_AUDIO);
    } catch (Exception ignore) {
    }
    try {
      DECODER = new OpusDecoder(48000, 1);
    } catch (Exception ignore) {
    }
  }

  public static void encode(byte[] output, short[] input) {
    ByteBuffer buf = ByteBuffer.wrap(output);
    byte[] frame = new byte[FRAME_SIZE/8 - 2]; // Two bytes for short
    for (int i = 0; i < input.length; i += FRAME_SIZE) {
      if (input.length - i < FRAME_SIZE) {
        return;
      }
      short len = 0;
      try {
        len = (short) ENCODER.encode(input, i, FRAME_SIZE, frame, 0, frame.length);
      } catch (OpusException e) {
        e.printStackTrace();
      }
      buf.putShort(len);
      buf.put(frame, 0, len);
    }
  }

  public static void decode(short[] output, byte[] input) {
    ByteBuffer buf = ByteBuffer.wrap(input);
    byte[] frame = new byte[FRAME_SIZE/8 - 2]; // Two bytes for short
    for (int i = 0; i < output.length; i += FRAME_SIZE) {
      if (output.length - i < FRAME_SIZE) {
        return;
      }
      short len = buf.getShort();
      buf.get(frame, 0, len);
      try {
        DECODER.decode(frame, 0, len, output, i, FRAME_SIZE, false);
      } catch (OpusException e) {
        e.printStackTrace();
      }
    }
  }
}
