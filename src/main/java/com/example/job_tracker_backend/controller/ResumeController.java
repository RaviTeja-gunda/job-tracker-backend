package com.example.job_tracker_backend.controller;

import com.example.job_tracker_backend.model.Job;
import com.example.job_tracker_backend.model.UserProfile;
import com.example.job_tracker_backend.repository.UserProfileRepository;
import com.example.job_tracker_backend.service.JobRecommendationService;
import com.example.job_tracker_backend.service.ResumeParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = "http://localhost:4200")
public class ResumeController {

    @Autowired
    private ResumeParserService resumeParserService;

    @Autowired
    private JobRecommendationService jobRecommendationService;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !file.getContentType().equalsIgnoreCase("application/pdf")) {
            return ResponseEntity.badRequest().body("Please upload a valid PDF file.");
        }

        try {
            // 1. Extract skills from PDF
            List<String> skills = resumeParserService.extractSkillsFromPdf(file);

            if (skills.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No recognizable skills found in the resume.");
            }

            // Clean skills just in case
            skills = skills.stream().map(s -> s.replaceAll("[\\[\\]\"]", "").trim()).filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            // 2. Save skills to User Profile
            UserProfile profile = userProfileRepository.findById(1L).orElse(new UserProfile());
            profile.setResumeSkills(String.join(",", skills));
            userProfileRepository.save(profile);

            return ResponseEntity
                    .ok(Collections.singletonMap("message", "Resume uploaded and skills saved successfully!"));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error parsing the resume file: " + e.getMessage());
        }
    }

    @GetMapping("/skills")
    public ResponseEntity<?> getSavedSkills() {
        Optional<UserProfile> profileOpt = userProfileRepository.findById(1L);
        if (profileOpt.isPresent() && profileOpt.get().getResumeSkills() != null) {
            List<String> skills = Arrays.stream(profileOpt.get().getResumeSkills().split(","))
                    .map(String::trim)
                    .map(s -> s.replaceAll("[\\[\\]\"]", ""))
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            return ResponseEntity.ok(skills);
        }
        return ResponseEntity.ok(Collections.emptyList());
    }

    @PostMapping("/refresh-jobs")
    public ResponseEntity<?> refreshJobs() {
        Optional<UserProfile> profileOpt = userProfileRepository.findById(1L);
        if (profileOpt.isEmpty() || profileOpt.get().getResumeSkills() == null
                || profileOpt.get().getResumeSkills().isEmpty()) {
            return ResponseEntity.badRequest().body("No resume skills found. Please upload a resume first.");
        }

        List<String> skills = Arrays.stream(profileOpt.get().getResumeSkills().split(","))
                .map(String::trim)
                .map(s -> s.replaceAll("[\\[\\]\"]", ""))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        try {
            List<Job> recommendedJobs = jobRecommendationService.fetchAndSaveRecommendations(skills);
            return ResponseEntity.ok(recommendedJobs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching job recommendations: " + e.getMessage());
        }
    }
}
