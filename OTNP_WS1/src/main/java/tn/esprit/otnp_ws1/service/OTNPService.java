package tn.esprit.otnp_ws1.service;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import tn.esprit.otnp_ws1.model.Client;

@WebService
public interface OTNPService {

    @WebMethod
    @WebResult(name = "processId")
    Long startPortability(
            @WebParam(name = "msisdn") String msisdn,
            @WebParam(name = "rioCode") String rioCode,
            @WebParam(name = "client") Client client
    );

    // ── NOUVEAU : démarrer un process OUT ────────────────────
    @WebMethod
    @WebResult(name = "processId")
    Long startPortabilityOut(
            @WebParam(name = "msisdn") String msisdn,
            @WebParam(name = "rioCode") String rioCode
    );

    @WebMethod
    @WebResult(name = "success")
    boolean sendDecision(
            @WebParam(name = "instanceId") Long instanceId,
            @WebParam(name = "status") String status
    );
}