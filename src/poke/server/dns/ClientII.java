package poke.server.dns;


public class ClientII {
	public static void main(String[] args) throws InterruptedException {
		ReplicatingMap map = new ReplicatingMap("localhost", 1111);
		map.values();
		map.get("1");
		map.get("2");
		map.get("3");

		}

}