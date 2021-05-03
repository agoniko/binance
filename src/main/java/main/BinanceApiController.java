package main;

import java.util.ArrayList;

import org.glassfish.jersey.message.internal.NewCookieProvider;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.exception.BinanceApiException;

public class BinanceApiController extends Thread {
	private static BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(
			"m4SuuUwia895qcjuM9fwz54A8bYiAY9I6kv4P0OtG3bs2BOyuqNbu57oYD6ng4Z0",
			"vUpBhifeRJR7R3va0VI4WA489XNwEVtXDYK7OsmZ9AS7KKDmtDQ5TZA3tQtHB8Te");

	final CandlestickInterval interval = CandlestickInterval.FOUR_HOURLY;
	static BinanceApiRestClient client = null;

	int errors = 0;

	public BinanceApiController() {
		client = clientInitialization();
	}

	public static BinanceApiRestClient clientInitialization() {
		return factory.newRestClient();
	}

	public static BinanceApiRestClient getClient() {
		return client;
	}

	@Override
	public void run() {
		BotController bot = BotController.getInstance();
		ArrayList<String> symbols = MyFunctions.getAllUSDTSymbol(client);
		ArrayList<String> buySymbols = new ArrayList<String>();
		ArrayList<String> sellSymbols = new ArrayList<String>();
		ArrayList<String> bollingerBuySymbols = new ArrayList<String>();
		ArrayList<String> bollingerSellSymbols = new ArrayList<String>();
		bot.sendSignal("Inizio ora con intervallo: " + interval.toString());
		bot.sendBuyButton("DOGEUSDT ha rotto le palle");
		while (true) {
			for (int i = 0; i < symbols.size(); i++) {
				String symbol = symbols.get(i);
				ArrayList<Candlestick> candles = (ArrayList<Candlestick>) client.getCandlestickBars(symbol, interval);
				if (candles.size() > 20) {
					BollingerBand bands = new BollingerBand(candles);
					double price = Double.parseDouble(candles.get(candles.size() - 1).getClose());

					if (!buySymbols.contains(symbol)) {
						if (ultimeCandeleSopraLaMedia(5, candles)) {
							bot.sendBuyButton(symbol + " ha rotto la resistenza");
							buySymbols.add(symbol);
						}
					}

					if (Oscillators.getRSI14(candles) < 30 && price < bands.getLower()
							&& !bollingerBuySymbols.contains(symbol)) {
						bollingerBuySymbols.add(symbol);
					}

					if (bollingerBuySymbols.contains(symbol) && Oscillators.getRSI14(candles) > 30) {
						bot.sendSignal(symbol + " aveva rotto il supporto ma ora rsi e tornato sopra a 30");
					}
				}
			}
		}
	}

	private void checkForAsset(String symbol, BinanceApiRestClient client) {
		Thread assetChecker = new AssetChecker(symbol, client);
		BotController bot = BotController.getInstance();
		assetChecker.start();
		try {
			assetChecker.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			bot.sendSignal("Eccezione nel checker dell'asset");
		}
		System.out.println("Riprendo");
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
			if (price < bands.getMiddle() || Oscillators.getRSI14(subList) < 70) {
				return false;
			}
		}
		return true;
	}

}
