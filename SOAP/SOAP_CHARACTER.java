package SOAP;


import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import java.io.StringReader;

public class SOAP_CHARACTER {

    static final String URL =
            "http://36.50.135.242:2221/CharacterService";

    static final String STUDENT_CODE = "B23DCCN516";
    static final String Q_CODE = "ml2CuXAY";

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
                .POST(HttpRequest.BodyPublishers.ofString(xml))
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString(
                        StandardCharsets.UTF_8
                )
        );

        Document doc = parseXML(response.body());

        if (doc.getElementsByTagNameNS("*", "Fault")
                .getLength() > 0) {
            throw new RuntimeException(response.body());
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException(
                    "HTTP " + response.statusCode()
                    + ": " + response.body()
            );
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

    // Lay gia tri return trong SOAP response
    static String getReturnValue(String xml) throws Exception {
        Document doc = parseXML(xml);

        NodeList nodes =
                doc.getElementsByTagNameNS("*", "return");

        if (nodes.getLength() == 0) {
            throw new RuntimeException(
                    "Khong tim thay return trong SOAP response: " + xml
            );
        }

        return nodes.item(0).getTextContent();
    }

    // Thoat cac ky tu dac biet trong XML
    static String escapeXML(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    // ========== 2. XU LY LOGIC ==========

    // Dao nguoc chuoi
    static String processData(String data) {
        return new StringBuilder(data).reverse().toString();
    }

    // ========== 3. HAM MAIN ==========

    public static void main(String[] args) {
        try {
            // Lay namespace
            String namespace = getNamespace();

            // a. Nhan chuoi tu server
            String response = callSOAP(
                    namespace,
                    "requestString",
                    new String[]{"studentCode", "qCode"},
                    new String[]{STUDENT_CODE, Q_CODE}
            );

            String data = getReturnValue(response);

            System.out.println("Data: " + data);

            // b. Dao nguoc chuoi
            String result = processData(data);

            System.out.println("Result: " + result);

            // c. Gui ket qua ve server
            String submitResponse = callSOAP(
                    namespace,
                    "submitString",
                    new String[]{
                            "studentCode", "qCode", "data"
                    },
                    new String[]{
                            STUDENT_CODE, Q_CODE, result
                    }
            );

            System.out.println("Da gui ket qua len server!");
            System.out.println("Server response: " + submitResponse);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
