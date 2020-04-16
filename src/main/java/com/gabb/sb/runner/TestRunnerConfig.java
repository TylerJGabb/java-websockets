package com.gabb.sb.runner;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.gabb.sb.server.ServerSpringBootApplication;

public class TestRunnerConfig {

    private static TestRunnerConfig oInstance;

    private String[] oBenchTags;
    private int oManagerWebSocketServerPort;
    private String oManagerWebSocketServerHost;

    public static TestRunnerConfig getInstance(){
        if(oInstance == null){
            synchronized (TestRunnerConfig.class) {
                if(oInstance == null) {
                    oInstance = new TestRunnerConfig();
                }
            }
        }
        return oInstance;
    }

    //todo read from default config
    private TestRunnerConfig() {
        oBenchTags = new String[0];
        oManagerWebSocketServerPort = ServerSpringBootApplication.PORT;
        oManagerWebSocketServerHost = ServerSpringBootApplication.HOST;
    }
    
    public TestRunnerConfig setFromDto(DTO aDto){
        if(aDto.benchTags != null) oBenchTags = aDto.benchTags.clone();
        if(aDto.managerWebSocketServerHost != null) oManagerWebSocketServerHost = aDto.managerWebSocketServerHost;
        if(aDto.managerWebSocketServerPort != null) oManagerWebSocketServerPort = aDto.managerWebSocketServerPort;
        return this;
    }
    

    public String[] getBenchTags() {
        return oBenchTags;
    }

    public int getManagerWebSocketServerPort() {
        return oManagerWebSocketServerPort;
    }

    public String getManagerWebSocketServerHost() {
        return oManagerWebSocketServerHost;
    }

    @JsonAutoDetect(
            fieldVisibility = JsonAutoDetect.Visibility.ANY
    )
    public static class DTO {
        private String[] benchTags;
        private Integer managerWebSocketServerPort;
        private String managerWebSocketServerHost;
        
        public DTO(){ }
    }
}
