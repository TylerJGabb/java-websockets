package com.gabb.sb;

import com.gabb.sb.architecture.ServerTestRunner;

//element visitor
@FunctionalInterface
public interface IResourceVisitor {
    boolean visit(ServerTestRunner aServerTestRunner);
}
