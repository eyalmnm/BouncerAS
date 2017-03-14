package com.em_projects.bouncer.network.comm;

public interface CommListener {
    public void newDataArrived(String newData);

    public void exceptionThrown(Throwable throwable);
}
