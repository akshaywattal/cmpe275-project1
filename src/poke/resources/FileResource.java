package poke.resources;

import java.io.File;
import java.io.IOException;

import poke.server.resources.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;

import eye.Comm.Header;
import eye.Comm.NameSpaceOperation;
import eye.Comm.Payload;
import eye.Comm.PokeStatus;
import eye.Comm.Request;
import eye.Comm.Header.Routing;
import eye.Comm.NameSpaceOperation.SpaceAction;
import eye.Comm.Request.Builder;
import eye.Comm.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

//import org.apache.commons.*;

public class FileResource implements Resource{
	
	protected static Logger logger = LoggerFactory.getLogger("server");

//	@Override
//	public Request process(Request request) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	

	@Override
	public Request process(Request request) {
		// TODO Auto-generated method stub
		
		
		//Request reply = buildMessage(request,PokeStatus.NOFOUND, "Request not fulfilled", null); 
		
		String fileName = request.getBody().getDocId().getFileName();
		//fileName = fileName;
		
		//File file = new File(fileName);
		
		Path path = Paths.get(fileName);
		byte[] fileData = null;
		//ByteString dat = ByteString.copyFrom(fileData);
		String fileNameInDir = path.getFileName().toString();
		
		if(!fileNameInDir.equals(fileName)){
			
			return buildMessage(request,PokeStatus.NOFOUND, "File Not found!",fileData);
		}
		else{
			
		try {
			fileData = Files.readAllBytes(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return buildMessage(request, PokeStatus.SUCCESS, fileName, fileData);
		}
	}


	private Request buildMessage(Request request, PokeStatus nofound,
			String message, byte[] fileData) {
		// TODO Auto-generated method stub
		Request.Builder r = Request.newBuilder();
		
		eye.Comm.Doc.Builder f = eye.Comm.Doc.newBuilder();
		f.setData(ByteString.copyFrom(fileData));
		f.setFileName(message);
		eye.Comm.Payload.Builder p = eye.Comm.Payload.newBuilder();
		p.setDocId(f.build());
		r.setBody(p.build());
		
		eye.Comm.Header.Builder h = eye.Comm.Header.newBuilder();
		h.setRoutingId(Routing.DOC);
		h.setOriginator(request.getHeader().getOriginator());
		
		if(nofound == PokeStatus.NOFOUND){
			h.setReplyMsg(message);
		}
		else{
			h.setReplyMsg(message);
		}
		r.setHeader(h.build());
		eye.Comm.Request reply = r.build();
	return reply;
	}

}
