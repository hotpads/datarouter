package com.hotpads.joblet;

public interface JobletCodec<T>{

    String marshallData();
    T unmarshallData();
    void unmarshallDataIfNotAlready();

    int calculateNumItems();
    int calculateNumTasks();

}
