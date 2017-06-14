package main;

import java.io.IOException;
import java.util.ArrayList;

import controller.CommandSender;
import controller.Device;
import controller.DeviceScanner;

public class Main {

	public static int cmdId = 0;

	public static void main(String[] args) {
		DeviceScanner ds = new DeviceScanner();
		CommandSender cs = new CommandSender();

		// Scan devices
		try {
			ds.scanDevices();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		while (ds.getListDevice().size() == 0) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		ds.getArrayDevice();
		Device d = ds.getListDevice().get(0);

		ArrayList<String> onParams = new ArrayList<String>();
		onParams.add("on");
		onParams.add("smooth");
		onParams.add("200");
		ArrayList<String> offParams = new ArrayList<String>();
		offParams.add("off");
		offParams.add("smooth");
		offParams.add("200");
		ArrayList<String> ctParams = new ArrayList<String>();
		ctParams.add("6000");
		ctParams.add("smooth");
		ctParams.add("200");
		ArrayList<String> rgbParams = new ArrayList<String>();
		rgbParams.add(String.valueOf(255 * 256 * 256 + 255 * 256 + 255));
		rgbParams.add("smooth");
		rgbParams.add("200");
		ArrayList<String> brightParams = new ArrayList<String>();
		brightParams.add("100");
		brightParams.add("smooth");
		brightParams.add("200");

		try {
			System.out.println(cs.sendCommand(d, "set_rgb", rgbParams));
		} catch (NumberFormatException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
		}

		try {
			ds.stopScanning();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
