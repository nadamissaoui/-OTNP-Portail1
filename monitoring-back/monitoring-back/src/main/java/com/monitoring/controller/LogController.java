package com.monitoring.controller;





import com.monitoring.dto.LogAnalysisRequest;

import com.monitoring.dto.LogAnalysisResponse;

import com.monitoring.dto.LogEntryDto;

import com.monitoring.entity.LogEntry;

import com.monitoring.service.AiAnalysisService;

import com.monitoring.service.ErrorHistoryService;

import com.monitoring.service.LogParserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.CrossOrigin;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.PutMapping;

import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.RestController;



import java.util.ArrayList;

import java.util.List;



@RestController

@RequestMapping("/api/logs")

@CrossOrigin(origins = "*")

@RequiredArgsConstructor

public class LogController {



    private final LogParserService logParserService;

    private final AiAnalysisService aiAnalysisService;

    private final ErrorHistoryService errorHistoryService;



    @PostMapping("/analyze")

    public ResponseEntity<List<LogAnalysisResponse>> analyzeLog(@RequestBody LogAnalysisRequest request) {

        List<LogEntry> entries = logParserService.parseAndSave(

                request.getLogContent(),

                request.getProcessId(),

                request.getWorkflowType()

        );



        List<LogAnalysisResponse> responses = new ArrayList<>();

        for (LogEntry entry : entries) {

            LogAnalysisResponse response = aiAnalysisService.analyzeError(entry);

            responses.add(response);

        }



        return ResponseEntity.ok(responses);

    }



    @GetMapping("/analyze/{logEntryId}")

    public ResponseEntity<LogAnalysisResponse> analyzeExisting(@PathVariable Long logEntryId) {

        LogAnalysisResponse response = aiAnalysisService.analyzeErrorById(logEntryId);

        return ResponseEntity.ok(response);

    }



    @GetMapping("/errors")

    public ResponseEntity<List<LogEntryDto>> getAllErrors() {

        return ResponseEntity.ok(errorHistoryService.getAllErrors());

    }



    @GetMapping("/errors/process/{processId}")

    public ResponseEntity<List<LogEntryDto>> getErrorsByProcess(@PathVariable String processId) {

        return ResponseEntity.ok(errorHistoryService.getErrorsByProcessId(processId));

    }



    @GetMapping("/errors/type/{errorType}")

    public ResponseEntity<List<LogEntryDto>> getErrorsByType(@PathVariable String errorType) {

        return ResponseEntity.ok(errorHistoryService.getErrorsByType(errorType));

    }



    @GetMapping("/process/{processId}")

    public ResponseEntity<List<LogEntryDto>> getLogsByProcess(@PathVariable String processId) {

        return ResponseEntity.ok(errorHistoryService.getLogsByProcessId(processId));

    }



    @GetMapping("/search")

    public ResponseEntity<List<LogEntryDto>> searchErrors(@RequestParam String keyword) {

        return ResponseEntity.ok(errorHistoryService.searchErrors(keyword));

    }



    @PutMapping("/{logEntryId}/resolve")

    public ResponseEntity<Void> markResolved(@PathVariable Long logEntryId) {

        errorHistoryService.markAsResolved(logEntryId);

        return ResponseEntity.ok().build();

    }



    @PutMapping("/solutions/{solutionId}/apply")

    public ResponseEntity<Void> markSolutionApplied(@PathVariable Long solutionId) {

        errorHistoryService.markSolutionAsApplied(solutionId);

        return ResponseEntity.ok().build();

    }

}

