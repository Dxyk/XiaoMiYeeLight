package controller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Scanner implements Runnable {

	private static final String DISCOVERY_MSG = "M-SEARCH * HTTP/1.1\r\n" 
			+ "HOST:239.255.255.250:1982\r\n"
			+ "MAN:\"ssdp:discover\"\r\n"
			+ "ST:wifi_bulb\r\n";
	private static final String MULTICAST_ADDR = "239.255.255.250";
	private static final int MULTICAST_PORT = 1982;
	private final int SOCKET_TIMEOUT = 2000;

	HashMap<String, Device> deviceList = new HashMap<>();

	private AtomicBoolean scanning = new AtomicBoolean(true);

	@Override
	public void run() {
		scanning.set(true);

		try {
			process();
		} catch (Exception e) {
			e.printStackTrace();
		}
		scanning.set(false);
	}

	private void process() throws IOException {
		byte[] searchPacket = DISCOVERY_MSG.getBytes();
		@SuppressWarnings("resource")
		MulticastSocket socket = new MulticastSocket(MULTICAST_PORT);
		socket.setSoTimeout(SOCKET_TIMEOUT);
		DatagramPacket packet;
		while (!Thread.currentThread().isInterrupted()) {
			byte[] buf = new byte[2048];
			packet = new DatagramPacket(buf, buf.length);
			try {
				socket.send(new DatagramPacket(searchPacket,
						searchPacket.length,
						InetAddress.getByName(MULTICAST_ADDR),
						MULTICAST_PORT));
				while (true) {
					socket.receive(packet);
					String lightData = new String(packet.getData());
					if (lightData.contains("HTTP/1.1 200 OK")) {
						// Discovery response
						Device deviceInfo = DeviceInfoFactory.createFromDeviceResponse(lightData);
						deviceList.put(deviceInfo.getDeviceId(), deviceInfo);
					} else if (lightData.contains("NOTIFY * HTTP/1.1")) {
						// Notify message
						Device deviceInfo = DeviceInfoFactory.createFromDeviceResponse(lightData);
						deviceList.put(deviceInfo.getDeviceId(), deviceInfo);
					} else {
						continue;
					}
				}
			} catch (SocketTimeoutException e) {
			}
		}
	}

	public HashMap<String, Device> getFoundDevices() {
		@SuppressWarnings("unchecked")
		HashMap<String, Device> foundDevices =  (HashMap<String, Device>) deviceList.clone();
		deviceList.clear();
		return foundDevices;
	}

}
