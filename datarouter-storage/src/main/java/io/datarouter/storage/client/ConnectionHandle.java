/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.storage.client;

public class ConnectionHandle{

	public static final int OUTERMOST_TICKET_NUMBER = 1;

	private Thread thread; //for debugging

	private long threadId;
	private String clientName;
	private long handleNum;
	private int numTickets;

	public ConnectionHandle(Thread thread, String clientName, long handleNum, int numTickets){
		this(thread.getId(), clientName, handleNum, numTickets);
		this.thread = thread;
	}

	protected ConnectionHandle(long threadId, String clientName, long handleNum, int numTickets){
		this.threadId = threadId;
		this.clientName = clientName;
		this.handleNum = handleNum;
		this.numTickets = numTickets;
	}

	public int incrementNumTickets(){
		return ++this.numTickets;
	}

	public int decrementNumTickets(){
		return --this.numTickets;
	}

	public boolean isOutermostHandle(){
		return numTickets == OUTERMOST_TICKET_NUMBER;
	}

	@Override
	public String toString(){
		return "[" + threadId + "," + clientName + "," + handleNum + "]#" + numTickets + "-" + thread.getName();
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((clientName == null) ? 0 : clientName.hashCode());
		result = prime * result + (int) (handleNum ^ (handleNum >>> 32));
		result = prime * result + (int) (threadId ^ (threadId >>> 32));
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 *
	 * Important:
	 *   - must include: threadId, clientName, handleNum
	 *   - most NOT include: numTickets
	 */
	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if(getClass() != obj.getClass()){
			return false;
		}
		ConnectionHandle other = (ConnectionHandle)obj;
		if(clientName == null){
			if(other.clientName != null){
				return false;
			}
		}else if(!clientName.equals(other.clientName)){
			return false;
		}
		if(handleNum != other.handleNum){
			return false;
		}
		if(threadId != other.threadId){
			return false;
		}
		return true;
	}

	public String getClientName(){
		return clientName;
	}

	public void setClientName(String clientName){
		this.clientName = clientName;
	}

	public long getHandleNum(){
		return handleNum;
	}

	public void setHandleNum(long handleNum){
		this.handleNum = handleNum;
	}

	public int getNumTickets(){
		return numTickets;
	}

	public void setNumTickets(int numTickets){
		this.numTickets = numTickets;
	}

	public long getThreadId(){
		return threadId;
	}

	public void setThreadId(long threadId){
		this.threadId = threadId;
	}

}
