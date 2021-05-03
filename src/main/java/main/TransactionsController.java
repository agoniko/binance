package main;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.Account;

public class TransactionsController {

	public static boolean buy(String symbol, double perc) {
		BinanceApiRestClient client = BinanceApiController.getClient();
		System.out.println("ciao1");
		System.out.println(client.getPrice(symbol).getPrice());
		Account account = client.getAccount((long) 59999, System.currentTimeMillis());
		System.out.println("ciaoooo");
		double saldo = Double.parseDouble(account.getAssetBalance("USDT").getFree());
		double amount = saldo * perc / 100;
		System.out.println("saldo: " + saldo);
		System.out.println("amount: " + amount);
		return true;
	}

}
