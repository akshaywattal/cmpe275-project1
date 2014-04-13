/*
 * copyright 2012, gash
 * 
 * Gash licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package poke.demo;

import java.util.Collection;

import poke.client.ClientCommand;
import poke.client.ClientPrintListener;
import poke.client.comm.CommListener;
import poke.server.dns.DataCache;
import poke.server.dns.ReplicatingMap;

/**
 * DEMO: how to use the command class
 * 
 * @author gash
 * 
 */
public class Jab {
	private String tag;
	private int count;
	private static String[] socketConn;
	private static String host;
	private static int port;
	private static Collection<Object> test;

	public Jab(String tag) {
		this.tag = tag;
	}

	public void run() throws InterruptedException {
		
		/*ReplicatingMap map = new ReplicatingMap("localhost", 1111);
		map.values();
		Thread.sleep(100);
		test = DataCache.cache.values();
		System.out.println("In Jab: " + test);
		socketConn = test.toString().split(":");
		host = socketConn[0].substring(1);
		port = Integer.parseInt(socketConn[1].substring(0, socketConn[1].length() - 1));*/
		ClientCommand cc = new ClientCommand( "192.168.0.125" , 5570 );
		CommListener listener = new ClientPrintListener("jab demo");
		cc.addListener(listener);

		//for (int i = 0; i < 3; i++) {
			//count++;
			cc.poke(tag, count);
		//}
	}

	public static void main(String[] args) {
		try {
			Jab jab = new Jab("jab");
			jab.run();

			// we are running asynchronously
			System.out.println("\nExiting in 5 seconds");
			Thread.sleep(5000);
			//System.exit(0);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
