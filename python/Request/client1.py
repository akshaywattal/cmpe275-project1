from socket import *
import comm_pb2
import sys
import struct

 
def buildFile(file):
	r = comm_pb2.Request()
	
	r.header.originator = "PythonClient"
	r.header.routing_id = comm_pb2.Header.DOC
	
	r.body.doc_id.file_name = file
#	r.body.ping.tag = tag

	m = r.SerializeToString()
	return m
	
def buildUser(userID, userName):
	r = comm_pb2.User()
	
	r.header.originator = "Python Client"
	r.header.routing_id = comm_pb2.Header.NAMESPACES
	
	r.body.space_op.action = comm_pb2.NameSpaceOperation.ADDSPACE
	r.body.space_op.u_id.user_id = userID
	r.body.space_op.u_id.user_name = userName
	
	m = r.SerializeToString()
	return m

def buildCourse(courseID, courseName, courseDescription):
	r = comm_pb2.Course()
	
	r.header.originator = "Python Client"
	r.header.routing_id = comm_pb2.Header.NAMESPACES
	
	r.body.space_op.action = comm_pb2.NameSpaceOperation.LISTSPACES
	r.body.space_op.c_id.course_id = courseID
	r.body.space_op.c_id.course_name = courseName
	r.body.space_op.c_id.course_description = courseDescription
	
	m = r.SerializeToString()
	return m

def sendMessage(Socket, Length, Message):
	Socket.sendall(Length + Message)

def getFileResponse(Socket):
    while True:
        data = Socket.recv(1024)
        r = comm_pb2.Request()
        r.ParseFromString(data)
        print("Recieved-filename "+(r.body.doc_id.file_name))
        target = open (r.body.doc_id.file_name, 'w')
        target.write(r.body.doc_id.data)
        target.close()
        response = raw_input("Reply: ")
        if response == "exit":
			return
        Socket.sendall(response)
        Socket.close()

def getCourseResponse(s):
	while True:
		data = Socket.recv(1024)
        r = comm_pb2.Request()
        r.ParseFromString(data)
        print("Recieved course ID : "+(r.body.space_op.c_id.course_id))
        print("Recieved course name : "+(r.body.space_op.c_id.course_name))
        print("Recieved course description : "+(r.body.space_op.c_id.course_description))
        response = raw_input("Reply: ")
        if response == "exit":
			return
        Socket.sendall(response)
        Socket.close()
		
def getUserResponse(s):
	while True:
		data = Socket.recv(1024)
        r = comm_pb2.Request()
        r.ParseFromString(data)
        print("Recieved user ID : "+(r.body.space_op.u_id.user_id))
        print("Recieved user name : "+(r.body.space_op.u_id.user_name))
        response = raw_input("Reply: ")
        if response == "exit":
			return
        Socket.sendall(response)
        Socket.close()

def createSocket():
    host = 'localhost'
    port = 5570

    s = socket(AF_INET, SOCK_STREAM)
    s.connect((host, port))
    print("Connected to "+(host)+" on port "+str(port))
	#initialMessage = raw_input("Send: ")
	
    print "Enter one of the request : "
    print "\nF for file \nU for user \nC for course\n"
    msgType = raw_input()
    
    if msgType == "U":
		msgUser = buildUser("ABC-1", "Akshay")
		sendMessage(s, struct.pack('>L',len(msgUser)), msgUser)
		getUserResponse(s)
	
    elif msgType == "C":
		msgCourse = buildCourse("C-13", "Machine Learning-2", "This is a course offered for Stanford")
		sendMessage(s, struct.pack('>L',len(msgCourse)), msgCourse)
		getCourseResponse(s)
		
    elif msgType == "F":
		msg = buildFile("abc.txt")
		sendMessage(s, struct.pack('>L',len(msg)), msg)
		getFileResponse(s)
		
createSocket()