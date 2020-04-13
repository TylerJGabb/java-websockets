package com.gabb.sb;

import com.gabb.sb.architecture.ServerTestRunner;

public class FreeResourceVisitor implements IResourceVisitor {

    //TODO: use host
    private final String oRunnerHost;

    public FreeResourceVisitor(String aRunnerHost) {
        oRunnerHost = aRunnerHost;
    }

    @Override
    public boolean visit(ServerTestRunner aServerTestRunner) {
        if(aServerTestRunner.getHost().equals(oRunnerHost)){
            aServerTestRunner.stopTest();
            return true;
        }
        return false;
    }
}
