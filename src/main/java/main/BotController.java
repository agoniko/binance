package main;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

public class BotController extends TelegramLongPollingBot {

	public static BotController instance = null;
	private TelegramBotsApi api;

	public static BotController getInstance() {
		if (instance == null) {
			instance = new BotController();
		}
		return instance;
	}

	private BotController() {
		
	}

	public String getBotUsername() {
		return "Echo Bot";
	}

	@Override
	public String getBotToken() {
		return "1718479498:AAHKf7tBQ8q3QBUgXbHhes87VEmHBvn4N3Y";
	}

	public void sendSignal(String msg) {
		String chatId = "567302496";
		SendMessage sendMessage = new SendMessage();
		sendMessage.setChatId(chatId);
		sendMessage.setText(msg);
		try {
			execute(sendMessage);
		} catch (TelegramApiException e) {
			// gestione errore in invio
		}
	}

	public void onUpdateReceived(Update update) {
		String msg = update.getMessage().getText();
		String chatId = update.getMessage().getChatId().toString();
		SendMessage sendMessage = new SendMessage();
		sendMessage.setChatId(chatId);
		sendMessage.setText(msg);
		try {
			execute(sendMessage);
		} catch (TelegramApiException e) {
			// gestione errore in invio
		}
	}
}
