package com.gabb.sb.spring.repos;

import com.gabb.sb.spring.entities.ManualTermination;
import org.hibernate.annotations.Where;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ManualTerminationRepo extends CrudRepository<ManualTermination, Integer> {


    Set<ManualTermination> findByProcessedAtIsNull();

}
