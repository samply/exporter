package de.samply.exporter.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

public class RequestMapper {

    private static ObjectMapper objectMapper = new ObjectMapper();

    // Body properties have priority over the parameters
    public static Map<String, String> fetchKeyValues(HttpServletRequest request, Method method) throws RequestMapperException {
        Map<String, String> keyValues = new HashMap<>();
        String body = fetchBody(request);
        String contentType = request.getHeader(HttpHeaders.CONTENT_TYPE);
        if (hasText(body) && hasText(contentType)) {
            keyValues.putAll(fetchKeyValuesOfBody(body, contentType));
        }
        Map<String, String> tempKeyValues = fetchKeyValuesOfRequestParamsAndHeaders(request, method);
        tempKeyValues.keySet().forEach(key -> {
            String value = tempKeyValues.get(key);
            if (hasText(value) && !hasText(keyValues.get(key))) {
                keyValues.put(key, value);
            }
        });

        return keyValues;
    }

    private static Map<String, String> decodeKeyValuesIfNecessary(Map<String, String> keyValues) {
        keyValues.keySet().forEach(key -> keyValues.put(key, decodeInBase64IfNecessary(keyValues.get(key))));
        return keyValues;
    }

    private static boolean hasText(String string) {
        return StringUtils.hasText(string);
    }

    private static String fetchBody(HttpServletRequest request) throws RequestMapperException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()))) {
            return fetchBody(reader);
        } catch (IOException e) {
            throw new RequestMapperException(e);
        }
    }

    private static String fetchBody(BufferedReader reader) throws IOException {
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line).append('\n');
        }
        return decodeInBase64IfNecessary(requestBody.toString());
    }

    private static String decodeInBase64IfNecessary(String value) {
        try {
            return new String(Base64.getDecoder().decode(value.trim()));
        } catch (IllegalArgumentException e) {
            return value;
        }
    }

    private static Map<String, String> fetchKeyValuesOfRequestParamsAndHeaders(HttpServletRequest request, Method method) {
        Map<String, String> requestParamAndHeadersValues = new HashMap<>();
        Set<Class> classFilter = Set.of(RequestParam.class, RequestHeader.class);
        Arrays.stream(method.getParameterAnnotations()).forEach(annotationsArray ->
                Arrays.stream(annotationsArray)
                        .filter(annotation -> classFilter.contains(annotation.annotationType()))
                        .forEach(annotation -> {
                            String paramName = (annotation.annotationType() == RequestParam.class) ?
                                    ((RequestParam) annotation).name() : ((RequestHeader) annotation).name();
                            String paramValue = (annotation.annotationType() == RequestParam.class) ?
                                    request.getParameter(paramName) : request.getHeader(paramName);
                            if (hasText(paramValue)) {
                                requestParamAndHeadersValues.put(paramName, paramValue);
                            }
                        }));
        return requestParamAndHeadersValues;
    }

    private static Map<String, String> fetchKeyValuesOfBody(String requestBody, @NotNull String contentType) throws RequestMapperException {
        if (!StringUtils.hasText(requestBody)) {
            return new HashMap<>();
        } else if (contentType.equals(MediaType.APPLICATION_JSON_VALUE)) {
            return fetchKeyValuesOfJsonBody(requestBody);
        } else if (contentType.equals(MediaType.APPLICATION_XML_VALUE)) {
            return fetchKeyValuesOfXmlBody(requestBody);
        } else {
            throw new RequestMapperException("Content Type not supported");
        }
    }

    private static Map<String, String> fetchKeyValuesOfXmlBody(String requestBody) throws RequestMapperException {
        try {
            return fetchKeyValuesOfXmlBodyWithoutExceptionHandling(requestBody);
        } catch (ParserConfigurationException | IOException | SAXException | TransformerException e) {
            throw new RequestMapperException(e);
        }
    }

    private static Map<String, String> fetchKeyValuesOfXmlBodyWithoutExceptionHandling(String requestBody) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        Map<String, String> result = new HashMap<>();
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(requestBody.getBytes()));
        NodeList nodeList = document.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (hasChildElements(node)) {
                    result.put(node.getNodeName(), extractChildStructure(node));
                } else {
                    result.put(node.getNodeName(), node.getTextContent());
                }
            }
        }
        return result;
    }

    private static boolean hasChildElements(Node node) {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                return true;
            }
        }
        return false;
    }

    private static String extractChildStructure(Node node) throws IOException, TransformerException {
        StringBuilder structure = new StringBuilder();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                structure.append(nodeToString(child));
            }
        }
        return structure.toString();
    }

    private static String nodeToString(Node node) throws TransformerException, IOException {
        try (StringWriter sw = new StringWriter()) {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(node), new StreamResult(sw));
            return sw.toString();
        }
    }


    @SuppressWarnings("unchecked")
    private static void flattenEntry(String prefix, Map.Entry<String, Object> entry, Map<String, String> flatMap) {
        if (entry.getValue() instanceof Map) {
            Map<String, Object> nestedMap = (Map<String, Object>) entry.getValue();
            for (Map.Entry<String, Object> nestedEntry : nestedMap.entrySet()) {
                flattenEntry(prefix + entry.getKey() + ".", nestedEntry, flatMap);
            }
        } else {
            flatMap.put(prefix + entry.getKey(), entry.getValue().toString());
        }
    }

    private static Map<String, String> fetchKeyValuesOfJsonBody(String requestBody) throws RequestMapperException {
        try {
            Map<String, String> result = new HashMap<>();
            JsonNode rootNode = objectMapper.readTree(requestBody);

            // Extract first-level keys and their string representations
            Iterator<String> fieldNames = rootNode.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = rootNode.get(fieldName);
                String value = fieldValue.toString();

                // Remove quotation marks if the value is enclosed in quotes
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }

                result.put(fieldName, value);
            }

            return result;
        } catch (JsonProcessingException | NullPointerException e) {
            throw new RequestMapperException(e);
        }
    }

}
