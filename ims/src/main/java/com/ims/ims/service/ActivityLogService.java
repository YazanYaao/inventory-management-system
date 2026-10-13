package com.ims.ims.service;

import com.ims.ims.model.ActivityLog;
import com.ims.ims.repository.ActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ActivityLogService {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    /**
     * Records a new activity log entry in the system
     */
    public ActivityLog logActivity(String type, String title, String description) {
        try {
            ActivityLog log = new ActivityLog(type, title, description);
            return activityLogRepository.save(log);
        } catch (Exception e) {
            // Logging failure should never block core business flow
            System.err.println("Warning: Failed to log activity: " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves the top 5 most recent activity logs for display on the dashboard
     */
    @Transactional(readOnly = true)
    public List<ActivityLog> getRecentActivities() {
        return activityLogRepository.findTop5ByOrderByCreatedAtDesc();
    }

    /**
     * Total count of logged activities
     */
    @Transactional(readOnly = true)
    public long countActivities() {
        return activityLogRepository.count();
    }
}
