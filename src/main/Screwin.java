package main;

import java.io.IOException;
import java.util.ArrayList;

import controller.CommandSender;
import controller.Device;
import controller.DeviceScanner;

public class Screwin {

	public static int cmdId = 0;

	public static String getClientMessage() {
		return "Usage:" + "\n\t q|quit: quit bulb manager"
				+ "\n\t h|help: print this message" + "\n\t r|refresh: refresh bulb list"
				+ "\n\t l|list: list all managed bulbs"
				+ "\n\t t|toggle <idx>: toggle bulb <idx>"
				+ "\n\t b|bright <idx> <brightness>: set brightness of bulb <idx>"
				+ "\n\t\t brightness: 1 ~ 100"
				+ "\n\t ct|color temperature <idx> <value>: change the color temperature of bulb <idx>"
				+ "\n\t\t value: 1700 ~ 6500"
				+ "\n\t rgb| <idx> <r> <g> <b>: change the rgb value of the bulb <idx>"
				+ "\n\t\t r: 1 ~ 255; g: 1 ~ 255; b: 1 ~ 255"
				+ "\n\t hsv| <idx> <hue> <sat>: change the hue and the saturation of the bulb <idx>"
				+ "\n\t\t hue: 0 ~ 359; sat: 0 ~ 100" + "";
	}

	public static void main(String[] args) {
		DeviceScanner ds = new DeviceScanner();
		CommandSender cs = new CommandSender();

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

		try {
			System.out.println(cs.sendCommand(d, "set_power", offParams));

			Thread.sleep(1000);

			cs.flash(ds.getListDevice());

			Thread.sleep(3000);

			System.out.println("===========Interrupting============");

			ArrayList<String> params = new ArrayList<>();
			params.add("100");
			params.add("smooth");
			params.add("200");
			System.out.println(cs.sendCommand(d, "set_bright", params));

//			System.out.println(cs.sendCommand(d, "toggle", new ArrayList<String>()));
//			System.out.println(cs.sendCommand(d, "set_rgb", params));
//			System.out.println(cs.sendCommand(d, "toggle", new ArrayList<String>()));
		} catch (NumberFormatException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			ds.stopScanning();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
