package com.gabb.sb.spring.repos;

import com.gabb.sb.spring.entities.Job;
import org.springframework.data.repository.CrudRepository;

public interface JobRepository extends CrudRepository<Job, Integer> { }