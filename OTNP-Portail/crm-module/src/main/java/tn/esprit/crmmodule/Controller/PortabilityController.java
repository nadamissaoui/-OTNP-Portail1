package tn.esprit.crmmodule.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.crmmodule.dto.*;
import tn.esprit.crmmodule.service.PortabilityService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/crm")
public class PortabilityController {

    private final PortabilityService portabilityService;

    public PortabilityController(PortabilityService portabilityService) {
        this.portabilityService = portabilityService;
    }
    private static final Logger logger = LoggerFactory.getLogger(PortabilityController.class);

    @PostMapping("/create-b2b")
    public CreatePortabilityB2BResponseDto createPortabilityB2B(
            @RequestBody CreatePortabilityB2BRequestDto request) {
        return portabilityService.createPortability(request);
    }


    @PostMapping("/create-gp")
    public CreatePortabilityGPResponseDto createPortabilityGP(
            @RequestBody CreatePortabilityGPRequestDto request) {
        return portabilityService.createPortabilityGP(request);
    }


    @PostMapping("/print-portability")
    public PrintPortabilityResponseDto printPortability(
            @RequestBody PrintPortabilityRequestDto request) {
        return portabilityService.printPortability(request);
    }


    @PostMapping("/search-portability-gp")
    public SearchPortabilityGPResponseDto searchPortabilityGP(@RequestBody SearchPortabilityGPRequestDto dto) {
        return portabilityService.searchPortabilityGP(dto);
    }


    @PostMapping("/update-portability-status")
    public ResponseEntity<UpdatePortabilityStatusResponseDto> updatePortabilityStatus(
            @RequestBody UpdatePortabilityStatusRequestDto requestDto) {
        UpdatePortabilityStatusResponseDto response = portabilityService.updatePortabilityStatus(requestDto);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/cancel-portability")
    public ResponseEntity<CancelPortabilityResponseDto> cancelPortability(
            @RequestBody CancelPortabilityRequestDto requestDto) {

        CancelPortabilityResponseDto response =
                portabilityService.cancelPortability(requestDto);

        return ResponseEntity.ok(response);
    }


    }