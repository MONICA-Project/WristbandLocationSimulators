package main.java;

import java.util.*;
import java.text.SimpleDateFormat;

class MonicaDeviceStatus {
	public String tagId, type, state, sensor, device;
	public String timestamp, unitOfMeasurements, observationType;

	public MonicaDeviceStatus() {
		SimpleDateFormat dateFormatUTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
		timestamp = dateFormatUTC.format(Calendar.getInstance().getTime());
	}
}

public class MonicaObservation {
	public String tagId, type, areaId, motion_state;
	public double lat, lon, speed, speed_x, speed_y, speed_z, x, y, z, bearing, height, herr, battery_level;
	public String timestamp;

	public MonicaObservation() {
		SimpleDateFormat dateFormatUTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
		timestamp = dateFormatUTC.format(Calendar.getInstance().getTime());
	}
}
