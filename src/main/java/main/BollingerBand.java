package main;

import java.sql.Timestamp;
import java.util.ArrayList;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;

public class BollingerBand {
	private Double upper, middle, lower;

	public double getUpper() {
		return upper;
	}

	public double getMiddle() {
		return middle;
	}

	public double getLower() {
		return lower;
	}

	public BollingerBand(ArrayList<Candlestick> candles) {
		if (candles.size() < 21) {
			middle = lower = upper = null;
			System.out.println("error");
			return;
		}
		middle = upper = lower = 0.0;
		double standardDeviation = 0;
		for (int i = candles.size() - 1; i > candles.size() - 22; i--) {
			middle += Double.parseDouble(candles.get(i).getClose());
		}
		middle /= 21;

		for (int i = candles.size() - 1; i > candles.size() - 22; i--) {
			double closePrice = Double.parseDouble(candles.get(i).getClose());
			standardDeviation += Math.pow(closePrice - middle, 2.0);
		}

		standardDeviation /= 21;
		standardDeviation = Math.sqrt(standardDeviation);

		upper = middle + 2 * standardDeviation;
		lower = middle - 2 * standardDeviation;
	}
}
