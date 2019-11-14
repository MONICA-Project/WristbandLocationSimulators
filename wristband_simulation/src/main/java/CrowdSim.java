package main.java;

import java.util.*;
import java.text.SimpleDateFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.DateFormat;
import java.util.Date;

/**
 * Target URL: (non VPN)
 * "https://portal.monica-cloud.eu/scral_wearable_movida/wristband-gw/wearable/localization
 *
 * 
 * @author arjenschoneveld
 *
 */
public class CrowdSim implements Runnable {
	enum FSMMachine {
		APPLICATION_STARTUP,
		DEVICE_NOT_REGISTERED,
		OBSERVATION_STARTUP,
		WAIT_FOR_OBSERVATION,
		OBSERVATION,
		STOP
	}

	private Thread statusThread;
	private boolean enabled = true;
	private boolean run = false;
	private List<Integer> wuids = new ArrayList<>();
	public int wristbandcount = 100;
	public int currentWristbandsCount = 100;
	public String name, type, protocol, targetAddressObservation, targetAddresRegistration;
	private ObjectMapper jsonMapper = new ObjectMapper();
	public int start_offset = 125000;
	private long referenceTimeStamp = 1563370772000L;
	public int threadID = 0;
	private double counter_obs_sent = 0;
	public Settings settingsCrowdSim = null;
	private RESTCall restCallAsync;
	private FSMMachine fsmStateMachine = FSMMachine.APPLICATION_STARTUP;
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public CrowdSim(Settings settingsCrowdSim) {
		name = "MONICA_868_Wristband_Simulator";
		type = "MONICA_868";

		this.settingsCrowdSim = settingsCrowdSim;

		loadSettings(settingsCrowdSim);

		restCallAsync = new RESTCall("", "ANY");

		statusThread = new Thread(this, "MONICARandomWristbandPublisher-thread");
		statusThread.setName(
				"com.sendrato.traxyz.publisher.json.MONICARandomWristbandPublisher: MONICA random wristband publication thread");
	}

	public void appendDeviceID(int uuID) {
		wuids.add(uuID);
	}

	public int getCounterDevices() {
		return wuids.size();
	}

	private void loadSettings(Settings settings) {
		this.targetAddressObservation = settings.urlConnection;
		this.targetAddresRegistration = settings.urlConnection.substring(0, settings.urlConnection.lastIndexOf('/'));
		this.wristbandcount = settings.counterElements;
		this.start_offset = settings.startIndex;
	}

	public void start() {
		run = true;
		statusThread.start();
	}

