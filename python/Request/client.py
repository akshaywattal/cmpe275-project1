from socket import *
import comm_pb2
import sys
import struct

 
def buildRequest(file):
	r = comm_pb2.Request()
	
	r.header.originator = "PythonClient"
	r.header.routing_id = comm_pb2.Header.DOC
	
	r.body.doc_id.file_name = file
#	r.body.ping.tag = tag

	m = r.SerializeToString()
	return m;
	
def buildUser(userID, userName):
	r = comm_pb2.User()
	
	r.header.originator = "Python Client"
	r.header.routing_id = comm_pb2.Header.NAMESPACES
	
	r.body.space_op.action = comm_pb2.NameSpaceOperation.ADDSPACE
	r.body.space_op.u_id.user_id = userID
	r.body.space_op.u_id.user_name = userName
	
	m = r.SerializeToString()
	return m;

def buildCourse(courseID, courseName, courseDescription):
	r = comm_pb2.Course()
	
	r.header.originator = "Python Client"
	r.header.routing_id = comm_pb2.Header.NAMESPACES
	
	r.body.space_op.action = comm_pb2.NameSpaceOperation.LISTSPACES;
	r.body.space_op.c_id.course_id = courseID
	r.body.space_op.c_id.course_name = courseName
	r.body.space_op.c_id.course_description = courseDescription
	
	m = r.SerializeToString()
	return m;

def createSocket():
    host = 'localhost'
    port = 5570
 
    s = socket(AF_INET, SOCK_STREAM)
    s.connect((host, port))
    print("Connected to "+(host)+" on port "+str(port))
	#initialMessage = raw_input("Send: ")
	msgUser = 
    msg = buildRequest("abc.txt")
    packed_len = struct.pack('>L',len(msg))
    s.sendall(packed_len + msg)
    while True:
        data = s.recv(1024)
        r = comm_pb2.Request()
        r.ParseFromString(data)
        print("Recieved-filename "+(r.body.doc_id.file_name))
        target = open (r.body.doc_id.file_name, 'w')
        target.write(r.body.doc_id.data)
        target.close()
        response = raw_input("Reply: ")
        if response == "exit":
			break
        s.sendall(response)
        s.close()
		
createSocket()