package tn.esprit.otnp_ws1.Controller;

import org.kie.server.api.model.instance.ProcessInstance;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.otnp_ws1.service.MonitoringService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/monitoring")
@CrossOrigin(origins = "*")
public class MonitoringController {

    private final MonitoringService monitoringService;

    public MonitoringController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    // ─── SEARCH PROCESS INSTANCES ─────────────────────────────────────────────

    @GetMapping("/search")
    public ResponseEntity<List<ProcessInstance>> searchDemandes(
            @RequestParam(required = false) Long processInstanceId,
            @RequestParam(required = false) String crmId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String msisdn,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String contractCode,
            @RequestParam(required = false) String dateDebut,
            @RequestParam(required = false) String dateFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(monitoringService.searchDemandes(
                processInstanceId, crmId, status, msisdn, phoneNumber,
                contractCode, dateDebut, dateFin, page, size));
    }

    // ─── RECYCLE PROCESS INSTANCES ────────────────────────────────────────────

    @PostMapping("/recycle")
    public ResponseEntity<?> recycle(@RequestBody Map<String, List<Long>> body) {
        List<Long> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Aucune instance sélectionnée"
            ));
        }
        List<Map<String, Object>> result = monitoringService.recycleProcessInstances(ids);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Recyclage terminé",
                "result", result
        ));
    }

    // ─── GET HUMAN TASKS RECYCLABLES ──────────────────────────────────────────

    @GetMapping("/tasks/recyclable")
    public ResponseEntity<?> getRecyclableTasks(
            @RequestParam(required = false) String containerId,
            @RequestParam(required = false) Long processInstanceId,
            @RequestParam(required = false) String dateDebut,
            @RequestParam(required = false) String dateFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        List<Map<String, Object>> tasks = monitoringService.getRecyclableTasks(
                containerId, processInstanceId, dateDebut, dateFin, page, size
        );
        return ResponseEntity.ok(tasks);
    }

    // ─── RECYCLE HUMAN TASKS ──────────────────────────────────────────────────

    @PostMapping("/tasks/recycle")
    public ResponseEntity<?> recycleTasks(@RequestBody Map<String, List<Long>> body) {
        List<Long> taskIds = body.get("ids");
        if (taskIds == null || taskIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Aucune tâche sélectionnée"
            ));
        }
        List<Map<String, Object>> result = monitoringService.recycleHumanTasks(taskIds);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Recyclage des tâches terminé",
                "result", result
        ));
    }

    // ─── STATISTIQUES GÉNÉRALES ───────────────────────────────────────────────

    @GetMapping("/statistics/performance")
    public ResponseEntity<?> getStatistiques() {
        return ResponseEntity.ok(monitoringService.getStatistiques());
    }

    // ─── STATISTIQUES MENSUELLES ──────────────────────────────────────────────

    @GetMapping("/statistics/monthly")
    public ResponseEntity<?> getMonthlyStatistiques() {
        return ResponseEntity.ok(monitoringService.getMonthlyStatistiques());
    }

    // ─── INSTANCES PAR STATUT (pour drill-down depuis le tableau) ────────────

    @GetMapping("/instances/by-status")
    public ResponseEntity<?> getInstancesByStatus(
            @RequestParam int status,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        List<Map<String, Object>> instances = monitoringService.getInstancesByStatus(status, type, page, size);
        return ResponseEntity.ok(instances);
    }


    // ─── NODE INSTANCE LOG (vision temps réel) ───────────────────────────────────

    @GetMapping("/instances/{id}/nodes")
    public ResponseEntity<?> getNodeInstanceLog(@PathVariable Long id) {
        List<Map<String, Object>> nodes = monitoringService.getNodeInstanceLog(id);
        return ResponseEntity.ok(nodes);
    }

    @GetMapping("/instances/diagnostic")
    public ResponseEntity<?> getDiagnostic(@RequestParam Long processInstanceId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("processInstanceId", processInstanceId);
        result.put("currentNode", monitoringService.getDerniereEtape(processInstanceId));

        return ResponseEntity.ok(result);
    }


    @GetMapping("/statistics/seasonal")
    public ResponseEntity<?> seasonalTrend(){

        return ResponseEntity.ok(
                monitoringService.getSeasonalTrend()
        );

    }


    @GetMapping("/instances/{id}/lifecycle")
    public ResponseEntity<?> getLifecycle(@PathVariable Long id){

        return ResponseEntity.ok(
                monitoringService.getLifecycle(id)
        );

    }

    @PostMapping("/instances/{id}/sms")
    public ResponseEntity<?> envoyerSms(

            @PathVariable Long id,

            @RequestParam String numero){

        monitoringService.envoyerSmsEtat(id,numero);

        return ResponseEntity.ok("SMS envoyé");

    }

    @GetMapping("/instances/{id}/variables")
    public ResponseEntity<Map<String, Object>> getInstanceVariables(@PathVariable Long id) {
        Map<String, Object> vars = monitoringService.getProcessVariables(id);
        return ResponseEntity.ok(vars != null ? vars : Map.of());
    }

}


