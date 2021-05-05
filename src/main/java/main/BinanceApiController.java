package main;

import java.util.ArrayList;

import org.glassfish.jersey.message.internal.NewCookieProvider;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.OrderType;
import com.binance.api.client.domain.TimeInForce;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.general.ExchangeFilter;
import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.domain.general.FilterType;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.exception.BinanceApiException;
import com.binance.api.client.domain.general.SymbolFilter;
import com.binance.api.client.domain.general.SymbolInfo;
import com.binance.api.client.domain.general.RateLimit;
import static com.binance.api.client.domain.account.NewOrder.limitBuy;
import static com.binance.api.client.domain.account.NewOrder.marketBuy;

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

	public Account getAccount() {
		Long recWindow = (long) 59999;
		Account account = client.getAccount(recWindow, System.currentTimeMillis());
		return account;
	}

	@Override
	public void run() {
		Long recWindow = (long) 59999;

		BotController bot = BotController.getInstance();
		ArrayList<String> symbols = MyFunctions.getAllUSDTSymbol(client);

		for (int i = 0; i < symbols.size(); i += 5) {
			String symbol = symbols.get(i);
			Account account = getAccount();
			Double saldo = Double.parseDouble(account.getAssetBalance("USDT").getFree());
			String quote = symbol.substring(0, symbol.indexOf("USDT"));
			Double price = Double.parseDouble(client.getPrice(symbol).getPrice());
			String quantity = getBuyQuantity(25.0, saldo, price);
			NewOrder Order = new NewOrder(symbol, OrderSide.BUY, OrderType.MARKET, null, quantity)
					.recvWindow(recWindow);
			client.newOrder(Order);

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			String balance = getSellQuantity(quote, price);
			Order = new NewOrder(symbol, OrderSide.SELL, OrderType.MARKET, null, balance).recvWindow(recWindow);
			System.out.println(quote + ": " + balance);
			// client.newOrder(Order);
		}

	}

	private String getSellQuantity(String quote, Double price) {
		Account account = getAccount();
		String balance = account.getAssetBalance(quote).getFree();
		if (price < 1) {
			balance = balance.substring(0, balance.indexOf("."));
		} else {
			int cont = price.toString().indexOf(".") + 1;
			balance = balance.substring(0, balance.indexOf(".") + cont);
		}
		return balance;
	}

	private String getBuyQuantity(double perc, double saldo, Double price) {
		Double amount = (saldo * (perc) / 100) / price;
		String am = amount.toString();
		int cont;
		if (price > 1) {
			cont = price.toString().indexOf(".") + 2;
			am = am.substring(0, am.indexOf(".") + cont);
		} else {
			am = am.substring(0, am.indexOf("."));
		}
		return am;
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
			if (price < bands.getMiddle() || Oscillators.getRSI14(subList) > 70) {
				return false;
			}
		}
		return true;
	}

}
