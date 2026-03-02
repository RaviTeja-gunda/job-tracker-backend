package com.example.job_tracker_backend.service;

import com.example.job_tracker_backend.dto.JSearchResponse;
import com.example.job_tracker_backend.model.Job;
import com.example.job_tracker_backend.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class JobRecommendationService {

    @Autowired
    private RestClient.Builder restClientBuilder;

    @Autowired
    private JobRepository jobRepository;

    @Value("${rapidapi.jsearch.key}")
    private String rapidApiKey;

    @Value("${rapidapi.jsearch.host}")
    private String rapidApiHost;

    public List<Job> fetchAndSaveRecommendations(List<String> skills) {
        if (skills == null || skills.isEmpty()) {
            return new ArrayList<>();
        }

        // We might have 20+ skills from NLP. A query with 20+ words acts as a strict
        // AND search,
        // returning 0 results. We need to pick the top 3 "most important" skills.
        // A simple heuristic for importance: longer skill names (e.g. 'machine
        // learning', 'spring boot')
        // are usually more specific than 'html' or 'css'.
        List<String> sortedSkills = new ArrayList<>(skills);
        sortedSkills.sort((a, b) -> Integer.compare(b.length(), a.length()));

        // A single specific skill usually yields the best results on Google Jobs,
        // because searching for 3 technologies simultaneously triggers a strict AND
        // search.
        String primarySkill = sortedSkills.get(0);
        String query = primarySkill + " developer";

        // Call the JSearch API
        JSearchResponse response = restClientBuilder.build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host(rapidApiHost)
                        .path("/search")
                        .queryParam("query", query)
                        .queryParam("page", 1)
                        .queryParam("num_pages", 1)
                        .queryParam("date_posted", "3days")
                        .build())
                .header("X-RapidAPI-Key", rapidApiKey)
                .header("X-RapidAPI-Host", rapidApiHost)
                .retrieve()
                .body(JSearchResponse.class);

        List<Job> recommendedJobs = new ArrayList<>();

        if (response != null && "OK".equals(response.getStatus()) && response.getData() != null) {
            for (JSearchResponse.JSearchJob apiJob : response.getData()) {
                Job job = new Job();
                job.setTitle(apiJob.getJob_title());
                job.setCompany(apiJob.getEmployer_name());
                // Shorten description so it doesn't break DB limits
                String desc = apiJob.getJob_description();
                if (desc != null && desc.length() > 1900) {
                    desc = desc.substring(0, 1900) + "...";
                }
                job.setDescription(desc);
                job.setJobUrl(apiJob.getJob_apply_link());
                job.setStatus(Job.JobStatus.RECOMMENDED);
                job.setAppliedDate(LocalDateTime.now());

                if (apiJob.getJob_posted_at_datetime_utc() != null) {
                    try {
                        job.setPostedDate(LocalDateTime.parse(apiJob.getJob_posted_at_datetime_utc().replace("Z", "")));
                    } catch (Exception e) {
                        job.setPostedDate(null);
                    }
                }

                // Save to DB
                jobRepository.save(job);
                recommendedJobs.add(job);
            }
        }

        return recommendedJobs;
    }
}
