package poke.server.dns;


public class Client {
	public static void main(String[] args) throws InterruptedException {
		ReplicatingMap map = new ReplicatingMap("192.168.0.123", 1111);

		//map.put("1", 1);
		
		
		map.put("2", "192.168.0.128:5570");
		//map.put("3", 3);
		//map.put("4", 4);
		//map.remove("4");
	}

}