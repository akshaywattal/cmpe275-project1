#include <boost/asio.hpp>
#include <boost/array.hpp>
#include <iostream>
#include <string>
#include "comm.pb.h"

boost::asio::io_service io_service;
boost::asio::ip::tcp::resolver resolver(io_service);
boost::asio::ip::tcp::socket sock(io_service);
boost::array<char, 1024> buffer;
Ping finger;
Doc doc;
Request req;
Payload body;
Header header;

void read_handler(const boost::system::error_code &ec, std::size_t bytes_transferred)
{
    if (!ec)
    {
        
        std::cout << std::string(buffer.data(), bytes_transferred) << std::endl;
        sock.async_read_some(boost::asio::buffer(buffer), read_handler);
    }
}

void connect_handler(const boost::system::error_code &ec)
{
    if (!ec)
    {
        finger.set_tag("hello");
        finger.set_number(100);
        std::cout<<"has body?"<<req.has_body();
        //doc.set_file_name("abc.txt");
        //doc.set_data("anydata");
        body.mutable_ping()->CopyFrom(finger);
        //req.mutable_payload
        //body.set_allocated_ping(body.mutable_ping()->CopyFrom(finger));
        header.set_originator("cpp");
        header.set_routing_id(Header_Routing_PING);
        req.mutable_header()->CopyFrom(header);
        req.mutable_body()->CopyFrom(body);
        std::cout<<"has body?"<<req.has_body();
        boost::asio::write(sock, boost::asio::buffer(req.SerializeAsString(), sizeof(req.SerializeAsString())));
        //std::cout<<req.SerializeAsString();
        sock.async_read_some(boost::asio::buffer(buffer), read_handler);
    }
}

void resolve_handler(const boost::system::error_code &ec, boost::asio::ip::tcp::resolver::iterator it)
{
    if (!ec)
    {
        sock.async_connect(*it, connect_handler);
    }
}

int main()
{
    boost::asio::ip::tcp::resolver::query query("localhost", "5570");
    resolver.async_resolve(query, resolve_handler);
    //std::cout<<"Testing";
    io_service.run(); 
}