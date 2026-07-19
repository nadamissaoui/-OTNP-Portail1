package tn.esprit.otnp_ws1.service;

import org.kie.api.runtime.manager.audit.NodeInstanceLog;

import org.kie.server.api.model.instance.NodeInstance;

import org.kie.server.client.QueryServicesClient;

import java.util.Comparator;

import java.util.List;

import org.kie.server.api.model.instance.ProcessInstance;

import org.kie.server.api.model.instance.TaskSummary;

import org.kie.server.client.KieServicesClient;

import org.kie.server.client.ProcessServicesClient;

import org.kie.server.client.QueryServicesClient;

import org.kie.server.client.UserTaskServicesClient;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service

public class MonitoringService {





    private static final Logger log = LoggerFactory.getLogger(MonitoringService.class);

    private static final int STATE_ACTIVE    = 1;

    private static final int STATE_COMPLETED = 2;

    private static final int STATE_ABORTED   = 3;



    private static final String[] MONTHS = {

            "January","February","March","April","May","June",

            "July","August","September","October","November","December"

    };



    private final KieServicesClient kieServicesClient;

    private final SmsService smsService;





    @Value("${monitoring.load-variables:true}")

    private boolean loadVariables;



    @Value("${monitoring.load-variables-only-active:true}")

    private boolean loadVariablesOnlyActive;



    @Value("${jbpm.container.id}")

    private String containerIdIn;



    @Value("${jbpm.container.out.id}")

    private String containerIdOut;



    public MonitoringService(

            @Lazy KieServicesClient kieServicesClient,

            SmsService smsService) {



        this.kieServicesClient = kieServicesClient;

        this.smsService = smsService;

    }



    // ─── CLIENTS ──────────────────────────────────────────────────────────────



    private QueryServicesClient queryClient() {

        if (kieServicesClient == null) return null;

        return kieServicesClient.getServicesClient(QueryServicesClient.class);

    }



    private ProcessServicesClient processClient() {

        if (kieServicesClient == null) return null;

        return kieServicesClient.getServicesClient(ProcessServicesClient.class);

    }



    private UserTaskServicesClient userTaskClient() {

        if (kieServicesClient == null) return null;

        return kieServicesClient.getServicesClient(UserTaskServicesClient.class);

    }



    // ─── SEARCH PROCESS INSTANCES ─────────────────────────────────────────────



