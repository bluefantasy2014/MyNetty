package netty.protocol.time;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
/*
 *知识 ： 1: ByteToMessageDecoder calls the decode() method with an internally maintained cumulative buffer whenever new data is received.
       2: decode() can decide to add nothing to out where there is not enough data in the cumulative buffer. 
       3: ByteToMessageDecoder will call decode() again when there is more data received.
       4: If decode() adds an object to out, it means the decoder decoded a message successfully. ByteToMessageDecoder will discard the read part of the cumulative buffer. 
          Please remember that you don't need to decode multiple messages. ByteToMessageDecoder will keep calling the decode() method until it adds nothing to out.
 * */
public class TimeDecoder extends ByteToMessageDecoder {  
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {  
        if (in.readableBytes() < 4) {
            return;  
        }

        out.add(in.readBytes(4));  
    }
}
