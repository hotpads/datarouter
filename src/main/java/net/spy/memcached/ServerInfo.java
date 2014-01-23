package net.spy.memcached;
 
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
 
/**
* Twitter extension for proper ketama & server weight support.
*/
public class ServerInfo {
    public String hostname;
    public int port;
    public InetSocketAddress addr;
    public int weight;
    
    public MemcachedNode connection;
 
    
    // compatibility
    public ServerInfo(InetSocketAddress addr) {
        // we could lookup the hostname & port, but it would make the unit tests fail. :(
        this(addr.getHostName(), addr.getPort(), addr, 1);
    }
    
    public ServerInfo(String hostname, int port, InetSocketAddress addr, int weight) {
        this.hostname = hostname;
        this.port = port;
        this.addr = addr;
        this.weight = weight;
    }
    
    @Override
    public String toString() {
        return "<ServerInfo host=" + hostname + " port=" + port + " addr=" + addr + " weight=" + weight + ">";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof ServerInfo)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        ServerInfo other = (ServerInfo) obj;
        return (other.addr.equals(this.addr)) && (other.weight == this.weight);
    }

	public String getHostname() {
		return hostname;
	}

	public int getPort() {
		return port;
	}

	public InetSocketAddress getAddr() {
		return addr;
	}

	public int getWeight() {
		return weight;
	}

	public MemcachedNode getConnection() {
		return connection;
	}
}