# ARCore Data Logger Specification

*English translation of `ARCore_Data_Logger_仕様書.md`. Translated for the UannaScan merge project; not maintained independently of the Japanese original — update that file first, then re-translate.*

## 1. Project Overview

### 1.1 Project Name
ARCore Data Logger

### 1.2 Purpose
An application that records, in real time, 6-degree-of-freedom (6-DoF) pose estimates and 3D point cloud data from Google ARCore's Visual-Inertial Odometry (VIO) on Android devices, saving them as text files for offline analysis.

### 1.3 Developer Information
- Original author: PyojinKim
- GitHub: https://github.com/PyojinKim/ARCore-Data-Logger
- License: MIT License

## 2. System Requirements

### 2.1 Hardware Requirements

#### 2.1.1 Mandatory Requirements
- ARCore-compatible Android device
- Camera hardware
- Accelerometer and gyroscope
- GPU supporting OpenGL ES 3.0 or later
- RAM: 3GB or more recommended
- Storage: 1GB or more free space

#### 2.1.2 Recommended Devices
- Google Pixel series
- Samsung Galaxy series (ARCore-compatible models)
- Other officially ARCore-supported devices

### 2.2 Software Requirements

#### 2.2.1 Android OS
- Minimum version: Android 8.0 (API level 26)
- Recommended version: Android 9.0 (API level 28) or later
- Target version: Android 13 (API level 33)
- Compile SDK: API level 34

#### 2.2.2 ARCore Requirements
- Google Play Services for AR (ARCore) must be installed
- ARCore version: 1.11.0 or later
- Device must be on the ARCore-supported device list

#### 2.2.3 Required Permissions
- `CAMERA`: camera access permission (mandatory)
- `WAKE_LOCK`: prevents the screen from sleeping

## 3. Application Specification

### 3.1 Architecture

#### 3.1.1 Main Class Structure
- `MainActivity`: main activity, UI control
- `ARCoreSession`: ARCore session management, data acquisition
- `ARCoreResultStreamer`: data file output management
- `AccumulatedPointCloud`: point cloud data accumulation
- `OutputDirectoryManager`: output directory management
- `FileStreamer`: base class for file writing

#### 3.1.2 Data Flow
1. Initialize the ARCore session
2. Acquire a camera frame
3. Extract 6-DoF pose data
4. Extract 3D point cloud data
5. Write to file in real time
6. Save the file when the session ends

### 3.2 User Interface

#### 3.2.1 Main Screen Layout
- ARCore camera view (full screen)
- Information display panel (semi-transparent overlay)
  - Feature point count
  - Tracking state
  - Failure reason
  - Update rate
- Control panel
  - START/STOP button
  - Recording time display
- Logo display (SFU, GRUVI)

#### 3.2.2 State Display
- **Tracking state**
  - TRACKING: tracking normally
  - PAUSED: temporarily paused
  - STOPPED: stopped
- **Failure reason**
  - NONE: no problem
  - LOW FEATURES: insufficient feature points
  - FAST MOTION: moving too fast
  - LOW LIGHT: insufficient lighting
  - BAD STATE: invalid state

### 3.3 Data Recording Specification

#### 3.3.1 6-DoF Pose Data (`ARCore_sensor_pose.txt`)
```
# Created at [timestamp] in Burnaby Canada
[timestamp] [qx] [qy] [qz] [qw] [tx] [ty] [tz]
```

**Format details:**
- `timestamp`: timestamp in nanoseconds
- `qx, qy, qz, qw`: quaternion (device attitude)
- `tx, ty, tz`: position coordinates (metres)

**Coordinate system:** ARCore world coordinate system
- X axis: right (+) / left (−)
- Y axis: up (+) / down (−), opposite to gravity
- Z axis: forward (+) / backward (−)

#### 3.3.2 3D Point Cloud Data (`ARCore_point_cloud.txt`)
```
# Created at [timestamp] in Burnaby Canada
[x] [y] [z] [R] [G] [B]
```

**Format details:**
- `x, y, z`: 3D coordinates (metres)
- `R, G, B`: RGB colour information (0–255)

**Filtering criteria:**
- Confidence threshold: 0.5 or higher
- Only points visible in the camera image
- Only points with valid colour information

### 3.4 File Management

#### 3.4.1 Storage Location
- **Internal storage**: `/data/user/0/com.pjinkim.arcore_data_logger/files/`
- **For external access**: `Android/data/com.pjinkim.arcore_data_logger/files/ARCore_Logs/`

#### 3.4.2 Folder Naming Convention
```
[prefix]YYYYMMDDHHMMSS[suffix]
Example: 20250527143046R_pjinkim_ARCore
```

#### 3.4.3 File Layout
```
session_folder/
├── ARCore_sensor_pose.txt    # 6-DoF pose data
└── ARCore_point_cloud.txt    # 3D point cloud data
```

## 4. Development Environment Specification

### 4.1 Development Tools
- **Android Studio**: latest stable version recommended
- **Gradle**: 8.2
- **Android Gradle Plugin**: 8.2.2
- **Build Tools**: 34.0.0

### 4.2 Dependencies
```gradle
dependencies {
    implementation "com.google.ar.sceneform.ux:sceneform-ux:1.11.0"
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    // for testing
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test:runner:1.5.2'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}
```

