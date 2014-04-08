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

import java.util.Random;

import poke.server.resources.Resource;
import eye.Comm.JobProposal;
import eye.Comm.Management;
import eye.Comm.NameValueSet;
import eye.Comm.NameValueSet.NodeType;
import eye.Comm.Request;

public class JobResource implements Resource {

	@Override
	public Request process(Request request) {
		// TODO Auto-generated method stub
		
		if(request.getBody().getJobOp().getData().getNameSpace().equals("competetion")) {
			
			JobProposal.Builder jp = JobProposal.newBuilder();
			jp.setNameSpace(request.getBody().getJobOp().getData().getNameSpace());
			jp.setOwnerId(request.getBody().getJobOp().getData().getOwnerId());
			jp.setJobId(String.valueOf(System.currentTimeMillis()));
			jp.setWeight(1);
			
			Management.Builder paxos = Management.newBuilder();
			paxos.setJobPropose(jp.build());
			
			//Fetch value from DNS
			
			//Broadcast to all except Self Cluster
			
			
		} 	
		return null;
	}

}
