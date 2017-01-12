package netty.jerry.study.executeOutOfEventLoop;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.SingleThreadEventExecutor;

//Q: 什么时候需要在EventLoop之外执行codec？？ 
public class ExecuteOutOfEventLoop {
	public static void main(String[] args) {
		Channel ch = null;
		ChannelPipeline p = ch.pipeline();
//		EventExecutor e1 = new SingleThreadEventExecutor();
//
//		p.addLast(new MyProtocolCodec()); 
//		p.addLast(e1, new MyDatabaseAccessingHandler());
	}
}
