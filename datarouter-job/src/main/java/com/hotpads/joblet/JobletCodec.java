package com.hotpads.joblet;

public interface JobletCodec<T>{

    String marshallData(T params);
    T unmarshallData(String encodedParams);

    int calculateNumItems(T params);
    int calculateNumTasks(T params);

}
