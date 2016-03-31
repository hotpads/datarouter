package com.hotpads.joblet;

public interface JobletCodec<T>{

    void marshallData();
    void unmarshallData();
    void unmarshallDataIfNotAlready();

    int calculateNumItems();
    int calculateNumTasks();

}
