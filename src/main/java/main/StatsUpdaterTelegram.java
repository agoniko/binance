package main;

import java.text.DecimalFormat;

public class StatsUpdaterTelegram extends Thread {
	BotController bot;
	ProjectStats stats;

	public StatsUpdaterTelegram() {
		bot = BotController.getInstance();
		stats = ProjectStats.getInstance();
	}

	@Override
	public void run() {
		final DecimalFormat df = new DecimalFormat("0.00");
		while (true) {
			try {
				sleep(600000);
				bot.sendSignal("SALDO: " + df.format(stats.getSaldo()));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
