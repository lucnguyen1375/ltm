package SOAP;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

public class SOAP_OBJECT {

    static final String URL =
        "http://36.50.135.242:2221/ObjectService";

    static final String STUDENT_CODE = "B23DCCN516";
    static final String Q_CODE = "P9lImcH1";

    static HttpClient client = HttpClient.newHttpClient();
    static Document wsdl;
    static final String XSD_NS = "http://www.w3.org/2001/XMLSchema";
    static final List<Document> schemas = new ArrayList<>();
    static String namespace;

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

    static String toXML(Node node) throws Exception {
        Transformer transformer =
                TransformerFactory.newInstance().newTransformer();

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

    static String escapeXML(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    // Lay WSDL va namespace
    static void connect() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL + "?wsdl"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
            request, HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException("Khong lay duoc WSDL");
        }

        wsdl = parseXML(response.body());
        namespace = wsdl.getDocumentElement()
                .getAttribute("targetNamespace");
        schemas.clear();
        loadSchemas(wsdl, URI.create(URL + "?wsdl"), new HashSet<>());
    }

    // DOM khong tu tai cac schema duoc import/include.
    static void loadSchemas(Document document, URI base, Set<URI> loaded)
            throws Exception {
        schemas.add(document);
        for (String tag : new String[] { "import", "include" }) {
            NodeList imports = document.getElementsByTagNameNS(XSD_NS, tag);
            for (int i = 0; i < imports.getLength(); i++) {
                String location = ((Element) imports.item(i))
                        .getAttribute("schemaLocation");
                if (location.isEmpty()) continue;
                URI uri = base.resolve(location);
                if (!loaded.add(uri)) continue;
                HttpResponse<String> response = client.send(
                    HttpRequest.newBuilder(uri).GET().build(),
                    HttpResponse.BodyHandlers.ofString()
                );
                if (response.statusCode() != 200) {
                    throw new IOException("Khong tai duoc schema: " + uri
                            + " (HTTP " + response.statusCode() + ")");
                }
                loadSchemas(parseXML(response.body()), uri, loaded);
            }
        }
    }

    static Element findComplexType(String typeNamespace, String name) {
        for (Document schema : schemas) {
            NodeList types = schema.getElementsByTagNameNS(XSD_NS, "complexType");
            for (int i = 0; i < types.getLength(); i++) {
                Element type = (Element) types.item(i);
                if (name.equals(type.getAttribute("name"))
                        && typeNamespace.equals(((Element) type.getParentNode())
                                .getAttribute("targetNamespace"))) {
                    return type;
                }
            }
        }
        throw new IllegalArgumentException("Khong tim thay complexType: " + name);
    }

    // Lay tham so tu element va complexType ma element tham chieu.
    static List<String> getParams(String method) {
        for (Document schema : schemas) {
        NodeList elements =
            schema.getElementsByTagNameNS(
                XSD_NS, "element"
            );

        for (int i = 0; i < elements.getLength(); i++) {
            Element element = (Element) elements.item(i);

            if (!method.equals(element.getAttribute("name"))) {
                continue;
            }

            String typeName = element.getAttribute("type");
            Element definition = element;
            if (!typeName.isEmpty()) {
                int colon = typeName.indexOf(':');
                String prefix = colon < 0 ? null : typeName.substring(0, colon);
                definition = findComplexType(element.lookupNamespaceURI(prefix),
                        typeName.substring(colon + 1));
            }
            NodeList sequences =
                definition.getElementsByTagNameNS(
                    XSD_NS, "sequence"
                );

            if (sequences.getLength() == 0) continue;

            List<String> params = new ArrayList<>();
            NodeList children = sequences.item(0).getChildNodes();

            for (int j = 0; j < children.getLength(); j++) {
                Node node = children.item(j);

                if (node instanceof Element &&
                    "element".equals(node.getLocalName())) {
                    params.add(
                        ((Element) node).getAttribute("name")
                    );
                }
            }

            return params;
        }
        }

        throw new RuntimeException(
            "Khong tim thay khai bao " + method + " trong WSDL"
        );
    }

    // Goi SOAP
    static Document callSOAP(
            String method, String... values
    ) throws Exception {

        List<String> names = getParams(method);

        if (names.size() != values.length) {
            throw new RuntimeException(
                "Sai so luong tham so cua " + method
            );
        }

        StringBuilder params = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            params.append("<").append(names.get(i)).append(">")
                  .append(values[i])
                  .append("</").append(names.get(i)).append(">");
        }

        String xml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<S:Envelope " +
            "xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
            "xmlns:ns=\"" + escapeXML(namespace) + "\">" +
            "<S:Body>" +
            "<ns:" + method + ">" +
            params +
            "</ns:" + method + ">" +
            "</S:Body></S:Envelope>";

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

        return doc;
    }

    // ========== 2. XU LY LOGIC ==========

    static Element findField(Element parent, String name) {
        NodeList nodes = parent.getElementsByTagNameNS("*", name);

        if (nodes.getLength() == 0) {
            throw new RuntimeException("Thieu truong: " + name);
        }

        return (Element) nodes.item(0);
    }

    static String processData(Element product) {

        double price = Double.parseDouble(
            findField(product, "price").getTextContent()
        );

        double taxRate = Double.parseDouble(
            findField(product, "taxRate").getTextContent()
        );

        double discount = Double.parseDouble(
            findField(product, "discount").getTextContent()
        );

        double finalPrice = price
                * (1 + taxRate / 100)
                * (1 - discount / 100);

        // Cap nhat finalPrice tren doi tuong
        findField(product, "finalPrice")
                .setTextContent(Double.toString(finalPrice));

        return Double.toString(finalPrice);
    }

    // Chuyen ProductY thanh XML de gui
    static String productToXML(Element product) throws Exception {
        StringBuilder result = new StringBuilder();
        NodeList children = product.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (child.getNodeType() == Node.ELEMENT_NODE) {
                result.append(toXML(child));
            }
        }

        return result.toString();
    }

    // ========== 3. HAM MAIN ==========

    public static void main(String[] args) {
        try {
            // 1. Ket noi WSDL
            connect();

            // 2. Goi requestProductY
            Document response = callSOAP(
                "requestProductY",
                escapeXML(STUDENT_CODE),
                escapeXML(Q_CODE)
            );

            NodeList returns =
                response.getElementsByTagNameNS("*", "return");

            if (returns.getLength() == 0) {
                throw new RuntimeException("Khong co ProductY");
            }

            Element product = (Element) returns.item(0);

            System.out.println("Product: " + toXML(product));

            // 3. Tinh finalPrice
            String finalPrice = processData(product);

            System.out.println("Final Price: " + finalPrice);

            // 4. Gui ProductY da cap nhat
            Document submitResponse = callSOAP(
                "submitProductY",
                escapeXML(STUDENT_CODE),
                escapeXML(Q_CODE),
                productToXML(product)
            );

            System.out.println(
                "Server response: " +
                toXML(submitResponse.getDocumentElement())
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
