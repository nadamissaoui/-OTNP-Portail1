package tn.esprit.crmmodule.util;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class XmlParser {

    // Parse une chaîne XML en Document
    public Document parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    // Récupère la valeur d'une balise dans un namespace donné
    public String getTagValueNS(Document doc, String namespace, String tagName) {
        NodeList nodeList = doc.getElementsByTagNameNS(namespace, tagName);
        return nodeList.getLength() > 0 ? nodeList.item(0).getTextContent() : null;
    }

    // Pour Element (sous-noeud)
    public String getTagValueNS(Element element, String namespace, String tagName) {
        NodeList nodeList = element.getElementsByTagNameNS(namespace, tagName);
        return nodeList.getLength() > 0 ? nodeList.item(0).getTextContent() : null;
    }

    // Version sans namespace (utile pour CancelPortability)
    public String getTagValue(Document doc, String tagName) {
        NodeList nodeList = doc.getElementsByTagNameNS("*", tagName);
        return nodeList.getLength() > 0 ? nodeList.item(0).getTextContent() : null;
    }
}
