package poke.server.dns;

import java.io.Serializable;
import java.util.HashMap;

public class Event implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum Operation {
		ADD, DELETE, GET
	}

	public Operation op;
	public String key;
	public Object value;
	public HashMap<String, Object> map = new HashMap<String, Object>();

}