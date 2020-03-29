package com.gabb.sb.spring.repos;

import com.gabb.sb.spring.entities.TestPlan;
import org.springframework.data.repository.CrudRepository;

public interface TestPlanRepo extends CrudRepository<TestPlan, Integer> { }