package main;

import java.util.ArrayList;
import java.util.Comparator;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.TickerPrice;
import com.binance.api.client.domain.market.TickerStatistics;
import com.tictactec.ta.*;
import com.tictactec.ta.lib.*;

public interface MyFunctions {

	static ArrayList<String> getAllUSDTSymbol(BinanceApiRestClient client) {
		ArrayList<TickerPrice> allPrices = (ArrayList) client.getAllPrices();
		ArrayList<String> symbols = new ArrayList<String>();
		ArrayList<TickerStatistics> tik = (ArrayList<TickerStatistics>) client.getAll24HrPriceStatistics();
		for (int i = 0; i < allPrices.size(); i++) {
			String symbol = allPrices.get(i).getSymbol();
			if (symbol.substring(symbol.length() - 4).equals("USDT")) {
				double volume = Double.parseDouble(tik.get(i).getVolume());
				if (symbol.length() > 8) {
					if (!symbol.substring(symbol.length() - 8).equals("DOWNUSDT")) {
						if (volume > 100000000) {
							symbols.add(allPrices.get(i).getSymbol());
						}
					}
				} else {
					if (volume > 100000000) {
						symbols.add(allPrices.get(i).getSymbol());
					}
				}
			}
		}
		symbols.sort(Comparator.naturalOrder());
		symbols.remove("EURUSDT");
		symbols.remove("USDCUSDT");
		symbols.remove("BUSDUSDT");
		symbols.remove("BKRWUSDT");
		symbols.remove("STORMUSDT");

		return symbols;
	}

}
