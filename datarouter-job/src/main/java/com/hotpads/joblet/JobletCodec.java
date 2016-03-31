package com.hotpads.joblet;

public interface JobletCodec{

    void marshallData();
    void unmarshallData();
    void unmarshallDataIfNotAlready();

    int calculateNumItems();
    int calculateNumTasks();

}
