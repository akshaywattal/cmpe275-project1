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
package poke.server.resources;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.beans.Beans;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.server.ServerInitializer;
import poke.server.conf.NodeDesc;
import poke.server.conf.ServerConf;
import poke.server.conf.ServerConf.ResourceConf;
import poke.server.management.ManagementQueue;
import poke.server.management.managers.ElectionManager;
import eye.Comm.Header;
import eye.Comm.Request;

/**
 * Resource factory provides how the server manages resource creation. We hide
 * the creation of resources to be able to change how instances are managed
 * (created) as different strategies will affect memory and thread isolation. A
 * couple of options are:
 * <p>
 * <ol>
 * <li>instance-per-request - best isolation, worst object reuse and control
 * <li>pool w/ dynamic growth - best object reuse, better isolation (drawback,
 * instances can be dirty), poor resource control
 * <li>fixed pool - favor resource control over throughput (in this case failure
 * due to no space must be handled)
 * </ol>
 * 
 * @author gash
 * 
 */
public class ResourceFactory {
	protected static Logger logger = LoggerFactory.getLogger("server");

	private static ServerConf cfg;
	private static AtomicReference<ResourceFactory> factory = new AtomicReference<ResourceFactory>();
	//Added as a part of Forwarding Request
	private ChannelFuture fchannel; // do not use directly call connect()!
	private EventLoopGroup group;
	private Map<String, InetSocketAddress> aliveNodes = new HashMap<String, InetSocketAddress>();
	private SocketAddress remoteSocketAddress;
	private SocketAddress localSocketAddress;

	public static void initialize(ServerConf cfg) {
		try {
			ResourceFactory.cfg = cfg;
			factory.compareAndSet(null, new ResourceFactory());
		} catch (Exception e) {
			logger.error("failed to initialize ResourceFactory", e);
		}
	}

	public static ResourceFactory getInstance() {
		ResourceFactory rf = factory.get();
		if (rf == null)
			throw new RuntimeException("Server not intialized");

		return rf;
	}

	private ResourceFactory() {
	}

	/**
	 * Obtain a resource
	 * 
	 * @param route
	 * @return
	 * @throws InterruptedException 
	 */
	public Resource resourceInstance(Request req, Channel conn) throws InterruptedException {
		// is the message for this server?
		if (req.getHeader().hasToNode()) {
			String iam = cfg.getServer().getProperty("node.id");
			if (iam.equalsIgnoreCase(req.getHeader().getToNode()))
				; // fall through and process normally
			else {
				// forward request
			}
		}
		
		if(ElectionManager.getInstance().isLeader()) {
			group = new NioEventLoopGroup();
			Bootstrap b = new Bootstrap();
			boolean compressComm = false;
			b.group(group).channel(NioSocketChannel.class).handler(new ServerInitializer(compressComm));
			b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
			b.option(ChannelOption.TCP_NODELAY, true);
			b.option(ChannelOption.SO_KEEPALIVE, true);
			
			//Gathering Information of Alive Nodes
			for (NodeDesc nn : cfg.getRoutingList()) {
				try { 
					if(!nn.getNodeId().equals(ElectionManager.getInstance().getLeaderId())) {
					InetSocketAddress isa = new InetSocketAddress( nn.getHost(), nn.getMgmtPort());
					ManagementQueue.nodeMap.put(nn.getNodeId(), isa);
					ChannelFuture cf = ManagementQueue.connect(isa);
					cf.awaitUninterruptibly(50001);
					
					if(cf.isDone()&&cf.isSuccess())
						aliveNodes.put(nn.getNodeId(), isa);
					
					cf.channel().closeFuture();
					}
					} catch(Exception e){logger.info("Connection refused!");}
				}
			
			//Generating Random Node ID from Alive Nodes
			Random rand = new Random();
		    int randomNum = rand.nextInt((aliveNodes.size() - 
		    		Integer.parseInt(ElectionManager.getInstance().getLeaderId()) + 1)) + Integer.parseInt(ElectionManager.getInstance().getLeaderId());
		    
		    //If Random Node ID is Same as Leader, Add One 
		    if(randomNum == Integer.parseInt(ElectionManager.getInstance().getLeaderId()))
		    		randomNum = randomNum + 1;
		    		
		    localSocketAddress = new InetSocketAddress( cfg.getRoutingList().get(randomNum).getHost(), cfg.getRoutingList().get(randomNum).getPort());
			
			// Make the connection attempt.
			fchannel = b.connect(localSocketAddress).syncUninterruptibly();
			Channel ch = fchannel.channel();
			ch.writeAndFlush(req);
			logger.info("I am Leader Node, request forwarded to Node: " + randomNum);
			return null;
			}
		else {
			ResourceConf rc = cfg.findById(req.getHeader().getRoutingId().getNumber());
			if (rc == null)
				return null;
			try {
				// strategy: instance-per-request
				Resource rsc = (Resource) Beans.instantiate(this.getClass().getClassLoader(), rc.getClazz());
				return rsc;
				} catch (Exception e) {
					logger.error("unable to create resource " + rc.getClazz());
					return null;
					}
			}
		}
	}
