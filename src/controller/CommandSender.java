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
		String result = method + ": ";

		try {
			Socket socket = new Socket(InetAddress.getByName(d.getDeviceIp()),
					d.getServicePort());
			socket.setKeepAlive(true);
			socket.setReuseAddress(true);
			socket.setSoTimeout(SOCKET_TIMEOUT);

//			System.out.println(
//					"Connecting to " + socket.getInetAddress() + ":" + socket.getPort());
//			System.out.println("Device ID: " + d.getDeviceId() + ", method: " + method);
//			System.out.print(message);

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
//				System.out.println(result);
				return result;
			} else {
				result += jsonObj.get("error").getAsJsonObject().get("message")
						.getAsString();
//				System.out.println(result);
				return result;
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
		try {
			stopFlash();
		} catch (InterruptedException e1) {
		}
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
			return sendCommand(d, "get_prop", prop);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Create another thread and starts flashing for times times.
	 * 
	 * @param d
	 *            The device
	 * @param times
	 *            Times to flash flash infinitely if set to -1
	 */
	public void flash(Device d, int times) {
		executor = Executors.newSingleThreadExecutor();
		Flash f = new Flash(d, times);
		executor.execute(f);
		executor.shutdown();
	}

	/**
	 * Stop flashing and kill the thread
	 * 
	 * @throws InterruptedException
	 */
	public void stopFlash() throws InterruptedException {
		if (executor != null) {
			try {
				executor.awaitTermination(2000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			executor.shutdownNow();
			while (!executor.isTerminated()) {
				Thread.sleep(1000);
				executor.shutdownNow();
			}
		}
	}

	/**
	 * A runnable Flash class
	 * 
	 * @author Xiangyu Kong
	 */
	private class Flash implements Runnable {

		private Device d;
		private int times;

		Flash(Device d, int times) {
			this.d = d;
			this.times = times;
		}

		@Override
		public void run() {
			// On and off parameters
			ArrayList<String> onParams = new ArrayList<String>();
			onParams.add("on");
			onParams.add("smooth");
			onParams.add("200");
			ArrayList<String> offParams = new ArrayList<String>();
			offParams.add("off");
			offParams.add("smooth");
			offParams.add("200");
			// turn on and of for times times
			for (int i = times; i != 0; i--) {
				try {
					Thread.sleep(1000);
					System.out.println(sendNonInterruptCommand(d, "set_power", onParams));
					Thread.sleep(500);
					System.out
							.println(sendNonInterruptCommand(d, "set_power", offParams));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					break;
				}
			}
		}
	}

}
