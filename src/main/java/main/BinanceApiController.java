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
	BinanceApiRestClient client;

	int errors = 0;

	public BinanceApiController() {
		client = clientInitialization();
	}

	public static BinanceApiRestClient clientInitialization() {
		return factory.newRestClient();
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

		while (true) {
			for (int i = 0; i < symbols.size(); i++) {
				String symbol = symbols.get(i);
				ArrayList<Candlestick> candles = (ArrayList<Candlestick>) client.getCandlestickBars(symbol, interval);
				if (candles.size() > 25) {
					if (Oscillators.getRSI14(candles) < 30) {
						bot.sendSignal(symbol + "Ha RSI14 < 30 su candele da 4h");
					}
					if (Oscillators.getRSI6(candles) < 30) {
						bot.sendSignal(symbol + "Ha RSI6 < 30 su candele da 4h");
					}
				}
			}
			System.out.println("Checcati tutti");
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

}
