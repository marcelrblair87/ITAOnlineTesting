package jm.gov.mtw.controllers;

import jm.gov.mtw.dto.AssessmentResultDto;
import jm.gov.mtw.dto.AssessmentSubmissionDto;
import jm.gov.mtw.models.Assessment;
import jm.gov.mtw.services.AssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/assessment")
public class AssessmentController {
    @Autowired
    private AssessmentService assessmentService;

    @PostMapping("/start")
    public ResponseEntity<?> startAssessment(@RequestParam String trn) {
        Assessment assessment = assessmentService.startAssessment(trn);
        return ResponseEntity.ok(assessment);
    }

    @GetMapping("/active/{sessionId}")
    public ResponseEntity<?> isActive(@PathVariable Long sessionId) {
        boolean active = assessmentService.isAssessmentActive(sessionId);
        return ResponseEntity.ok(Map.of("active", active));
    }

    @PostMapping("/complete/{sessionId}")
    public ResponseEntity<?> complete(@PathVariable Long sessionId) {
        assessmentService.completeAssessment(sessionId);
        return ResponseEntity.ok(Map.of("completed", true));
    }

    @PostMapping("/submit")
    public ResponseEntity<AssessmentResultDto> submit(@RequestBody AssessmentSubmissionDto submission, Authentication auth) {
        String trn = (String) auth.getPrincipal();
        AssessmentResultDto result = assessmentService.submitAssessment(trn, submission.getAnswers());
        return ResponseEntity.ok(result);
    }
}
