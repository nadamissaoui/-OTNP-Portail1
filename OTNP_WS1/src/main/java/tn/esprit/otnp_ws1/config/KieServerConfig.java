package tn.esprit.otnp_ws1.config;

import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class KieServerConfig {

    @Value("${jbpm.server.url:http://localhost:8080/kie-server/services/rest/server}")
    private String url;

    @Value("${jbpm.server.user:wbadmin}")
    private String user;

    @Value("${jbpm.server.password:wbadmin}")
    private String password;

    @Bean
    @Lazy
    public KieServicesClient kieServicesClient() {
        try {
            KieServicesConfiguration config = KieServicesFactory.newRestConfiguration(url, user, password);
            config.setTimeout(60000L);
            config.setMarshallingFormat(MarshallingFormat.JSON);
            return KieServicesFactory.newKieServicesClient(config);
        } catch (Exception e) {
            System.err.println("Erreur d'initialisation du client KIE Server : " + e.getMessage());
            throw new IllegalStateException("Impossible de se connecter au KIE Server : " + url, e);
        }
    }

    @Bean
    @Lazy
    public ProcessServicesClient processServicesClient(KieServicesClient kieServicesClient) {
        return kieServicesClient.getServicesClient(ProcessServicesClient.class);
    }

    @Bean
    @Lazy
    public QueryServicesClient queryServicesClient(KieServicesClient kieServicesClient) {
        return kieServicesClient.getServicesClient(QueryServicesClient.class);
    }
}