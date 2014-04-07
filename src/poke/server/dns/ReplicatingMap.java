package poke.server.dns;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

public class ReplicatingMap extends HashMap<String, Object> {

	private static final long serialVersionUID = 1L;
	private ClientBootstrap bootstrap;
	private ChannelFuture future;

	public ReplicatingMap(String hostname, int port)
			throws InterruptedException {
		bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(new ObjectEncoder(),
						new ObjectDecoder(), new ClientHandler());
			}
		});
		future = bootstrap.connect(new InetSocketAddress(hostname, port));
		future.await();
	}

	@Override
	public Object put(String key, Object value) {
		Event e = new Event();
		e.op = Event.Operation.ADD;
		e.key = key;
		e.value = value;
		future.getChannel().write(e);
		return super.put(key, value);
	}

	@Override
	public Object remove(Object key) {
		Event e = new Event();
		e.op = Event.Operation.DELETE;
		e.key = (String) key;
		future.getChannel().write(e);
		return super.remove(key);
	}

	@Override
	public Collection<Object> values() {
		Event e = new Event();
		e.op = Event.Operation.GET;
		future.getChannel().write(e);
		return super.values();
	}
	
}