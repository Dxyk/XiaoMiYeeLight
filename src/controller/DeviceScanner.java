package Things.XiaoMiYeeLight;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DeviceScanner {

	private ExecutorService executor = null;
	private final Scanner scanner = new Scanner();

	public void scanDevices() throws InterruptedException {
		executor = Executors.newSingleThreadExecutor();
		executor.execute(scanner);
	}

	public void stopScanning() throws InterruptedException {
		executor.awaitTermination(500, TimeUnit.MILLISECONDS);
		executor.shutdownNow();
		while (!executor.isTerminated()) {
			executor.shutdownNow();
		}
	}
	
	public ArrayList<Device> getListDevice() {
		ArrayList<Device> result = new ArrayList<>();
		for (String key : scanner.deviceList.keySet()) {
			result.add(scanner.deviceList.get(key));
		}
		return result;
	}
	
	public String listDevices() {
		String result = "";
		ArrayList<Device> list = getListDevice();
		
		for (int i = 0; i < list.size(); i ++) {
			result += i + ": ";
			result += list.get(i) + "\n";
		}
		return result;
	}

}
