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
package poke.resources;

import io.netty.channel.ChannelFuture;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;

import poke.server.dns.DataCache;
import poke.server.dns.ReplicatingMap;
import poke.server.management.ManagementQueue;
import poke.server.management.managers.NetworkManager;
import poke.server.resources.Resource;
import poke.server.storage.MongoDBDAO;
import eye.Comm.Header;
import eye.Comm.JobDesc;
import eye.Comm.JobOperation;
import eye.Comm.JobProposal;
import eye.Comm.Management;
import eye.Comm.NameSpaceOperation;
import eye.Comm.NameValueSet;
import eye.Comm.Payload;
import eye.Comm.PokeStatus;
import eye.Comm.JobDesc.JobCode;
import eye.Comm.JobOperation.JobAction;
import eye.Comm.NameSpaceOperation.SpaceAction;
import eye.Comm.NameValueSet.NodeType;
import eye.Comm.Request;

public class JobResource implements Resource {
	public static Map<String, Object> test = new ConcurrentHashMap<String, Object>();
	private static String[] socketConn1;
	private static String[] socketConn2;
	private static String host;
	private static int port;
	
	@Override
	public Request process(Request request) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		
		if(request.getBody().getJobOp().getData().getNameSpace().equals("competition")) {
			
			JobProposal.Builder jp = JobProposal.newBuilder();
			jp.setNameSpace(request.getBody().getJobOp().getData().getNameSpace());
			jp.setOwnerId(Long.parseLong(NetworkManager.getInstance().getClusterId())); //This should be the Cluster ID of Originator of Job Propose
			jp.setJobId(request.getHeader().getOriginator());
			jp.setWeight(1);
			
			Management.Builder paxos = Management.newBuilder();
			paxos.setJobPropose(jp.build());
			
			//Fetch value from DNS
			try {
				ReplicatingMap map = new ReplicatingMap("192.168.0.123", 1111);
				map.values();
				Thread.sleep(1000);
				test = DataCache.cache;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//Broadcast to all except Self Cluster
			Set<Entry<String, Object>> abc =  test.entrySet();
			Iterator objItr = abc.iterator();
			while(objItr.hasNext()) {
				String text = objItr.next().toString();
				socketConn1 = text.split("=");
				if(!socketConn1[0].equals(NetworkManager.getInstance().getClusterId())) {
					socketConn2 = socketConn1[1].split(":");
					host = socketConn2[0];
					port = Integer.parseInt(socketConn2[1]);
					port = port + 100;
					InetSocketAddress isa = new InetSocketAddress( host, port);
					ChannelFuture cf = ManagementQueue.connect(isa);
					cf.channel().writeAndFlush(paxos.build());
				}
				}
			}
		
		if(request.getBody().getJobOp().getData().getNameSpace().equals("listcourses")) {
			Request reply = buildMessage(request,PokeStatus.NOFOUND, "Request not fulfilled", request.getBody().getSpaceOp().getAction());
			
			MongoDBDAO mclient = new MongoDBDAO();
			try {
				
				mclient.getDBConnection();
				mclient.getDB(mclient.getDbName());
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					}
			mclient.getCollection("coursecollection");
			//BasicDBObject view = new BasicDBObject("courseid",course.getCourseId());
			DBCursor cursor = mclient.findAll();
			Request.Builder r = Request.newBuilder();
			
			NameValueSet.Builder nvc = NameValueSet.newBuilder();
			NameValueSet.Builder nv = NameValueSet.newBuilder();
			nv.setNodeType(NodeType.VALUE);
			
			JobDesc.Builder jd = JobDesc.newBuilder();
			jd.setNameSpace("listcourses");
			jd.setOwnerId(1234);
			jd.setJobId("C-1234");
			jd.setStatus(JobCode.JOBRECEIVED);
			
			while (cursor.hasNext()) {
				nvc.setNodeType(NodeType.VALUE);
				nvc.setName((String) cursor.next().get("coursename"));
				nvc.setValue((String) cursor.curr().get("coursedesc"));
				nv.addNode(nvc.build());
				}
			
			jd.setOptions(nv.build());
			
			JobOperation.Builder jb = JobOperation.newBuilder();
			jb.setAction(JobAction.ADDJOB);
			jb.setJobId("C-1234");
			jb.setData(jd.build());
			
			eye.Comm.Payload.Builder p = Payload.newBuilder();
			p.setJobOp(jb.build());
			
			
			eye.Comm.Header.Builder header = Header.newBuilder();
			header.setOriginator(request.getHeader().getOriginator());
			header.setRoutingId(eye.Comm.Header.Routing.JOBS);
			header.setReplyMsg("Course Details Gash");
			r.setHeader(header.build());
			r.setBody(p.build());
			
			
			eye.Comm.Request req = r.build();
			return req;
		}
		
		if(request.getBody().getJobOp().getData().getNameSpace().equals("getdescription")) {
			Request reply = buildMessage(request,PokeStatus.NOFOUND, "Request not fulfilled", request.getBody().getSpaceOp().getAction());
			
			MongoDBDAO mclient = new MongoDBDAO();
			try {
				
				mclient.getDBConnection();
				mclient.getDB(mclient.getDbName());
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					}
			mclient.getCollection("coursecollection");
			BasicDBObject view = new BasicDBObject("coursename",request.getBody().getJobOp().getData().getOptions().getNode(0).getName());
			DBCursor cursor = mclient.findData(view);
			Request.Builder r = Request.newBuilder();
			
			NameValueSet.Builder nvc = NameValueSet.newBuilder();
			NameValueSet.Builder nv = NameValueSet.newBuilder();
			nv.setNodeType(NodeType.VALUE);
			
			JobDesc.Builder jd = JobDesc.newBuilder();
			jd.setNameSpace("listcourses");
			jd.setOwnerId(1234);
			jd.setJobId("C-1234");
			jd.setStatus(JobCode.JOBRECEIVED);
			
			while (cursor.hasNext()) {
				nvc.setNodeType(NodeType.VALUE);
				nvc.setName((String) cursor.next().get("coursename"));
				nvc.setValue((String) cursor.curr().get("coursedesc"));
				nv.addNode(nvc.build());
				}
			
			jd.setOptions(nv.build());
			
			JobOperation.Builder jb = JobOperation.newBuilder();
			jb.setAction(JobAction.ADDJOB);
			jb.setJobId("C-1234");
			jb.setData(jd.build());
			
			eye.Comm.Payload.Builder p = Payload.newBuilder();
			p.setJobOp(jb.build());
			
			
			eye.Comm.Header.Builder header = Header.newBuilder();
			header.setOriginator(request.getHeader().getOriginator());
			header.setRoutingId(eye.Comm.Header.Routing.JOBS);
			header.setReplyMsg("Course Description Gash");
			r.setHeader(header.build());
			r.setBody(p.build());
			
			
			eye.Comm.Request req = r.build();
			return req;
		}
				
		return null;
	}
	
