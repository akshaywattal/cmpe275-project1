/*
 * copyright 2014, gash
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
package poke.server.management.managers;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.monitor.HeartMonitor;
import poke.monitor.HeartMonitor.MonitorClosedListener;
import poke.server.Server;
import poke.server.conf.NodeDesc;
import poke.server.conf.ServerConf;
import poke.server.management.ManagementQueue;
import eye.Comm.LeaderElection;
import eye.Comm.LeaderElection.Builder;
import eye.Comm.LeaderElection.VoteAction;
import eye.Comm.Management;
import eye.Comm.Network;
import eye.Comm.Network.NetworkAction;

/**
 * The network manager contains the node's view of the network.
 * 
 * @author gash
 * 
 */
public class NetworkManager {
	protected static Logger logger = LoggerFactory.getLogger("management");
	protected static AtomicReference<NetworkManager> instance = new AtomicReference<NetworkManager>();

	private String nodeId;
	private String leaderId;
	ServerConf conf; 
	List<Integer> nodeList = new ArrayList<Integer>();
	int announcements = 0;
	//protected static ChannelFuture channelf; // do not use directly, call connect()!
	private EventLoopGroup group = new NioEventLoopGroup();

	/** @brief the number of votes this server can cast */
	private int votes = 1;

	public static NetworkManager getInstance(String id, ServerConf conf) {
		instance.compareAndSet(null, new NetworkManager(id, conf));
		return instance.get();
	}

	public static NetworkManager getInstance() {
		return instance.get();
	}

	/**
	 * initialize the manager for this server
	 * 
	 * @param nodeId
	 *            The server's (this) ID
	 */
	protected NetworkManager(String nodeId, ServerConf conf) {
		this.nodeId = nodeId;
		this.conf = conf;
		this.leaderId = conf.getServer().getProperty("leader.id");
	}

	/**
	 * @param args
	 * @throws InterruptedException 
	 * @throws NumberFormatException 
	 */
	public void processRequest(Network req, Channel channel, SocketAddress sa) throws NumberFormatException, InterruptedException {
		if (req == null || channel == null || sa == null)
			return;

		logger.info("Network: node '" + req.getNodeId() + "' sent a " + req.getAction());

		/**
		 * Outgoing: when a node joins to another node, the connection is
		 * monitored to relay to the requester that the node (this) is active -
		 * send a heartbeatMgr
		 */
		if (req.getAction().getNumber() == NetworkAction.NODEJOIN_VALUE) {
			if (channel.isOpen()) {
				// can i cast socka?
				SocketAddress socka = channel.localAddress();
				if (socka != null) {
					InetSocketAddress isa = (InetSocketAddress) socka;
						
					logger.info("NODEJOIN: " + isa.getHostName() + ", " + isa.getPort());
					HeartbeatManager.getInstance().addOutgoingChannel(req.getNodeId(), isa.getHostName(),
							isa.getPort(), channel, sa);
					
					//Management leaderAnnounce = sendLeader();		
					//logger.info(channel.localAddress().toString());
					
					//ManagementQueue.enqueueResponse(leaderAnnounce, channel, sa);
					
					// Make the connection attempt.
					//ChannelFuture channelf = ManagementQueue.connect(isa);
					//channelf.awaitUninterruptibly(50001);
					//channelf.channel().writeAndFlush(leaderAnnounce);
				}
			} else
				logger.warn(req.getNodeId() + " not writable");
		} else if (req.getAction().getNumber() == NetworkAction.NODEDEAD_VALUE) {
			// possible failure - node is considered dead
			
		} else if (req.getAction().getNumber() == NetworkAction.NODELEAVE_VALUE) {
			// node removing itself from the network (gracefully)
		} else if (req.getAction().getNumber() == NetworkAction.ANNOUNCE_VALUE) {
			// nodes sending their info in response to a create map
			//logger.info("Needs election");
			
			announcements++;
			int node = Integer.parseInt(req.getNodeId());
			System.out.print(node);
			//System.out.print(conf.getServer().getProperty("leader.id"));
			if (node != Integer.parseInt(conf.getServer().getProperty("leader.id")))
				nodeList.add(node);
			if(nodeList.size()>0 && announcements==2) 
			{
				boolean checkValueIsSame;
				if(nodeList.get(0)==nodeList.get(1)) checkValueIsSame = true;
				else checkValueIsSame = false;
				if(!checkValueIsSame) 
					{
					
					LeaderElection.Builder le = LeaderElection.newBuilder();
					le.setVote(VoteAction.ELECTION);
					LeaderElection lereq = le.build();
					Management.Builder m = Management.newBuilder();
					m.setElection(lereq);
					
					announcements = 0; 
					logger.info("Needs election");
					//ManagementQueue.enqueueRequest(m.build(), channel, channel.remoteAddress());
							
					//ElectionManager.getInstance().processRequest(lereq);
					}
					
			}
						
		} else if (req.getAction().getNumber() == NetworkAction.CREATEMAP_VALUE) {
			// request to create a network topology map
		}
	

		// may want to reply to exchange information
	}
	

	public Management sendLeader()
	{
	//Network.Builder n = Network.newBuilder();
	//n.setAction(NetworkAction.ANNOUNCE);
	LeaderElection.Builder le = LeaderElection.newBuilder();
	le.setNodeId(nodeId);
	le.setBallotId(leaderId);
	//System.out.println(leaderId);
	le.setVote(VoteAction.DECLAREWINNER);
	le.setDesc(leaderId);
	//n.setNodeId(conf.getServer().getProperty("leader.id"));
	Management.Builder msg = Management.newBuilder();
	//msg.setGraph(n.build());
	msg.setElection(le.build());
	return msg.build();
	}
	
	
}
