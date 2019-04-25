package pl.parser.nbp;

import java.io.Serializable;

/**
 * Created by admin on 22.04.2019.
 */

public final class Currency implements Serializable {
    private final String currencyCode;
    private final Double sellRate;
    private final Double buyRate;

    public Currency(String currencyCode, Double buyRate, Double sellRate) {
        this.currencyCode = currencyCode;
        this.sellRate = sellRate;
        this.buyRate = buyRate;
    }

    public Double getSellRate() {
        return sellRate;
    }

    public Double getBuyRate() {
        return buyRate;
    }

    @Override
    public String toString() {
        return "Currency{" +
                "currencyCode='" + currencyCode + '\'' +
                ", sellRate=" + sellRate +
                '}';
    }
}
