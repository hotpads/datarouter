package com.hotpads.datarouter.client.imp.memcached;


public abstract class MemcachedClientFactory 
//implements ClientFactory
{
//	private static Logger logger = Logger.getLogger(MemcachedClientFactory.class);

//	@Override
//	public Client createClient(Datapus datapus, String clientName, Properties properties) throws Exception{
//		MemcachedClient datapusClient = new MemcachedClient();
//		datapusClient.name = clientName;
//		
//		String configFileLocation = properties.getProperty("client."+clientName+".configFile");
//		net.spy.memcached.MemcachedClient spyClient = createSpyClient(configFileLocation);
//		datapusClient.spyClient = spyClient;
//		
//		return datapusClient;
//	}
//	
//	
//	
//	protected static net.spy.memcached.MemcachedClient createSpyClient(String configFileLocation) throws IOException {
//		ConnectionFactory factory = 
//			//use ketama hash for consistent server hashing
//			new KetamaConnectionFactory(8 * DefaultConnectionFactory.DEFAULT_OP_QUEUE_LEN, //default is 16384
//											DefaultConnectionFactory.DEFAULT_READ_BUFFER_SIZE); 
//		List<InetSocketAddress> servers = getServers(configFileLocation);
//		if (servers.size() > 0) {
//			logger.debug("creating new client");
//			return new net.spy.memcached.MemcachedClient(factory, servers);
//		}
//		return null;
//	}
//	
//	protected static List<InetSocketAddress> getServers(String configFileLocation) {
//		BufferedReader br = null;
//		List<String> lines = new LinkedList<String>();
//
//		try{
//			br = new BufferedReader(new FileReader(configFileLocation));
//			String line = null;
//			while ((line = br.readLine()) != null) {
//				if (line.startsWith("#"))
//					continue; //ignore comments
//				lines.add(line);
//			}
//		} catch(IOException e){ 
//		} finally {	
//			try {
//				if (br != null)
//					br.close();
//			} catch (IOException e) { }
//			br = null;
//		}
//		
//		StringBuilder servers = new StringBuilder();
//		for(String line : CollectionTool.nullSafe(lines)){ 
//			servers.append(" "+line); 
//		}
//		List<InetSocketAddress> addresses = null;
//		try {
//			addresses = AddrUtil.getAddresses(servers.toString());
//		} catch (IllegalArgumentException e) {
//			logger.debug("No correctly specified servers for " + configFileLocation);
//			addresses = GenericsFactory.makeArrayList();
//		}
//		return addresses;
//	}
}