    public List<ProcessInstance> searchDemandes(Long processInstanceId, String crmId, Integer status,

                                                String msisdn, String phoneNumber, String contractCode,

                                                String dateDebut, String dateFin, int page, int pageSize) {

        QueryServicesClient qc = queryClient();

        if (qc == null) return new ArrayList<>();



        if (processInstanceId != null) {
            ProcessInstance single = qc.findProcessInstanceById(processInstanceId);
            List<ProcessInstance> result = new ArrayList<>();
            if (single != null) {
                ProcessInstance enriched = enrichWithVariables(single);
                if (matchesProcessFilters(enriched, status, crmId, msisdn, phoneNumber, contractCode, dateDebut, dateFin)) {
                    result.add(enriched);
                }
            }
            return result;
        }

        List<ProcessInstance> instances = qc.findProcessInstancesByStatus(
                getTargetStatuses(status), 0, Math.max(pageSize, 10000), "start_date", false);

        if (instances == null) return new ArrayList<>();

        return instances.stream()
                .map(this::enrichWithVariables)
                .filter(i -> matchesProcessFilters(i, status, crmId, msisdn, phoneNumber, contractCode, dateDebut, dateFin))
                .skip((long) page * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
    }


    // ─── HUMAN TASKS — GET ────────────────────────────────────────────────────



    public List<Map<String, Object>> getRecyclableTasks(String containerId, Long processInstanceId,
                                                        String dateDebut, String dateFin, int page, int size) {
        UserTaskServicesClient taskClient = userTaskClient();
        List<Map<String, Object>> recyclable = new ArrayList<>();

        if (taskClient != null) {
            try {
                List<TaskSummary> tasks = taskClient.findTasksOwned(
                        "wbadmin", Arrays.asList("Ready", "Reserved", "InProgress"), page, size);
                if (tasks != null) {
                    recyclable.addAll(tasks.stream()
                            .filter(t -> {
                                if (processInstanceId != null && !processInstanceId.equals(t.getProcessInstanceId())) return false;
                                if (containerId != null && !containerId.trim().isEmpty() && !containerId.equals(t.getContainerId())) return false;
                                if (t.getCreatedOn() != null) {
                                    String td = t.getCreatedOn().toString();
                                    if (dateDebut != null && !dateDebut.trim().isEmpty() && td.compareTo(dateDebut) < 0) return false;
                                    if (dateFin   != null && !dateFin.trim().isEmpty()   && td.compareTo(dateFin)   > 0) return false;
                                }
                                return true;
                            })
                            .map(t -> {
                                Map<String, Object> map = new HashMap<>();
                                map.put("id", t.getId());
                                map.put("name", t.getName());
                                map.put("processInstanceId", t.getProcessInstanceId());
                                map.put("containerId", t.getContainerId());
                                map.put("status", t.getStatus());
                                map.put("createdOn", t.getCreatedOn());
                                map.put("itemType", "TASK");
                                return map;
                            })
                            .collect(Collectors.toList()));
                }
            } catch (Exception e) {
                log.error("Erreur Human Tasks : {}", e.getMessage());
            }
        } else {
            log.warn("UserTaskServicesClient indisponible, affichage des processus bloqués uniquement");
        }

        recyclable.addAll(getBlockedProcessCandidates(containerId, processInstanceId, dateDebut, dateFin));
        return recyclable;
    }


    // ─── HUMAN TASKS — RECYCLE ────────────────────────────────────────────────



    public List<Map<String, Object>> recycleHumanTasks(List<Long> taskIds) {

        UserTaskServicesClient taskClient = userTaskClient();

        if (taskClient == null) throw new IllegalStateException("UserTaskServicesClient indisponible");



        List<Map<String, Object>> results = new ArrayList<>();

        for (Long taskId : taskIds) {

            Map<String, Object> res = new HashMap<>();
            res.put("taskId", taskId);
            try {
                String cid = findContainerIdForTask(taskId, taskClient);
                if (cid == null) {
                    res.putAll(recycleOneProcessInstance(taskId));
                    results.add(res);
                    continue;
                }
                log.info(">>> [RECYCLE TASK] taskId={} containerId={}", taskId, cid);
                taskClient.releaseTask(cid, taskId, "wbadmin");
                res.put("success", true);
                res.put("message", "Tache remise en etat Ready");

                log.info(">>> [RECYCLE TASK] Succes pour taskId={}", taskId);

            } catch (Exception e) {

                res.put("success", false);

                res.put("message", e.getMessage());

                log.error(">>> [RECYCLE TASK] Echec pour taskId={} : {}", taskId, e.getMessage());

            }

            results.add(res);

        }

        return results;
    }

    private String findContainerIdForTask(Long taskId, UserTaskServicesClient taskClient) {
        try {

            List<TaskSummary> tasks = taskClient.findTasksOwned(

                    "wbadmin", Arrays.asList("Ready", "Reserved", "InProgress", "Completed"), 0, 500);

            if (tasks != null) {

                Optional<TaskSummary> found = tasks.stream()

                        .filter(t -> t.getId().equals(taskId)).findFirst();

                if (found.isPresent()) return found.get().getContainerId();
            }
        } catch (Exception e) {
            log.warn("findContainerIdForTask taskId={} : {}", taskId, e.getMessage());
        }
        return null;
    }


    // ─── RECYCLAGE PROCESS INSTANCES ──────────────────────────────────────────



    public List<Map<String, Object>> recycleProcessInstances(List<Long> ids) {
        ProcessServicesClient pc = processClient();
        QueryServicesClient   qc = queryClient();
        if (pc == null || qc == null) throw new IllegalStateException("Client jBPM indisponible");

        List<Map<String, Object>> results = new ArrayList<>();
        for (Long oldId : ids) {
            results.add(recycleOneProcessInstance(oldId));
        }
        return results;
    }

    private Map<String, Object> recycleOneProcessInstance(Long oldId) {
        ProcessServicesClient pc = processClient();
        QueryServicesClient qc = queryClient();
        if (pc == null || qc == null) {
            return Map.of("oldInstanceId", oldId, "success", false, "message", "Client jBPM indisponible");
        }

        try {
            System.out.println(">>> [RECYCLE] Ancienne instance : " + oldId);
            ProcessInstance old = qc.findProcessInstanceById(oldId);
            if (old == null) {
                System.out.println(">>> [RECYCLE] Instance introuvable : " + oldId);
                return Map.of("oldInstanceId", oldId, "success", false, "message", "Instance introuvable");
            }
            System.out.println(">>> [RECYCLE] Container : " + old.getContainerId());
            System.out.println(">>> [RECYCLE] Process ID : " + old.getProcessId());

            NodeInstance activeNode = findActiveNode(oldId);
            if (activeNode != null && isSignalNode(activeNode)) {
                String signalName = activeNode.getName();
                Object signalPayload = resolveSignalPayload(signalName);
                pc.signalProcessInstance(old.getContainerId(), oldId, signalName, signalPayload);
                System.out.println(">>> [RECYCLE] Signal envoye : " + signalName + " | payload=" + signalPayload);
                return Map.of(
                        "oldInstanceId", oldId,
                        "success", true,
                        "message", "Signal envoye avec succes : " + signalName
                );
            }

            Map<String, Object> vars = pc.getProcessInstanceVariables(old.getContainerId(), oldId);
            if (vars == null) vars = new HashMap<>();
            Long newId = pc.startProcess(old.getContainerId(), old.getProcessId(), vars);
            System.out.println(">>> [RECYCLE] Nouvelle instance creee : " + newId);
            return Map.of("oldInstanceId", oldId, "newInstanceId", newId, "success", true, "message", "Nouvelle instance creee avec succes");
        } catch (Exception e) {
            System.err.println(">>> [RECYCLE] Echec pour l'instance " + oldId + " : " + e.getMessage());
            return Map.of("oldInstanceId", oldId, "success", false, "message", e.getMessage());
        }
    }

    private NodeInstance findActiveNode(Long processInstanceId) {
        QueryServicesClient qc = queryClient();
        if (qc == null) return null;

        try {
            List<NodeInstance> activeNodes = qc.findActiveNodeInstances(processInstanceId, 0, 50);
            if (activeNodes != null && !activeNodes.isEmpty()) {
                activeNodes.sort(Comparator.comparing(NodeInstance::getId).reversed());
                return activeNodes.get(0);
            }
        } catch (Exception e) {
            log.warn("findActiveNode instance {} : {}", processInstanceId, e.getMessage());
        }
        return null;
    }

    private boolean isSignalNode(NodeInstance node) {
        if (node == null || node.getName() == null) return false;
        return node.getName().toLowerCase(Locale.ROOT).contains("signal");
    }

    private Object resolveSignalPayload(String signalName) {
        if ("Signal_Donor_Received".equalsIgnoreCase(signalName)) {
            return "DONOR_ACCEPTED";
        }
        if ("Signal_Eligibilite_Validee".equalsIgnoreCase(signalName)) {
            return "ELIGIBILITY_OK";
        }
        if ("Signal_Decision".equalsIgnoreCase(signalName)) {
            return "REJECTED";
        }
        return "RECYCLE";
    }


    // ─── HELPER : charge toutes les instances IN + OUT ────────────────────────



    private void loadAllInstances(List<ProcessInstance> allIn, List<ProcessInstance> allOut) {

        QueryServicesClient qc = queryClient();

        if (qc == null) return;

        try {

            // findProcessInstances(page, size) — signature correcte jBPM 7.x

            List<ProcessInstance> batch = qc.findProcessInstances(0, 10000);

            if (batch == null) return;

            for (ProcessInstance pi : batch) {

                String cid = pi.getContainerId() != null ? pi.getContainerId() : "";

                if (cid.equals(containerIdIn))       allIn.add(pi);

                else if (cid.equals(containerIdOut)) allOut.add(pi);

            }

        } catch (Exception e) {

            log.warn("loadAllInstances : {}", e.getMessage());

        }

    }



    // ─── STATISTIQUES GENERALES ───────────────────────────────────────────────



    public Map<String, Object> getStatistiques() {

        List<ProcessInstance> allIn  = new ArrayList<>();

        List<ProcessInstance> allOut = new ArrayList<>();

        loadAllInstances(allIn, allOut);



        long totalIn  = allIn.size();

        long totalOut = allOut.size();

        Map<String, Object> statsIn  = computeStats(allIn);

        Map<String, Object> statsOut = computeStats(allOut);



        double rateIN  = totalIn  > 0 ? 100.0 * (long) statsIn.get("completed")  / totalIn  : 0;

        double rateOUT = totalOut > 0 ? 100.0 * (long) statsOut.get("completed") / totalOut : 0;



        Map<String, Object> result = new LinkedHashMap<>();

        result.put("PortabilityIN",  statsIn);

        result.put("PortabilityOUT", statsOut);

        result.put("successRateIN",  rateIN);

        result.put("successRateOUT", rateOUT);

        return result;

    }



    // ─── STATISTIQUES MENSUELLES ──────────────────────────────────────────────



    public List<Map<String, Object>> getMonthlyStatistiques() {

        long[] inByMonth  = new long[12];

        long[] outByMonth = new long[12];



        List<ProcessInstance> allIn  = new ArrayList<>();

        List<ProcessInstance> allOut = new ArrayList<>();

        loadAllInstances(allIn, allOut);



        for (ProcessInstance pi : allIn) {

            if (pi.getDate() == null) continue;

            Calendar cal = Calendar.getInstance();

            cal.setTime(pi.getDate());

            inByMonth[cal.get(Calendar.MONTH)]++;

        }

        for (ProcessInstance pi : allOut) {

            if (pi.getDate() == null) continue;

            Calendar cal = Calendar.getInstance();

            cal.setTime(pi.getDate());

            outByMonth[cal.get(Calendar.MONTH)]++;

        }



        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 0; i < 12; i++) {

            Map<String, Object> row = new LinkedHashMap<>();

            row.put("month",         MONTHS[i]);

            row.put("PortabilityIN",  inByMonth[i]);

            row.put("PortabilityOUT", outByMonth[i]);

            result.add(row);

        }

        return result;

    }



