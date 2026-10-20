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
                    "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                    "<soap:Body>" +
                    "<GetCurrencyRates xmlns=\"http://www.mnb.hu/arfolyamok/\">" +
                    "<startDate>" + startDate + "</startDate>" +
                    "<endDate>" + endDate + "</endDate>" +
                    "<currencyNames>" + currency + "</currencyNames>" +
                    "</GetCurrencyRates>" +
                    "</soap:Body>" +
                    "</soap:Envelope>";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://www.mnb.hu/arfolyamok.asmx"))
                    .header("Content-Type", "text/xml; charset=utf-8")
                    .header("SOAPAction", "http://www.mnb.hu/arfolyamok/IArfolyamService/GetCurrencyRates")
                    .POST(HttpRequest.BodyPublishers.ofString(soapXml))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String xmlResponse = response.body();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlResponse)));

            NodeList rateNodes = doc.getElementsByTagName("Rate");
            for (int i = 0; i < rateNodes.getLength(); i++) {
                Element rateElement = (Element) rateNodes.item(i);
                String date = rateElement.getAttribute("date");
                String rateText = rateElement.getTextContent().replace(",", ".");
                if (!rateText.isEmpty()) {
                    double rateValue = Double.parseDouble(rateText);
                    rates.add(new RateRecord(date, rateValue));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rates;
    }
}