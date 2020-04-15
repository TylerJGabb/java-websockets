package com.gabb.sb.server.repos;

import com.gabb.sb.server.entities.ManualTermination;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface ManualTerminationRepo extends CrudRepository<ManualTermination, Integer> {


    Set<ManualTermination> findByProcessedAtIsNull();

}
