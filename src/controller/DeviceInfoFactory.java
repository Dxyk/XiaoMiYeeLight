package controller;

import java.util.ArrayList;
import java.util.List;

public class DeviceInfoFactory {

	public static Device createFromDeviceResponse(String deviceResponse) {
		DeviceInfoBuilder builder = new DeviceInfoBuilder();

		String[] headers = deviceResponse.split("\r\n");

		for (String header : headers) {
			// skipping response status
			if (!header.contains(": ")) {
				continue;
			}
			String[] values = header.split(": ");
			String key = values[0];
			String value = (values.length == 2) ? values[1].trim() : "";

			try {
				matchKeyWithDeviceInfo(builder, key, value);
			} catch (IllegalArgumentException e) {
			}
		}

		return builder.createDeviceInfo();
	}

	private static void matchKeyWithDeviceInfo(DeviceInfoBuilder builder, String key,
			String value) {
		switch (DeviceAttribute.fromString(key)) {
			case LOCATION:
				String[] values = value.split(":");
				builder.setIp(values[1].substring(2));
				builder.setServicePort(Integer.valueOf(values[2]));
				break;
			case ID:
				builder.setDeviceId(value);
				break;
			case MODEL:
				builder.setModel(value);
				break;
			case FW:
				builder.setFirmwareVersion(value);
				break;
			case SUPPORT:
				String[] commands = value.split(" ");
				List<DeviceCommand> commandList = new ArrayList<>();
				for (String command : commands) {
					commandList.add(DeviceCommand.fromString(command));
				}
				builder.setSupportedActions(commandList);
				break;
			case POWER:
				builder.setPower(value);
				break;
			case BRIGHT:
				builder.setBright(Integer.valueOf(value));
				break;
			case COLOR_MODE:
				builder.setColorMode(DeviceColorMode.fromString(value));
				break;
			case COLOR_TEMP:
				builder.setColorTemperature(Integer.valueOf(value));
				break;
			case RGB:
				builder.setRgb(Integer.valueOf(value));
				break;
			case HUE:
				builder.setHue(Integer.valueOf(value));
				break;
			case SAT:
				builder.setSat(Integer.valueOf(value));
				break;
			case NAME:
				builder.setName(value);
				break;
		}
	}
}
