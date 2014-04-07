package poke.server.dns;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

public class DNSHandler extends SimpleChannelUpstreamHandler {

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Object o=e.getMessage();
		if(o instanceof Event){
			if (((Event) o).op.equals(Event.Operation.ADD)) {
				DataCache.cache.put(((Event) o).key, ((Event) o).value);
			} else if (((Event) o).op.equals(Event.Operation.DELETE)) {
				DataCache.cache.remove(((Event) o).key);
			}
			 else if (((Event) o).op.equals(Event.Operation.GET)) {
				((Event) o).map.putAll(DataCache.cache);
					
				}
		}
		System.out.println("-------------------------------");
		System.out.println(DataCache.cache);
		System.out.println("-------------------------------");
		ctx.getChannel().write(o);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
	}
}