    // ─── INSTANCES PAR STATUT (drill-down) ───────────────────────────────────



    public List<Map<String, Object>> getInstancesByStatus(int status, String type, int page, int size) {

        // 1. Récupération de la liste de base (pool)

        List<ProcessInstance> allIn = new ArrayList<>();

        List<ProcessInstance> allOut = new ArrayList<>();

        loadAllInstances(allIn, allOut);



        List<ProcessInstance> pool = new ArrayList<>();

        if (type == null || type.trim().isEmpty()) {

            pool.addAll(allIn);

            pool.addAll(allOut);

        } else if ("IN".equalsIgnoreCase(type)) {

            pool = allIn;

        } else if ("OUT".equalsIgnoreCase(type)) {

            pool = allOut;

        }



        // 2. Traitement avec intégration du NodeName et NodeType

        QueryServicesClient qc = queryClient(); // On initialise le client une seule fois ici



        return pool.stream()

                .filter(pi -> pi.getState() == status)

                .skip((long) page * size)

                .limit(size)

                .map(pi -> {

                    Map<String, Object> map = new LinkedHashMap<>();



                    map.put("id", pi.getId());

                    map.put("processId", pi.getProcessId());

                    map.put("state", pi.getState());

                    map.put("startDate", pi.getDate() != null ? pi.getDate().toString() : "N/A");

                    map.put("type", containerIdIn.equals(pi.getContainerId()) ? "IN" : "OUT");



                    // ================= NODE TIME + STATUS =================

                    try {

                        List<NodeInstance> nodes = qc.findNodeInstances(pi.getId(), 0, 100);



                        if (nodes != null && !nodes.isEmpty()) {



                            nodes.sort(Comparator.comparing(NodeInstance::getId).reversed());

                            NodeInstance dernier = nodes.get(0);



                            map.put("nodeName", dernier.getName());

                            map.put("nodeType", dernier.getNodeType());



                            String duration = "N/A";

                            String statusTime = "N/A";



                            if (dernier.getDate() != null) {



                                Date nodeStartDate = dernier.getDate();



                                long diffMillis = new Date().getTime() - nodeStartDate.getTime();

                                long diffMinutes = diffMillis / (1000 * 60);

                                long diffHours = diffMinutes / 60;



                                duration = diffHours + "h " + (diffMinutes % 60) + "m";



                                map.put("timeNode", duration);



                                int allowedHours = getExpectedTime(dernier.getNodeType());



                                if (diffHours > allowedHours) {

                                    statusTime = "NOT OK";

                                } else {

                                    statusTime = "OK";

                                }

                            }



                            map.put("statusTime", statusTime);



                        } else {

                            map.put("nodeName", "N/A");

                            map.put("nodeType", "N/A");

                            map.put("timeNode", "N/A");

                            map.put("statusTime", "N/A");

                        }



                    } catch (Exception e) {

                        map.put("nodeName", "Erreur");

                        map.put("nodeType", "N/A");

                        map.put("timeNode", "N/A");

                        map.put("statusTime", "N/A");

                    }



                    return map;

                })

                .collect(Collectors.toList());

    }



