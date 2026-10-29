package SOAP;


import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import java.io.StringReader;

public class SOAP_DATA_1 {

    static final String URL =
            "http://36.50.135.242:2221/DataService";

    static final String STUDENT_CODE = "B23DCCN516";
    static final String Q_CODE = "b9ws2CuS";

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
                        xml, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        Document doc = parseXML(response.body());

        if (response.statusCode() != 200 ||
                doc.getElementsByTagNameNS("*", "Fault")
                        .getLength() > 0) {
            throw new RuntimeException(response.body());
        }

        return response.body();
    }

    // ========== 2. XU LY LOGIC ==========

    static List<Integer> parseNumbers(String xml) throws Exception {
        Document doc = parseXML(xml);

        NodeList nodes =
                doc.getElementsByTagNameNS("*", "return");

        List<Integer> numbers = new ArrayList<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);

            NodeList children = element.getChildNodes();
            boolean hasElements = false;

            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);

                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    hasElements = true;
                    numbers.add(Integer.parseInt(
                            child.getTextContent().trim()
                    ));
                }
            }

            if (!hasElements) {
                String value = element.getTextContent().trim();
                if (!value.isEmpty()) {
                    numbers.add(Integer.parseInt(value));
                }
            }
        }

        if (nodes.getLength() == 0) {
            throw new RuntimeException(
                    "Khong tim thay return: " + xml
            );
        }

        return numbers;
    }

    // Kiem tra so nguyen to
    static boolean isPrime(int n) {
        if (n < 2) return false;

        for (int i = 2; i <= n / i; i++) {
            if (n % i == 0) {
                return false;
            }
        }

        return true;
    }

    // Dem so nguyen to va tinh checksum
    static String processData(List<Integer> numbers) {
        int primeCount = 0;
        long checksum = 0;

        for (int i = 0; i < numbers.size(); i++) {
            int value = numbers.get(i);

            if (isPrime(value)) {
                primeCount++;
            }

            checksum = (checksum +
                    (long) (i + 1) * value) % 100000;
        }

        checksum = (checksum + 100000) % 100000;

        return "primeCount=" + primeCount
                + ";checksum=" + checksum;
    }

    // ========== 3. HAM MAIN ==========

    public static void main(String[] args) {
        try {
            // Lay namespace
            String namespace = getNamespace();

            // a. Goi getData
            String response = callSOAP(
                    namespace,
                    "getData",
                    new String[]{"studentCode", "qCode"},
                    new String[]{STUDENT_CODE, Q_CODE}
            );

            // b. Lay danh sach so nguyen
            List<Integer> numbers = parseNumbers(response);

            System.out.println("Data: " + numbers);

            // c. Dem so nguyen to va tinh checksum
            String result = processData(numbers);

            System.out.println("Result: " + result);

            // d. Gui ket qua
            String submitResponse = callSOAP(
                    namespace,
                    "submitDataString",
                    new String[]{
                            "studentCode", "qCode", "data"
                    },
                    new String[]{
                            STUDENT_CODE, Q_CODE, result
                    }
            );

            System.out.println("Da gui ket qua!");
            System.out.println("Server response: " + submitResponse);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
