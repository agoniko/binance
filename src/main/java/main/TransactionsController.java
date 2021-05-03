package main;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.general.ExchangeFilter;
import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.domain.general.SymbolInfo;
import com.binance.api.client.domain.market.TickerPrice;
import com.binance.api.client.domain.market.TickerStatistics;

import static com.binance.api.client.domain.account.NewOrder.limitBuy;
import static com.binance.api.client.domain.account.NewOrder.marketBuy;

public class TransactionsController {

	public static boolean buy(String symbol, double perc) {
		BinanceApiRestClient client = BinanceApiController.getClient();
		double price = Double.parseDouble(client.getPrice(symbol).getPrice());
		Account account = client.getAccount((long) 59999, System.currentTimeMillis());
		double saldo = Double.parseDouble(account.getAssetBalance("USDT").getFree());
		double amount = saldo * perc / 100;
		// NewOrderResponse newOrderResponse = client.newOrder(marketBuy("LINKETH",
		// "1000"));
		return false;
	}

	public static boolean sell(String symbol, double perc) {
		BinanceApiRestClient client = BinanceApiController.getClient();
		TickerPrice ticker = client.getPrice(symbol);
		ExchangeInfo info = client.getExchangeInfo();
		SymbolInfo sinfo = info.getSymbolInfo(symbol);
		System.out.println(sinfo.getBaseAssetPrecision() + " " + sinfo.getQuotePrecision());

		double price = Double.parseDouble(ticker.getPrice());
		Account account = client.getAccount((long) 59999, System.currentTimeMillis());
		double saldo = Double.parseDouble(account.getAssetBalance("USDT").getFree());
		double amount = saldo * perc / 100;
		// client.newOrderTest(marketBuy(symbol, "1000"));
		return false;
	}

}