	public Request buildMessage(Request request,PokeStatus pks, String message, SpaceAction spAction)
	{
	/*NameSpaceStatus.Builder ns = NameSpaceStatus.newBuilder();
	ns.setStatus(pks);
	Payload.Builder py = Payload.newBuilder();
	py.setSpaceStatus(ns.build());
	Header.Builder he = Header.newBuilder();
	he.setRoutingId(Routing.NAMESPACES);
	he.setOriginator(request.getHeader().getOriginator());
	he.setReplyMsg(message);
	he.setReplyCode(pks);
	reply.setHeader(he.build());
	reply.setBody(py.build());*/
		
	//If CRUD is for User
	if (request.getBody().getSpaceOp().hasUId()) {
		Request.Builder r = Request.newBuilder();
		eye.Comm.User.Builder f = eye.Comm.User.newBuilder();
		f.setUserId(request.getBody().getSpaceOp().getUId().getUserId());
		
		NameSpaceOperation.Builder b = NameSpaceOperation.newBuilder();
		b.setAction(spAction);
		b.setUId(f.build());
		
		eye.Comm.Payload.Builder p = Payload.newBuilder();
		p.setSpaceOp(b.build());
		r.setBody(p.build());
		
		eye.Comm.Header.Builder h = Header.newBuilder();
		h.setOriginator(request.getHeader().getOriginator());
		h.setRoutingId(eye.Comm.Header.Routing.NAMESPACES);
		h.setReplyMsg(message);
		r.setHeader(h.build());
		
		eye.Comm.Request reply = r.build();
		
		return reply;
		}
	
	//If CRUD is for Course
	else if(request.getBody().getSpaceOp().hasCId()) {
		Request.Builder r = Request.newBuilder();
		eye.Comm.Course.Builder f = eye.Comm.Course.newBuilder();
		f.setCourseId(request.getBody().getSpaceOp().getCId().getCourseId());
		
		NameSpaceOperation.Builder b = NameSpaceOperation.newBuilder();
		b.setAction(spAction);
		b.setCId(f.build());
		
		eye.Comm.Payload.Builder p = Payload.newBuilder();
		p.setSpaceOp(b.build());
		r.setBody(p.build());
		
		eye.Comm.Header.Builder h = Header.newBuilder();
		h.setOriginator(request.getHeader().getOriginator());
		h.setRoutingId(eye.Comm.Header.Routing.NAMESPACES);
		h.setReplyMsg(message);
		r.setHeader(h.build());
		
		eye.Comm.Request reply = r.build();
		
		return reply;
		} 
	
		// payload containing data
		/*Request.Builder r = Request.newBuilder();
		eye.Comm.Payload.Builder p = Payload.newBuilder();
		p.setPing(f.build());
		r.setBody(p.build());*/
		
		//Request.Builder r = Request.newBuilder();
		
		
		// header with routing info
		/*eye.Comm.Header.Builder h = Header.newBuilder();
		h.setOriginator("client");
		h.setTag("test finger");
		h.setTime(System.currentTimeMillis());
		h.setRoutingId(eye.Comm.Header.Routing.PING);
		r.setHeader(h.build());*/
		
		/*eye.Comm.Course.Builder f = eye.Comm.Course.newBuilder();
		f.setCourseId("C-12");
		f.setCourseName("Machine Learning");
		f.setCourseDescription("This is a course offered for Stanford");
		
		NameSpaceOperation.Builder b = NameSpaceOperation.newBuilder();
		b.setAction(SpaceAction.ADDSPACE);
		b.setCId(f.build());*/
	return null;
	}

}
