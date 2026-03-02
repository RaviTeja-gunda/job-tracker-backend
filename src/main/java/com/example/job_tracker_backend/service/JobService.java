package com.example.job_tracker_backend.service;

import com.example.job_tracker_backend.model.Job;
import com.example.job_tracker_backend.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public Job getJobById(Long id) {
        return jobRepository.findById(id).orElseThrow(() -> new RuntimeException("Job not found"));
    }

    public Job createJob(Job job) {
        if (job.getStatus() == null) {
            job.setStatus(Job.JobStatus.APPLIED);
        }
        if (job.getAppliedDate() == null && job.getStatus() != Job.JobStatus.RECOMMENDED) {
            job.setAppliedDate(LocalDateTime.now());
        }
        return jobRepository.save(job);
    }

    public Job updateJob(Long id, Job jobDetails) {
        Job job = getJobById(id);

        if (jobDetails.getTitle() != null)
            job.setTitle(jobDetails.getTitle());
        if (jobDetails.getCompany() != null)
            job.setCompany(jobDetails.getCompany());
        if (jobDetails.getDescription() != null)
            job.setDescription(jobDetails.getDescription());
        if (jobDetails.getJobUrl() != null)
            job.setJobUrl(jobDetails.getJobUrl());

        if (jobDetails.getStatus() != null && job.getStatus() != jobDetails.getStatus()) {
            job.setStatus(jobDetails.getStatus());
            if (jobDetails.getStatus() == Job.JobStatus.APPLIED && job.getAppliedDate() == null) {
                job.setAppliedDate(LocalDateTime.now());
            }
        }

        return jobRepository.save(job);
    }

    public void deleteJob(Long id) {
        jobRepository.deleteById(id);
    }
}
