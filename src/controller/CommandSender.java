package controller;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

//import org.codehaus.jettison.json.JSONException;
//import org.codehaus.jettison.json.JSONObject;

/**
 * @author Xiangyu Kong
 */
public class CommandSender {

	private final int SOCKET_TIMEOUT = 2000;

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

		String message = "{\"id\":" + cmdId + ",\"method\":\"" + method + "\",\"params\":[";
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
	 * Send a command to the selected device
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
		String message = getMsg(method, params);

		// System.out.println(message);

		try {
			Socket socket = new Socket(InetAddress.getByName(d.getDeviceIp()), d.getServicePort());
			socket.setKeepAlive(true);
			socket.setReuseAddress(true);
			socket.setSoTimeout(SOCKET_TIMEOUT);

			System.out.println("Connecting to " + socket.getInetAddress() + ":" + socket.getPort());
			System.out.println("Device ID: " + d.getDeviceId() + ", method: " + method);

			// write from socket to send command
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.write(message.getBytes());
			out.flush();

			// recieve from light for response
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String lightData = "";
			lightData = in.readLine();

			socket.close();

			JsonParser jp = new JsonParser();
			JsonObject jsonObj = jp.parse(lightData).getAsJsonObject();
			
			if (jsonObj.has("result")) {
				return jsonObj.get("result").getAsJsonArray().get(0).getAsString();
			} else {
				return jsonObj.get("error").getAsJsonObject().get("message").getAsString();
			}
			
//			JSONObject jsonObj = new JSONObject(lightData);
//			if (jsonObj.has("result")) {
//				return jsonObj.getJSONArray("result").getString(0);
//			} else {
//				return jsonObj.getJSONObject("error").getString("message");
//			}
		} catch (SocketTimeoutException e) {
			return null;
		} catch (NoRouteToHostException e) {
			return null;
		}
//		} catch (JSONException e) {
//			e.printStackTrace();
//			return null;
//		}
	}

	/**
	 * @param d
	 *            The selected device
	 * @param parsedInput
	 *            The parsed input with the first one as the method; Second one
	 *            as the method; Following are the parameters
	 * @return The sendCommand result
	 */
	public String sendToggleCommand(Device d, String[] parsedInput) {
		try {
			if (parsedInput.length != 2) {
				System.out.println("format: t <idx>");
				return null;
			}
			ArrayList<String> params = new ArrayList<>();
			return sendCommand(d, "toggle", params);
		} catch (NumberFormatException e) {
			System.out.println("The second index must be an integer!");
			return null;
		} catch (IOException e) {
			System.out.println("IOException");
			return null;
		}
	}

	/**
	 * @param d
	 *            The selected device
	 * @param parsedInput
	 *            The parsed input with the first one as the method; Second one
	 *            as the method; Following are the parameters
	 * @return The sendCommand result
	 */
	public String sendBrightCommand(Device d, String[] parsedInput) {
		try {
			if (parsedInput.length != 3) {
				return "format: b <idx> <brightness>";
			}
			ArrayList<String> params = new ArrayList<>();
			for (int i = 2; i < parsedInput.length; i++) {
				params.add(parsedInput[i]);
			}
			return sendCommand(d, "set_bright", params);
		} catch (NumberFormatException e) {
			System.out.println("The index must be an int");
			return null;
		} catch (IOException e) {
			System.out.println("IOException");
			return null;
		}
	}

	/**
	 * @param d
	 *            The selected device
	 * @param parsedInput
	 *            The parsed input with the first one as the method; Second one
	 *            as the method; Following are the parameters
	 * @return The sendCommand result
	 */
	public String sendColorTemperatureCommand(Device d, String[] parsedInput) {
		try {
			if (parsedInput.length != 3) {
				return "format: ct <idx> <value>";
			}
			ArrayList<String> params = new ArrayList<>();
			params.add(parsedInput[2]);
			params.add("smooth");
			params.add("500");
			return sendCommand(d, "set_ct_abx", params);
		} catch (NumberFormatException e) {
			System.out.println("The index must be an int");
			return null;
		} catch (IOException e) {
			System.out.println("IOException");
			return null;
		}
	}

	/**
	 * @param d
	 *            The selected device
	 * @param parsedInput
	 *            The parsed input with the first one as the method; Second one
	 *            as the method; Following are the parameters
	 * @return The sendCommand result
	 */
	public String sendRGBCommand(Device d, String[] parsedInput) {
		try {
			if (parsedInput.length != 5) {
				return "format: rgb <idx> <r> <g> <b>";
			}
			int r = Integer.valueOf(parsedInput[2]);
			int g = Integer.valueOf(parsedInput[3]);
			int b = Integer.valueOf(parsedInput[4]);
			int rgb = r * 65536 + g * 256 + b;
			ArrayList<String> params = new ArrayList<>();
			params.add(String.valueOf(rgb));
			params.add("smooth");
			params.add("500");
			return sendCommand(d, "set_rgb", params);
		} catch (NumberFormatException e) {
			System.out.println("The index must be an int");
			return null;
		} catch (IOException e) {
			System.out.println("IOException");
			return null;
		}
	}

	/**
	 * @param d
	 *            The selected device
	 * @param parsedInput
	 *            The parsed input with the first one as the method; Second one
	 *            as the method; Following are the parameters
	 * @return The sendCommand result
	 */
	public String sendHSVCommand(Device d, String[] parsedInput) {
		try {
			if (parsedInput.length != 4) {
				return "format: hsv <idx> <hue> <sat>";
			}
			ArrayList<String> params = new ArrayList<>();
			params.add(parsedInput[2]);
			params.add(parsedInput[3]);
			params.add("smooth");
			params.add("500");
			String response = sendCommand(d, "set_hsv", params);
			return response;
		} catch (NumberFormatException e) {
			System.out.println("The index must be an int");
			return null;
		} catch (IOException e) {
			System.out.println("IOException");
			return null;
		}
	}

}
