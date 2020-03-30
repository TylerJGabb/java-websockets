package com.gabb.sb.spring.controllers;

import java.util.List;

public class TestPlanPostDTO {
    public Integer maximumAllowedFailures;
    public Integer requiredPasses;
    public Integer maxTestRunners;
    public Integer priority;
    public String buildName;
    public List<String> tags;
    public List<String> ignoredTags;
}
