package poke.server.dns;


public class Client {
	public static void main(String[] args) throws InterruptedException {
		ReplicatingMap map = new ReplicatingMap("localhost", 1111);

		map.put("1", 1);
		map.put("2", 2);
		map.put("3", 3);
		map.put("4", 4);
		map.remove("4");
	}

}