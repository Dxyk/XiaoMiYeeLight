package controller;

import java.util.List;

public class Device {
    String ip;
    int servicePort;
    String deviceId;
    String model;
    String name;
    String firmwareVersion;
    List<DeviceCommand> supportedActions;
    String power;
    int brightness;
    DeviceColorMode colorMode;
    int colorTemperature;
    int rgb;
    int hue;
    int saturation;

    public Device(String ip, int servicePort, String deviceId, String model, String name, String firmwareVersion, List<DeviceCommand> supportedActions, String power, DeviceColorMode colorMode, int bright, int colorTemperature, int rgb, int hue, int sat) {
        this.ip = ip;
        this.servicePort = servicePort;
        this.deviceId = deviceId;
        this.model = model;
        this.name = name;
        this.firmwareVersion = firmwareVersion;
        this.supportedActions = supportedActions;
        this.power = power;
        this.colorMode = colorMode;
        this.brightness = bright;
        this.colorTemperature = colorTemperature;
        this.rgb = rgb;
        this.hue = hue;
        this.saturation = sat;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public List<DeviceCommand> getSupportedActions() {
        return supportedActions;
    }
    
    public String getDeviceIp() {
    	return ip;
    }
    
    public int getServicePort() {
    	return servicePort;
    }
    
    public String toString() {
    	String result = "";
    	result += "ip = " + ip + ":" + servicePort + "; ";
    	result += "model = " + model + "; ";
    	result += "power = " + power + "; ";
    	result += "brightness = " + brightness + "; ";
    	result += "rgb = " + rgb + ";";
    	
    	return result;
    }


}
