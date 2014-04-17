cmake_minimum_required (VERSION 2.6)
project (cpp)
# The version number.
set (Tutorial_VERSION_MAJOR 1)
set (Tutorial_VERSION_MINOR 0)
set(Boost_ADDITIONAL_VERSIONS “1.55” “1.55.0”)
set(BOOST_ROOT "/usr/local/include/boost")
set(BOOST_INCLUDE_DIRS "/Users/shankey/Development/Externals/boost_1_55_0")
	

INCLUDE_DIRECTORIES( ${BOOST_INCLUDE_DIR} )

FIND_PACKAGE( Boost 1.55 COMPONENTS system REQUIRED )



LINK_DIRECTORIES(${Boost_LIBRARY_DIRS})

include(FindProtobuf)
find_package(Protobuf REQUIRED)
include_directories(${PROTOBUF_INCLUDE_DIR})

ADD_LIBRARY(A comm.pb.cc)

add_executable(cppclient cpp_client.cpp)

target_link_libraries(cppclient A ${Boost_LIBRARIES} ${PROTOBUF_LIBRARY})


