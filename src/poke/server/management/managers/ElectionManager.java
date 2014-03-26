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

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.server.conf.NodeDesc;
import poke.server.conf.ServerConf;
import poke.server.management.ManagementQueue;
import eye.Comm.LeaderElection;
import eye.Comm.Management;
import eye.Comm.Network;
import eye.Comm.LeaderElection.VoteAction;

/**
 * The election manager is used to determine leadership within the network.
 * 
 * @author gash
 * 
 */
public class ElectionManager {
	protected static Logger logger = LoggerFactory.getLogger("management");
	protected static AtomicReference<ElectionManager> instance = new AtomicReference<ElectionManager>();
	public AtomicInteger nominations = new AtomicInteger(0);
	
	
	
	private String nodeId, leaderId;
	ServerConf conf; 
	List<String> nodeList;
	int i;

	/** @brief the number of votes this server can cast */
	private int votes = 1;

	public static ElectionManager getInstance(String id, ServerConf conf, int votes) {
		instance.compareAndSet(null, new ElectionManager(id, conf, votes));
		return instance.get();
	}

	public static ElectionManager getInstance() {
		return instance.get();
	}

	/**
	 * initialize the manager for this server
	 * 
	 * @param nodeId
	 *            The server's (this) ID
	 */
	protected ElectionManager(String nodeId, ServerConf conf, int votes) {
		this.nodeId = nodeId;
		
		this.conf = conf;
		
		if (votes >= 0)
			this.votes = votes;
				
		this.leaderId = conf.getServer().getProperty("leader.id");
		
		this.nodeList = new ArrayList<String>();
	}

	/**
	 * @param args
	 */
	public void processRequest(LeaderElection req, Channel channel, SocketAddress sa) throws NumberFormatException, InterruptedException {
		if (req == null)
			return;

		if (req.hasExpires()) {
			long ct = System.currentTimeMillis();
			if (ct > req.getExpires()) {
				// election is over
				return;
			}
		}

		if (req.getVote().getNumber() == VoteAction.ELECTION_VALUE) {
			// an election is declared!
			logger.info("Election Started!");
			int rounds = Integer.parseInt(conf.getServer().getProperty("diameter"));
			for(int i=0; i<rounds;i++)
			{
				LeaderElection.Builder le = LeaderElection.newBuilder();
				le.setNodeId(conf.getServer().getProperty("node.id"));
				le.setBallotId(leaderId);
				//System.out.println(leaderId);
				le.setVote(VoteAction.NOMINATE);
				le.setDesc(conf.getServer().getProperty("leader.id"));
				Management.Builder msg = Management.newBuilder();
				msg.setElection(le.build());
				
				for (NodeDesc nn : conf.getNearest().getNearestNodes().values()) {
					try
					{ InetSocketAddress isa = new InetSocketAddress( nn.getHost(), nn.getMgmtPort());
					ChannelFuture cf = ManagementQueue.connect(isa);
					cf.awaitUninterruptibly(50001);
					if(cf.isDone()&&cf.isSuccess())
					{
					cf.channel().writeAndFlush(msg.build());
					logger.info("Nomination sent by" + nodeId);
					}
					}
															
					catch(Exception e){logger.info("Election Message refused by " + nn.getHost() + ":" +nn.getMgmtPort());}
				}
				
				while(nominations.get() < conf.getNearest().getNearestNodes().size())
				{
					Thread.sleep(5000);
					logger.info(nodeId + " received " + nominations + " nominations");
					logger.info(nodeId + " Waiting for nominations...");
				}
					if(nominations.get() >= conf.getNearest().getNearestNodes().size()) nominations.set(0); 
					
				
			}
			
			
			} else if (req.getVote().getNumber() == VoteAction.DECLAREVOID_VALUE) {
			// no one was elected, I am dropping into standby mode`
		} else if (req.getVote().getNumber() == VoteAction.DECLAREWINNER_VALUE) {
			//logger.info("here");
			// some node declared themself the leader
			String node = req.getBallotId();
			/*System.out.println(node);
			System.out.println(leaderId);*/
			
			//Adding both to List
			nodeList.add(node);
			nodeList.add(leaderId);
			
			System.out.println("Leader ID received from Peer:" +  nodeList.get(0));
			System.out.println("Leader ID in Conf File:" +  nodeList.get(1));
			
			/*if (!node.equals(leaderId))
			nodeList.add(node);*/
			
			//Checking if its end node and matching Leader ID with Received Leader ID
			if(nodeList.size()==2 && nodeList.get(0).equals(nodeList.get(1)) && conf.getNearest().getNearestNodes().size()==1) 
			leaderId = nodeList.get(0);
			
			//Checking if its middle node and matching Leader ID with Received Leader ID
			else if(nodeList.size()==4 && nodeList.get(0).equals(nodeList.get(1)) && nodeList.get(2).equals(nodeList.get(1)) && conf.getNearest().getNearestNodes().size()==2) 
			leaderId = nodeList.get(0);
			
			//Changed = to equals
			else if(nodeList.size()>=2 && !nodeList.get(0).equals(nodeList.get(1))) 
				{	
					
					LeaderElection.Builder le = LeaderElection.newBuilder();
					le.setNodeId(conf.getServer().getProperty("node.id"));
					le.setBallotId(conf.getServer().getProperty("leader.id"));
					le.setVote(VoteAction.ELECTION);
					le.setDesc(conf.getServer().getProperty("leader.id"));
					Management.Builder msg = Management.newBuilder();
					msg.setElection(le.build());
					logger.info("Needs election");
					for (NodeDesc nn : conf.getRoutingList()) {
						try
						{ InetSocketAddress isa = new InetSocketAddress( nn.getHost(), nn.getMgmtPort());
						ChannelFuture cf = ManagementQueue.connect(isa);
						cf.awaitUninterruptibly(50001);
						if(cf.isDone()&&cf.isSuccess())
						cf.channel().writeAndFlush(msg.build());
						}
												
																
						catch(Exception e){logger.info("Connection refused!");}
					}
					
									
				}
			
									
			
			
			
		} else if (req.getVote().getNumber() == VoteAction.ABSTAIN_VALUE) {
			// for some reason, I decline to vote
		} else if (req.getVote().getNumber() == VoteAction.NOMINATE_VALUE) {
			logger.info("Nomination rec!");
			nominations.incrementAndGet();
			
			int comparedToMe = req.getBallotId().compareTo(leaderId);
			if (comparedToMe == -1) {
				// Someone else has a higher priority, forward nomination
				// TODO forward
				leaderId = req.getBallotId();
				
			} else if (comparedToMe == 1) {
				// I have a higher priority, nominate myself
				// TODO nominate myself
			
			}
		}
	}
}
