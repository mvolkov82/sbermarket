package com.okeandra.demo.services.creators;

import com.okeandra.demo.models.YmlObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Qualifier("xmlCreator")
public abstract class XmlCreator {
    public static String BODY_REDUNDANT_FIRST_LINE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    public void saveXmlFile(String fileName, YmlObject yml) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {

            String newXmlHeaderWithCurrentDateTime = setHeaderCurrentDateTime(yml.getHeaderContent());
            writer.write(newXmlHeaderWithCurrentDateTime);

            String body = getBodyAsXML(yml);
            String bodyWithoutRedundantXmlHeader = body.substring(BODY_REDUNDANT_FIRST_LINE.length());
            writer.write(bodyWithoutRedundantXmlHeader);

            String newXmlFooter = yml.getFooterContent();
            writer.write(newXmlFooter);

        } catch (Exception e) {
            System.out.println("Error by writing resulted xml");
            System.out.println(e.getMessage());
        }
    }

    public abstract String getBodyAsXML(YmlObject ymlObject);

    public String getTextFromValue(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }
    public Document getDocument(YmlObject ymlObject) {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder build = null;
        try {
            build = documentFactory.newDocumentBuilder();
            Document document = build.newDocument();
            document.createTextNode(ymlObject.getHeaderContent());
            return document;

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Ошибка при создании финального документа для фида GroupPrice");
    }

    public String transformBodyToString(Document document) {
        StringWriter stringWriter = new StringWriter();
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(document);
            transformer.transform(source, new StreamResult(stringWriter));

        } catch (TransformerException e) {
            System.out.println("transformBodyToString() Error outputting document");
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("transformBodyToString() Error outputting document");
            System.out.println(e.getMessage());
        }
        return stringWriter.toString();
    }

    private String setHeaderCurrentDateTime(String ymlHeader) {
        String prefix = "<yml_catalog date=\"";
        String postfix = "\">";
        int startIndex = ymlHeader.indexOf(prefix);
        int endIndex = ymlHeader.indexOf(postfix);
        String feedDateTime = ymlHeader.substring(startIndex+prefix.length(), endIndex);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String feedWithCurrentDateTime = LocalDateTime.now().format(formatter);
        return ymlHeader.replace(feedDateTime, feedWithCurrentDateTime);
    }

}
