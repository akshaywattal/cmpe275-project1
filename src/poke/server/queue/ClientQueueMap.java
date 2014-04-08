package poke.server.queue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientQueueMap {
	
	public static Map<String, PerChannelQueue> clientMap = new ConcurrentHashMap<String, PerChannelQueue>();

}


