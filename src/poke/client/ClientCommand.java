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
package poke.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.client.comm.CommConnection;
import poke.client.comm.CommListener;
import eye.Comm.Doc;
import eye.Comm.Course;
import eye.Comm.Course;
import eye.Comm.Header;
import eye.Comm.JobDesc;
import eye.Comm.NameValueSet;
import eye.Comm.JobDesc.JobCode;
import eye.Comm.JobOperation;
import eye.Comm.JobOperation.JobAction;
import eye.Comm.JobProposal;
import eye.Comm.Header.Routing;
import eye.Comm.Management;
import eye.Comm.NameSpaceOperation;
import eye.Comm.NameSpaceOperation.SpaceAction;
import eye.Comm.NameValueSet.NodeType;
import eye.Comm.Payload;
import eye.Comm.Ping;
import eye.Comm.Request;
import eye.Comm.User;

/**
 * The command class is the concrete implementation of the functionality of our
 * network. One can view this as a interface or facade that has a one-to-one
 * implementation of the application to the underlining communication.
 * 
 * IN OTHER WORDS (pay attention): One method per functional behavior!
 * 
 * @author gash
 * 
 */
public class ClientCommand {
	protected static Logger logger = LoggerFactory.getLogger("client");

	private String host;
	private int port;
	private CommConnection comm;

	public ClientCommand(String host, int port) {
		this.host = host;
		this.port = port;

		init();
	}

	private void init() {
		comm = new CommConnection(host, port);
	}

	/**
	 * add an application-level listener to receive messages from the server (as
	 * in replies to requests).
	 * 
	 * @param listener
	 */
	public void addListener(CommListener listener) {
		comm.addListener(listener);
	}

	/**
	 * Our network's equivalent to ping
	 * 
	 * @param tag
	 * @param num
	 */
	public void poke(String tag, int num) {
		// data to send
		/*Ping.Builder f = eye.Comm.Ping.newBuilder();
		f.setTag(tag);
		f.setNumber(num);*/
		
		/*Doc.Builder doc = Doc.newBuilder();
		doc.setFileName("abc.txt");
		Payload.Builder pl = Payload.newBuilder();
		pl.setDocId(doc.build());
		eye.Comm.Header.Builder h = Header.newBuilder();
		h.setOriginator("client");
		h.setRoutingId(eye.Comm.Header.Routing.DOC);*/
		
		//Request.Builder req = Request.newBuilder();
		//req.setHeader(h.build());
	//	req.setBody(pl.build());
		
		/*try {
			comm.sendMessage(req.build());
		} catch (Exception e) {
			logger.warn("Unable to deliver message, queuing");
		}*/
		
		/*User.Builder f = User.newBuilder();
		f.setUserId("ABC-1");
		f.setUserName("Akshay");*/

		
		/*JobProposal.Builder j = JobProposal.newBuilder();
		j.setNameSpace("competetion");
		j.setOwnerId(1234);
		j.setJobId("1234");
		j.setWeight(10);
		
		
		Management.Builder m = Management.newBuilder();
		m.setJobPropose(j.build());*/
		
		//Request for List Courses
		/*JobDesc.Builder jd = JobDesc.newBuilder();
		jd.setNameSpace("listcourses");*/
		
		//Request for List Courses
		JobDesc.Builder jd = JobDesc.newBuilder();
		jd.setNameSpace("getdescription");
		
		NameValueSet.Builder nvc = NameValueSet.newBuilder();
		NameValueSet.Builder nv = NameValueSet.newBuilder();
		nv.setNodeType(NodeType.VALUE);
		nvc.setNodeType(NodeType.VALUE);
		nvc.setName("Machine Learning-2");
		nvc.setValue("CMPE275/CS101");
		nv.addNode(nvc.build());
		
		/*JobDesc.Builder jd = JobDesc.newBuilder();
		jd.setNameSpace("competetion");*/
		jd.setOwnerId(1234);
		jd.setJobId("C-1234");
		jd.setStatus(JobCode.JOBRECEIVED);
		jd.setOptions(nv.build());
		
		
		JobOperation.Builder jb = JobOperation.newBuilder();
		jb.setAction(JobAction.ADDJOB);
		jb.setJobId("C-1234");
		jb.setData(jd.build());
		
		
		//Course.Builder c = Course.newBuilder();
		//c.setCourseId("C-131245");
		//c.setCourseName("Machine Learning-2");
		//c.setCourseDescription("This is a course offered for Stanford");
		
		//NameSpaceOperation.Builder b = NameSpaceOperation.newBuilder();
		//b.setAction(SpaceAction.ADDSPACE);
		//b.setAction(SpaceAction.LISTSPACES);
		//b.setCId(c.build());
		//b.setUId(f.build());
		
		
		
		// payload containing data
		/*Request.Builder r = Request.newBuilder();
		eye.Comm.Payload.Builder p = Payload.newBuilder();
		p.setPing(f.build());
		r.setBody(p.build());*/
		
		Request.Builder r = Request.newBuilder();
		eye.Comm.Payload.Builder p = Payload.newBuilder();
		p.setJobOp(jb.build());
		
		
		// header with routing info
		/*eye.Comm.Header.Builder h = Header.newBuilder();
		h.setOriginator("client");
		h.setTag("test finger");
		h.setTime(System.currentTimeMillis());
		h.setRoutingId(eye.Comm.Header.Routing.PING);
		r.setHeader(h.build());*/
		
		eye.Comm.Header.Builder header = Header.newBuilder();
		header.setOriginator("client-1");
		header.setRoutingId(eye.Comm.Header.Routing.JOBS);
		r.setHeader(header.build());
		r.setBody(p.build());
		
		
		eye.Comm.Request req = r.build();

		try {
			comm.sendMessage(req);
		} catch (Exception e) {
			logger.warn("Unable to deliver message, queuing");
		}
	}

}
