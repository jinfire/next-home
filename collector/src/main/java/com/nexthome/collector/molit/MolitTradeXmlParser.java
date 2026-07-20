package com.nexthome.collector.molit;

import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Component
public class MolitTradeXmlParser {

    public MolitTradePage parse(String xml) {
        try {
            Document document = secureFactory().newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            String resultCode = text(document.getDocumentElement(), "resultCode");
            String resultMessage = text(document.getDocumentElement(), "resultMsg");
            if (!"000".equals(resultCode) && !"00".equals(resultCode)) {
                throw new MolitApiException("국토교통부 API 오류(" + resultCode + "): " + resultMessage);
            }

            int totalCount = integer(text(document.getDocumentElement(), "totalCount"), 0);
            NodeList nodes = document.getElementsByTagName("item");
            List<MolitTradeItem> items = new ArrayList<>(nodes.getLength());
            for (int index = 0; index < nodes.getLength(); index++) {
                items.add(toItem((Element) nodes.item(index)));
            }
            return new MolitTradePage(totalCount, List.copyOf(items));
        } catch (MolitApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new MolitApiException("국토교통부 API XML 응답을 해석할 수 없습니다.", exception);
        }
    }

    private MolitTradeItem toItem(Element item) {
        int year = integer(text(item, "dealYear"), 0);
        int month = integer(text(item, "dealMonth"), 0);
        int day = integer(text(item, "dealDay"), 0);
        String cancellation = text(item, "cdealDay");
        return new MolitTradeItem(
                text(item, "aptSeq"),
                required(item, "aptNm"),
                required(item, "umdNm"),
                required(item, "jibun"),
                Long.parseLong(required(item, "dealAmount").replace(",", "").trim()) * 10_000L,
                new BigDecimal(required(item, "excluUseAr")),
                LocalDate.of(year, month, day),
                nullableInteger(text(item, "floor")),
                nullableInteger(text(item, "buildYear")),
                cancellation.isBlank() ? null : LocalDate.parse(cancellation));
    }

    private DocumentBuilderFactory secureFactory() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory;
    }

    private String required(Element element, String tag) {
        String value = text(element, tag);
        if (value.isBlank()) {
            throw new MolitApiException("필수 응답 필드가 없습니다: " + tag);
        }
        return value;
    }

    private String text(Element element, String tag) {
        NodeList nodes = element.getElementsByTagName(tag);
        if (nodes.getLength() == 0) {
            return "";
        }
        Node node = nodes.item(0);
        return node.getTextContent() == null ? "" : node.getTextContent().trim();
    }

    private int integer(String value, int defaultValue) {
        return value.isBlank() ? defaultValue : Integer.parseInt(value.trim());
    }

    private Integer nullableInteger(String value) {
        return value.isBlank() ? null : Integer.valueOf(value.trim());
    }
}
