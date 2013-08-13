Akka Cluster Example
====================

This is an example of creating a master distributing work to a set of competing workers. It is highly inspired by this blog post: http://letitcrash.com/post/29044669086/balancing-workload-across-nodes-with-akka-2

Clone the repo and run the server with the node port as parameter (seed nodes are 1337 and 1338):
    ./run.sh 1337

Additional nodes can be run without specifying a port:
    ./run.sh

