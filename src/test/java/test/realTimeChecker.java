package test;

import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.util.ArrayList;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;

import main.BollingerBand;
import main.MyFunctions;
import main.Oscillators;

public class realTimeChecker extends Thread {
	private static BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(
			"m4SuuUwia895qcjuM9fwz54A8bYiAY9I6kv4P0OtG3bs2BOyuqNbu57oYD6ng4Z0",
			"vUpBhifeRJR7R3va0VI4WA489XNwEVtXDYK7OsmZ9AS7KKDmtDQ5TZA3tQtHB8Te");

	private BinanceApiRestClient client;
	private CandlestickInterval interval = CandlestickInterval.FIFTEEN_MINUTES;

	private double balance = 50.0;
	private Double amount = null;
	Long currentTime;

	public realTimeChecker() {
		client = factory.newRestClient();
	}

	@Override
	public void run() {
		ArrayList<String> symbols = MyFunctions.getAllUSDTSymbol(client);
		while (true) {
			for (int i = 0; i < symbols.size(); i++) {
				String symbol = symbols.get(i);
				ArrayList<Candlestick> candles = (ArrayList<Candlestick>) client.getCandlestickBars(symbol, interval);
				ArrayList<Candlestick> subList = new ArrayList<Candlestick>(candles.subList(0, candles.size() - 1));
				BollingerBand subBands = new BollingerBand(subList);

				double lastCandleClosePrice = Double.parseDouble(subList.get(subList.size() - 1).getClose());
				if (lastCandleClosePrice > subBands.getUpper()) {
					double price = Double.parseDouble(candles.get(candles.size() - 1).getClose());
					BollingerBand bands = new BollingerBand(candles);
					if (price > bands.getUpper()) {
						checkForSell(symbol);
						System.out.println("saldo: " + balance);
					}

				}

			}

		}
	}

	public void checkForSell(String symbol) {
		double price = Double.parseDouble(client.getPrice(symbol).getPrice());
		double stopPrice = price * 98 / 100;
		double oldPrice = price;
		System.out.println("Comprato: " + symbol + " a: " + price);

		while (true) {
			aggiornaSaldo(price);
			if (price <= stopPrice) {
				setAmountToNull();
				return;
			} else if (price > oldPrice) {
				stopPrice = price * 98.5 / 100;
			}
			oldPrice = price;
			price = Double.parseDouble(client.getPrice(symbol).getPrice());
		}
	}

	public void aggiornaSaldo(double price) {
		setAmount(price);
		balance = amount * price;
	}

	void setAmountToNull() {
		this.amount = null;
	}

	private void setAmount(double price) {
		if (this.amount == null) {
			this.amount = balance / price;
		}
	}

	BuyAsset checkForBuy(ArrayList<Candlestick> candles, long time) {
		if (candles.size() < 22) {
			return null;
		}
		ArrayList<Candlestick> subList;
		int cont = 0;
		while (time > candles.get(cont).getCloseTime()) {
			cont++;
		}
		subList = new ArrayList<Candlestick>(candles.subList(0, cont + 1));
		if (subList.size() < 21)
			return null;
		BollingerBand bands = new BollingerBand(subList);
		double closePrice = Double.parseDouble(candles.get(cont).getClose());
		if (closePrice > bands.getUpper()) {
			currentTime = candles.get(cont).getCloseTime();
			return new BuyAsset(candles.get(cont).getCloseTime(), closePrice);
		}
		return null;
	}

	private ArrayList<ArrayList<Candlestick>> sort(ArrayList<ArrayList<Candlestick>> allCandles) {
		boolean scambiato = true;
		while (scambiato) {
			scambiato = false;
			for (int j = 0; j < allCandles.size() - 1; j++) {
				ArrayList<Candlestick> temp;
				if (allCandles.get(j).get(0).getOpenTime() > allCandles.get(j + 1).get(0).getOpenTime()) {
					temp = allCandles.get(j);
					allCandles.set(j, allCandles.get(j + 1));
					allCandles.set(j + 1, temp);
					scambiato = true;
				}
			}
		}
		return allCandles;
	}

	ArrayList<Candlestick> getCandlesAfterTime(ArrayList<Candlestick> candles, long time) {
		int cont = 0;
		while (candles.get(cont).getOpenTime() < time) {
			cont++;
		}
		// 21 is the minimum amount of candles needed to calculate the bollinger bands
		if (cont > 21)
			candles = new ArrayList<Candlestick>(candles.subList(cont - 21, candles.size()));
		return candles;
	}

	private boolean ultimeCandeleSopraLaMedia(int n, ArrayList<Candlestick> candles) {
		double currentPrice = Double.parseDouble(candles.get(candles.size() - 1).getClose());
		BollingerBand bbands = new BollingerBand(candles);
		if (currentPrice < bbands.getUpper()) {
			return false;
		}
		for (int i = candles.size() - 1; i > candles.size() - n; i--) {
			ArrayList<Candlestick> subList = new ArrayList<Candlestick>(candles.subList(0, i));
			double price = Double.parseDouble(subList.get(subList.size() - 1).getClose());
			BollingerBand bands = new BollingerBand(subList);
			if (price < bands.getMiddle()) {
				return false;
			}
		}
		return true;
	}
}
