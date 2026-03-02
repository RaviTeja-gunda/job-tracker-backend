package com.example.job_tracker_backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class JSearchResponse {
    private String status;
    private List<JSearchJob> data;

    @Data
    public static class JSearchJob {
        private String job_id;
        private String employer_name;
        private String job_title;
        private String job_description;
        private String job_apply_link;
        private String job_posted_at_datetime_utc;
    }
}
