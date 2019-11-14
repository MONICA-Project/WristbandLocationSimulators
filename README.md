# Wristband Localization Simulator
<!-- Short description of the project. -->

Wristband Localization Simulator is a Java Gradle project that allows to simulate provisioning of wristbands localization observations in a pre-defined geographic area.

<!-- A teaser figure may be added here. It is best to keep the figure small (<500KB) and in the same repo -->

## Getting Started
<!-- Instruction to make the project up and running. -->

The project documentation is available on the [Wiki](https://github.com/MONICA-Project/template/wiki).

## Deployment
<!-- Deployment/Installation instructions. If this is software library, change this section to "Usage" and give usage examples -->

## Development
<!-- Developer instructions. -->

### Prerequisite

List of prerequisites are:
  - Ensure having Java JDK installed (Version 1.8) to modify source code (SDK is enough to run it); 
  - Download and install gradle [See this link for Windows][https://www.tutorialspoint.com/gradle/gradle_installation.htm]

### Test
After configuring `Settings`, under $REPOROOT/Releases folder, types command:
```bash
sh launch_1.1.0.sh
```

### Build

- Go to folder $REPOROOT\wristband_simulation
- Type command: 

```bash
$ gradle uberJar
```

It launches build java project
	
```bash
$ sh launch.sh
```

It launches simulation with associated file settings reported in [$REPOROOT\settings\settingsApplication.conf](settings/settingsApplication.conf)
  
### Settings

Content of file settings (see [$REPOROOT\settings\settingsApplication.conf](settings/settingsApplication.conf)): 
 
-	**URL**: SCRAL URL TO PUT Observation and Registration(default=http://monapp-lst.monica-cloud.eu:8490/scral/v1.0/wristband-gw/wearable/localization) 
-	**Latitude**:  GroundPlanePosition Latitude, the reference position for localization random simulation 
-	**Longitude**: GroundPlanePosition Longitude, the reference position for localization random simulation 
-	**MaxDistanceNorth_meters**: Max Distance North (in meters) for random placement of the position observation generated 
-	**MaxDistanceEast_meters**: Max Distance East (in meters) for random placement of the position observation generated 
-	**NumberWristbands**: Number of Wristbands simulated 
-	**VariableNumberWristband**: Increase between two differen DeviceID (keep to 1) 
-	**AreaId**: Fixed content message field 
-	**DeviceType**: Fixed content message field (default: 868) 
-	**Device**: Fixed content message field (default: wearable) 
-	**Sensor**: Fixed content message field (default: tag) 
-	**ObservationType**: Fixed content message field (default: proprietary) 
-	**UnitOfMeasurements**: Fixed content message field (default: meters) 
-	**IntervalSendingSecs**: Interval between two different slots of location observations of all devices (in seconds) 
-	**PrefixDevice**: Prefix for Device Id or tagID for JSON message (default: GeoTag). Simulation creates deviceID=${PrefixDevice}ID, with ID in range(0, NumberWristbands) 
-	**EnableRegistrationPhase**: Enable Registration Phase at beginning of simulation, 1 to enable 0 to disable. NOTE: Registration is mandatory when there is some difference in deviceIDs generated from the past 
-	**EnableObservationPhase**: Enable Observation Sending Phase. NOTE: Each DeviceID must be registered 
-	**TimeoutWaitingBetweenObservations_ms**: Timeout (in milliseconds) to wait between two different observations from two different devices in specific Thread created 
-	**CounterDevicePerThread**: Number of devices passed for each Thread generated. NOTE: The number of thread generated is equal to ceil(NumberWristbands/CounterDevicePerThread) 
-	**TimeoutWaitLaunchSingleThreadms**: Timeout (in millisecond) for initial delay in single thread launch 
-	**PathFileOutput**: Path to redirect standard output. NOTE: it acts only if SendStdOutToFile is enabled (set to 1). The user must take care about local path set (Check if folder exists) 
-	**PathFileError**: Path to redirect standard error. NOTE: it acts only if SendStdErrorToFile is enabled (set to 1). The user must take care about local path set (Check if folder exists) 
-	**SendStdErrorToFile**: Enable Standard Output redirect to file, 1: Enabled to PathFileOutput, 0 to maintain it in console 
-	**SendStdOutToFile**: Enable Standard Error redirect to file, 1: Enabled to PathFileError, 0 to maintain it in console 

  
## Contributing
Contributions are welcome. 

Please fork, make your changes, and submit a pull request. For major changes, please open an issue first and discuss it with the other authors.

## Affiliation
![MONICA](https://github.com/MONICA-Project/template/raw/master/monica.png)  
This work is supported by the European Commission through the [MONICA H2020 PROJECT](https://www.monica-project.eu) under grant agreement No 732350.

> # Notes
>
> * The above templace is adapted from [[1](https://github.com/cpswarm/template), [2](https://www.makeareadme.com), [3](https://gist.github.com/PurpleBooth/109311bb0361f32d87a2), [4](https://github.com/dbader/readme-template)].
> * Versioning: Use [SemVer](http://semver.org/) and tag the repository with full version string. E.g. `v1.0.0`
> * License: Provide a LICENSE file at the top level of the source tree. You can use Github to [add a license](https://help.github.com/en/articles/adding-a-license-to-a-repository). This template repository has an [Apache 2.0](LICENSE) file.
>
> *Remove this section from the actual readme.*
