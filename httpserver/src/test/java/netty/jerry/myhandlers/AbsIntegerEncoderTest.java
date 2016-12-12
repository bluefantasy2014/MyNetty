package netty.jerry.myhandlers;

import org.junit.Assert;
import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;

public class AbsIntegerEncoderTest {
	@Test
	public void testEncoded() {
		ByteBuf buf = Unpooled.buffer(); // #2
		for (int i = 1; i < 10; i++) {
			buf.writeInt(i * -1);
		}
		EmbeddedChannel channel = new EmbeddedChannel(new AbsIntegerEncoder());
		Assert.assertTrue(channel.writeOutbound(buf));
		Assert.assertTrue(channel.finish());
		// read bytes #6
		ByteBuf output = (ByteBuf) channel.readOutbound();
		for (int i = 1; i < 10; i++) {
			Assert.assertEquals(i, output.readInt());
		}
		Assert.assertFalse(output.isReadable());
		Assert.assertNull(channel.readOutbound());
	}
}
