package com.hotpads.joblet;

public interface JobletCodec<T>{

    String marshallData(T params);
    T unmarshallData(String encodedParams);
    void unmarshallDataIfNotAlready(String encodedParams);

    int calculateNumItems();
    int calculateNumTasks();

}
