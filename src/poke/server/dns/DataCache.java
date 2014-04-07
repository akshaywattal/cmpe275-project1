package poke.server.dns;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataCache {
	public static Map<String, Object> cache = new ConcurrentHashMap<String, Object>();
}