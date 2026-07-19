package tn.esprit.otnp_ws1.config;

import tn.esprit.otnp_ws1.service.OTNPService;
import jakarta.xml.ws.Endpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebServiceConfig {

    private final Bus bus;
    private final OTNPService otnpService;

    // On injecte le service ici pour éviter l'erreur de constructeur
    public WebServiceConfig(Bus bus, OTNPService otnpService) {
        this.bus = bus;
        this.otnpService = otnpService;
    }

    @Bean
    public Endpoint endpoint() {
        // Utilisation du service injecté
        EndpointImpl endpoint = new EndpointImpl(bus, otnpService);
        endpoint.publish("/OTNPService");
        return endpoint;
    }
}