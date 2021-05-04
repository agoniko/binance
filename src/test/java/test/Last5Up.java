package test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;

import main.BollingerBand;
import main.MyFunctions;
import main.Oscillators;

public class Last5Up extends Thread {

	private static BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(
			"m4SuuUwia895qcjuM9fwz54A8bYiAY9I6kv4P0OtG3bs2BOyuqNbu57oYD6ng4Z0",
			"vUpBhifeRJR7R3va0VI4WA489XNwEVtXDYK7OsmZ9AS7KKDmtDQ5TZA3tQtHB8Te");

	private BinanceApiRestClient client;
	private CandlestickInterval interval = CandlestickInterval.FOUR_HOURLY;

	private double balance = 50.0;
	private Double amount = null;
	Long currentTime;

	public Last5Up() {
		client = factory.newRestClient();
	}

	@Override
	public void run() {
		int cont = 0;
		ArrayList<String> symbols = MyFunctions.getAllUSDTSymbol(client);
		ArrayList<ArrayList<Candlestick>> allCandles = new ArrayList<ArrayList<Candlestick>>(symbols.size());
		for (int i = 0; i < symbols.size(); i++) {
			allCandles.add((ArrayList<Candlestick>) client.getCandlestickBars(symbols.get(i), interval));
			cont++;
			if (cont > 5) {
				System.out.print(".");
				cont = 0;
			}
		}
		System.out.println();
		allCandles = sort(allCandles);
		currentTime = allCandles.get(0).get(0).getOpenTime();
		for (int i = 0; i < allCandles.size(); i++) {
			ArrayList<Candlestick> candles = allCandles.get(i);
			for (int j = 0; j < candles.size(); j++) {
				if (candles.get(j).getOpenTime() >= currentTime) {
					candles = new ArrayList<Candlestick>(candles.subList(j, candles.size()));
					break;
				}
			}
			if (candles.size() > 100) {
				BuyAsset asset = checkForBuy(candles);
				if (asset.buyTime != null) {
					currentTime = asset.buyTime;
					if (candles.get(0).getCloseTime() < asset.buyTime) {
						for (int j = 0; j < candles.size(); j++) {
							if (candles.get(j).getCloseTime() > asset.buyTime) {
								candles = new ArrayList<Candlestick>(candles.subList(j, candles.size()));
								break;
							}
						}
						Long sellTime = checkForSell(candles, asset.price);
						System.out.println("saldo: " + balance);
						System.out.println("comprato: " + new Timestamp(asset.buyTime));
						if (sellTime != null) {
							System.out.println("venduto: " + new Timestamp(sellTime));
						} else {
							System.out.println("Non ci sono abbastanza dati per sapere se hai venduto");
						}
						System.out.println();

					}

				}
			}
		}

	}

	public BuyAsset checkForBuy(ArrayList<Candlestick> candles) {
		for (int cont = 30; cont < candles.size(); cont++) {
			double price = Double.parseDouble(candles.get(cont).getClose());
			ArrayList<Candlestick> subList = new ArrayList<Candlestick>(candles.subList(0, cont + 1));
			if (ultimeCandeleSopraLaMedia(5, subList)) {
				return new BuyAsset(subList.get(cont).getCloseTime(), price);
			}
		}
		return null;
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
			if (price < bands.getMiddle() || Oscillators.getRSI14(subList) > 70) {
				return false;
			}
		}
		return true;
	}

	private Long checkForSell(ArrayList<Candlestick> candles, double price) {
		double stopLoss = 3.0;
		double takeProfit = 6.0;

		double stopPrice = price - (price * stopLoss / 100);
		double profitPrice = price + (price * takeProfit / 100);
		for (int i = 0; i < candles.size(); i++) {
			double currentPrice = Double.parseDouble(candles.get(i).getClose());
			double highPrice = Double.parseDouble(candles.get(i).getHigh());
			double lowPrice = Double.parseDouble(candles.get(i).getLow());
			aggiornaSaldo(currentPrice);
			// System.out.println(highPrice + " " + profitPrice + " " + lowPrice + " " +
			// stopPrice);
			if (highPrice >= profitPrice) {
				aggiornaSaldo(highPrice);
				setAmountToNull();
				currentTime = candles.get(i).getCloseTime();
				return candles.get(i).getCloseTime();
			}
			if (currentPrice <= stopPrice) {
				aggiornaSaldo(currentPrice);
				setAmountToNull();
				currentTime = candles.get(i).getCloseTime();
				return candles.get(i).getCloseTime();
			}
		}
		setAmountToNull();
		return null;
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

}
