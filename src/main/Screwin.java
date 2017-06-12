package main;

import java.io.IOException;
import java.util.ArrayList;

import controller.CommandSender;
import controller.Device;
import controller.DeviceScanner;

public class Screwin {

public static int cmdId = 0;
	
	public static String getClientMessage() {
		return "Usage:"
				+ "\n\t q|quit: quit bulb manager"
				+ "\n\t h|help: print this message"
				+ "\n\t r|refresh: refresh bulb list"
				+ "\n\t l|list: list all managed bulbs"
				+ "\n\t t|toggle <idx>: toggle bulb <idx>"
				+ "\n\t b|bright <idx> <brightness>: set brightness of bulb <idx>"
				+ "\n\t\t brightness: 1 ~ 100"
				+ "\n\t ct|color temperature <idx> <value>: change the color temperature of bulb <idx>"
				+ "\n\t\t value: 1700 ~ 6500"
				+ "\n\t rgb| <idx> <r> <g> <b>: change the rgb value of the bulb <idx>"
				+ "\n\t\t r: 1 ~ 255; g: 1 ~ 255; b: 1 ~ 255"
				+ "\n\t hsv| <idx> <hue> <sat>: change the hue and the saturation of the bulb <idx>"
				+ "\n\t\t hue: 0 ~ 359; sat: 0 ~ 100"
				+ "";
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
		
		Device d = ds.getListDevice().get(0);
		
//		try {
//			cs.sendCommand(d, "toggle", new ArrayList<String>());
//		} catch (NumberFormatException e2) {
//			e2.printStackTrace();
//		} catch (IOException e2) {
//			e2.printStackTrace();
//		}
		
		int count = 0;
//		int red = 255;
//		int green = 0;
//		int blue = 0;
//		ArrayList<String> param = new ArrayList<String>();
//		while (count < 20000000) {
//			try {
//				Thread.sleep(200);
//			} catch (InterruptedException e1) {
//				e1.printStackTrace();
//			}
//			param.clear();
//			
//			if (red != 0) {
//				red -= 20;
//				green += 20;
//			} else {
//				
//			}
//			
//			param.add(String.valueOf(red * 256 * 256 + green * 256 + blue));
//			param.add("sudden");
//			param.add("0");
//			try {
//				cs.sendCommand(d, "set_rgb", param);
//			} catch (NumberFormatException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			count += 20;
//		}
		
		ArrayList<String> param = new ArrayList<>();
		while (count < 50) {
			try {
				Thread.sleep(1000);
				cs.sendCommand(d, "toggle", param);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			count ++;
		}
		
		try {
			ds.stopScanning();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
