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

import io.netty.handler.codec.base64.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;

import poke.client.comm.CommListener;
import poke.client.util.ClientUtil;
import eye.Comm.Header;

/**
 * example listener that an application would use to receive events.
 * 
 * @author gash
 * 
 */
public class ClientPrintListener implements CommListener {
	protected static Logger logger = LoggerFactory.getLogger("connect");

	private String id;

	public ClientPrintListener(String id) {
		this.id = id;
	}

	@Override
	public String getListenerID() {
		return id;
	}

	@Override
	public void onMessage(eye.Comm.Request msg) {
		if (logger.isDebugEnabled())
			ClientUtil.printHeader(msg.getHeader());

		if (msg.getHeader().getRoutingId().getNumber() == Header.Routing.PING_VALUE)
			ClientUtil.printPing(msg.getBody().getPing());
		else if (msg.getHeader().getRoutingId().getNumber() == Header.Routing.NAMESPACES_VALUE) {
			System.out.println(msg.getHeader().getReplyMsg());
		} else if (msg.getHeader().getRoutingId().getNumber() == Header.Routing.JOBS_VALUE) {
			System.out.println(msg.getBody());
		} else if (msg.getHeader().getRoutingId().getNumber() == Header.Routing.MANAGE_VALUE) {
			// management responses
		} else if (msg.getHeader().getRoutingId().getNumber()== Header.Routing.DOC_VALUE){
			System.out.println(msg.getBody().getDocId().getData());
			ByteString data = msg.getBody().getDocId().getData();
			byte[] byteData = data.toByteArray();
			//FileOutputStream fos;
			try {
				File file = new File(msg.getBody().getDocId().getFileName());
				FileUtils.writeByteArrayToFile(file, byteData);
//				fos = new FileOutputStream(msg.getBody().getDocId().getFileName());
//				fos.write(byteData);
//				fos.flush();
//	            fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
            			
		}
		else {
			// unexpected reply - how do you handle this?
			System.out.println("404 Not Found");
		}
	}
}
