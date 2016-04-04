package com.hotpads.joblet;

public interface JobletCodec<T>{

    String marshallData(T params);
    T unmarshallData();
    void unmarshallDataIfNotAlready();

    int calculateNumItems();
    int calculateNumTasks();

}
