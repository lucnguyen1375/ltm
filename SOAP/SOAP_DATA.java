package SOAP;


import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import java.io.StringReader;

public class SOAP_DATA {

    static final String URL =
            "http://36.50.135.242:2221/DataService";

    static final String STUDENT_CODE = "B23DCCN516";
    static final String Q_CODE = "r5WQ7y3F";

    static HttpClient client = HttpClient.newHttpClient();

    // ========== 1. XU LY KET NOI ==========

    // Lay namespace tu WSDL
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

    // Goi phuong thuc SOAP
    static String callSOAP(
            String namespace,
            String method,
            String[] names,
            String[] values
    ) throws Exception {

        StringBuilder params = new StringBuilder();

        for (int i = 0; i < names.length; i++) {
            params.append("<").append(names[i]).append(">")
                    .append(values[i])
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
                .POST(HttpRequest.BodyPublishers.ofString(xml))
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString(
                        StandardCharsets.UTF_8
                )
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException(
                    "HTTP " + response.statusCode()
                    + ": " + response.body()
            );
        }

        Document result = parseXML(response.body());

        if (result.getElementsByTagNameNS("*", "Fault")
                .getLength() > 0) {
            throw new RuntimeException(response.body());
        }

        return response.body();
    }

    // Chuyen XML thanh Document
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

    // ========== 2. XU LY LOGIC ==========

    // Chuyen XML thanh danh sach so nguyen
    static List<Integer> parseNumbers(String xml)
            throws Exception {

        Document doc = parseXML(xml);
        NodeList nodes =
                doc.getElementsByTagNameNS("*", "return");

        List<Integer> numbers = new ArrayList<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            String value = nodes.item(i)
                    .getTextContent().trim();

            if (!value.isEmpty()) {
                numbers.add(Integer.parseInt(value));
            }
        }

        return numbers;
    }

    // Dem so chan va so le
    static String[] processData(List<Integer> numbers) {
        int even = 0;
        int odd = 0;

        for (int number : numbers) {
            if (number % 2 == 0) {
                even++;
            } else {
                odd++;
            }
        }

        return new String[]{
                "EVEN=" + even,
                "ODD=" + odd
        };
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

            // b. Doc danh sach
            List<Integer> numbers = parseNumbers(response);

            System.out.println("Data: " + numbers);

            // c. Dem so chan va le
            String[] result = processData(numbers);

            System.out.println("Result: "
                    + Arrays.toString(result));

            // d. Gui ket qua ve server
            String submitResponse = callSOAP(
                    namespace,
                    "submitDataStringArray",
                    new String[]{
                            "studentCode", "qCode", "data", "data"
                    },
                    new String[]{
                            STUDENT_CODE, Q_CODE,
                            result[0], result[1]
                    }
            );

            System.out.println("Server response:");
            System.out.println(submitResponse);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
