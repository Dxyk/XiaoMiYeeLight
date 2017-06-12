package controller;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class DeviceScanner {

	private ExecutorService executor = null;
	private final Scanner scanner = new Scanner();

	/**
	 * Starts a new thread and scan
	 * 
	 * @throws InterruptedException
	 */
	public void scanDevices() throws InterruptedException {
		executor = Executors.newSingleThreadExecutor();
		executor.execute(scanner);
	}

	/**
	 * Interrupt the thread and stop scanning
	 * 
	 * @throws InterruptedException
	 */
	public void stopScanning() throws InterruptedException {
		executor.awaitTermination(2000, TimeUnit.MILLISECONDS);
		executor.shutdownNow();
		while (!executor.isTerminated()) {
			Thread.sleep(1000);
			executor.shutdownNow();
		}
	}
	
	/**
	 * @return the Json Array of devices
	 */
	public JsonArray getArrayDevice() {
		JsonArray arr = new JsonArray();
		for (String key : scanner.deviceList.keySet()) {
			Gson gson = new Gson();
			JsonElement json = gson.toJsonTree(scanner.deviceList.get(key));
			JsonObject jsonObj = json.getAsJsonObject();
			arr.add(jsonObj);
		}
		System.out.println(arr.toString());
		return arr;
	}
	
	/**
	 * @return an array list of devices
	 */
	public ArrayList<Device> getListDevice() {
		ArrayList<Device> result = new ArrayList<>();
		for (String key : scanner.deviceList.keySet()) {
			result.add(scanner.deviceList.get(key));
		}
		return result;
	}
	
	/**
	 * @return return a string of all devices
	 */
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
