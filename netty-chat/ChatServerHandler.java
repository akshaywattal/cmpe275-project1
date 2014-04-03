package org.example.netty.chat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;

// ChannelInboundMessageHandlerAdapter implements general handler interface
// "<String>" is used as we need process incoming string messages only
public class ChatServerHandler extends ChannelInboundMessageHandlerAdapter<String> {

	// keeping track of all channels in ChannelGroup
	private static final ChannelGroup channels = new DefaultChannelGroup();
	
	// keeping track of all active channels by overriding handlerAdded method.
	// this method will be called when a new chat client connects to the chat server.
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

		Channel incoming = ctx.channel();
		// notifying other chat clients that a new chat client has joined
		for(Channel channel : channels){
			
			channel.write("[Server] - " + incoming.remoteAddress() + " has joined!\n");
			
		}
		// adding the new chat client to the ChannelGroup
		channels.add(ctx.channel());
		
	}
	
	// managing chat client disconnecting from a chat server 
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		
		Channel incoming = ctx.channel();
		
		// notifying all clients that a chat client has disconnected
		for(Channel channel : channels){
			
			channel.write("[Server] - " + incoming.remoteAddress() + " has left!\n");
			
		}
		
		// removing that client from ChannelGroup
		channels.remove(ctx.channel());
		
	}
	
	public void messageReceived(ChannelHandlerContext arg0, String message) throws Exception {
		
		// identifying who has sent us a message through channel object
		Channel incoming = arg0.channel();
		
		// sending message to every client by iterating over all known channels and writing
		// message to each channel except from the one from which we received the message
		for (Channel channel : channels){
			
			if(channel != incoming){
				
				channel.write("[" + incoming.remoteAddress() + "] " + message + "\n");
				
			}
			
		}

	}

}
