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
	r = comm_pb2.Request()
	
	r.header.originator = "Python Client"
	r.header.routing_id = comm_pb2.Header.NAMESPACES
	
	r.body.space_op.action = comm_pb2.NameSpaceOperation.ADDSPACE
	r.body.space_op.u_id.user_id = userID
	r.body.space_op.u_id.user_name = userName
	
	m = r.SerializeToString()
	return m

def buildGetCourseDescription():
    r = comm_pb2.Request()
    r.header.originator = "Python Client"
    r.header.routing_id = comm_pb2.Header.JOBS
    r.body.job_op.action = comm_pb2.JobOperation.ADDJOB
    r.body.job_op.data.name_space = "1"
    r.body.job_op.data.owner_id = 2
    r.body.job_op.data.job_id = "3"
    r.body.job_op.data.status = comm_pb2.JobDesc.JOBRUNNING
    r.body.job_op.data.options.node_type = comm_pb2.NameValueSet.NODE
    r.body.job_op.data.options.name = "Machine Learning-2"
    r.body.job_op.data.options.value = "CMPE275/CS101"
    m = r.SerializeToString()
    return m

def getCourseDescriptionResponse(Socket):
    while True:
        data = Socket.recv(1024)
        r = comm_pb2.Request()
        r.ParseFromString(data)
        print("Recieved course name : "+(r.body.job_op.data.options.name))
        print("Recieved course value : "+(r.body.job_op.data.options.value))
        response = raw_input("Reply: ")
        if response == "exit":
            return
        Socket.sendall(response)
        Socket.close()

def buildCourse(courseID, courseName, courseDescription):
	r = comm_pb2.Request()
	
	r.header.originator = "client-1"
	r.header.routing_id = comm_pb2.Header.NAMESPACES
	
	r.body.space_op.action = comm_pb2.NameSpaceOperation.ADDSPACE
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
		data = s.recv(1024)
        print(data)
        r = comm_pb2.Request()
        r.ParseFromString(data)
#        print("Recieved course ID : "+(r.body.space_op.c_id.course_id))
#        print("Recieved course name : "+(r.body.space_op.c_id.course_name))
#        print("Recieved course description : "+(r.body.space_op.c_id.course_description))
        response = raw_input("Reply: ")
        if response == "exit":
			return
        s.sendall(response)
        s.close()
		
def getUserResponse(s):
	while True:
		data = s.recv(1024)
        print(data)
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
    host = '192.168.0.123'
    port = 5570

    s = socket(AF_INET, SOCK_STREAM)
    s.connect((host, port))
    print("Connected to "+(host)+" on port "+str(port))
	#initialMessage = raw_input("Send: ")
	
    print "Enter one of the request : "
    print "\nF for file \nU for user \nC for course\nD for List All Courses (Standardized)\nG for Get Course Description(Standardized)"
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

    elif msgType == "G":
        msg = buildGetCourseDescription()
        sendMessage(s, struct.pack('>L',len(msg)), msg)
        getCourseDescriptionResponse(s)

createSocket()