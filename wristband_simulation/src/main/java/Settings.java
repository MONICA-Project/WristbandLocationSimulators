package main.java;

import java.io.*;

public class Settings {

    public int counterElements;
    public int counterDevicePerThread;

    public int startIndex;
    public String urlConnection;
    public String prefixTagID;
    public String deviceType;
    public double referenceLatitude;
    public double referenceLongitude;
    public double maxDistanceEast_meters;
    public double maxDistanceNorth_meters;
    public boolean isVariableWristbandsVariable;
    public String areaID;
    public String deviceName;
    public String SensorName;
    public String observationType;
    public String unitOfMeasurements;
    public String username;
    public String password;
    public int intervalSendingSecs;
    public int timeoutWaitingBetweenObservations_ms;
    public int timeoutWaitRegistration_ms;
    public boolean enableRegistrationPhase;
    public boolean enableObservationPhase;
    public String pathFileOutput;
    public String pathFileError;
    public boolean sendStdOutToFile;
    public boolean sendStdErrorToFile;
    public int timeoutWaitLaunchSingleThreadms;

    public Settings(Settings s1) {
        copy(s1);
    }

    public void copy(Settings s1) {
        this.counterElements = s1.counterElements;

        this.startIndex = s1.counterElements;
        this.urlConnection = s1.urlConnection;
        this.referenceLatitude = s1.referenceLatitude;
        this.referenceLongitude = s1.referenceLongitude;
        this.maxDistanceEast_meters = s1.maxDistanceEast_meters;
        this.maxDistanceNorth_meters = s1.maxDistanceNorth_meters;
        this.prefixTagID = s1.prefixTagID;
        this.deviceType = s1.deviceType;
        this.isVariableWristbandsVariable = s1.isVariableWristbandsVariable;
        this.areaID = s1.areaID;
        this.deviceName = s1.deviceName;
        this.SensorName = s1.SensorName;
        this.observationType = s1.observationType;
        this.unitOfMeasurements = s1.unitOfMeasurements;
        this.isVariableWristbandsVariable = s1.isVariableWristbandsVariable;
        this.areaID = s1.areaID;
        this.intervalSendingSecs = s1.intervalSendingSecs;
        this.enableRegistrationPhase = s1.enableRegistrationPhase;
        this.enableObservationPhase = s1.enableObservationPhase;
        this.timeoutWaitingBetweenObservations_ms = s1.timeoutWaitingBetweenObservations_ms;
        this.timeoutWaitRegistration_ms = s1.timeoutWaitRegistration_ms;
        this.pathFileOutput = s1.pathFileOutput;
        this.pathFileError = s1.pathFileError;
        this.counterDevicePerThread = s1.counterDevicePerThread;
        this.sendStdOutToFile = s1.sendStdOutToFile;
        this.sendStdErrorToFile = s1.sendStdErrorToFile;
        this.timeoutWaitLaunchSingleThreadms = s1.timeoutWaitLaunchSingleThreadms;
        this.username = s1.username;
        this.password = s1.password;
    }

    public Settings() {
        this.counterElements = 10;

        this.startIndex = 0;
        this.urlConnection = "http://monappdwp3.monica-cloud.eu:8440/scral/v1.0/wristband-gw/wearable/localization";
        this.referenceLatitude = 45.06721;
        this.referenceLongitude = 7.654421;
        this.maxDistanceEast_meters = 300;
        this.maxDistanceNorth_meters = 300;
        this.prefixTagID = "GPSTag";
        this.deviceType = "868";
        this.deviceName = "wearable";
        this.SensorName = "tag";
        this.observationType = "proprietary";
        this.unitOfMeasurements = "meters";
        this.isVariableWristbandsVariable = false;
        this.enableRegistrationPhase = false;
        this.areaID = "test";
        this.intervalSendingSecs = 20;
        this.enableObservationPhase = true;
        this.enableRegistrationPhase = false;
        this.timeoutWaitingBetweenObservations_ms = 100;
        this.timeoutWaitRegistration_ms = 10;
        this.pathFileOutput = "outputFile.txt";
        this.pathFileError = "errorFile.txt";
        this.counterDevicePerThread = 1;
        this.sendStdOutToFile = false;
        this.sendStdErrorToFile = false;
        this.timeoutWaitLaunchSingleThreadms = 500;
        this.username = "";
        this.password = "";
    }

