package com.gabb.sb.architecture.messages;

/**
 * The common interface that all messages sent throughout the network must implement. 
 */
public interface IMessage { 
	String getSource();
	String getDestination();
}
