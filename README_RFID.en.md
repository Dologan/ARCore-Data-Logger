# RFID Tag Localization System Using ARCore

*English translation of `README_RFID.md`. Translated for the UannaScan merge project; not
maintained independently of the Japanese original — update that file first, then re-translate.*

This project is an extended version of ARCore Data Logger with RFID tag detection and
localization added. The system uses ARCore's Visual-Inertial Odometry (VIO) to track the
device's position and orientation, and estimates RFID tag positions from detections made at
multiple viewpoints.

## Features

- **ARCore integration**: uses ARCore for accurate device pose tracking
- **RFID tag detection**: simulates RFID tag detection via a button press
- **Tag localization**: estimates the 3D position of RFID tags using triangulation
- **Data visualization**: 3D and 2D plots of the device trajectory and tag positions
- **Comprehensive analysis**: statistical analysis of localization accuracy

## System Requirements

### Android app
- ARCore-compatible Android device
- Android Studio
- Minimum SDK version: 24 (Android 7.0)

### Python analysis
- Python 3.7+
- Required packages:
  ```
  numpy
  matplotlib
  pandas
  scipy
  ```

## Installation

### 1. Setting up the Android app

1. Open the project in Android Studio
2. Build and install the app on an ARCore-compatible device
3. Grant the camera and storage permissions when prompted

### 2. Setting up the Python environment

Install the required Python packages:
```bash
pip install numpy matplotlib pandas scipy
```

## Usage

### 1. Collecting data

1. **Launch the Android app**
2. **Start recording**: press the "Start Recording" button
3. **Walk around**: move through the area where you want to detect RFID tags
4. **Detect a tag**: press the "Detect RFID Tag" button whenever you want to simulate an RFID
   tag detection
   - For better localization accuracy, press the button from several different positions
   - The system records the device's pose (position and orientation) at each detection
5. **Stop recording**: press "Stop Recording" when finished

### 2. Analysing data

#### Quick test
Analyse the data with the test script:
```bash
python test_rfid_localization.py
```

#### Manual analysis
```python
from rfid_tag_localization import RFIDTagLocalizer

# Initialize with the session folder
localizer = RFIDTagLocalizer("path/to/your/session/folder")

# Load the pose data
localizer.load_pose_data()

# Localize all detected tags
localizer.localize_all_tags(max_distance=5.0)

# Generate a report
localizer.generate_report()

# Show the visualization
localizer.plot_results()
```

## Data Format

### ARCore pose data (`ARCore_sensor_pose.txt`)

Each line contains:
```
timestamp qx qy qz qw tx ty tz [tag_id]
```

where:
- `timestamp`: timestamp in nanoseconds
- `qx, qy, qz, qw`: quaternion for device orientation
- `tx, ty, tz`: position (metres)
- `tag_id`: optional RFID tag ID (present when a tag was detected)

Example:
```
1685188246123456789 0.001234 -0.005678 0.012345 0.999876 1.234567 -0.567890 1.500000
1685188246156789012 0.001235 -0.005679 0.012346 0.999875 1.234568 -0.567891 1.500001 TAG001
```

## About the ARCore Coordinate System

### ARCore world-coordinate-system definition
ARCore uses a right-handed coordinate system:

- **X axis**: right (+X) / left (−X)
- **Y axis**: up (+Y) / down (−Y)
  - the gravity vector points in the −Y direction
- **Z axis**: behind the camera (+Z) / in front of the camera (−Z)
  - the direction the camera is facing is −Z

### 🔥 **Top View (X–Z) — the most important viewpoint** 🔥

**Why is the X–Z plane the top view?**

In the ARCore coordinate system, the Y axis represents the vertical (gravity) direction, so
**the X–Z plane is the horizontal plane**. This means:

1. **X axis**: shows left/right movement
2. **Z axis**: shows forward/backward movement (the −Z direction is where the camera faces)
3. **Bird's-eye view**: shows device movement and tag positions from above

This viewpoint is the most important for the following reasons:
- **Understanding spatial relationships**: the relative positions of tags and the device are
  intuitive
- **Grasping movement patterns**: the device's movement trajectory is clear
- **Visualizing detection direction**: the camera's facing direction and detection direction are
  easy to see
- **Validating localization**: makes it possible to check whether the estimated tag positions
  are plausible

### How coordinate axes are shown in the visualizations

#### 1. 3D view
- All axes are shown as-is (X, Y, Z)

