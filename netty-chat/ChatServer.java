package org.example.netty.chat;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ChatServer {
	
	public static void main(String[] args) throws Exception {
		
		new ChatServer(8000).run();
		
	}
	
	// port to listen for incoming connections
	private final int port;

	public ChatServer(int port){
		
		this.port = port;
		
	}
	
	// listens to incoming connections and hand them of processing
	public void run() throws Exception {
		
		// this group will accept incoming connections as they arrive
		// and pass them all for processing to worker group
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		try {
			
			// this defines how the serve will process incoming connections
			ServerBootstrap bootstrap = new ServerBootstrap()
					.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)  // channel will be using new io sockets for communication
					.childHandler(new ChatServerInitiallizer());  // implementing a class which will handle any incoming messages
			
			// binding bootstrap object to the specific port and 
			// it starts listening for incoming connections 
			bootstrap.bind(port).sync().channel().closeFuture().sync();
			
			
		}
		
		finally {
			
			// cleaning up used event loop groups
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
			
		}
	}

}
