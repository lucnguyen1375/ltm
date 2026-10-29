package WS_DATA;


import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import java.io.StringReader;

public class DataService {

    static final String URL =
        "http://36.50.135.242:2221/DataService";

    static final String STUDENT_CODE = "B23DCCN516";
    static final String Q_CODE = "JGE4xHNk";

    static HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) throws Exception {

        // Lay namespace tu WSDL
        String wsdl = client.send(
            HttpRequest.newBuilder()
                .uri(URI.create(URL + "?wsdl"))
                .GET().build(),
            HttpResponse.BodyHandlers.ofString()
        ).body();

        Document wsdlDoc = parseXML(wsdl);
        String namespace =
            wsdlDoc.getDocumentElement()
                   .getAttribute("targetNamespace");

        // a. Goi getData
        String response = callSOAP(
            namespace, "getData",
            new String[]{"studentCode", "qCode"},
            new String[]{STUDENT_CODE, Q_CODE}
        );

        Document doc = parseXML(response);
        NodeList nodes = doc.getElementsByTagNameNS("*", "return");

        List<String> numbers = new ArrayList<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            String value = nodes.item(i).getTextContent().trim();
            if (!value.isEmpty()) {
                numbers.add(value);
            }
        }

        System.out.println("Danh sach: " + numbers);

        // b. Sap xep de tao so lon nhat
        numbers.sort((a, b) -> (b + a).compareTo(a + b));

        StringBuilder result = new StringBuilder();

        for (String number : numbers) {
            result.append(number);
        }

        String answer = result.toString();

        if (!answer.isEmpty() && answer.charAt(0) == '0') {
            answer = "0";
        }

        System.out.println("Ket qua: " + answer);

        // c. Gui ket qua len server
        String submitResponse = callSOAP(
            namespace, "submitDataString",
            new String[]{"studentCode", "qCode", "data"},
            new String[]{STUDENT_CODE, Q_CODE, answer}
        );

        System.out.println("Server response:");
        System.out.println(submitResponse);
    }

    static String callSOAP(String namespace,String method,String[] names,String[] values) throws Exception {

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
                "HTTP " + response.statusCode() +
                ": " + response.body()
            );
        }

        Document resultDoc = parseXML(response.body());

        if (resultDoc.getElementsByTagNameNS("*", "Fault")
                     .getLength() > 0) {
            throw new RuntimeException(response.body());
        }

        return response.body();
    }

    static Document parseXML(String xml) throws Exception {
        DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);
        factory.setFeature(
            "http://apache.org/xml/features/disallow-doctype-decl",
            true
        );

        DocumentBuilder builder = factory.newDocumentBuilder();

        return builder.parse(
            new InputSource(new StringReader(xml))
        );
    }
}
