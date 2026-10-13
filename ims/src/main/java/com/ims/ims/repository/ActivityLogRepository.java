package com.ims.ims.repository;

import com.ims.ims.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    /**
     * Retrieve the top 5 most recent activities ordered by creation timestamp descending
     */
    List<ActivityLog> findTop5ByOrderByCreatedAtDesc();
}
