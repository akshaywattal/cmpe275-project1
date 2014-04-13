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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.server.conf.NodeDesc;
import poke.server.conf.ServerConf;
import poke.server.dns.DataCache;
import poke.server.dns.ReplicatingMap;
import poke.server.management.ManagementQueue;
import poke.server.queue.ClientQueueMap;
import poke.server.queue.PerChannelQueue;
import eye.Comm.Header;
import eye.Comm.JobBid;
import eye.Comm.JobDesc;
import eye.Comm.JobOperation;
import eye.Comm.JobProposal;
import eye.Comm.Management;
import eye.Comm.NameValueSet;
import eye.Comm.Network;
import eye.Comm.Payload;
import eye.Comm.Request;
import eye.Comm.JobDesc.JobCode;
import eye.Comm.JobOperation.JobAction;
import eye.Comm.NameValueSet.NodeType;
import eye.Comm.Network.NetworkAction;

/**
 * The job manager class is used by the system to assess and vote on a job. This
 * is used to ensure leveling of the servers take into account the diversity of
 * the network.
 * 
 * @author gash
 * 
 */
public class JobManager {
	protected static Logger logger = LoggerFactory.getLogger("management");
	protected static AtomicReference<JobManager> instance = new AtomicReference<JobManager>();

	public AtomicInteger yesCount = new AtomicInteger(0);
	public AtomicInteger noCount = new AtomicInteger(0);
	//depends upon number of nodes in cluster
	public AtomicInteger total = new AtomicInteger(0); 
	public AtomicInteger winner = new AtomicInteger(0);
	
	private String nodeId;
	private ServerConf conf;
	public AtomicInteger countNode = new AtomicInteger(0);
	
	public List<InetSocketAddress> addressList = new CopyOnWriteArrayList<InetSocketAddress>();

	public static JobManager getInstance(String id, ServerConf conf) {
		instance.compareAndSet(null, new JobManager(id,conf));
		return instance.get();
	}

	public static JobManager getInstance() {
		return instance.get();
	}

	public JobManager(String nodeId,ServerConf conf) {
		this.nodeId = nodeId;
		this.conf = conf;
	}

	/**
	 * a new job proposal has been sent out that I need to evaluate if I can run
	 * it
	 * 
	 * @param req
	 *            The proposal
	 */
	public void processRequest(JobProposal req, Channel channel, SocketAddress sa) {
		
		//If Current Leader receives request, he forwards it to all slaves
		if(ElectionManager.getInstance().isLeader())
		{

			JobProposal.Builder jp = JobProposal.newBuilder();
			jp.setNameSpace(req.getJobId());
			jp.setOwnerId(req.getOwnerId());
			jp.setJobId(channel.localAddress().toString());
			jp.setWeight(1); //Using for Channel Now
			
			Management.Builder paxos = Management.newBuilder();
			paxos.setJobPropose(jp.build());
			
			
		for (NodeDesc nn : conf.getRoutingList()) {
			InetSocketAddress isa = new InetSocketAddress( nn.getHost(), nn.getMgmtPort());
			if((Integer.parseInt(nn.getNodeId())!=Integer.parseInt(nodeId)))
			addressList.add(isa);
			}

			if(addressList.size()>0)
			{
			for(InetSocketAddress isa : addressList)	{
				try
				{ 
				ChannelFuture cf = ManagementQueue.connect(isa);
				cf.awaitUninterruptibly(50001);
				if(cf.isDone()&&cf.isSuccess())
				{
				cf.channel().writeAndFlush(paxos.build());
				logger.info("PAXOS request forwarded to slaves by leader" + nodeId);
				}
				cf.channel().closeFuture();
				}
														
				catch(Exception e){
					
					logger.info("PAXOS request refused by " + isa.getHostName() + ":" +isa.getPort()); 
						}
			}
			
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			}
		}
		
		else {
			Random random = new Random();
			int bid = random.nextInt(2);
			JobBid.Builder jbid = JobBid.newBuilder();
			jbid.setJobId(req.getJobId());
			jbid.setOwnerId(req.getOwnerId()); //This is the Cluster ID
			jbid.setNameSpace(req.getNameSpace());
			jbid.setBid(bid);
			
			String[] socketConn2 = req.getJobId().split(":");
			String[] host = socketConn2[0].split("/");
			int port = Integer.parseInt(socketConn2[1]);
			
			sa = new InetSocketAddress(host[0],port);
			
			//channel.connect(sa);
			
			Management.Builder msg = Management.newBuilder();
			msg.setJobBid(jbid.build());
			
			ChannelFuture ch = ManagementQueue.connect(sa);
			ch.channel().writeAndFlush(msg.build());
			//ManagementQueue.enqueueResponse(msg.build(), channel, (SocketAddress)sa);
			}
		}

