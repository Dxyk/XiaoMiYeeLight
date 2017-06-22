package controller;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * A command sender that sends commands to the deZ
 * 
 * @author Xiangyu Kong
 */
public class CommandSender {

	private final int SOCKET_TIMEOUT = 2000;

	private ExecutorService executor;

	private static int cmdId = 0;


	/**
	 * Helper method to build request method.
	 * 
	 * @param method
	 *            The method
	 * @param params
	 *            The parameters
	 * @return The method
	 */
	private String getMsg(String method, ArrayList<String> params) {
		cmdId++;

		String message = "{\"id\":" + cmdId + ",\"method\":\"" + method
				+ "\",\"params\":[";
		for (int i = 0; i < params.size(); i++) {
			try {
				Integer.valueOf(params.get(i));
				message += params.get(i);
			} catch (NumberFormatException e) {
				message += "\"" + params.get(i) + "\"";
			}
			if (i != params.size() - 1) {
				message += ",";
			}
		}
		message += "]}\r\n";

		return message;
	}


	/**
	 * Helper function that sends a command to the device
	 * 
	 * 
	 * A list of methods and their parameters:
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
	 * Note:
	 * effect: "sudden" or "smooth"
	 * duration: 50 ~
	 * 
	 * @param d
	 *            The device
	 * @param method
	 *            The method
	 * @param params
	 *            The list of parameters
	 * @return the result of the command. return null if exception
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private String sendNonInterruptCommand(Device d, String method,
			ArrayList<String> params) throws UnknownHostException, IOException {
		String message = getMsg(method, params);
		String result = d.getDeviceId() + ": " + method + ": ";

		try {
			Socket socket = new Socket(InetAddress.getByName(d.getDeviceIp()),
					d.getServicePort());
			socket.setKeepAlive(true);
			socket.setReuseAddress(true);
			socket.setSoTimeout(SOCKET_TIMEOUT);

			// Testing
			// System.out.println(
			// "Connecting to " + socket.getInetAddress() + ":" +
			// socket.getPort());
			// System.out.println("Device ID: " + d.getDeviceId() + ", method: "
			// + method);
			// System.out.print(message);

			// write from socket to send command
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.write(message.getBytes());
			out.flush();

			// recieve from light for response
			BufferedReader in = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			String lightData = "";
			lightData = in.readLine();

			socket.close();

			JsonParser jp = new JsonParser();
			JsonObject jsonObj = jp.parse(lightData).getAsJsonObject();

			if (jsonObj.has("result")) {
				result += jsonObj.get("result").getAsJsonArray().get(0).getAsString();
				// System.out.println(result);
				return result;
			} else if (jsonObj.has("error")) {
				result += jsonObj.get("error").getAsJsonObject().get("message")
						.getAsString();
				// System.out.println(result);
				return result;
			} else {
				return null;
			}

		} catch (SocketTimeoutException e) {
			return null;
		} catch (NoRouteToHostException e) {
			return null;
		}
	}


	/**
	 * Send a command to the selected device and interrupt the flashing thread
	 * 
	 * @param d
	 *            The selected device
	 * @param method
	 *            The method to be performed
	 * @param params
	 *            The parameters
	 * @return OK if success; error message if invalid command; null if error
	 *         occured
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public String sendCommand(Device d, String method, ArrayList<String> params)
			throws IOException, NumberFormatException {

		stopFlash();
		return sendNonInterruptCommand(d, method, params);
	}


	/**
	 * Get the property of a device d.
	 * 
	 * @param d
	 *            The device
	 * @param property
	 *            The property
	 * @return The property of the device
	 */
	public String getProperty(Device d, String property) {
		ArrayList<String> prop = new ArrayList<String>();
		prop.add(property);
		try {
			return sendNonInterruptCommand(d, "get_prop", prop);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}


	/**
	 * Create another thread and starts flashing all devices.
	 * 
	 * @param dl
	 *            The device list
	 */
	public void flash(ArrayList<Device> dl) {
		executor = Executors.newSingleThreadExecutor();
		Flash f = new Flash(dl);
		executor.execute(f);
		executor.shutdown();
	}


	/**
	 * Stop flashing and kill the thread
	 */
	public void stopFlash() {
		if (executor != null) {
			try {
				executor.awaitTermination(2000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			executor.shutdownNow();
			while (!executor.isTerminated()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				} finally {
					executor.shutdownNow();
				}
			}
		}
	}

	/**
	 * A runnable Flash class
	 * 
	 * @author Xiangyu Kong
	 */
	private class Flash implements Runnable {

		static final int WHITE = (0 << 16) | (0 << 8) | 0;
		static final int RED = (255 << 16) | (0 << 8) | 0;
		static final int GREEN = (0 << 16) | (255 << 8) | 0;
		static final int BLUE = (0 << 16) | (0 << 8) | 255;
		static final int PURPLE = (160 << 16) | (32 << 8) | 240;
		static final int YELLOW = (255 << 16) | (255 << 8) | 0;
		static final int ORANGE = (255 << 16) | (165 << 8) | 0;
		static final int SKYBLUE = (135 << 16) | (206 << 8) | 255;

		static final int HIGH_BRIGHT = 60;
		static final int MEDIUM_TO_HIGH_BRIGHT = 41;
		static final int MEDIUM_TO_LOW_BRIGHT = 39;
		static final int LOW_BRIGHT = 20;

		// Controls the interval of change of color and brightness
		private final int interval = 4000;

		private ArrayList<Device> dl;
		private int color;
		private int bright;


		Flash(ArrayList<Device> dl) {
			this.dl = dl;
			this.color = WHITE;
		}


		/**
		 * change the brightness value according to previous brightness value
		 * 
		 * @return the new bright value
		 */
		private int changeBright() {
			switch (bright) {
				case HIGH_BRIGHT: {
					bright = MEDIUM_TO_LOW_BRIGHT;
					break;
				}
				case MEDIUM_TO_LOW_BRIGHT: {
					bright = LOW_BRIGHT;
					break;
				}
				case LOW_BRIGHT: {
					bright = MEDIUM_TO_HIGH_BRIGHT;
					break;
				}
				case MEDIUM_TO_HIGH_BRIGHT: {
					bright = HIGH_BRIGHT;
					break;
				}
				default: {
					bright = LOW_BRIGHT;
				}
			}
			return bright;
		}


		/**
		 * change the RGB value according to previous RGB value
		 * 
		 * @return the new RGB value
		 */
		private int changeRGB() {
			switch (color) {
				case WHITE:
					color = RED;
					break;
				case RED:
					color = ORANGE;
					break;
				case ORANGE:
					color = YELLOW;
					break;
				case YELLOW:
					color = GREEN;
					break;
				case GREEN:
					color = SKYBLUE;
					break;
				case SKYBLUE:
					color = BLUE;
					break;
				case BLUE:
					color = PURPLE;
					break;
				case PURPLE:
					color = RED;
					break;
				default:
					color = WHITE;
					break;
			}
			return color;
		}


		/**
		 * Send a command to a specific device with its corresponding socket.
		 * 
		 * @param d
		 *            The device
		 * @param socket
		 *            The corresponding socket
		 * @param method
		 *            The method name
		 * @param message
		 *            the message to be sent
		 * @return the response
		 * @throws IOException
		 */
		private String send(Device d, Socket socket, String method, String message)
				throws IOException {
			String result = d.getDeviceId() + ": " + method + ": ";
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.write(message.getBytes());
			out.flush();

			// recieve from light for response
			BufferedReader in = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			String lightData = "";
			lightData = in.readLine();
			JsonParser jp = new JsonParser();
			JsonObject jsonObj = jp.parse(lightData).getAsJsonObject();

			if (jsonObj.has("result")) {
				result += jsonObj.get("result").getAsJsonArray().get(0).getAsString();
				// System.out.println(result);
				return result;
			} else if (jsonObj.has("error")) {
				result += jsonObj.get("error").getAsJsonObject().get("message")
						.getAsString();
				// System.out.println(result);
				return result;
			} else {
				return jsonObj.toString();
			}
		}


		@Override
		public void run() {
			ArrayList<String> brightParams = new ArrayList<String>();
			brightParams.add("20");
			brightParams.add("smooth");
			brightParams.add(String.valueOf(interval));
			ArrayList<String> rgbParams = new ArrayList<String>();
			rgbParams.add(String.valueOf(""));
			rgbParams.add("smooth");
			rgbParams.add(String.valueOf(interval));
			// check if devices are off and remove off devices
			for (Device d : dl) {
				if (getProperty(d, "power").equals("off")) {
					dl.remove(d);
				}
			}
			// create a hashmap and store all sockets corresponding to the
			// devices
			HashMap<Device, Socket> map = new HashMap<>();
			for (Device d : dl) {
				Socket socket;
				try {
					socket = new Socket(InetAddress.getByName(d.getDeviceIp()),
							d.getServicePort());
					socket.setKeepAlive(true);
					socket.setReuseAddress(true);
					socket.setSoTimeout(SOCKET_TIMEOUT);
					map.put(d, socket);
				} catch (IOException e) {
					map.remove(d);
					e.printStackTrace();
				}
			}
			//
			while (true) {
				changeRGB();
				changeBright();
				rgbParams.set(0, String.valueOf(color));
				brightParams.set(0, String.valueOf(bright));
				String rgbMessage = getMsg("set_rgb", rgbParams);
				String brightMessage = getMsg("set_bright", brightParams);
				try {
					for (Device d : map.keySet()) {
						Socket socket = map.get(d);
						send(d, socket, "set_bright", brightMessage);
						send(d, socket, "set_rgb", rgbMessage);
						// for testing purposes
//						System.out.println(send(d, socket, "set_bright", brightMessage));
//						System.out.println(send(d, socket, "set_rgb", rgbMessage));
					}
					Thread.sleep(interval);
				} catch (IOException e) {
//					e.printStackTrace();
				} catch (InterruptedException e) {
					for (Socket socket : map.values()) {
						try {
							socket.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
					break;
				}
			}
		}

	}

}
