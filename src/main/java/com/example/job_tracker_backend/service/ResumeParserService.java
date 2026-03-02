package com.example.job_tracker_backend.service;

import opennlp.tools.tokenize.SimpleTokenizer;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class ResumeParserService {

    // Expanded Dictionary of skills
    private static final Set<String> KNOWN_SKILLS = new HashSet<>(Arrays.asList(
            "java", "spring boot", "spring", "hibernate", "c", "c++", "c#", ".net", "python", "django", "flask",
            "ruby", "ruby on rails", "php", "laravel", "go", "golang", "rust", "swift", "kotlin", "objective-c",
            "javascript", "typescript", "angular", "react", "react native", "vue.js", "vue", "svelte", "node.js",
            "express", "html", "html5", "css", "css3", "sass", "less", "bootstrap", "tailwind",
            "mysql", "postgresql", "oracle", "sql server", "mongodb", "cassandra", "redis", "elasticsearch",
            "neo4j", "sqlite", "graphql", "rest", "rest api", "soap",
            "aws", "amazon web services", "azure", "google cloud", "gcp", "docker", "kubernetes", "k8s",
            "terraform", "ansible", "jenkins", "circleci", "gitlab ci", "github actions",
            "machine learning", "deep learning", "nlp", "computer vision", "tensorflow", "pytorch", "scikit-learn",
            "pandas", "numpy", "kafka", "rabbitmq", "apache spark", "hadoop", "airflow",
            "git", "svn", "agile", "scrum", "kanban", "jira", "linux", "unix", "bash", "shell scripting",
            "microservices", "serverless", "data structures", "algorithms"));

    public List<String> extractSkillsFromPdf(MultipartFile file) throws IOException {
        String pdfText = extractText(file);
        return extractSkillsWithNlp(pdfText);
    }

    private String extractText(MultipartFile file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }

    private List<String> extractSkillsWithNlp(String text) {
        Set<String> foundSkills = new HashSet<>();

        // 1. Tokenize the document
        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
        String[] tokens = tokenizer.tokenize(text.toLowerCase());

        // 2. N-Gram Extraction (Check sequences of words up to 3 words long)
        // This prevents picking 'java' out of 'javascript' when tokenization fails,
        // while also easily catching 'spring boot' or 'google cloud platform'
        int maxGram = 3;

        for (int i = 0; i < tokens.length; i++) {
            StringBuilder currentPhase = new StringBuilder();

            for (int j = 0; j < maxGram && (i + j) < tokens.length; j++) {
                if (j > 0) {
                    currentPhase.append(" ");
                }
                currentPhase.append(tokens[i + j]);

                String skillCandidate = currentPhase.toString();
                if (KNOWN_SKILLS.contains(skillCandidate)) {
                    foundSkills.add(skillCandidate);
                }
            }
        }

        return new ArrayList<>(foundSkills);
    }
}
