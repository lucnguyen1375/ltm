package WS_DATA;


import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import java.io.*;

public class WS_OBJECT {

    static final String URL =
            "http://36.50.135.242:2221/ObjectService";

    static final String STUDENT_CODE = "B23DCCN516";
    static final String Q_CODE = "uocWLeG6";

    // Can doi chieu ten tham so voi WSDL/XSD
    static final String PARAM_STUDENT = "studentCode";
    static final String PARAM_QCODE = "qCode";
    static final String PARAM_DATA = "data";

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

    static Document newDocument() throws Exception {
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().newDocument();
    }

    static String toXML(Document doc) throws Exception {
        Transformer transformer =
                TransformerFactory.newInstance()
                    .newTransformer();

        transformer.setOutputProperty(
            OutputKeys.ENCODING, "UTF-8"
        );

        StringWriter writer = new StringWriter();

        transformer.transform(
            new DOMSource(doc),
            new StreamResult(writer)
        );

        return writer.toString();
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

        return parseXML(response.body())
                .getDocumentElement()
                .getAttribute("targetNamespace");
    }

    // Tao SOAP va gui request
    static Document callSOAP(
            String namespace,
            String method,
            List<Employee> employees
    ) throws Exception {

        Document doc = newDocument();

        String soapNS =
            "http://schemas.xmlsoap.org/soap/envelope/";

        Element envelope =
            doc.createElementNS(soapNS, "soap:Envelope");
        doc.appendChild(envelope);

        Element body =
            doc.createElementNS(soapNS, "soap:Body");
        envelope.appendChild(body);

        Element operation =
            doc.createElementNS(namespace, "ns:" + method);
        operation.setAttribute("xmlns:ns", namespace);
        body.appendChild(operation);

        Element student =
            doc.createElement(PARAM_STUDENT);
        student.setTextContent(STUDENT_CODE);
        operation.appendChild(student);

        Element qcode =
            doc.createElement(PARAM_QCODE);
        qcode.setTextContent(Q_CODE);
        operation.appendChild(qcode);

        // Them danh sach EmployeeY neu la submit
        if (employees != null) {
            for (Employee emp : employees) {

                Element data = doc.createElement(PARAM_DATA);

                // Sao chep cac truong cua EmployeeY
                NodeList children =
                    emp.original.getChildNodes();

                for (int i = 0; i < children.getLength(); i++) {
                    Node child = children.item(i);

                    if (child.getNodeType() == Node.ELEMENT_NODE) {
                        data.appendChild(
                            doc.importNode(child, true)
                        );
                    }
                }

                operation.appendChild(data);
            }
        }

        String xml = toXML(doc);

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

        Document result = parseXML(response.body());

        if (response.statusCode() != 200 ||
            result.getElementsByTagNameNS("*", "Fault")
                .getLength() > 0) {
            throw new RuntimeException(response.body());
        }

        return result;
    }

    // ========== 2. XU LY LOGIC ==========

    static class Employee {
        String name;
        LocalDate startDate;
        Element original;

        Employee(Element element) {
            this.original = element;
            this.name = getField(element, "name");

            String date = getField(element, "startDate");

            // ISO date: yyyy-MM-dd hoac yyyy-MM-ddTHH:mm:ss
            this.startDate = LocalDate.parse(
                date.substring(0, 10)
            );
        }
    }

    static String getField(Element element, String name) {
        NodeList nodes =
            element.getElementsByTagNameNS("*", name);

        if (nodes.getLength() == 0) {
            throw new RuntimeException(
                "Khong tim thay truong: " + name
            );
        }

        return nodes.item(0).getTextContent().trim();
    }

    // Doc danh sach EmployeeY
    static List<Employee> parseEmployees(Document doc) {

        List<Employee> employees = new ArrayList<>();

        NodeList nodes =
            doc.getElementsByTagNameNS("*", "startDate");

        for (int i = 0; i < nodes.getLength(); i++) {
            Node parent = nodes.item(i).getParentNode();

            if (parent instanceof Element) {
                employees.add(
                    new Employee((Element) parent)
                );
            }
        }

        return employees;
    }

    // Sap xep nhan vien theo tham nien
    static List<Employee> processData(List<Employee> data) {

        List<Employee> result = new ArrayList<>(data);

        result.sort(
            Comparator.comparing(
                (Employee e) -> e.startDate
            )
        );

        return result;
    }

    static void printEmployees(List<Employee> data) {
        for (Employee e : data) {
            System.out.println(
                e.name + " | " + e.startDate
            );
        }
    }

    // ========== 3. HAM MAIN ==========

    public static void main(String[] args) {
        try {
            String namespace = getNamespace();

            // a. Goi requestListEmployeeY
            Document response = callSOAP(
                namespace,
                "requestListEmployeeY",
                null
            );

            List<Employee> employees =
                parseEmployees(response);

            System.out.println("Danh sach ban dau:");
            printEmployees(employees);

            // b. Sap xep theo tham nien
            List<Employee> result =
                processData(employees);

            System.out.println("Danh sach sau sap xep:");
            printEmployees(result);

            // c. Gui ket qua
            Document submitResponse = callSOAP(
                namespace,
                "submitListEmployeeY",
                result
            );

            System.out.println("Da gui ket qua!");
            System.out.println("Server: " + toXML(submitResponse));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
