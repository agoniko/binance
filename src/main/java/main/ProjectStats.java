package main;

public class ProjectStats {

	private static ProjectStats instance;
	private double usdt;
	private Double amount;

	public static ProjectStats getInstance() {
		if (instance == null)
			instance = new ProjectStats();
		return instance;
	}

	private ProjectStats() {
		usdt = 50.0;
	}

	public void aggiornaSaldo(double price) {
		setAmount(price);
		usdt = amount * price;
	}

	void setAmountToNull() {
		this.amount = null;
	}
	
	private void setAmount(double price) {
		if (this.amount == null) {
			this.amount = usdt / price;

		}
	}

	public double getSaldo() {
		return usdt;
	}

}
