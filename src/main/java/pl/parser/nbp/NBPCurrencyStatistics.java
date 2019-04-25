package pl.parser.nbp;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class NBPCurrencyStatistics {

    public String getBuyingMean(List<Optional<Currency>> currencies) {
        DecimalFormat df = new DecimalFormat("#.####");
        return df.format(currencies.stream().filter(Optional::isPresent).map(Optional::get).mapToDouble(Currency::getBuyRate)
                .average().orElse(0.0));
    }

    public String getSellingStandardDeviance(List<Optional<Currency>> currencies) {
        double standardDeviation = 0.0;
        long length = currencies.stream().filter(Optional::isPresent).map(Optional::get).mapToDouble(Currency::getBuyRate).count();


        Double sum = currencies.stream().filter(Optional::isPresent).map(Optional::get).mapToDouble(Currency::getSellRate).sum();

        double mean = sum / length;

        for (Optional<Currency> currency : currencies) {
            if (currency.isPresent())
                standardDeviation += Math.pow(currency.get().getSellRate() - mean, 2);
        }

        DecimalFormat df = new DecimalFormat("#.####");
        return df.format(Math.sqrt(standardDeviation / length));

    }

}
