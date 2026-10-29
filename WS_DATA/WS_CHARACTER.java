package WS_DATA;


import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import java.io.StringReader;

public class WS_CHARACTER {

    static final String URL =
        "http://36.50.135.242:2221/CharacterService";

    static final String STUDENT_CODE = "B23DCCN516";
    static final String Q_CODE = "bmAqn5Cy";

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

    // Chuyen SOAP XML thanh danh sach ma ky tu
    static List<Integer> parseData(String xml) throws Exception {
        Document doc = parseXML(xml);

        NodeList nodes =
            doc.getElementsByTagNameNS("*", "return");

        if (nodes.getLength() == 0) {
            throw new RuntimeException(
                "Khong tim thay return: " + xml
            );
        }

        List<Integer> data = new ArrayList<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);

            NodeList children = element.getChildNodes();
            boolean hasElements = false;

            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);

                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    hasElements = true;
                    data.add(Integer.parseInt(
                        child.getTextContent().trim()
                    ));
                }
            }

            if (!hasElements) {
                String value = element.getTextContent().trim();

                if (!value.isEmpty()) {
                    data.add(Integer.parseInt(value));
                }
            }
        }

        return data;
    }

    // Xoay mang sang phai
    static List<Integer> processData(List<Integer> data) {

        int n = data.size();

        if (n == 0) return new ArrayList<>();

        // Gia tri ASCII cua ky tu dau tien
        int k = data.get(0) % n;

        List<Integer> result = new ArrayList<>();

        // Xoay phai k vi tri
        for (int i = 0; i < n; i++) {
            int index = (i - k + n) % n;
            result.add(data.get(index));
        }

        return result;
    }

    // ========== 3. HAM MAIN ==========

    public static void main(String[] args) {
        try {
            String namespace = getNamespace();

            // a. Lay danh sach ky tu
            String response = callSOAP(
                namespace,
                "requestCharacter",
                new String[]{"studentCode", "qCode"},
                new String[]{STUDENT_CODE, Q_CODE}
            );

            List<Integer> data = parseData(response);

            System.out.println("Data: " + data);

            // b. Xoay mang sang phai
            List<Integer> result = processData(data);

            System.out.println("Result: " + result);

            // c. Gui ket qua ve server
            String[] names = new String[result.size() + 2];
            String[] values = new String[result.size() + 2];

            names[0] = "studentCode";
            names[1] = "qCode";
            values[0] = STUDENT_CODE;
            values[1] = Q_CODE;

            for (int i = 0; i < result.size(); i++) {
                names[i + 2] = "data";
                values[i + 2] = String.valueOf(result.get(i));
            }

            String submitResponse = callSOAP(
                namespace,
                "submitCharacterCharArray",
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
