package org.example.netty.chat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

// this class will initialize socket channel
public class ChatClientInitializer extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel arg0) throws Exception {

		// for organizing our communication
		ChannelPipeline pipeline = arg0.pipeline();
		
		// expecting frames of specified size(bytes) delimited by line endings
		pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
		
		// since we are exchanging only strings among clients
		// and server we can use string decoder
		pipeline.addLast("decoder", new StringDecoder());
		
		// string encoder to encode string into bytes to send to server
		pipeline.addLast("encoder", new StringEncoder());
		
		// handling all the decoders to incoming strings from the server
		pipeline.addLast("handler", new ChatClientHandler());

	}

}
