package SOAP;


import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import java.io.*;

public class SOAP_OBJECT_1 {

    static final String URL =
            "http://36.50.135.242:2221/ObjectService";

    static final String STUDENT_CODE = "B23DCCN516";
    static final String Q_CODE = "xoD2DsBk";

    // Can doi chieu ten tham so voi WSDL/XSD
    static final String PARAM_STUDENT = "studentCode";
    static final String PARAM_QCODE = "qCode";
    static final String PARAM_DATA = "data";

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

    // Chuyen node XML thanh chuoi
    static String toXML(Node node) throws Exception {
        Transformer transformer =
                TransformerFactory.newInstance()
                        .newTransformer();

        transformer.setOutputProperty(
                OutputKeys.OMIT_XML_DECLARATION, "yes"
        );

        StringWriter writer = new StringWriter();

        transformer.transform(
                new DOMSource(node),
                new StreamResult(writer)
        );

        return writer.toString();
    }

    // ========== 2. XU LY LOGIC ==========

    static class Customer {
        String customerId;
        int purchaseCount;
        double totalSpent;
        Element original;

        Customer(Element element) {
            original = element;

            customerId = getField(element, "customerId");
            purchaseCount = Integer.parseInt(
                    getField(element, "purchaseCount")
            );
            totalSpent = Double.parseDouble(
                    getField(element, "totalSpent")
            );
        }
    }

    static String getField(Element element, String name) {
        NodeList nodes =
                element.getElementsByTagNameNS("*", name);

        if (nodes.getLength() == 0) {
            throw new RuntimeException("Thieu truong: " + name);
        }

        return nodes.item(0).getTextContent().trim();
    }

    // Lay danh sach Customer tu SOAP response
    static List<Customer> parseCustomers(String xml)
            throws Exception {

        Document doc = parseXML(xml);

        NodeList ids =
                doc.getElementsByTagNameNS("*", "customerId");

        List<Customer> customers = new ArrayList<>();

        for (int i = 0; i < ids.getLength(); i++) {
            Node parent = ids.item(i).getParentNode();

            if (parent instanceof Element) {
                customers.add(new Customer((Element) parent));
            }
        }

        return customers;
    }

    // Loc va sap xep
    static List<Customer> processData(List<Customer> data) {

        List<Customer> result = new ArrayList<>();

        for (Customer c : data) {
            if (c.purchaseCount >= 6 && c.totalSpent >= 4000) {
                result.add(c);
            }
        }

        result.sort(
                Comparator.comparingDouble(
                    (Customer c) -> c.totalSpent
                ).reversed().thenComparing(c -> c.customerId)
        );

        return result;
    }

    // Chuyen Customer thanh XML
    static String customerToXML(Customer customer)
            throws Exception {

        StringBuilder xml = new StringBuilder();

        NodeList children =
                customer.original.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (child.getNodeType() == Node.ELEMENT_NODE) {
                xml.append(toXML(child));
            }
        }

        return xml.toString();
    }

    // Tao tham so gui danh sach Customer
    static String buildSubmitParams(
            List<Customer> customers
    ) throws Exception {

        StringBuilder params = new StringBuilder();

        params.append("<").append(PARAM_STUDENT).append(">")
                .append(escapeXML(STUDENT_CODE))
                .append("</").append(PARAM_STUDENT).append(">");

        params.append("<").append(PARAM_QCODE).append(">")
                .append(escapeXML(Q_CODE))
                .append("</").append(PARAM_QCODE).append(">");

        for (Customer c : customers) {
            params.append("<").append(PARAM_DATA).append(">")
                    .append(customerToXML(c))
                    .append("</").append(PARAM_DATA).append(">");
        }

        return params.toString();
    }

    // ========== 3. HAM MAIN ==========

    public static void main(String[] args) {
        try {
            String namespace = getNamespace();

            // a. Nhan danh sach Customer
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

            // b. Loc va sap xep
            List<Customer> result =
                    processData(customers);

            System.out.println("Danh sach sau xu ly:");
            printCustomers(result);

            // c. Gui danh sach ve server
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

    static void printCustomers(List<Customer> customers) {
        for (Customer c : customers) {
            System.out.println(
                    c.customerId +
                    " | purchaseCount=" + c.purchaseCount +
                    " | totalSpent=" + c.totalSpent
            );
        }
    }
}
