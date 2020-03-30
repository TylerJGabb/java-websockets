package com.gabb.sb.spring.repos;

import com.gabb.sb.spring.entities.Job;
import com.gabb.sb.spring.entities.Run;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface RunRepo extends CrudRepository<Run, Integer> {

    @Query("SELECT r FROM Run r WHERE r.status = 'IN_PROGRESS' AND r.job = :job")
    public Set<Run> findInProgressForJob(@Param("job") Job job);

}
