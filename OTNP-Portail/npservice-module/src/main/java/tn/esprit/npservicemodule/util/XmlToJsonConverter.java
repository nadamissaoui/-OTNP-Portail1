package tn.esprit.npservicemodule.util;

import org.json.JSONObject;
import org.json.XML;

public class XmlToJsonConverter {

    /**
     * Convertit une chaîne XML en JSONObject.
     *
     * @param xml la chaîne XML à convertir
     * @return JSONObject correspondant
     */
    public static JSONObject convert(String xml) {
        if (xml == null || xml.isEmpty()) {
            return new JSONObject();
        }

        // Nettoyer les namespaces pour éviter les erreurs de parsing
        xml = xml.replaceAll("(?i)soapenv:", "")
                .replaceAll("(?i)tns:", "")
                .replaceAll("(?i)xmlns(:[a-z]+)?=\"[^\"]*\"", "");

        try {
            return XML.toJSONObject(xml);
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }
}