> **Translator's note (UannaScan merge, 2026):** this reflects the state of the repository before
> modernisation. The current `app/build.gradle` uses AGP 9.4.0 with `com.google.ar:core:1.56.0`
> pinned explicitly, and the Sceneform Gradle *plugin* has been removed (it only ever compiled
> `.sfa`/`.sfb` model assets, this project has none, and it does not work with modern AGP) — the
> `sceneform-ux` runtime dependency above is unaffected. See the top-level `README.md` and
> `docs/compatibility.md` in the UannaScan repository for the current toolchain and the reasoning
> behind holding `targetSdk` at 34 rather than tracking `compileSdk`.

## 5. Data Analysis Tool Specification

### 5.1 Python Visualisation Scripts

#### 5.1.1 File: `visualize_arcore_data.py`
**Features:**
- 3D trajectory visualisation
- Time-series data plotting
- Point cloud display
- Statistical report generation

**Required libraries:**
```
numpy>=1.20.0
matplotlib>=3.3.0
scipy>=1.7.0
```

#### 5.1.2 File: `save_analysis_results.py`
**Features:**
- Detailed statistics computation
- Saving visualisation images
- JSON/text report output
- Per-session analysis

### 5.2 Analysis Result Output

#### 5.2.1 Generated Files
```
analysis_results/
├── trajectory_3d.png           # 3D trajectory image
├── time_series_analysis.png    # time-series analysis image
├── point_cloud_3d.png          # point cloud image
├── analysis_report.json        # machine-readable report
└── analysis_report.txt         # human-readable report
```

#### 5.2.2 Statistics Computed
- **Session information**: duration, sample count, update rate
- **Motion analysis**: distance travelled, speed statistics
- **Rotation analysis**: roll, pitch, yaw ranges
- **Position statistics**: mean position, standard deviation, range
- **Point cloud statistics**: point count, coordinate range

## 6. Performance Specification

### 6.1 Update Rate
- **ARCore**: up to 60Hz
- **UI updates**: 10Hz (100ms interval)
- **File writes**: real time

### 6.2 Memory Management
- **Point cloud**: accumulates up to 50,000 points
- **Memory usage**: depends on image processing and point cloud data
- **Long recordings**: memory usage must be monitored

### 6.3 Storage Usage
- **Pose data**: approximately 2KB/second (at 30Hz)
- **Point cloud**: variable (depends on the environment)
- **Estimate**: approximately 500KB–2MB per minute

## 7. Error Handling

### 7.1 ARCore-Related Errors
- **ARCore not installed**: direct the user to the Google Play Store
- **Version mismatch**: prompt an update
- **Device not supported**: display a clear error message

### 7.2 Permission-Related Errors
- **Camera permission denied**: the app exits
- **Permission requests**: implemented via the runtime permission request flow

### 7.3 File I/O Errors
- **Insufficient disk space**: show an error and stop gracefully
- **Write failure**: log the failure and keep retrying

## 8. Security Specification

### 8.1 Data Protection
- **Internal storage use**: restricts access from other apps
- **Minimal permissions**: requests only the minimum permissions necessary
- **Data encryption**: relies on standard Android encryption

### 8.2 Privacy
- **Location data**: only relative coordinates are recorded (no absolute position)
- **Image data**: not saved directly (only colour information is extracted)
- **Personally identifiable information**: none is collected

## 9. Test Specification

### 9.1 Unit Tests
- **Data processing**: quaternion conversion, coordinate transformation
- **File I/O**: write and read functionality
- **Error handling**: verification of exception handling

### 9.2 Integration Tests
- **ARCore session**: from initialization through to termination
- **UI operation**: button interactions, state transitions
- **Data integrity**: verification of recorded data

### 9.3 Device Tests
- **Multiple devices**: confirm operation on different ARCore-compatible devices
- **OS versions**: confirm compatibility across different Android versions
- **Long-duration operation**: check for memory leaks and performance degradation

## 10. Operations and Maintenance

### 10.1 Logging
- **Levels**: INFO, WARN, ERROR
- **Destination**: Android Logcat
- **Content**: session state, error details, performance information

### 10.2 Version Management
- **Semantic versioning**: MAJOR.MINOR.PATCH
- **Release notes**: record of features added and bugs fixed
- **Backward compatibility**: data format compatibility is maintained

### 10.3 Support
- **Documentation**: README and specification kept up to date
- **GitHub Issues**: used to manage bug reports and feature requests
- **Community**: supported through the open-source community

## 11. Planned Future Extensions

### 11.1 Feature Extensions
- **Real-time visualisation**: 3D display while recording
- **Data compression**: reducing file size
- **Cloud sync**: automatic data backup

### 11.2 Enhanced Analysis Features
- **Machine learning**: trajectory pattern recognition
- **Statistical analysis**: more detailed motion analysis
- **Comparison feature**: comparing multiple sessions

### 11.3 Platform Extensions
- **iOS support**: developing an ARKit version
- **Web version**: a browser version using WebXR
- **Cross-platform**: a Flutter/React Native version

---

**Last updated**: May 27, 2025
**Version**: 1.0
**Author**: ARCore Data Logger development team
