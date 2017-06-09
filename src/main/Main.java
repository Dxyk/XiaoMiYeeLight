package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import controller.CommandSender;
import controller.Device;
import controller.DeviceScanner;

public class Main {
	
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
	
	private static Device getDevice(ArrayList<Device> dl, String[] parsedInput) {
		if (parsedInput.length < 2) {
			System.out.println("Format error. Hit h for help");
			return null;
		}
		int index;
		try {
			index = Integer.valueOf(parsedInput[1]);
			if (index > dl.size() || index < 0) {
				System.out.println("Invalid index");
				return null;
			} else {
				return dl.get(Integer.valueOf(parsedInput[1]));
			}
		} catch (NumberFormatException e) {
			System.out.println("Index must be an integer.");
			return null;
		}
	}
	
	public static void main(String[] args) {
		DeviceScanner ds = new DeviceScanner();
		CommandSender cs = new CommandSender();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input = "";
		Device d;
		
		System.out.println(getClientMessage());
		System.out.print(">>> ");
		
		try {
			ds.scanDevices();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		while (true) {
			
			try {
				input = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			String[] parsedInput = input.split(" ");			
			String cmd = parsedInput[0];
			if (cmd.equalsIgnoreCase("q") || cmd.equalsIgnoreCase("quit")) {
				System.out.println("Thanks for using! Quitting ...");
				break;
			} else if (cmd.equalsIgnoreCase("h") || cmd.equalsIgnoreCase("help")) {
				System.out.println(getClientMessage());
			} else if (cmd.equalsIgnoreCase("r") || cmd.equalsIgnoreCase("refresh")) {
				System.out.println("Refreshing ...");
				ds.listDevices();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}	
			} else if (cmd.equalsIgnoreCase("l") || cmd.equalsIgnoreCase("list")) {
				System.out.println("Result: ");
				System.out.println(ds.listDevices());
			} else if (cmd.equalsIgnoreCase("t") || cmd.equalsIgnoreCase("toggle")) {
				// toggle
				if ((d = getDevice(ds.getListDevice(), parsedInput)) != null) {
					System.out.println(cs.sendToggleCommand(d, parsedInput));
				}
			} else if (cmd.equalsIgnoreCase("b") || cmd.equalsIgnoreCase("bright")) {
				// bright
				if ((d = getDevice(ds.getListDevice(), parsedInput)) != null) {
					System.out.println(cs.sendBrightCommand(d, parsedInput));
				}
			} else if (cmd.equalsIgnoreCase("ct") || cmd.equalsIgnoreCase("color temperature")) {
				// change color temperature
				if ((d = getDevice(ds.getListDevice(), parsedInput)) != null) {
					System.out.println(cs.sendColorTemperatureCommand(d, parsedInput));
				}
			} else if (cmd.equalsIgnoreCase("rgb")) {
				// change color
				if ((d = getDevice(ds.getListDevice(), parsedInput)) != null) {
					System.out.println(cs.sendRGBCommand(d, parsedInput));
				}
			} else if (cmd.equalsIgnoreCase("hsv")) {
				// change hsv
				if ((d = getDevice(ds.getListDevice(), parsedInput)) != null) {
					System.out.println(cs.sendHSVCommand(d, parsedInput));
				}
			} else if (cmd.equalsIgnoreCase("")) {
				
			} else {
				System.out.println("Invalid Command.");
				System.out.println(getClientMessage());
			}
			
			System.out.print(">>> ");
		}
		
		try {
			ds.stopScanning();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
