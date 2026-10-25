package org.example.beadandojava;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class MnbService {

    public static class RateRecord {
        private String date;
        private double rate;

        public RateRecord(String date, double rate) {
            this.date = date;
            this.rate = rate;
        }

        public String getDate() { return date; }
        public double getRate() { return rate; }
    }

    public List<RateRecord> getExchangeRates(String startDate, String endDate, String currency) {
        List<RateRecord> rates = new ArrayList<>();

        try {
            String soapXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                    "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                    "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
                    "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                    "<soap:Body>" +
                    "<GetExchangeRates xmlns=\"http://www.mnb.hu/webservices/\">" +
                    "<startDate>" + startDate + "</startDate>" +
                    "<endDate>" + endDate + "</endDate>" +
                    "<currencyNames>" + currency + "</currencyNames>" +
                    "</GetExchangeRates>" +
                    "</soap:Body>" +
                    "</soap:Envelope>";

            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://www.mnb.hu/arfolyamok.asmx"))
                    .header("Content-Type", "text/xml; charset=utf-8")
                    .header("SOAPAction", "\"http://www.mnb.hu/webservices/GetExchangeRates\"")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .POST(HttpRequest.BodyPublishers.ofString(soapXml))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String xmlResponse = response.body();

            if (xmlResponse == null || xmlResponse.trim().isEmpty()) {
                return rates;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlResponse)));

            NodeList resultNodes = doc.getElementsByTagName("GetExchangeRatesResult");
            String innerXml = null;
            if (resultNodes.getLength() > 0) {
                innerXml = resultNodes.item(0).getTextContent();
            }

            if (innerXml != null && !innerXml.trim().isEmpty()) {
                Document innerDoc = builder.parse(new InputSource(new StringReader(innerXml)));
                NodeList dayNodes = innerDoc.getElementsByTagName("Day");

                for (int i = 0; i < dayNodes.getLength(); i++) {
                    Element dayElement = (Element) dayNodes.item(i);
                    String date = dayElement.getAttribute("date");

                    NodeList rateNodes = dayElement.getElementsByTagName("Rate");
                    if (rateNodes.getLength() > 0) {
                        Element rateElement = (Element) rateNodes.item(0);
                        String rateText = rateElement.getTextContent().replace(",", ".");
                        if (!rateText.isEmpty()) {
                            double rateValue = Double.parseDouble(rateText);
                            rates.add(new RateRecord(date, rateValue));
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rates;
    }
}