package com.hotpads.config.job.databean;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;

import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrComparableTool;


@Entity()
@AccessType("field")
public class JobletQueue extends BaseDatabean<JobletQueueKey,JobletQueue>{

	/************* persistent properties ********************************/

    @Id
	private JobletQueueKey key;
    private Integer numTickets = 0;
    private Integer maxTickets = 1;


    /********************** columns ************************/

    public static final String
    	keyName = "key",
    	COL_id = "id",
    	FIELD_id = keyName+"."+COL_id,
    	COL_numTickets = "numTickets",
    	COL_maxTickets = "maxTickets";

    /*********************** constructor *************************************/

	JobletQueue(){
		this.key = new JobletQueueKey(null);
	}

	public JobletQueue(String id, Integer maxTickets){
		this.key = new JobletQueueKey(id);
		this.maxTickets = maxTickets;
	}

	/************************** databean **********************************/

    @Override
    public Class<JobletQueueKey> getKeyClass(){
    	return JobletQueueKey.class;
    }

    @Override
	public JobletQueueKey getKey() {
		return this.key;
	}

    /*************************** comparators ********************************/

    public static class NumTicketsComparator implements Comparator<JobletQueue> {
    	private boolean ascending = false;
    	public NumTicketsComparator(){
    	}
    	public NumTicketsComparator(boolean ascending){
    		this.ascending = ascending;
    	}

    	@Override
    	public int compare(JobletQueue a, JobletQueue b) {
    		int compare = DrComparableTool.nullFirstCompareTo(a.getNumTickets(), b.getNumTickets());
    		if(ascending){
    			return compare;
    		}
    		return -1 * compare;
    	}

    }

    /********************* static *******************************************/

    public static int sumNumTickets(Collection<JobletQueue> in){
    	int sum = 0;
    	for(JobletQueue q : DrCollectionTool.nullSafe(in)){
    		sum += q.getNumTickets()==null?0:q.getNumTickets();
    	}
    	return sum;
    }

    public static Map<String,JobletQueue> getById(Collection<JobletQueue> in){
    	Map<String,JobletQueue> out = new TreeMap<>();
    	for(JobletQueue q : DrCollectionTool.nullSafe(in)){
    		out.put(q.getId(), q);
    	}
    	return out;
    }


    /************************ get/set ****************************************/

    public Integer getNumTickets() {
		return numTickets;
	}

	public void setNumTickets(Integer numTickets) {
		this.numTickets = numTickets;
	}

	public Integer getMaxTickets() {
		return maxTickets;
	}

	public void setMaxTickets(Integer maxTickets) {
		this.maxTickets = maxTickets;
	}

	public String getId(){
		return this.key.getId();
	}
}
