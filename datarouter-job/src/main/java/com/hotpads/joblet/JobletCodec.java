package com.hotpads.joblet;

public interface JobletCodec<P>{

    String marshallData(P params);
    P unmarshallData(String encodedParams);

    int calculateNumItems(P params);
    int calculateNumTasks(P params);

}
