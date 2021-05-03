package main;

import java.util.ArrayList;

import com.binance.api.client.domain.market.Candlestick;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

public interface MovingAverages {

	// Returns the percentage of indicators that are under the price passed as
	// argument
	static double percentOfBuySignals(ArrayList<Candlestick> candles) {
		double price = Double.parseDouble(candles.get(candles.size() - 1).getClose());
		int result = 0;
		int size = candles.size();
		if (size > 10 && price > getSMA(candles, 10))
			result++;
		if (size > 20 && price > getSMA(candles, 20))
			result++;
		if (size > 30 && price > getSMA(candles, 30))
			result++;
		if (size > 50 && price > getSMA(candles, 50))
			result++;
		if (size > 100 && price > getSMA(candles, 100))
			result++;
		if (size > 10 && price > getEMA(candles, 10))
			result++;
		if (size > 20 && price > getEMA(candles, 20))
			result++;
		if (size > 30 && price > getEMA(candles, 30))
			result++;
		if (size > 50 && price > getEMA(candles, 50))
			result++;
		if (size > 100 && price > getEMA(candles, 100))
			result++;
		return (double) ((result * 100) / 10);
	}

	static double getSMA(ArrayList<Candlestick> candles, int n) {
		double result = 0;

		for (int i = candles.size() - n; i < n; i++) {
			try {
				double close = Double.parseDouble(candles.get(i).getClose());
				result += close;

			} catch (java.lang.IndexOutOfBoundsException e) {
				System.out.println("err");
				i = n - 1;
			}
		}
		return result / n;

	}

	static Double getEMA(ArrayList<Candlestick> candles, int n) {
		double prices[] = new double[candles.size()];
		int cont = 0;
		for (int i = 0; i < candles.size(); i++) {
			prices[cont] = Double.parseDouble(candles.get(i).getClose());
			cont++;
		}
		Core c = new Core();
		MInteger begin = new MInteger();
		MInteger length = new MInteger();
		begin.value = -1;
		length.value = -1;
		RetCode retCode = RetCode.InternalError;
		int resultSize = c.rsiLookback(n);
		double tempOutPut[] = new double[resultSize];
		retCode = c.ema(prices.length - n, prices.length - 1, prices, n, begin, length, tempOutPut);
		if (retCode.toString().equals("Success")) {
			return tempOutPut[resultSize - 1];
		} else {
			return null;
		}
	}

}
