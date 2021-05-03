package main;

import java.util.ArrayList;
import java.util.List;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

public class BotController extends TelegramLongPollingBot {

	public static BotController instance = null;
	private TelegramBotsApi api;
	final String chatId = "567302496";

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
		SendMessage sendMessage = new SendMessage();
		sendMessage.setChatId(chatId);
		sendMessage.setText(msg);
		try {
			execute(sendMessage);
		} catch (TelegramApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void onUpdateReceived(Update update) {
		if (update.hasCallbackQuery()) {
			CallbackQuery callBack = update.getCallbackQuery();
			String msg = callBack.getData();
			String op = msg.substring(0, msg.indexOf(" "));
			if (op.equals("compra")) {
				String symbol = msg.substring(msg.indexOf(" ") + 1);
				TransactionsController.buy(symbol, 50);
			} else if (op.equals("vendi")) {
				String symbol = msg.substring(msg.indexOf(" ") + 1);
				TransactionsController.sell(symbol, 50);
			}
		}
	}

	public void sendBuyButton(String testo) {
		List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
		List<InlineKeyboardButton> buttons1 = new ArrayList<>();
		String symbol = testo.substring(0, testo.indexOf(" "));
		buttons1.add(new InlineKeyboardButton().setText("COMPRA").setCallbackData("compra " + symbol));
		buttons.add(buttons1);

		InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
		markupKeyboard.setKeyboard(buttons);
		SendMessage sendMessage = new SendMessage();
		sendMessage.setChatId(chatId);
		sendMessage.setText(testo);
		sendMessage.setReplyMarkup(markupKeyboard);
		try {
			execute(sendMessage);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	public void sendSellButton(String testo) {
		List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
		List<InlineKeyboardButton> buttons1 = new ArrayList<>();
		String symbol = testo.substring(0, testo.indexOf(" "));
		buttons1.add(new InlineKeyboardButton().setText("VENDI").setCallbackData("vendi " + symbol));
		buttons.add(buttons1);

		InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
		markupKeyboard.setKeyboard(buttons);
		SendMessage sendMessage = new SendMessage();
		sendMessage.setChatId(chatId);
		sendMessage.setText(testo);
		sendMessage.setReplyMarkup(markupKeyboard);
		try {
			execute(sendMessage);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
}
