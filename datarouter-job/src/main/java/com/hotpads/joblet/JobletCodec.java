package com.hotpads.joblet;

public interface JobletCodec<P>{

    String marshallData(P params);
    P unmarshallData(String encodedParams);

    int calculateNumItems(P params);

    default int calculateNumTasks(P params){
    	return calculateNumItems(params);//usually the same, but feel free to override
    }

}
