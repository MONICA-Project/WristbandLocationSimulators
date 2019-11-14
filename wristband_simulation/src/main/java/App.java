package main.java;

import java.util.*;
import java.io.PrintStream;
// import org.apache.http.HttpHeaders;

public class App {
	public static String applicationVersion = "1.1.0";
	public static String pathSettings = "settingsFile.txt";
	public static String separatorLabelField = "=";
	

	public static Boolean GetInlineArgs(String[] args) {
		if (args.length == 0)
			return false;

		if (args.length >= 1)
			App.pathSettings = args[0];

		if (args.length >= 2)
			App.separatorLabelField = args[1];

		return true;
	}

	private static final Position calculateNewPosition(Position refPosLLH, Settings settings) {
		Random r = new Random(System.currentTimeMillis());

		PositionENU planeDistance = new PositionENU();

		planeDistance.North = settings.maxDistanceNorth_meters * r.nextDouble();
		planeDistance.East = settings.maxDistanceEast_meters * r.nextDouble();

		Position newPosReturn = refPosLLH.CalculateNewPositionWith2DDistance(planeDistance);

		return newPosReturn;
	}

	public static void testPositions(Settings settingsCrowdSim) {
		Position[] vectorPosition = new Position[10];
		try {

			for (int indexElement = 0; indexElement < 10; indexElement++) {
				Position newPosition =

						calculateNewPosition(
								new Position(settingsCrowdSim.referenceLatitude, settingsCrowdSim.referenceLongitude),
								settingsCrowdSim);

				vectorPosition[indexElement] = newPosition;

			}
		} catch (Exception exc) {

		}

	}

	public enum FSMMachineFill {
		CREATE_THREAD, APPEND_WUID
	}

	public static void setPathOutputError(Settings settings) {
		try {
			if (settings.sendStdOutToFile == true && settings.pathFileOutput.isEmpty() == false) {
				System.out.println("SET Standard Output To File: " + settings.pathFileOutput);
				PrintStream fileOut = new PrintStream(settings.pathFileOutput);
				System.setOut(fileOut);
			}
			if (settings.sendStdErrorToFile == true && settings.pathFileError.isEmpty() == false) {
				System.out.println("SET Standard Error To File: " + settings.pathFileError);
				PrintStream fileError = new PrintStream(settings.pathFileError);
				System.setErr(fileError);
			}

		} catch (Exception e) {
			System.err.println("setPathOutputError Exception: " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		try {
			System.out.println("CROWD HEATMAP GENERATION STARTED "+applicationVersion+" (MultiThread Distributed)");

			Settings settingsCrowdSim = new Settings();

			GetInlineArgs(args);

			settingsCrowdSim.getSettingsFromFile(pathSettings, separatorLabelField);

			// App.testPositions(settingsCrowdSim);

			System.out.println(
					"CROWD HEATMAP START EMULATION NUMBERDEVICES: " + Integer.toString(settingsCrowdSim.counterElements)
							+ "CounterDevicePerThread: " + Integer.toString(settingsCrowdSim.counterDevicePerThread));

			App.setPathOutputError(settingsCrowdSim);

			FSMMachineFill fsmMachineFille = FSMMachineFill.CREATE_THREAD;

			CrowdSim crowdSimulationPointer = null;
			int threadID = 1;

			for (int indexLocalization = 0; indexLocalization < settingsCrowdSim.counterElements; indexLocalization++) {

				switch (fsmMachineFille) {
				case CREATE_THREAD:
					crowdSimulationPointer = new CrowdSim(settingsCrowdSim);
					crowdSimulationPointer.threadID = threadID;

					threadID++;

					crowdSimulationPointer.appendDeviceID(indexLocalization);

					System.out.println("THREAD CROWDSIMULATION ID: " + Integer.toString(crowdSimulationPointer.threadID)
							+ " DeviceID Appended: " + Integer.toString(indexLocalization));

					fsmMachineFille = FSMMachineFill.APPEND_WUID;

					break;

				case APPEND_WUID:
					if (crowdSimulationPointer != null) {
						crowdSimulationPointer.appendDeviceID(indexLocalization);

						System.out.println(
								"THREAD CROWDSIMULATION ID: " + Integer.toString(crowdSimulationPointer.threadID)
										+ " DeviceID Appended: " + Integer.toString(indexLocalization));
					}
					break;

				default:
					break;
				}
				if (crowdSimulationPointer != null) {
					if (crowdSimulationPointer.getCounterDevices() >= settingsCrowdSim.counterDevicePerThread) {

						System.out.println("THREAD CROWDSIMULATION ID: "
								+ Integer.toString(crowdSimulationPointer.threadID) + " NumberDevices: "
								+ Integer.toString(crowdSimulationPointer.getCounterDevices()));

						crowdSimulationPointer.start();

						if (settingsCrowdSim.timeoutWaitLaunchSingleThreadms > 0)
							Thread.sleep(settingsCrowdSim.timeoutWaitLaunchSingleThreadms);

						fsmMachineFille = FSMMachineFill.CREATE_THREAD;
					}
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}
