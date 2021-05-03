package main;

import java.util.ArrayList;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.TickerStatistics;
import com.tictactec.ta.lib.*;

public interface Oscillators {

	static boolean breakSupport(String price, ArrayList<Candlestick> candles) {

		double currentPrice = Double.parseDouble(price);
		BollingerBand band = new BollingerBand(candles);
		Double lower = band.getLower();
		if (lower != null && currentPrice < lower)
			return true;
		return false;
	}

	static boolean breakResistance(String price, ArrayList<Candlestick> candles) {

		double currentPrice = Double.parseDouble(price);
		BollingerBand band = new BollingerBand(candles);
		Double upper = band.getUpper();
		if (upper != null && currentPrice > upper)
			return true;
		return false;
	}

	static Double getMACD(ArrayList<Candlestick> candles) {
		double inReal[] = new double[candles.size()];
		for (int i = 0; i < candles.size(); i++) {
			inReal[i] = Double.parseDouble(candles.get(i).getClose());
		}

		Core c = new Core();
		int optInFastPeriod = 12;
		int optInSlowPeriod = 26;
		int optInSignalPeriod = 9;
		MInteger outBegIdx = new MInteger();
		MInteger outNBElement = new MInteger();
		int size = candles.size();
		double outMACD[] = new double[size];
		double outMACDSignal[] = new double[size];
		double outMACDHist[] = new double[size];
		RetCode r = c.macd(0, candles.size() - 1, inReal, optInFastPeriod, optInSlowPeriod, optInSignalPeriod,
				outBegIdx, outNBElement, outMACD, outMACDSignal, outMACDHist);
		if (r.toString().equals("Success")) {
			return outMACDHist[outNBElement.value - 1];
		}
		return null;

	}

	static TickerStatistics getMinVolTraded(BinanceApiRestClient client) {
		ArrayList<TickerStatistics> allPrices = (ArrayList<TickerStatistics>) client.getAll24HrPriceStatistics();
		TickerStatistics min = allPrices.get(0);
		for (int i = 1; i < allPrices.size(); i++) {
			String symbol = allPrices.get(i).getSymbol();
			if (symbol.substring(symbol.length() - 4).equals("USDT")) {
				double minVolume = Double.parseDouble(min.getVolume());
				double thisVolume = Double.parseDouble(allPrices.get(i).getVolume());
				if (thisVolume < minVolume) {
					min = allPrices.get(i);
				}
			}
		}
		return min;
	}

	static TickerStatistics getMaxVolTraded(BinanceApiRestClient client) {
		ArrayList<TickerStatistics> allPrices = (ArrayList<TickerStatistics>) client.getAll24HrPriceStatistics();
		TickerStatistics max = allPrices.get(0);
		for (int i = 1; i < allPrices.size(); i++) {
			String symbol = allPrices.get(i).getSymbol();
			if (symbol.substring(symbol.length() - 4).equals("USDT")) {
				double maxVolume = Double.parseDouble(max.getVolume());
				double thisVolume = Double.parseDouble(allPrices.get(i).getVolume());
				if (thisVolume > maxVolume) {
					max = allPrices.get(i);
				}
			}
		}
		return max;
	}

	static Double getRSI14(ArrayList<Candlestick> candles) {
		int n = 14;
		Core c = new Core();
		double prices[] = new double[candles.size()];
		int cont = 0;
		for (int i = 0; i < candles.size(); i++) {
			prices[cont] = Double.parseDouble(candles.get(i).getClose());
			cont++;
		}

		MInteger begin = new MInteger();
		MInteger length = new MInteger();
		begin.value = -1;
		length.value = -1;
		RetCode retCode = RetCode.InternalError;
		int resultSize = candles.size();
		double[] tempOutPut = new double[resultSize];
		retCode = c.rsi(0, prices.length - 1, prices, n, begin, length, tempOutPut);
		if (retCode.toString().equals("Success")) {
			return tempOutPut[length.value - 1];
		} else {
			return null;
		}
	}

	static Double getRSI6(ArrayList<Candlestick> candles) {
		int n = 6;
		Core c = new Core();
		double prices[] = new double[candles.size()];
		int cont = 0;
		for (int i = 0; i < candles.size(); i++) {
			prices[cont] = Double.parseDouble(candles.get(i).getClose());
			cont++;
		}

		MInteger begin = new MInteger();
		MInteger length = new MInteger();
		begin.value = -1;
		length.value = -1;
		RetCode retCode = RetCode.InternalError;
		int resultSize = candles.size();
		double[] tempOutPut = new double[resultSize];
		retCode = c.rsi(0, prices.length - 1, prices, n, begin, length, tempOutPut);
		if (retCode.toString().equals("Success")) {
			return tempOutPut[length.value - 1];
		} else {
			return null;
		}
	}

}
