package com.monitoring.service;

import com.monitoring.repository.ProcessInstanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PredictionService {

    private static final Logger log = LoggerFactory.getLogger(PredictionService.class);
    private static final String[] MONTHS =
        {"Janvier","Fevrier","Mars","Avril","Mai","Juin","Juillet","Aout","Septembre","Octobre","Novembre","Decembre"};

    private final ProcessInstanceRepository processInstanceRepository;

    public PredictionService(ProcessInstanceRepository processInstanceRepository) {
        this.processInstanceRepository = processInstanceRepository;
    }

    public Map<String, Object> predict() {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            long[] inByMonth  = new long[12];
            long[] outByMonth = new long[12];

            List<Object[]> rows = processInstanceRepository.countByMonthWithProcessId();
            if (rows == null || rows.isEmpty()) {
                return fallback("Aucune instance trouv\u00e9e");
            }

            for (Object[] row : rows) {
                int month   = ((Number) row[0]).intValue() - 1;
                String pid  = row[1] != null ? row[1].toString().toUpperCase() : "";
                long count  = ((Number) row[2]).longValue();
                if (pid.contains("IN"))   inByMonth[month] += count;
                else if (pid.contains("OUT")) outByMonth[month] += count;
                else inByMonth[month] += count;
            }

            List<Double> inVals  = new ArrayList<>();
            List<Double> outVals = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                if (inByMonth[i] > 0 || outByMonth[i] > 0) {
                    inVals.add((double) inByMonth[i]);
                    outVals.add((double) outByMonth[i]);
                }
            }

            if (inVals.size() < 2) {
                return fallback("Seulement " + inVals.size() + " mois avec donn\u00e9es (< 2 requis)");
            }

            double nextIn  = linearReg(inVals);
            double nextOut = linearReg(outVals);

            int nextMonthIdx = Calendar.getInstance().get(Calendar.MONTH);
            double lastTotal = inVals.get(inVals.size()-1) + outVals.get(outVals.size()-1);
            double predTotal = nextIn + nextOut;

            result.put("month", MONTHS[nextMonthIdx]);
            result.put("predictedIN",  Math.round(nextIn));
            result.put("predictedOUT", Math.round(nextOut));
            result.put("totalPredicted", Math.round(predTotal));
            result.put("trend", predTotal > lastTotal ? "up" : "down");
            result.put("lastMonthIN",  inVals.get(inVals.size()-1));
            result.put("lastMonthOUT", outVals.get(outVals.size()-1));
        } catch (Exception e) {
            log.warn("Erreur prediction: {}", e.getMessage(), e);
            return fallback("Erreur: " + e.getMessage());
        }
        return result;
    }

    private double linearReg(List<Double> values) {
        int n = values.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += values.get(i);
            sumXY += i * values.get(i);
            sumX2 += i * i;
        }
        double denom = n * sumX2 - sumX * sumX;
        if (Math.abs(denom) < 1e-10) return values.get(n - 1);
        double slope = (n * sumXY - sumX * sumY) / denom;
        double intercept = (sumY - slope * sumX) / n;
        return Math.max(0, slope * n + intercept);
    }

    private Map<String, Object> fallback(String reason) {
        log.info("Prediction fallback: {}", reason);
        Map<String, Object> fb = new LinkedHashMap<>();
        fb.put("month", "-");
        fb.put("predictedIN", 0);
        fb.put("predictedOUT", 0);
        fb.put("totalPredicted", 0);
        fb.put("trend", "stable");
        fb.put("lastMonthIN", 0);
        fb.put("lastMonthOUT", 0);
        fb.put("reason", reason);
        return fb;
    }
}
