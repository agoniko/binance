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

public class FiftMinUpBuy extends Thread {
	private static BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(
			"m4SuuUwia895qcjuM9fwz54A8bYiAY9I6kv4P0OtG3bs2BOyuqNbu57oYD6ng4Z0",
			"vUpBhifeRJR7R3va0VI4WA489XNwEVtXDYK7OsmZ9AS7KKDmtDQ5TZA3tQtHB8Te");

	private BinanceApiRestClient client;
	private CandlestickInterval interval = CandlestickInterval.FIFTEEN_MINUTES;

	private double balance = 50.0;
	private Double amount = null;
	Long currentTime;

	public FiftMinUpBuy() {
		client = factory.newRestClient();
	}

	@Override
	public void run() {
		int cont = 0;
		ArrayList<String> symbols = MyFunctions.getAllUSDTSymbol(client);
		ArrayList<ArrayList<Candlestick>> allCandles = new ArrayList<ArrayList<Candlestick>>(symbols.size());
		for (int i = 0; i < symbols.size(); i++) {
			ArrayList<Candlestick> candles = (ArrayList<Candlestick>) client.getCandlestickBars(symbols.get(i),
					interval);
			if (candles.get(candles.size() - 1).getCloseTime() > System.currentTimeMillis())
				allCandles.add(candles);
			cont++;
			if (cont > 5) {
				System.out.print(".");
				cont = 0;
			}
		}
		System.out.println();
		allCandles = sort(allCandles);

		for (int i = 0; i < allCandles.size(); i++) {
			int size = allCandles.get(i).size();
			System.out.println(new Timestamp(allCandles.get(i).get(size - 1).getCloseTime()));
		}

		cont = 0;
		currentTime = allCandles.get(cont).get(0).getOpenTime();
		boolean comprato = false;
		Long firstBuy = null;
		Long lastSell = currentTime;
		while (currentTime < System.currentTimeMillis() - 900000) {
			for (int i = 0; i < allCandles.size(); i++) {
				BuyAsset asset = checkForBuy(allCandles.get(i), currentTime);
				if (asset != null) {
					if (!comprato) {
						comprato = !comprato;
						firstBuy = asset.buyTime;
					}
					aggiornaSaldo(asset.price);
					lastSell = checkForSell(allCandles.get(i), asset);
					if (lastSell == null) {
						lastSell = currentTime;
					}

					System.out.println("saldo: " + balance + " time: " + new Timestamp(lastSell));
				}
			}
			cont++;
			currentTime += 900000;
		}
		System.out.println("start time: " + new Timestamp(firstBuy));
		System.out.println("Last sell: " + new Timestamp(lastSell));
	}

	public Long checkForSell(ArrayList<Candlestick> candles, BuyAsset asset) {
		int cont = 0;
		while (asset.buyTime > candles.get(cont).getCloseTime()) {
			cont++;
		}
		System.out.println("check for sell");
		double stopPrice = asset.price * 98.5 / 100;
		double oldPrice = asset.price;
		for (int i = cont; i < candles.size(); i++) {
			System.out.print(".");
			ArrayList<Candlestick> subList = new ArrayList<Candlestick>(candles.subList(0, i + 1));
			BollingerBand bands = new BollingerBand(subList);
			double closePrice = Double.parseDouble(candles.get(i).getClose());
			aggiornaSaldo(closePrice);
			if (closePrice <= stopPrice) {
				setAmountToNull();
				currentTime = candles.get(i).getCloseTime();
				System.out.println();
				return currentTime;
			} else if (closePrice > oldPrice) {
				stopPrice = closePrice * 98.5 / 100;
			}
			oldPrice = closePrice;
		}
		setAmountToNull();
		System.out.println();
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
