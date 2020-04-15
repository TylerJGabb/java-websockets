package com.gabb.sb.server.resourcepool;

//element
public interface IResourceAcceptsVisitor {
    public boolean accept(IResourceVisitor visitor);
}
