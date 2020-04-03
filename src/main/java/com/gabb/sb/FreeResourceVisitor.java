package com.gabb.sb;

import com.gabb.sb.architecture.ServerTestRunner;

public class FreeResourceVisitor implements IResourceVisitor {

    private final String oRunnerAddressToString;

    public FreeResourceVisitor(String aRunnerAddressToString) {
        oRunnerAddressToString = aRunnerAddressToString;
    }

    @Override
    public boolean visit(ServerTestRunner aServerTestRunner) {
        if(aServerTestRunner.getAddress().toString().equalsIgnoreCase(oRunnerAddressToString)){
            aServerTestRunner.stopTest();
            return true;
        }
        return false;
    }
}
