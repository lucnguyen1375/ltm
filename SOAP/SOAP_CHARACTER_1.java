package SOAP;


import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import java.io.StringReader;

public class SOAP_CHARACTER_1 {

    static final String URL =
        "http://36.50.135.242:2221/CharacterService";

    static final String STUDENT_CODE = "B23DCCN516";
    static final String Q_CODE = "JyBpN6w3";

    static HttpClient client = HttpClient.newHttpClient();

    // ========== 1. XU LY KET NOI ==========

    static Document parseXML(String xml) throws Exception {
        DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);
        factory.setFeature(
            "http://apache.org/xml/features/disallow-doctype-decl",
            true
        );

        return factory.newDocumentBuilder().parse(
            new InputSource(new StringReader(xml))
        );
    }

    static String getNamespace() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(URL + "?wsdl"))
            .GET()
            .build();

        HttpResponse<String> response = client.send(
            request, HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException(
                "Khong lay duoc WSDL: " + response.statusCode()
            );
        }

        Document doc = parseXML(response.body());

        return doc.getDocumentElement()
            .getAttribute("targetNamespace");
    }

    static String escapeXML(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    static String callSOAP(
        String namespace,
        String method,
        String[] names,
        String[] values
    ) throws Exception {

        StringBuilder params = new StringBuilder();

        for (int i = 0; i < names.length; i++) {
            params.append("<").append(names[i]).append(">")
                .append(escapeXML(values[i]))
                .append("</").append(names[i]).append(">");
        }

        String xml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<soap:Envelope " +
            "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
            "xmlns:ns=\"" + namespace + "\">" +
            "<soap:Body>" +
            "<ns:" + method + ">" +
            params +
            "</ns:" + method + ">" +
            "</soap:Body></soap:Envelope>";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(URL))
            .header("Content-Type", "text/xml; charset=utf-8")
            .header("SOAPAction", "\"\"")
            .POST(HttpRequest.BodyPublishers.ofString(
                xml, StandardCharsets.UTF_8
            ))
            .build();

        HttpResponse<String> response = client.send(
            request, HttpResponse.BodyHandlers.ofString()
        );

        Document doc = parseXML(response.body());

        if (response.statusCode() != 200 ||
            doc.getElementsByTagNameNS("*", "Fault").getLength() > 0) {
            throw new RuntimeException(response.body());
        }

        return response.body();
    }

    // ========== 2. XU LY LOGIC ==========

    // Lay danh sach chuoi tu SOAP response
    static List<String> parseData(String xml) throws Exception {
        Document doc = parseXML(xml);

        NodeList nodes =
            doc.getElementsByTagNameNS("*", "return");

        if (nodes.getLength() == 0) {
            throw new RuntimeException(
                "Khong tim thay return: " + xml
            );
        }

        List<String> data = new ArrayList<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);

            NodeList children = element.getChildNodes();
            boolean hasElements = false;

            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);

                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    hasElements = true;
                    data.add(child.getTextContent());
                }
            }

            if (!hasElements) {
                data.add(element.getTextContent());
            }
        }

        return data;
    }

    // Che thong tin nhay cam
    static String maskLog(String log) {

        log = log.replaceAll(
            "(?i)(email=)[^\\s]+",
            "$1[EMAIL]"
        );

        log = log.replaceAll(
            "(?i)(phone=)[^\\s]+",
            "$1[PHONE]"
        );

        log = log.replaceAll(
            "(?i)(token=)[^\\s]+",
            "$1[TOKEN]"
        );

        return log;
    }

    // Lay do uu tien ERROR -> WARN -> INFO
    static int getPriority(String log) {
        if (log.startsWith("ERROR")) return 0;
        if (log.startsWith("WARN")) return 1;
        if (log.startsWith("INFO")) return 2;
        return 3;
    }

    // Xu ly danh sach
    static List<String> processData(List<String> data) {

        List<String> result = new ArrayList<>();

        for (String log : data) {
            result.add(maskLog(log));
        }

        result.sort(
            Comparator.comparingInt(SOAP_CHARACTER_1::getPriority)
        );

        return result;
    }

    // ========== 3. HAM MAIN ==========

    public static void main(String[] args) {
        try {
            String namespace = getNamespace();

            // a. Nhan danh sach log
            String response = callSOAP(
                namespace,
                "requestStringArray",
                new String[]{"studentCode", "qCode"},
                new String[]{STUDENT_CODE, Q_CODE}
            );

            List<String> data = parseData(response);
            System.out.println("Data: " + data);

            // b. Che thong tin va sap xep
            List<String> result = processData(data);
            System.out.println("Result: " + result);

            // c. Gui danh sach ve server
            String[] names = new String[result.size() + 2];
            String[] values = new String[result.size() + 2];

            names[0] = "studentCode";
            names[1] = "qCode";
            values[0] = STUDENT_CODE;
            values[1] = Q_CODE;

            for (int i = 0; i < result.size(); i++) {
                names[i + 2] = "data";
                values[i + 2] = result.get(i);
            }

            String submitResponse = callSOAP(
                namespace,
                "submitStringArray",
                names,
                values
            );

            System.out.println("Da gui ket qua!");
            System.out.println("Server: " + submitResponse);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
