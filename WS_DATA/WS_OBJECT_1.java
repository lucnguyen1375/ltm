package WS_DATA;


import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

public class WS_OBJECT_1 {

    static final String URL =
            "http://36.50.135.242:2221/ObjectService";

    static final String STUDENT_CODE = "B23DCCN516";
    static final String Q_CODE = "xXU61PxP";

    // Ten tham so phai khop WSDL/XSD
    static final String PARAM_STUDENT = "studentCode";
    static final String PARAM_QCODE = "qCode";
    static final String PARAM_CUSTOMERS = "data";

    static HttpClient client = HttpClient.newHttpClient();

    // ========== 1. KET NOI SOAP ==========

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
                "Loi lay WSDL: " + response.statusCode()
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
            String params
    ) throws Exception {

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
                HttpResponse.BodyHandlers.ofString(
                    StandardCharsets.UTF_8
                )
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

    static class Customer {
        String customerId;
        String location;
        int purchaseCount;
        float totalSpent;

        Customer(Element element) {
            customerId = getField(element, "customerId");
            location = getField(element, "location");

            purchaseCount = Integer.parseInt(
                getField(element, "purchaseCount")
            );

            totalSpent = Float.parseFloat(
                getField(element, "totalSpent")
            );
        }
    }

    static String getField(Element element, String name) {
        NodeList nodes =
            element.getElementsByTagNameNS("*", name);

        if (nodes.getLength() == 0) {
            throw new RuntimeException(
                "Thieu truong: " + name
            );
        }

        return nodes.item(0).getTextContent().trim();
    }

    // Chuyen SOAP XML thanh danh sach Customer
    static List<Customer> parseCustomers(String xml)
            throws Exception {

        Document doc = parseXML(xml);

        NodeList nodes =
            doc.getElementsByTagNameNS("*", "customerId");

        List<Customer> customers = new ArrayList<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node parent = nodes.item(i).getParentNode();

            if (parent instanceof Element) {
                customers.add(
                    new Customer((Element) parent)
                );
            }
        }

        return customers;
    }

    // Loc khach hang tiem nang
    static List<Customer> processData(List<Customer> data) {

        List<Customer> result = new ArrayList<>();

        for (Customer c : data) {
            if (c.totalSpent > 500 &&
                c.purchaseCount >= 3) {
                result.add(c);
            }
        }

        return result;
    }

    // Chuyen Customer thanh XML
    static String customerToXML(Customer c) {

        return "<customerId>" +
                    escapeXML(c.customerId) +
               "</customerId>" +

               "<location>" +
                    escapeXML(c.location) +
               "</location>" +

               "<purchaseCount>" +
                    c.purchaseCount +
               "</purchaseCount>" +

               "<totalSpent>" +
                    c.totalSpent +
               "</totalSpent>";
    }

    // Tao tham so SOAP de gui danh sach
    static String buildSubmitParams(
            List<Customer> customers
    ) {
        StringBuilder params = new StringBuilder();

        params.append("<").append(PARAM_STUDENT).append(">")
            .append(escapeXML(STUDENT_CODE))
            .append("</").append(PARAM_STUDENT).append(">");

        params.append("<").append(PARAM_QCODE).append(">")
            .append(escapeXML(Q_CODE))
            .append("</").append(PARAM_QCODE).append(">");

        for (Customer c : customers) {
            params.append("<").append(PARAM_CUSTOMERS).append(">")
                .append(customerToXML(c))
                .append("</").append(PARAM_CUSTOMERS).append(">");
        }

        return params.toString();
    }

    static void printCustomers(List<Customer> customers) {
        for (Customer c : customers) {
            System.out.println(
                c.customerId +
                " | " + c.location +
                " | purchaseCount=" + c.purchaseCount +
                " | totalSpent=" + c.totalSpent
            );
        }
    }

    // ========== 3. HAM MAIN ==========

    public static void main(String[] args) {
        try {
            // Lay namespace
            String namespace = getNamespace();

            // a. Goi requestListCustomer
            String requestParams =
                "<" + PARAM_STUDENT + ">" +
                escapeXML(STUDENT_CODE) +
                "</" + PARAM_STUDENT + ">" +

                "<" + PARAM_QCODE + ">" +
                escapeXML(Q_CODE) +
                "</" + PARAM_QCODE + ">";

            String response = callSOAP(
                namespace,
                "requestListCustomer",
                requestParams
            );

            List<Customer> customers =
                parseCustomers(response);

            System.out.println("Danh sach ban dau:");
            printCustomers(customers);

            // b. Loc khach hang
            List<Customer> result =
                processData(customers);

            System.out.println("Danh sach sau khi loc:");
            printCustomers(result);

            // c. Gui ket qua
            String submitParams =
                buildSubmitParams(result);

            String submitResponse = callSOAP(
                namespace,
                "submitListCustomer",
                submitParams
            );

            System.out.println("Da gui ket qua!");
            System.out.println("Server: " + submitResponse);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