    public Boolean getParameterFromString(String line, String separator) {
        try {
            String linePurged = line.trim();

            if (linePurged.isEmpty() == true)
                return false;

            if (linePurged.contains(separator) == false)
                return false;

            String[] lineParts = linePurged.split(separator);

            if (lineParts.length < 2)
                return false;

            switch (lineParts[0]) {
            case "URL":
                urlConnection = lineParts[1];
                return true;

            case "Latitude":
                referenceLatitude = Double.parseDouble(lineParts[1]);
                return true;
            case "Longitude":
                referenceLongitude = Double.parseDouble(lineParts[1]);
                return true;
            case "MaxDistanceNorth_meters":
                maxDistanceNorth_meters = Double.parseDouble(lineParts[1]);
                return true;
            case "MaxDistanceEast_meters":
                maxDistanceEast_meters = Double.parseDouble(lineParts[1]);
                return true;
            case "NumberWristbands":
                counterElements = Integer.parseInt(lineParts[1]);
                return true;

            case "AuthUsername":
                username = lineParts[1];
                return true;

            case "AuthPassword":
                password = lineParts[1];
                return true;

            case "TimeoutWaitRegistration_ms":
                this.timeoutWaitRegistration_ms = Integer.parseInt(lineParts[1]);
            break;

            case "TimeoutWaitLaunchSingleThreadms":
                timeoutWaitLaunchSingleThreadms = Integer.parseInt(lineParts[1]);
                return true;

            case "SendStdErrorToFile":
                sendStdErrorToFile = Integer.parseInt(lineParts[1]) == 0 ? false : true;
                return true;
            case "SendStdOutToFile":
                sendStdOutToFile = Integer.parseInt(lineParts[1]) == 0 ? false : true;
                return true;

            case "PathFileOutput":
                pathFileOutput = lineParts[1];
                return true;
            case "PathFileError":
                pathFileError = lineParts[1];
                return true;

            case "CounterDevicePerThread":
                counterDevicePerThread = Integer.parseInt(lineParts[1]);
                return true;

            case "PrefixDevice":
                prefixTagID = lineParts[1];
                return true;

            case "VariableNumberWristband":
                isVariableWristbandsVariable = Integer.parseInt(lineParts[1]) == 0 ? false : true;
                return true;
            case "AreaId":
                areaID = lineParts[1];
                return true;

            case "Device":
                deviceName = lineParts[1];
                return true;
            case "Sensor":
                SensorName = lineParts[1];
                return true;
            case "ObservationType":
                observationType = lineParts[1];
                return true;
            case "UnitOfMeasurements":
                unitOfMeasurements = lineParts[1];
                return true;
            case "IntervalSendingSecs":
                intervalSendingSecs = Integer.parseInt(lineParts[1]);
                return true;

            case "EnableRegistrationPhase":
                enableRegistrationPhase = Integer.parseInt(lineParts[1]) == 0 ? false : true;
                return true;

            case "TimeoutWaitingBetweenObservations_ms":
                timeoutWaitingBetweenObservations_ms = Integer.parseInt(lineParts[1]);
                return true;

            case "EnableObservationPhase":
                enableObservationPhase = Integer.parseInt(lineParts[1]) == 0 ? false : true;
                return true;
            default:
                break;

            }

            return false;

        } catch (Exception exc) {

            return false;
        }

    }

    public Boolean getSettingsFromFile(String pathFile, String separator) throws FileNotFoundException {

        try {
            File fileInput = new File(pathFile);

            if (fileInput.canRead() == false) {
                System.err.println("Settings Unable to Load Settings From FILE" + pathFile + "(DEFAULT ASSUMED)");
                return false;
            }

            InputStream inputStream = new FileInputStream(fileInput);
            BufferedReader buffRead = new BufferedReader(new InputStreamReader(inputStream));
            int counterSettingsExtracted = 0;

            String line = "";
            while ((line = buffRead.readLine()) != null) {
                if (getParameterFromString(line, separator) == true) {
                    counterSettingsExtracted++;
                }
            }

            buffRead.close();
            inputStream.close();

            System.out.println("Settings Extract Settings From File:" + pathFile + ", CounterSettingsExtracted:"
                    + counterSettingsExtracted + "");
        } catch (Exception exc) {
            return false;
        }

        return false;
    }
}