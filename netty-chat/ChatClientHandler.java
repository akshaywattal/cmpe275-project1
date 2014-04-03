package org.example.netty.chat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;

// ChannelInboundMessageHandlerAdapter needs to 
// handle incoming string objects in our case  
public class ChatClientHandler extends ChannelInboundMessageHandlerAdapter<String> {

	public void messageReceived(ChannelHandlerContext arg0, String arg1) throws Exception {
		
		// printing any message received from server
		System.out.println(arg1);

	}

}
