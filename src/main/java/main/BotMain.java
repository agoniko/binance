package main;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

public class BotMain {
	

	public static void main(String[] args) {
		ApiContextInitializer.init();
		TelegramBotsApi api = new TelegramBotsApi();
		BotController bot = BotController.getInstance();
		try {
			api.registerBot(bot);
		} catch (TelegramApiRequestException e) {
			e.printStackTrace();
		}
		Thread t = new BinanceApiController();
		t.start();

	}

}
