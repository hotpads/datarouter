package net.spy.memcached;

import java.util.Collection;
import java.util.Iterator;

/**
 * Interface for locating a node by hash value.
 */
public interface NodeLocator {

	/**
	 * Get the primary location for the given key.
	 *
	 * @param k the object key
	 * @return the QueueAttachment containing the primary storage for a key
	 */
	MemcachedNode getPrimary(String k);

	/**
	 * Get an iterator over the sequence of nodes that make up the backup
	 * locations for a given key.
	 *
	 * @param k the object key
	 * @return the sequence of backup nodes.
	 */
	Iterator<MemcachedNode> getSequence(String k);

	/**
	 * Get all memcached nodes.  This is useful for broadcasting messages.
	 */
	Collection<MemcachedNode> getAll();

	/**
	 * add a new server to the locator
	 * does not handle connecting to it
	 */
	void addServer(ServerInfo server);

	/**
	 * moves server to failed servers
	 */
	void removeServer(MemcachedNode node);

	/**
	 * get failed servers (for reconnecting)
	 */
	Collection<ServerInfo> getFailedServers();

	/**
	 * move failed servers to good and redo the distribution
	 */
	void moveAllFailedToGood();

}
