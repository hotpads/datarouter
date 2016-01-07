package com.hotpads.datarouter.client;

public class ClientTableNodeNames{

	private String clientName;
	private String tableName;
	private String nodeName;
	
	
	public ClientTableNodeNames(String clientName, String tableName, String nodeName){
		this.clientName = clientName;
		this.tableName = tableName;
		this.nodeName = nodeName;
	}


	public String getClientName(){
		return clientName;
	}

	public String getTableName(){
		return tableName;
	}

	public String getNodeName(){
		return nodeName;
	}

	
	/********************** eclipse generated ***************************/

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clientName == null) ? 0 : clientName.hashCode());
		result = prime * result + ((nodeName == null) ? 0 : nodeName.hashCode());
		result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj){
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		ClientTableNodeNames other = (ClientTableNodeNames)obj;
		if(clientName == null){
			if(other.clientName != null)
				return false;
		}else if(!clientName.equals(other.clientName))
			return false;
		if(nodeName == null){
			if(other.nodeName != null)
				return false;
		}else if(!nodeName.equals(other.nodeName))
			return false;
		if(tableName == null){
			if(other.tableName != null)
				return false;
		}else if(!tableName.equals(other.tableName))
			return false;
		return true;
	}


	@Override
	public String toString(){
		return "ClientTableNodeNames [clientName=" + clientName + ", tableName=" + tableName + ", nodeName=" + nodeName
				+ "]";
	}
	
	
}