	/**
	 * a job bid for my job
	 * 
	 * @param req
	 *            The bid
	 */
	public void processRequest(JobBid req, Channel channel, SocketAddress sa) {
		
	
	long own = req.getOwnerId();
	//This needs to be updated to include Cluster Leader ID as Originator	
	if(req.getOwnerId()==Long.parseLong(conf.getServer().getProperty("cluster.id")))
	{	
		//Gather Response
		Map<String, Object> test = null;
		try {
			ReplicatingMap map = new ReplicatingMap("192.168.0.123", 1111);
			map.values();
			Thread.sleep(1000);
			test = DataCache.cache;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		if(test.size()-1==countNode.getAndIncrement()) {	
			winner.set(0);
			countNode.set(0);
		}
		
		if(req.getBid() == 1 && winner.get()==0) {
			Request.Builder r = Request.newBuilder();
		
			JobDesc.Builder jd = JobDesc.newBuilder();
			jd.setNameSpace("winner");
			jd.setOwnerId(1234);
			jd.setJobId("C-1234");
			jd.setStatus(JobCode.JOBRECEIVED);
			
			
			JobOperation.Builder jb = JobOperation.newBuilder();
			jb.setAction(JobAction.ADDJOB);
			jb.setJobId("C-1234");
			jb.setData(jd.build());
			
			eye.Comm.Payload.Builder p = Payload.newBuilder();
			p.setJobOp(jb.build());
			
			eye.Comm.Header.Builder header = Header.newBuilder();
			header.setOriginator(String.valueOf(req.getOwnerId()));
			header.setRoutingId(eye.Comm.Header.Routing.JOBS);
			header.setReplyMsg("The competetion will be held at :" + req.getJobId());
			r.setHeader(header.build());
			r.setBody(p.build());
			
			
			eye.Comm.Request reqFinal = r.build();
			
			PerChannelQueue sq = ClientQueueMap.clientMap.get(req.getNameSpace());
			ClientQueueMap.clientMap.remove(req.getOwnerId());
			sq.enqueueResponse(reqFinal, null);
			System.out.println("The competetion will be held at :" + req.getBid());
			winner.set(1);
		} else
		{
			System.out.println("Got Job Bid as 0");
		}
		
		
		//Respond to Client
	}
	else {	
	int globalBid = 0;	
	if (req.getBid()==1)  
		yesCount.getAndIncrement();  
	else 
		noCount.getAndIncrement();
	
	total.getAndIncrement();
	
	//Total depends upon number of nodes in cluster
	if(total.get()==1 && yesCount.get()>total.get()/2) {
		globalBid = 1;
	
	JobBid.Builder jbid = JobBid.newBuilder();
	jbid.setJobId(req.getJobId());
	jbid.setOwnerId(req.getOwnerId());
	jbid.setNameSpace(req.getNameSpace());
	jbid.setBid(globalBid);
	
	//Figure out Channel of Originating Cluster
	Management.Builder msg = Management.newBuilder();
	msg.setJobBid(jbid.build());
	
	Map<String, Object> test = null;
	try {
		ReplicatingMap map = new ReplicatingMap("192.168.0.123", 1111);
		map.values();
		Thread.sleep(1000);
		test = DataCache.cache;
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	//Return Global Bid to Originator
	Set<Entry<String, Object>> abc =  test.entrySet();
	Iterator objItr = abc.iterator();
	while(objItr.hasNext()) {
		String text = objItr.next().toString();
		String[] socketConn1 = text.split("=");
		if(socketConn1[0].equals(String.valueOf(req.getOwnerId()))) {
			String[] socketConn2 = socketConn1[1].split(":");
			String host = socketConn2[0];
			int port = Integer.parseInt(socketConn2[1]);
			port = port + 100;
			InetSocketAddress isa = new InetSocketAddress( host, port);
			ChannelFuture cf = ManagementQueue.connect(isa);
			cf.channel().writeAndFlush(msg.build());
			break;
		}
		}
	} 
	else if (total.get()==1 && noCount.get()>total.get()/2) {
		globalBid = 0;
	
	JobBid.Builder jbid = JobBid.newBuilder();
	jbid.setJobId(req.getJobId());
	jbid.setOwnerId(req.getOwnerId());
	jbid.setNameSpace(req.getNameSpace());
	jbid.setBid(globalBid);
	
	//Figure out Channel of Originating Cluster
	Management.Builder msg = Management.newBuilder();
	msg.setJobBid(jbid.build());
	
	Map<String, Object> test = null;
	try {
		ReplicatingMap map = new ReplicatingMap("192.168.0.123", 1111);
		map.values();
		Thread.sleep(1000);
		test = DataCache.cache;
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	//Return Global Bid to Originator
	Set<Entry<String, Object>> abc =  test.entrySet();
	Iterator objItr = abc.iterator();
	while(objItr.hasNext()) {
		String text = objItr.next().toString();
		String[] socketConn1 = text.split("=");
		if(socketConn1[0].equals(String.valueOf(req.getOwnerId()))) {
			String[] socketConn2 = socketConn1[1].split(":");
			String host = socketConn2[0];
			int port = Integer.parseInt(socketConn2[1]);
			port = port + 100;
			InetSocketAddress isa = new InetSocketAddress( host, port);
			ChannelFuture cf = ManagementQueue.connect(isa);
			cf.channel().writeAndFlush(msg.build());
			break;
		}
		}
	}
	}
	}
}
