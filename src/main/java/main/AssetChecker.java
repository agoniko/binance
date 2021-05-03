package main;

import com.binance.api.client.BinanceApiRestClient;

public class AssetChecker extends Thread {

	String symbol;
	BinanceApiRestClient client;

	public AssetChecker(String symbol, BinanceApiRestClient client) {
		this.client = client;
		this.symbol = symbol;
	}

	@Override
	public void run() {
		double boughtPrice = Double.parseDouble(client.getPrice(symbol).getPrice());
		double limitPrice = boughtPrice * 103 / 100;
		double stopLoss = boughtPrice * 98 / 100;
		ProjectStats stats = ProjectStats.getInstance();
		BotController bot = BotController.getInstance();
		while (true) {
			double price = Double.parseDouble(client.getPrice(symbol).getPrice());
			stats.aggiornaSaldo(price);
			if (price >= limitPrice) {
				bot.sendSignal(symbol + " +3%");
				bot.sendSignal("SALDO: " + stats.getSaldo());
				stats.setAmountToNull();
				break;
			}
			if (price <= stopLoss) {
				bot.sendSignal(symbol + " -2%");
				bot.sendSignal("SALDO: " + stats.getSaldo());
				stats.setAmountToNull();
				break;
			}
		}
	}

}
