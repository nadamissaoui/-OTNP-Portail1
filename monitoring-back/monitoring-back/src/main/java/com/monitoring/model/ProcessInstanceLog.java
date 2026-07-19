package com.monitoring.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "processinstancelog", schema = "jbpm") // Mappe directement ta table MySQL jbpm
public class ProcessInstanceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "processInstanceId")
    private Long processInstanceId;

    @Column(name = "processId")
    private String processId;

    @Column(name = "processName")
    private String processName;

    @Column(name = "processVersion")
    private String processVersion;

    private Integer status; // 1 = En cours, 3 = Terminé, etc.

    @Column(name = "start_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;

    @Column(name = "end_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    private Long duration;

    @Column(name = "user_identity")
    private String userIdentity;

    // 🔽 Ajoute tes Getters et Setters (ou utilise @Data de Lombok)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProcessInstanceId() { return processInstanceId; }
    public void setProcessInstanceId(Long processInstanceId) { this.processInstanceId = processInstanceId; }
    public String getProcessId() { return processId; }
    public void setProcessId(String processId) { this.processId = processId; }
    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
}