	public void run() {
		long time_last;
		long time_now;
		long delta;

		if(settingsCrowdSim.username.length()>0 && settingsCrowdSim.password.length()>0) {
			System.out.println("Request SET Authentication Basic: Username: "+settingsCrowdSim.username+", password: "+settingsCrowdSim.password);
			restCallAsync.setAuthenticationCredentials(settingsCrowdSim.username, settingsCrowdSim.password);
		}

		restCallAsync.startAsyncClient();

		try {

			time_last 	= 0;
			time_now 	= 0;

			while (run) {
				Thread.sleep(1000);
				time_now 	= System.currentTimeMillis();

				switch(fsmStateMachine) {
					case APPLICATION_STARTUP:
						fsmStateMachine = settingsCrowdSim.enableRegistrationPhase == true ? FSMMachine.DEVICE_NOT_REGISTERED:FSMMachine.OBSERVATION;
					break;

					case DEVICE_NOT_REGISTERED:
						System.out.println("Registration GOST Started");
						registerAllDevices();
						fsmStateMachine = settingsCrowdSim.enableObservationPhase == true ? FSMMachine.OBSERVATION_STARTUP:FSMMachine.STOP;
					break;

					case OBSERVATION_STARTUP:
						time_last 		= time_now;
						fsmStateMachine = FSMMachine.WAIT_FOR_OBSERVATION;
					break;

					case WAIT_FOR_OBSERVATION:
						delta 		= time_now-time_last;

						if (delta > settingsCrowdSim.intervalSendingSecs * 1000) 
						{
							System.out.println("Elapsed Timemout For Observable: "+Long.toString(delta));
							fsmStateMachine = FSMMachine.OBSERVATION;
						}
					break;

					case OBSERVATION:
						System.out.println("PublishObservation Active");
						publishWristbandLocations();
						time_last = time_now;

						fsmStateMachine = FSMMachine.WAIT_FOR_OBSERVATION;
						break;

					case STOP:
						System.out.println("Application Stopped");
						run = false;
					break;
				}

			}

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	private String getAuthHeader() {
		String up = "username:password";
		String encoded = new String(Base64.getEncoder().encode(up.getBytes()));
		return "Basic " + encoded;
	}

	private final void publishWristbandLocations() {

		restCallAsync.resetCounterReference();

		for (Integer wuid : wuids) {

			System.out.println(dateFormat.format(new Date()) + "ThreadID:" + Integer.toString(threadID)
					+ " publishWristbandLocations " + settingsCrowdSim.prefixTagID + Integer.toString(wuid));

			Position newPosition = CrowdSim.calculateNewPosition(
					new Position(settingsCrowdSim.referenceLatitude, settingsCrowdSim.referenceLongitude),
					settingsCrowdSim);

			try {
				MonicaObservation monObs = getNewObservation(wuid, newPosition);
				publishWristband(monObs);

				if (settingsCrowdSim.timeoutWaitingBetweenObservations_ms > 0)
					Thread.sleep(settingsCrowdSim.timeoutWaitingBetweenObservations_ms);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static final Position calculateNewPosition(Position refPosLLH, Settings settings) {
		Random r = new Random(System.currentTimeMillis());

		PositionENU planeDistance = new PositionENU();

		planeDistance.North = settings.maxDistanceNorth_meters * r.nextDouble();
		planeDistance.East = settings.maxDistanceEast_meters * r.nextDouble();

		Position newPosReturn = new Position(refPosLLH);
		Position positionBack = null;

		positionBack = newPosReturn.CalculateNewPositionWith2DDistance(planeDistance);

		if (positionBack == null)
			System.err.println("calculateNewPosition error, pointer is null");

		return positionBack;
	}

	private final MonicaObservation getNewObservation(int wuid, Position newPosition) {
		MonicaObservation dev = new MonicaObservation();
		long deltaTimeExtracted = 0;

		deltaTimeExtracted = System.currentTimeMillis()-referenceTimeStamp;

		dev.tagId = settingsCrowdSim.prefixTagID + String.format("%02d", wuid);
		dev.lat = newPosition.Latitude;// settingsCrowdSim.referenceLatitude + 0.001 * r.nextDouble();
		dev.lon = newPosition.Longitude;// settingsCrowdSim.referenceLongitude + 0.001 * r.nextDouble();
		dev.speed = (double)deltaTimeExtracted;
		dev.speed /= 1000;
		dev.speed_x = 0.0;
		dev.speed_y = 0.0;
		dev.speed_z = 0.0;
		dev.bearing = 0.0;
		dev.height = 0.0;

		dev.x = 0.0;
		dev.y = 0.0;
		dev.z = 0.0;

		dev.areaId = settingsCrowdSim.areaID;
		dev.motion_state = "unknown";
		dev.type = settingsCrowdSim.deviceType;

		dev.battery_level = 2900 / 1000.0; // In volts.

		return dev;
	}

	private final void publishWristband(MonicaObservation dev) {
		try {

			if (dev == null) {
				System.err.println("publishWristband ERROR Pointer DEV is NULL");
				return;
			}

			String data = jsonMapper.writeValueAsString(dev);
			System.out.println(
					"ThreadID: " + Integer.toString(threadID) + "DeviceID:" + dev.tagId + ", OBSERVATION: " + data);

			restCallAsync.RESTAsync(targetAddressObservation, RESTCall.TypeHTTPCall.HTTP_PUT, data);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private final void registerAllDevices() {

		for (Integer wuid : wuids) {
			try {
				postDeviceStatus(wuid, "active");
				Thread.sleep(settingsCrowdSim.timeoutWaitRegistration_ms);
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	private void postDeviceStatus(int wuid, String status) {
		try {
			MonicaDeviceStatus dev = new MonicaDeviceStatus();

			dev.tagId 				= settingsCrowdSim.prefixTagID + String.format("%02d", wuid);
			dev.type 				= settingsCrowdSim.deviceType;
			dev.device 				= settingsCrowdSim.deviceName;
			dev.sensor 				= settingsCrowdSim.SensorName;
			dev.unitOfMeasurements 	= settingsCrowdSim.unitOfMeasurements;
			dev.observationType 	= settingsCrowdSim.observationType;
			dev.state 				= status;

			String data = jsonMapper.writeValueAsString(dev);

			restCallAsync.RESTAsync(targetAddresRegistration, RESTCall.TypeHTTPCall.HTTP_POST, data);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
