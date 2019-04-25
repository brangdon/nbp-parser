package pl.parser.nbp;


public class MainClass {

    public static void main(String[] args) throws Exception {

        NBPDocumentParser nbpDocumentParser = new NBPDocumentParser(args[0], args[1], args[2]);

        NBPCurrencyStatistics nbpCurrencyStatistics = new NBPCurrencyStatistics();

        System.out.println(nbpCurrencyStatistics.getBuyingMean(nbpDocumentParser.getCurrencies()));
        System.out.println(nbpCurrencyStatistics.getSellingStandardDeviance(nbpDocumentParser.getCurrencies()));

    }


}
