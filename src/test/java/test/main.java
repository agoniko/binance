package test;

import main.BinanceApiController;

public class main {

	public static void main(String[] args) {
		Thread t = new realTimeChecker();
		t.start();
	}

}
