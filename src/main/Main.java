package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import controller.CommandSender;
import controller.Device;
import controller.DeviceScanner;

/**
 * @author Xiangyu Kong
 *
 */
public class Main {

	private DeviceScanner ds = new DeviceScanner();
	private CommandSender cs = new CommandSender();

	private List<String> lightGroup = null;


	public Main() {
		super();
	}


	public void OnStart() {
		try {
			ds.scanDevices();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	/**
	 * @return an arraylist of devices
	 */
	public ArrayList<Device> getSearchedDevices() {
		return ds.getListDevice();
	}


	/**
	 * @return the Json array of all devices
	 */
	public JsonArray getArrayDevices() {
		return ds.getArrayDevice();
	}


	/**
	 * Send a command to the device of the id
	 * The command sent will kill the flash thread and stop the device from
	 * flashing
	 * 
	 * A list of methods and their parameters:
	 * 
	 * method parameters
	 * 
	 * get_prop []
	 * get the properties of the light
	 * 
	 * set_ct_abx [ct_value, effect, duration]
	 * set the color temperature of the light
	 * ct_value: 1700 ~ 6500
	 * 
	 * set_rgb [rgb_value, effect, duration]
	 * set the color of the light. Format: RGB
	 * rgb_value: 0 ~ 16777215 (0xFFFFFF)
	 * 
	 * set_hsv [hue_value, saturation_value, effect, duration]
	 * set the hue and saturation value of the light
	 * hue_value: 0 ~ 359
	 * saturation: 0 ~ 100
	 * 
	 * set_bright [brightness, effect, duration]
	 * set the brightness of the light
	 * brightness: 0 ~ 100
	 * 
	 * toggle []
	 * toggle the light for on and off
	 * 
	 * set_power [on/off, effect, duration]
	 * turn on or off
	 * 
	 * Note: effect could be "sudden" or "smooth"
	 * duration: 50 ~
	 * 
	 * @param id
	 *            id of the device
	 * @param method
	 *            the method
	 * @param param
	 *            the parameters
	 */
	public void sendCommand(String id, String method, ArrayList<String> param) {
		String result = null;
		for (Device d : getSearchedDevices()) {
			if (d.getDeviceId().equals(id)) {
				try {
					result = cs.sendCommand(d, method, param);
					System.out.println(result);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return;
	}


	/**
	 * Send a command to all the connected devices.
	 * The command sent will kill the flash thread and stop the device from
	 * flashing
	 * 
	 * List of methods and parameters: see above
	 * 
	 * @param method
	 *            The method to send
	 * @param param
	 *            The parameters for the method
	 */
	public void sendCommandToAll(String method, ArrayList<String> param) {
		String result = null;
		for (Device d : getSearchedDevices()) {
			if (lightGroup != null && lightGroup.size() > 0) {
				if (!lightGroup.contains(d.getDeviceId())) {
					continue;
				}
			}
			try {
				//				cs.sendCommand(d, method, param);
				result = cs.sendCommand(d, method, param);
				System.out.println(result);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * Send a flash command to all devices
	 */
	public void sendFlashCommandToAll() {
		System.out.println("Flashing all devices");
		cs.flash(getSearchedDevices());
	}


	/**
	 * Send a stop-flash command to all devices
	 */
	public void sendStopFlashCommandToAll() {
		System.out.println("Stopping all devices");
		for (Device d : getSearchedDevices()) {
			if (lightGroup != null && lightGroup.size() > 0) {
				if (!lightGroup.contains(d.getDeviceId())) {
					continue;
				}
			}
			cs.stopFlash();
		}
		setBrightness(25);
		setColor(255, 255, 255);
	}


	public void turnOn(boolean on) {
		ArrayList<String> param = new ArrayList<>();
		if (on == true) {
			param.add("on");
		} else {
			param.add("off");
		}
		param.add("smooth");
		param.add("200");
		sendCommandToAll("set_power", param);
	}


	public boolean setColor(int r, int g, int b) {
		ArrayList<String> param = new ArrayList<>();
		int rgb = (r << 16) | (g << 8) | b;
		param.add(rgb + "");
		param.add("smooth");
		param.add("200");
		sendCommandToAll("set_rgb", param);
		return true;
	}


	public boolean setBrightness(int bri) {
		ArrayList<String> param = new ArrayList<>();
		param.add(bri + "");
		param.add("smooth");
		param.add("200");
		sendCommandToAll("set_bright", param);
		return true;
	}


	public boolean setEffect(String effect) {
		boolean ret = true;
		if (effect != null) {
			if (effect.equalsIgnoreCase("colorloop")) {
				sendFlashCommandToAll();
			} else if (effect.equalsIgnoreCase("none")) {
				sendStopFlashCommandToAll();
			} else {
				ret = false;
			}
		} else {
			ret = false;
		}
		return ret;
	}


	/**
	 * For testing purposes
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Main yl = new Main();

		ArrayList<String> onParams = new ArrayList<String>();
		onParams.add("on");
		onParams.add("smooth");
		onParams.add("500");
		ArrayList<String> offParams = new ArrayList<String>();
		offParams.add("off");
		offParams.add("smooth");
		offParams.add("500");
		ArrayList<String> rgbParams = new ArrayList<>();
		rgbParams.add(String.valueOf((255 << 16) | (255 << 8) | 255));
		rgbParams.add("smooth");
		rgbParams.add("500");
		ArrayList<String> brightParam = new ArrayList<>();
		brightParam.add("50");
		brightParam.add("smooth");
		brightParam.add("500");

		try {
			yl.ds.scanDevices();

			while (yl.getSearchedDevices().size() != 2) {
				//				System.out.println(yl.getArrayDevices());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			System.out.println(yl.getArrayDevices());

			yl.sendCommandToAll("set_power", onParams);

			Thread.sleep(1000);

			yl.sendCommandToAll("set_rgb", rgbParams);

			Thread.sleep(1000);

			yl.sendCommandToAll("set_bright", brightParam);

			Thread.sleep(1000);

			yl.sendFlashCommandToAll();

			Thread.sleep(10000);

			//			yl.sendStopFlashCommandToAll();

			yl.ds.stopScanning();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}
