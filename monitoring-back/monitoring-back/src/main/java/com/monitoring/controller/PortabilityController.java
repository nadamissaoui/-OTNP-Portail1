package com.monitoring.controller;

import com.monitoring.dto.SavePortabilityRequestDto;
import com.monitoring.model.PortabilityRequest;
import com.monitoring.service.PortabilitySaveService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portability")
@CrossOrigin(origins = "http://localhost:3000")
public class PortabilityController {

    private final PortabilitySaveService portabilitySaveService;

    public PortabilityController(PortabilitySaveService portabilitySaveService) {
        this.portabilitySaveService = portabilitySaveService;
    }

    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> save(@RequestBody SavePortabilityRequestDto dto) {
        PortabilityRequest saved = portabilitySaveService.save(dto);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "id", saved.getId(),
                "processInstanceId", saved.getProcessInstanceId(),
                "message", "Portability request saved successfully"
        ));
    }

    @GetMapping("/all")
    public ResponseEntity<List<PortabilityRequest>> getAll() {
        return ResponseEntity.ok(portabilitySaveService.findAll());
    }

    @GetMapping("/{processInstanceId}")
    public ResponseEntity<PortabilityRequest> getByProcessInstanceId(@PathVariable Long processInstanceId) {
        PortabilityRequest request = portabilitySaveService.findByProcessInstanceId(processInstanceId);
        if (request == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(request);
    }

    @PutMapping("/{processInstanceId}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Long processInstanceId,
            @RequestBody Map<String, String> body) {
        portabilitySaveService.updateStatus(processInstanceId, body.get("status"));
        return ResponseEntity.ok(Map.of("success", true));
    }
}
