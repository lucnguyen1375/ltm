
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import java.io.StringReader;

public class WS_CHARACTER_1 {

    static final String URL =
        "http://36.50.135.242:2221/CharacterService";

    static final String STUDENT_CODE = "B23DCCN516";
    static final String Q_CODE = "GmHUBR9h";

    // Kiem tra ten tham so trong WSDL/XSD
    static final String PARAM_1 = "studentCode";
    static final String PARAM_2 = "qCode";
    static final String PARAM_3 = "data";

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
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException("Khong lay duoc WSDL");
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
            "xmlns:ns=\"" + escapeXML(namespace) + "\">" +
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
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        Document doc = parseXML(response.body());

        if (response.statusCode() != 200 ||
            doc.getElementsByTagNameNS("*", "Fault").getLength() > 0) {
            throw new RuntimeException(response.body());
        }

        return response.body();
    }

    // ========== 2. XU LY LOGIC ==========

    // Doc danh sach chuoi tu SOAP
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

    // Dem so nguyen am
    static int countVowels(String word) {
        int count = 0;

        word = word.toLowerCase(Locale.ROOT);

        for (char c : word.toCharArray()) {
            if ("aeiou".indexOf(c) != -1) {
                count++;
            }
        }

        return count;
    }

    // Phan nhom va sap xep
    static List<String> processData(List<String> data) {

        Map<Integer, List<String>> groups =
            new TreeMap<>();

        // Nhom cac tu theo so nguyen am
        for (String word : data) {
            int count = countVowels(word);

            groups.computeIfAbsent(
                count, k -> new ArrayList<>()
            ).add(word);
        }

        List<String> result = new ArrayList<>();

        // Sap xep cac tu trong tung nhom
        for (List<String> group : groups.values()) {
            Collections.sort(group);
            result.addAll(group);
        }

        return result;
    }

    // ========== 3. HAM MAIN ==========

    public static void main(String[] args) {
        try {
            String namespace = getNamespace();

            // a. Nhan danh sach chuoi
            String response = callSOAP(
                namespace,
                "requestStringArray",
                new String[]{PARAM_1, PARAM_2},
                new String[]{STUDENT_CODE, Q_CODE}
            );

            List<String> data = parseData(response);

            System.out.println("Data: " + data);

            // b. Phan nhom va sap xep
            List<String> result = processData(data);

            System.out.println("Result: " + result);
            System.out.println("So phan tu gui: " + result.size());
            for (int i = 0; i < result.size(); i++) {
                System.out.println("data[" + i + "] = \"" + result.get(i) + "\"");
            }

            // c. Gui danh sach ve server
            String[] names = new String[result.size() + 2];
            String[] values = new String[result.size() + 2];

            names[0] = PARAM_1;
            names[1] = PARAM_2;
            values[0] = STUDENT_CODE;
            values[1] = Q_CODE;

            for (int i = 0; i < result.size(); i++) {
                names[i + 2] = PARAM_3;
                values[i + 2] = result.get(i);
            }

            String submitResponse = callSOAP(
                namespace,
                "submitCharacterStringArray",
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