#### 2. **🔥 Top view (X–Z plane) — most important**
- X axis: right/left direction
- −Z axis: forward/backward direction (camera viewpoint)
- **Use this view to assess tag-localization accuracy**

#### 3. Side view (X–Y plane)
- X axis: right/left direction
- Y axis: up/down (vertical) direction
- **Note**: this is a side view (it is *not* the top view)

#### 4. Camera-direction arrow
- The red arrow shows the direction the device's camera is facing
- In the ARCore coordinate system, the camera direction is the −Z axis
- **This is shown most clearly in the top view**

## Localization Algorithm

The system uses two methods to localize tags:

### 1. Weighted-centroid method (≥2 detections)
- Computes the centroid of the detection positions
- Weighted by detection confidence (currently uniform)

### 2. Ray-intersection method (≥3 detections)
- Projects a ray from the device position along its line of sight
- Searches for the point that minimizes the distance to all the rays
- More accurate when there are multiple detections

## Visualization

The system generates several plots:

1. **3D trajectory**: the device path and tag positions in 3D space
2. **2D top view**: a bird's-eye view of the trajectory and tags
3. **2D side view**: a side view of the trajectory and tags (with corrected axes)
4. **Detection timeline**: tag detections over time
5. **Error analysis**: localization accuracy metrics

## Configuration

### Localization parameters

Adjustable in `rfid_tag_localization.py`:

```python
# Maximum distance for a valid detection (metres)
max_distance = 5.0

# Minimum number of detections required for localization
min_detections = 2

# Ray-intersection tolerance
intersection_tolerance = 0.1
```

### Android app parameters

In `MainActivity.java`:

```java
// Simulated tag ID (can be changed dynamically)
private static final String SIMULATED_TAG_ID = "TAG001";
```

> **Translator's note (UannaScan merge, 2026):** `MainActivity.java` has since grown two more
> hardcoded simulated-tag buttons alongside this one (`detectRFIDTag1`/`detectRFIDTag2`/
> `detectRFIDTagBoth`, see `ARCore-Data-Logger/app/src/main/java/com/pjinkim/arcore_data_logger/MainActivity.java`).
> UannaScan replaces this whole mechanism with a general-purpose waypoint marker (see
> `UannaScan/docs/architecture.md`'s `marker` table), of which RFID is one kind among several,
> rather than adding a fourth hardcoded button.

## Troubleshooting

### Common issues

1. **"No ARCore session" error**: confirm the device supports ARCore and the app has camera
   permission
2. **"No tag detections" error**: make sure you pressed "Detect RFID Tag" while recording
3. **Poor localization accuracy**: collect more detections from different positions and angles
4. **"File not found" error**: check that `ARCore_sensor_pose.txt` exists in the session folder

### Tips for good data quality

1. **Multiple viewpoints**: detect the tag from at least 3–4 different positions
2. **Varied angles**: approach the tag area from different directions
3. **Steady movement**: avoid sudden movements at the moment of detection
4. **Good lighting**: ensure enough light for ARCore tracking

## Integrating with a Real RFID Reader

To integrate with real RFID hardware:

1. **Replace the button detection**: modify `MainActivity.java` to listen for RFID reader events
2. **Add an RFID library**: include an appropriate RFID reader SDK
3. **Update the tag ID**: use the actual tag ID reported by the RFID reader
4. **Add RSSI data**: include signal-strength information to improve localization

Integration example:
```java
// Replace the button click with an RFID detection callback
private void onRFIDTagDetected(String tagId, float rssi) {
    if (mARCoreSession != null && mARCoreSession.isRecording()) {
        mARCoreSession.detectRFIDTag(tagId, rssi);
    }
}
```

## Performance Considerations

- **Recording frequency**: ARCore pose data is recorded at approximately 30Hz
- **Memory usage**: large sessions can require significant memory during analysis
- **Processing time**: localization time scales with the number of detections
- **Accuracy**: typical accuracy is 10–50cm depending on conditions

## Planned Future Extensions

1. **RSSI-based localization**: use signal strength for distance estimation
2. **Kalman filtering**: improve pose estimation through sensor fusion
3. **Real-time localization**: process detections while recording
4. **Multi-tag support**: localize multiple tags simultaneously
5. **Machine learning**: use ML to improve detection and localization

## License

This project extends the original ARCore Data Logger. See the original license terms.

## Contact

For questions or issues, refer to the original ARCore Data Logger documentation or open an issue
in the project repository.
