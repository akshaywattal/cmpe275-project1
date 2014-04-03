package org.example.netty.chat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.Channel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class ChatClient {
	
	public static void main(String[] args) throws Exception {
		
		new ChatClient("localhost", 8000).run();
		
	}
	
	private final String host;
	private final int port;

	public ChatClient(String host, int port){
		
		this.host = host;
		this.port = port;
		
	}
	
	// defines the client behavior
	public void run() throws Exception{
		
		EventLoopGroup group = new NioEventLoopGroup();
		
		try{
			
			// using Bootstrap class to set up a channel for using EventLoopGroup
			Bootstrap bootstrap = new Bootstrap()
					.group(group)
					.channel(NioSocketChannel.class)  // channel will be using new io sockets for communication
					.handler(new ChatClientInitializer()); // channel will be handled by ChatClientInitializer
			
			// making connection to the server
			Channel channel = bootstrap.connect(host, port).sync().channel();
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			
			// loop to take user input from console and write to the server
			while (true){
				
				channel.write(in.readLine() + "\r\n");
				
			}
			
		} 
		
		// shutting down EventLoopGroup when exiting above while loop for any reason
		finally {
			
			group.shutdownGracefully();
			
		}
	}

}
