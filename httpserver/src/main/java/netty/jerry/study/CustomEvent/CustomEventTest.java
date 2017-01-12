package netty.jerry.study.CustomEvent;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;

enum CustomEvent{
	MyCustomEvent; 
}

class CustomEventHandler extends ChannelInboundHandlerAdapter {
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
		if (evt == CustomEvent.MyCustomEvent) { // do something
			
		}
	}
}

public class CustomEventTest {
	public static void main(String[] args) {
		Channel channel = null; 
		ChannelPipeline pipeline = channel.pipeline();
		pipeline.fireUserEventTriggered(CustomEvent.MyCustomEvent);
	}
}
