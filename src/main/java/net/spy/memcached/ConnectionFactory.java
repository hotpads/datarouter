package net.spy.memcached;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import net.spy.memcached.ops.Operation;

/**
 * Factory for creating instances of MemcachedConnection.
 * This is used to provide more fine-grained configuration of connections.
 */
public interface ConnectionFactory {

	/**
	 * Create a MemcachedConnection for the given SocketAddresses.
	 *
	 * @param addrs the addresses of the memcached servers
	 * @return a new MemcachedConnection connected to those addresses
	 * @throws IOException for problems initializing the memcached connections
	 */
	MemcachedConnection createConnection(ServerInfo[] serverList)
		throws IOException;

	/**
	 * Create a new memcached node.
	 */
	MemcachedNode createMemcachedNode(SocketAddress sa,
			SocketChannel c, int bufSize);

	/**
	 * Create a BlockingQueue for operations for a connection.
	 */
	BlockingQueue<Operation> createOperationQueue();

	/**
	 * Create a NodeLocator instance for the given list of nodes.
	 */  
	NodeLocator createLocator(ServerInfo[] serverList);

	/**
	 * Get the operation factory for connections built by this connection
	 * factory.
	 */
	OperationFactory getOperationFactory();

}
