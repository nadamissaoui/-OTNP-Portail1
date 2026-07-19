package tn.esprit.npservicemodule.config;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class CorrelationFilter implements Filter {
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String MDC_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        // On récupère l'ID envoyé par jBPM
        String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);

        if (correlationId != null) {
            // On le "colle" au contexte de log actuel
            MDC.put(MDC_KEY, correlationId);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            // Très important : on nettoie après l'appel
            MDC.remove(MDC_KEY);
        }
    }
}
