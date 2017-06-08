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

public class CommandSender {

	private final int SOCKET_TIMEOUT = 2000;

	private static int cmdId = 0;

	private String sendCommand(Device d, String method, ArrayList<String> params) throws IOException, NumberFormatException {
		cmdId ++;
		
		String message = "{\"id\":" + cmdId + ",\"method\":\""
				+ method + "\",\"params\":[";
		for (int i = 0; i < params.size(); i ++) {
			try {
				Integer.valueOf(params.get(i));
				message += params.get(i);
			} catch(NumberFormatException e) {
				message += "\"" + params.get(i) + "\"";
			}
			if (i != params.size() - 1) {
				message += ",";
			}
		}
		message += "]}\r\n";

		System.out.println(message);
		
		try {
			Socket socket = new Socket(InetAddress.getByName(d.getDeviceIp()), 
					d.getServicePort());
			socket.setKeepAlive(true);
			socket.setReuseAddress(true);
			socket.setSoTimeout(SOCKET_TIMEOUT);
			
			System.out.println("Connecting to " + socket.getInetAddress() + ":" + socket.getPort());
			System.out.println("socket is connected: " + socket.isConnected());
			
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
			return lightData;
		} catch(SocketTimeoutException e) {
			return "Timeout";
		} catch(NoRouteToHostException e) {
			return "no route";
		}
	}
	
	public String sendToggleCommand(ArrayList<Device> dl, String[] parsedInput) {
		String response;
		try {
			if (parsedInput.length != 2) {
				return "format: t <idx>";
			}
			int index = Integer.valueOf(parsedInput[1]);
			if (index > dl.size() || index < 0) {
				return "Invalid index";
			}
			ArrayList<String> params = new ArrayList<>();
			for (int i = 2; i < parsedInput.length; i ++) {
				params.add(parsedInput[i]);
			}
			response = sendCommand(dl.get(index), "toggle", params);
			return response;
		} catch (NumberFormatException e) {
			return "The second index must be an integer!";
		} catch (IOException e) {
			e.printStackTrace();
			return "IOException";
		}
	}
	
	
	public String sendBrightCommand(ArrayList<Device> dl, String[] parsedInput) {
		try {
			if (parsedInput.length != 3) {
				return "format: b <idx> <brightness>";
			}
			int index = Integer.valueOf(parsedInput[1]);
			if (index > dl.size() || index < 0) {
				return "Invalid index";
			}
			ArrayList<String> params = new ArrayList<>();
			for (int i = 2; i < parsedInput.length; i ++) {
				params.add(parsedInput[i]);
			}
			String response = sendCommand(dl.get(index), "set_bright", params);
			return response;
		} catch (NumberFormatException e) {
			return "The second index must be an integer!";
		} catch (IOException e) {
			e.printStackTrace();
			return "IOException";
		}
	}


	public String sendColorTemperatureCommand(ArrayList<Device> dl, String[] parsedInput) {
		try {
			if (parsedInput.length != 3) {
				return "format: ct <idx> <value>";
			}
			int index = Integer.valueOf(parsedInput[1]);
			if (index > dl.size() || index < 0) {
				return "Invalid index";
			}
			ArrayList<String> params = new ArrayList<>();
			params.add(parsedInput[2]);
			params.add("smooth");
			params.add("500");
			String response = sendCommand(dl.get(index), "set_ct_abx", params);
			return response;
		} catch (NumberFormatException e) {
			return "The index must be an int";
		} catch (IOException e) {
			e.printStackTrace();
			return "IOException";
		}
	}
	
	public String sendRGBCommand(ArrayList<Device> dl, String[] parsedInput) {
		try {
			if (parsedInput.length != 5) {
				return "format: rgb <idx> <r> <g> <b>";
			}
			int index = Integer.valueOf(parsedInput[1]);
			if (index > dl.size() || index < 0) {
				return "Invalid index";
			}
			int r = Integer.valueOf(parsedInput[2]);
			int g = Integer.valueOf(parsedInput[3]);
			int b = Integer.valueOf(parsedInput[4]);
			int rgb = r * 65536 + g * 256 + b;
			ArrayList<String> params = new ArrayList<>();
			params.add(String.valueOf(rgb));
			params.add("smooth");
			params.add("500");
			String response = sendCommand(dl.get(index), "set_rgb", params);
			return response;
		} catch (NumberFormatException e) {
			return "The second index must be an integer!";
		} catch (IOException e) {
			e.printStackTrace();
			return "IOException";
		}
	}
	
	public String sendHSVCommand(ArrayList<Device> dl, String[] parsedInput) {
		try {
			if (parsedInput.length != 4) {
				return "format: hsv <idx> <hue> <sat>";
			}
			int index = Integer.valueOf(parsedInput[1]);
			if (index > dl.size() || index < 0) {
				return "Invalid index";
			}
			ArrayList<String> params = new ArrayList<>();
			params.add(parsedInput[2]);
			params.add(parsedInput[3]);
			params.add("smooth");
			params.add("500");
			String response = sendCommand(dl.get(index), "set_hsv", params);
			return response;
		} catch (NumberFormatException e) {
			return "The second index must be an integer!";
		} catch (IOException e) {
			e.printStackTrace();
			return "IOException";
		}
	}
	
}
