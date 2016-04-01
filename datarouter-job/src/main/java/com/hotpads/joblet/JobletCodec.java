package com.hotpads.joblet;

public interface JobletCodec<T>{

    void marshallData();
    T unmarshallData();
    void unmarshallDataIfNotAlready();

    int calculateNumItems();
    int calculateNumTasks();

}
