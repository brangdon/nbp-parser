package pl.parser.nbp;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;


public class NBPDocumentParser {

    private static String currencyCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> nbpURLs;
    private final String nbpString = "http://www.nbp.pl/kursy/xml/c";
    private final int numberOfPages = 26;

    public NBPDocumentParser(String currencyCode, String startDate, String endDate) {
        this.currencyCode = currencyCode.toUpperCase();
        this.startDate = LocalDate.parse(startDate);
        this.endDate = LocalDate.parse(endDate);
        nbpURLs = new ArrayList<>();

        prepareUris();
    }

    private void prepareUris() {

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        List<LocalDate> totalDates =
                LongStream.iterate(0, i -> i + 1)
                        .limit(daysBetween).mapToObj(i -> startDate.plusDays(i))
                        .collect(Collectors.toList());

        ExecutorService executorService = Executors.newCachedThreadPool();
        List<Callable<String>> taskList = new ArrayList<>();

        for (LocalDate localDate : totalDates) {
            String parsedDate = parseLocalData(localDate);

            Callable<String> callable = () -> {
                for (int i = 1; i <= numberOfPages; i++) {
                    String formatted = String.format("%03d", i);
                    String url = new StringBuffer().append(nbpString).append(formatted).append("z").append(parsedDate).append(".xml").toString();
                    if (URLExists(url)) {
                        return url;
                    }
                }

                return "";
            };

            taskList.add(callable);
        }

        List<Future<String>> futures = null;
        try {
            futures = executorService.invokeAll(taskList);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Future<String> future : futures) {
            try {
                nbpURLs.add(future.get());
            } catch (Exception e) {

            }
        }
        executorService.shutdown();

    }


    private String parseLocalData(LocalDate localDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd");

            return sdf.format(sdf.parse(localDate.toString())).replace("-", "");
        } catch (ParseException e) {
        }
        return "";
    }

    private boolean URLExists(String targetUrl) {
        HttpURLConnection urlConnection;
        try {
            urlConnection = (HttpURLConnection) new
                    URL(targetUrl).openConnection();
            urlConnection.setRequestMethod("HEAD");
            urlConnection.setConnectTimeout(200);
            urlConnection.setReadTimeout(200);
            return (urlConnection.getResponseCode() ==
                    HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            return false;
        }
    }


    private static Document getDocument(String fileUri) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(fileUri);
        return doc;
    }


    private static Optional<Currency> getCurrencyFromDocument(String fileUri) {
        Optional<Currency> currency = Optional.ofNullable(null);

        try {
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            XPathExpression expressionBuyRate = xpath.compile(getExpressionBuyRate());
            XPathExpression expressionSellRate = xpath.compile(getExpressionSellRate());

            Node nodeBuy = (Node) expressionBuyRate.evaluate(getDocument(fileUri), XPathConstants.NODE);
            Node nodeSell = (Node) expressionSellRate.evaluate(getDocument(fileUri), XPathConstants.NODE);

            NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
            Double buyRate = new Double(String.valueOf(nf.parse(nodeBuy.getNodeValue())));
            Double sellRate = new Double(String.valueOf(nf.parse(nodeSell.getNodeValue())));
            currency = Optional.of(new Currency(currencyCode, buyRate, sellRate));
        } catch (Exception e) {
        }

        return currency;
    }

    public List<Optional<Currency>> getCurrencies() {
        List<Optional<Currency>> currencies = nbpURLs.stream().map(NBPDocumentParser::getCurrencyFromDocument).collect(Collectors.toList());
        return currencies;
    }

    private static String getExpressionBuyRate() {
        final String uri = "/tabela_kursow/pozycja[kod_waluty='" + currencyCode + "']/kurs_kupna/text()";
        return uri;
    }

    private static String getExpressionSellRate() {
        final String uri = "/tabela_kursow/pozycja[kod_waluty='" + currencyCode + "']/kurs_sprzedazy/text()";
        return uri;
    }

}

