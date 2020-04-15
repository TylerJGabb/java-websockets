package com.gabb.sb.server.resourcepool;

//element visitor
@FunctionalInterface
public interface IResourceVisitor {
    boolean visit(ServerTestRunner aServerTestRunner);
}
