package tn.esprit.npservicemodule.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import tn.esprit.npservicemodule.dto.NPResponseDto;
import tn.esprit.npservicemodule.dto.ProcessMessageRequestDto;
import tn.esprit.npservicemodule.service.NPServiceClient;
import tn.esprit.npservicemodule.service.WorkflowService;

@RestController
@RequestMapping("/np")
public class NPController {


    private final NPServiceClient serviceClient;
    private final WorkflowService workflowService;
    public NPController(NPServiceClient serviceClient, WorkflowService workflowService) {
        this.serviceClient = serviceClient;
        this.workflowService = workflowService;
    }
    private static final Logger logger = LoggerFactory.getLogger(NPController.class);
    @PostMapping("/process")
    // On remplace le DTO par un simple String !
    public NPResponseDto process(@RequestBody String msisdn) {

        // Et on donne directement le String a service client
        return serviceClient.processMessage(msisdn);
    }




    @PostMapping("/signal-donor/{instanceId}")
    public String triggerSignal(@PathVariable Long instanceId) {
        workflowService.sendDonorSignal(instanceId);
        return "Signal envoyé au processus " + instanceId;
    }
}