    // ─── HELPERS ──────────────────────────────────────────────────────────────



    private Map<String, Object> computeStats(List<ProcessInstance> instances) {

        Map<String, Object> map = new LinkedHashMap<>();

        map.put("total",     (long) instances.size());

        map.put("completed", instances.stream().filter(p -> STATE_COMPLETED == p.getState()).count());

        map.put("aborted",   instances.stream().filter(p -> STATE_ABORTED   == p.getState()).count());

        map.put("active",    instances.stream().filter(p -> STATE_ACTIVE    == p.getState()).count());

        return map;

    }



    private Map<String, Object> buildEmptyStats() {
        Map<String, Object> empty = new LinkedHashMap<>();
        empty.put("total", 0L); empty.put("completed", 0L);
        empty.put("aborted", 0L); empty.put("active", 0L);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("PortabilityIN",  new LinkedHashMap<>(empty));
        result.put("PortabilityOUT", new LinkedHashMap<>(empty));
        result.put("successRateIN",  0.0);
        result.put("successRateOUT", 0.0);
        return result;
    }

    private List<Map<String, Object>> getBlockedProcessCandidates(String containerId, Long processInstanceId,
                                                                  String dateDebut, String dateFin) {
        return getInstancesByStatus(STATE_ACTIVE, null, 0, 1000).stream()
                .filter(this::isRecyclableActiveProcess)
                .filter(p -> {
                    Long pid = toLong(p.get("id"));
                    if (processInstanceId != null && !processInstanceId.equals(pid)) return false;
                    if (containerId != null && !containerId.trim().isEmpty()) {
                        String type = String.valueOf(p.get("type"));
                        if ("IN".equalsIgnoreCase(type) && !containerId.equals(containerIdIn)) return false;
                        if ("OUT".equalsIgnoreCase(type) && !containerId.equals(containerIdOut)) return false;
                    }
                    Object startDate = p.get("startDate");
                    if (startDate != null) {
                        String td = String.valueOf(startDate);
                        if (dateDebut != null && !dateDebut.trim().isEmpty() && td.compareTo(dateDebut) < 0) return false;
                        if (dateFin   != null && !dateFin.trim().isEmpty()   && td.compareTo(dateFin)   > 0) return false;
                    }
                    return true;
                })
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", p.get("id"));
                    map.put("name", p.get("nodeName"));
                    map.put("processInstanceId", p.get("id"));
                    map.put("containerId", "IN".equalsIgnoreCase(String.valueOf(p.get("type"))) ? containerIdIn : containerIdOut);
                    map.put("status", p.get("statusTime"));
                    map.put("createdOn", p.get("startDate"));
                    map.put("itemType", "PROCESS");
                    return map;
                })
                .collect(Collectors.toList());
    }

    private boolean isRecyclableActiveProcess(Map<String, Object> process) {
        String nodeName = String.valueOf(process.get("nodeName"));
        return nodeName != null
                && !nodeName.trim().isEmpty()
                && !"N/A".equalsIgnoreCase(nodeName)
                && !"Erreur".equalsIgnoreCase(nodeName);
    }

    private boolean matchesProcessFilters(ProcessInstance instance, Integer status, String crmId,
                                          String msisdn, String phoneNumber, String contractCode,
                                          String dateDebut, String dateFin) {
        if (instance == null) return false;
        if (status != null && !Objects.equals(instance.getState(), status)) return false;
        if (instance.getDate() != null) {
            String d = instance.getDate().toString();
            if (dateDebut != null && !dateDebut.trim().isEmpty() && d.compareTo(dateDebut) < 0) return false;
            if (dateFin   != null && !dateFin.trim().isEmpty()   && d.compareTo(dateFin)   > 0) return false;
        }

        Map<String, Object> variables = instance.getVariables() != null ? instance.getVariables() : Collections.emptyMap();
        if (!matchesAnyVariable(variables, crmId, "crmId", "idcrm", "idCrm", "crmID", "refCrm")) return false;
        if (!matchesAnyVariable(variables, contractCode, "contractCode", "contractType", "contract")) return false;
        if (!matchesPhoneVariable(variables, msisdn, true)) return false;
        return matchesPhoneVariable(variables, phoneNumber, false);
    }

    private boolean matchesAnyVariable(Map<String, Object> variables, String expected, String... keys) {
        if (expected == null || expected.trim().isEmpty()) return true;
        String needle = normalizeForSearch(expected);
        for (String key : keys) {
            Object value = variables.get(key);
            if (value != null && normalizeForSearch(value).contains(needle)) return true;
        }
        return false;
    }

    private boolean matchesPhoneVariable(Map<String, Object> variables, String expected, boolean preferMsisdn) {
        if (expected == null || expected.trim().isEmpty()) return true;
        String needle = normalizePhone(expected);
        List<String> keys = preferMsisdn
                ? Arrays.asList("msisdn", "phoneNumber", "numeroOrange")
                : Arrays.asList("phoneNumber", "msisdn", "numeroOrange");
        for (String key : keys) {
            Object value = variables.get(key);
            if (value == null) continue;
            String candidate = normalizePhone(value);
            if (candidate.contains(needle) || candidate.contains(strip216(needle)) || ("216" + candidate).contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeForSearch(Object value) {
        return String.valueOf(value).trim().toLowerCase(Locale.ROOT);
    }

    private String normalizePhone(Object value) {
        return String.valueOf(value).replaceAll("[^0-9]", "");
    }

    private String strip216(String value) {
        return value != null && value.startsWith("216") ? value.substring(3) : value;
    }

    private Long toLong(Object value) {
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return value != null ? Long.parseLong(String.valueOf(value)) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private ProcessInstance enrichWithVariables(ProcessInstance instance) {
        if (instance == null) return null;

        if (!loadVariables) { instance.setVariables(Collections.emptyMap()); return instance; }

        if (loadVariablesOnlyActive && !Integer.valueOf(STATE_ACTIVE).equals(instance.getState())) {

            instance.setVariables(Collections.emptyMap()); return instance;

        }

        ProcessServicesClient pc = processClient();

        if (pc == null) { instance.setVariables(Collections.emptyMap()); return instance; }

        try {

            Map<String, Object> vars = pc.getProcessInstanceVariables(instance.getContainerId(), instance.getId());

            instance.setVariables(vars != null ? vars : new HashMap<>());

        } catch (Exception e) {

            instance.setVariables(new HashMap<>());

            log.warn("Variables indisponibles instance {} ({}, etat {}) : {}",

                    instance.getId(), instance.getContainerId(), instance.getState(), shortenMessage(e.getMessage()));

        }

        return instance;

    }



    private static String shortenMessage(String msg) {

        if (msg == null) return "erreur jBPM";

        return msg.length() <= 120 ? msg : msg.substring(0, 120) + "...";

    }



    private List<Integer> getTargetStatuses(Integer status) {

        return status != null ? Arrays.asList(status) : Arrays.asList(1, 2, 3);

    }







    // ─── NODE INSTANCE LOG (pour vision temps réel) ─────────────────────────────



    public List<Map<String, Object>> getNodeInstanceLog(Long processInstanceId) {

        QueryServicesClient qc = queryClient();

        if (qc == null) {

            log.error("QueryServicesClient indisponible pour getNodeInstanceLog");

            return new ArrayList<>();

        }



        try {

            // Récupérer les node instances actives pour ce process instance

            Object nodesObj = qc.findActiveNodeInstances(processInstanceId, 0, 100);

            List<?> nodes = (List<?>) nodesObj;



            if (nodes == null || nodes.isEmpty()) {

                // Si pas de nodes actifs, essayer de récupérer les nodes complétés

                nodesObj = qc.findCompletedNodeInstances(processInstanceId, 0, 100);

                nodes = (List<?>) nodesObj;

            }



            if (nodes == null) return new ArrayList<>();



            List<Map<String, Object>> result = new ArrayList<>();

            for (Object nodeObj : nodes) {

                Map<String, Object> map = new LinkedHashMap<>();



                // Utiliser réflexion pour accéder aux propriétés

                try {

                    java.lang.reflect.Method getNodeId = nodeObj.getClass().getMethod("getNodeId");

                    map.put("nodeId", getNodeId.invoke(nodeObj));



                    java.lang.reflect.Method getName = nodeObj.getClass().getMethod("getName");

                    map.put("nodeName", getName.invoke(nodeObj));



                    java.lang.reflect.Method getNodeType = nodeObj.getClass().getMethod("getNodeType");

                    map.put("nodeType", getNodeType.invoke(nodeObj));



                    java.lang.reflect.Method getProcessInstanceId = nodeObj.getClass().getMethod("getProcessInstanceId");

                    map.put("processInstanceId", getProcessInstanceId.invoke(nodeObj));



                    java.lang.reflect.Method getDate = nodeObj.getClass().getMethod("getDate");

                    Object dateObj = getDate.invoke(nodeObj);

                    map.put("entryTime", dateObj != null ? dateObj.toString() : "N/A");



                    // Calculer la durée

                    String duration = "N/A";

                    if (dateObj != null) {

                        long diff = new Date().getTime() - ((java.util.Date) dateObj).getTime();

                        long seconds = diff / 1000;

                        long minutes = seconds / 60;

                        long hours = minutes / 60;



                        if (hours > 0) {

                            duration = String.format("%dh %dm", hours, minutes % 60);

                        } else if (minutes > 0) {

                            duration = String.format("%dm %ds", minutes, seconds % 60);

                        } else {

                            duration = String.format("%ds", seconds);

                        }

                    }

                    map.put("duration", duration);

                    map.put("status", "ACTIVE");



                } catch (Exception e) {

                    log.warn("Erreur réflexion sur node: {}", e.getMessage());

                    continue;

                }



                result.add(map);

            }



            return result;



        } catch (Exception e) {

            log.error("Erreur lors de la récupération des NodeInstanceLog pour instance {} : {}",

                    processInstanceId, e.getMessage());

            return new ArrayList<>();

        }

    }







    public String getDerniereEtape(Long processInstanceId) {

        QueryServicesClient qc = queryClient();

        if (qc == null) return "Indisponible";



        try {

            // 1. On utilise NodeInstance (type correct)

            List<NodeInstance> nodes = qc.findNodeInstances(processInstanceId, 0, 100);



            if (nodes != null && !nodes.isEmpty()) {

                // 2. Tri par ID (le plus grand ID est le nœud le plus récent)

                nodes.sort(Comparator.comparing(NodeInstance::getId).reversed());



                // 3. Retourne le nom

                return nodes.get(0).getName();

            }

        } catch (Exception e) {

            log.error("Erreur lors de la lecture des étapes pour l'instance {}: {}", processInstanceId, e.getMessage());

        }

        return "En cours...";

    }

    // Dans MonitoringService.java



    public Map<String, String> getDiagnosticDetails(Long processInstanceId) {
        Map<String, String> details = new HashMap<>();
        QueryServicesClient qc = queryClient();


        List<NodeInstance> nodes = qc.findNodeInstances(processInstanceId, 0, 100);



        if (nodes != null && !nodes.isEmpty()) {

            nodes.sort(Comparator.comparing(NodeInstance::getId).reversed());

            NodeInstance dernierNode = nodes.get(0);



            details.put("nodeName", dernierNode.getName());

            details.put("nodeType", dernierNode.getNodeType());

        } else {

            details.put("nodeName", "N/A");

            details.put("nodeType", "N/A");

        }

        return details;
    }


    private int getExpectedTime(String nodeType) {


        if (nodeType == null) return 0;



        switch (nodeType) {



            case "StartNode":

            case "EndNode":

            case "Split":

            case "Join":

                return 0;



            case "ActionNode":

                return 1;



            case "WorkItemNode":

                return 2;



            case "EventNode":

                return 24;



            case "HumanTaskNode":

                return 24;



            default:

                return 0;

        }

    }


    private String statusLabel(Integer state) {
        if (state == null) return "UNKNOWN";
        switch (state) {
            case STATE_ACTIVE:
                return "ACTIVE";
            case STATE_COMPLETED:
                return "COMPLETED";
            case STATE_ABORTED:
                return "ABORTED";
            default:
                return "UNKNOWN";
        }
    }


    public List<Map<String, Object>> getSeasonalTrend() {



        int[] totals = new int[12];



        List<ProcessInstance> allIn = new ArrayList<>();

        List<ProcessInstance> allOut = new ArrayList<>();



        loadAllInstances(allIn, allOut);



        for(ProcessInstance pi : allIn){



            if(pi.getDate()==null) continue;



            Calendar cal=Calendar.getInstance();

            cal.setTime(pi.getDate());



            totals[cal.get(Calendar.MONTH)]++;

        }



        for(ProcessInstance pi : allOut){



            if(pi.getDate()==null) continue;



            Calendar cal=Calendar.getInstance();

            cal.setTime(pi.getDate());



            totals[cal.get(Calendar.MONTH)]++;

        }



        List<Map<String,Object>> result=new ArrayList<>();



        for(int i=0;i<12;i++){



            Map<String,Object> row=new LinkedHashMap<>();



            row.put("month",MONTHS[i]);

            row.put("total",totals[i]);



            result.add(row);

        }



        return result;

    }



    public List<Map<String, Object>> getLifecycle(Long processInstanceId) {



        QueryServicesClient qc = queryClient();



        List<Map<String, Object>> result = new ArrayList<>();



        try {



            // Tous les nœuds du processus

            List<NodeInstance> nodes =

                    qc.findNodeInstances(processInstanceId,0,500);



            if(nodes == null || nodes.isEmpty()){

                return result;

            }



            // Nœud actuellement actif

            List<NodeInstance> activeNodes =

                    qc.findActiveNodeInstances(processInstanceId,0,50);

            System.out.println("===== ACTIVE NODES =====");



            for(NodeInstance n : activeNodes){

                System.out.println("ID = " + n.getId());

                System.out.println("NAME = " + n.getName());

            }



            Long activeId = null;



            if(activeNodes != null && !activeNodes.isEmpty()){



                activeId = activeNodes.get(0).getId();



            }



            // Trier dans l'ordre d'exécution

            nodes.sort(Comparator.comparing(NodeInstance::getId));



            Set<String> alreadyAdded = new HashSet<>();



            for(NodeInstance node : nodes){



                String businessStep = getBusinessStep(node.getName());



                if(businessStep == null)

                    continue;



                // éviter les doublons

                if(alreadyAdded.contains(businessStep))

                    continue;



                alreadyAdded.add(businessStep);



                Map<String,Object> map = new LinkedHashMap<>();



                map.put("step",businessStep);



                map.put("start",

                        node.getDate()==null ?

                                "-" :

                                node.getDate().toString());



                map.put("end","-");



                map.put("duration","-");



                if(activeId != null && node.getId().equals(activeId)){



                    map.put("status","ACTIVE");



                }else{



                    map.put("status","COMPLETED");



                }



                result.add(map);



            }



        }catch(Exception e){



            e.printStackTrace();



        }



        return result;



    }



    private String getBusinessStep(String node){



        if(node == null)

            return null;



        switch(node){



            case "NP_Create":

                return "Création de la demande";



            case "Check Eligibility":

                return "Vérification de l'éligibilité";



            case "Update CRM":

                return "Mise à jour CRM";



            case "Signal_Donor_Received":

                return "Réception de la réponse Donor";



            case "Check 4":

                return "Traitement de la réponse";



            default:

                return null;

        }



    }



    public void envoyerSmsEtat(Long processId,String numero){



        QueryServicesClient qc=queryClient();



        ProcessInstance pi=qc.findProcessInstanceById(processId);



        if(pi==null)

            return;



        if(pi.getState()==2){



            smsService.sendSMS(



                    numero,



                    "Bonjour, votre demande de portabilité est terminée avec succès. Votre numéro est maintenant actif chez Orange Tunisie."



            );



        }



        else if(pi.getState()==1){



            smsService.sendSMS(



                    numero,



                    "Votre demande de portabilité est toujours en cours de traitement. Nous vous informerons dès sa finalisation."



            );



        }



    }

}





