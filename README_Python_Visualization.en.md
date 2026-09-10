# ARCore Data Logger — Python Visualization Tool

*English translation of `README_Python_Visualization.md`. Translated for the UannaScan merge
project; not maintained independently of the Japanese original — update that file first, then
re-translate.*

Scripts for visualizing data recorded by ARCore Data Logger, in Python.

## Features

This Python visualization tool provides the following:

### 1. Visualizing 6-DoF pose data
- **3D trajectory display**: shows the device's movement trajectory in 3D space
- **Coordinate frame display**: shows the device's orientation at each point in time as coordinate axes
- **Time-series graphs**: position, quaternion, speed, and distance from the origin over time

### 2. Visualizing the 3D point cloud
- **Colour point cloud**: displays the 3D points with their RGB colour information
- **Overlaid with the device trajectory**: shows the point cloud and device path together

### 3. Data statistics report
- Statistics such as recording duration, update rate, and distance travelled
- Data range and sample count

## Installation

### 1. Installing the required libraries
```bash
pip install -r requirements.txt
```

or install them individually:
```bash
pip install numpy matplotlib
```

### 2. Making the script executable (Linux/Mac)
```bash
chmod +x visualize_arcore_data.py
```

## Usage

### Basic usage

1. **Record data with the ARCore app**
   - Launch the app and press the START button
   - After recording data, press the STOP button
   - The file is downloaded automatically

2. **Visualize with the Python script**
   ```bash
   # Default (uses the downloaded_logs folder)
   python visualize_arcore_data.py

   # Specify a particular folder
   python visualize_arcore_data.py path/to/your/data/folder

   # Specify a particular session folder
   python visualize_arcore_data.py downloaded_logs/ARCore_Logs/20250527142630R_pjinkim_ARCore
   ```

### Command-line arguments

```bash
python visualize_arcore_data.py [data_folder_path]
```

- `data_folder_path`: path to the folder containing the ARCore data files
- if omitted, the `downloaded_logs` folder is used
- if there are multiple session folders, the most recent one is chosen automatically

## Data File Formats

### `ARCore_sensor_pose.txt`
```
# Created at [timestamp] in Burnaby Canada
[timestamp] [qx] [qy] [qz] [qw] [tx] [ty] [tz]
...
```
- `timestamp`: timestamp in nanoseconds
- `qx, qy, qz, qw`: quaternion (device orientation)
- `tx, ty, tz`: position (metres)

### `ARCore_point_cloud.txt`
```
# Created at [timestamp] in Burnaby Canada
[x] [y] [z] [R] [G] [B]
...
```
- `x, y, z`: 3D coordinates (metres)
- `R, G, B`: RGB colour information (0–255)

## Visualization Details

### 1. 3D trajectory plot
- **Blue line**: the device's movement trajectory
- **Green circle**: the starting point
- **Red square**: the end point
- **RGB axes**: the device's orientation at each point in time (red = X axis, green = Y axis, blue = Z axis)

### 2. Time-series plots
- **Position graph**: X, Y, Z coordinates over time
- **Quaternion graph**: qx, qy, qz, qw over time
- **Speed graph**: the device's movement speed
- **Distance graph**: distance from the origin

### 3. Point cloud plot
- **Coloured points**: displayed with their actual RGB colour
- **Device trajectory**: the device's movement path shown as a red line

## About the ARCore Coordinate System

### ARCore world-coordinate-system definition
ARCore uses a right-handed coordinate system:

- **X axis**: right (+X) / left (−X)
- **Y axis**: up (+Y) / down (−Y)
  - the gravity vector points in the −Y direction
  - the Y axis is determined from the gravity direction when ARCore initializes
- **Z axis**: behind the camera (+Z) / in front of the camera (−Z)
  - the direction the camera is facing is −Z
  - determined from the camera's orientation at the start of the session

### How coordinate axes are shown in the visualizations

#### 1. 3D view
- All axes are shown as-is (X, Y, Z)
- Coordinate frame arrows:
  - **Red**: X axis (rightward)
  - **Green**: Y axis (upward)
  - **Blue**: Z axis (behind the camera)

#### 2. Top view (X–Y plane)
- X axis: left/right movement
- Y axis: up/down movement
- This viewpoint shows the gravity direction (Y axis) against horizontal movement (X axis)

#### 3. Side view (X–Z plane)
- X axis: left/right movement
- Y axis of the plot shows the −Z axis (forward/backward relative to the camera)
- **Important**: the sign of the Z axis is flipped (−Z) for a more intuitive display
- This makes the camera moving forward/backward appear as up/down movement in the plot

#### 4. Interpreting the camera direction
- The device's orientation is shown at each point in time by its coordinate frame (the RGB arrows)
- The blue arrow (Z axis) points behind the camera, so the camera itself faces the opposite direction from the blue arrow

### About the Coordinate Transformation

#### ARCore → visualization coordinate transformation
- 3D view: uses ARCore coordinates directly
- Top view: shows the X–Y plane as-is
- Side view: X axis unchanged, uses −Z as the plotted Y axis

#### Coordinate system in RFID tag localization
In the RFID localization system:
- the camera-direction vector is computed as the −Z direction
- localization results are output in the ARCore coordinate system
- the coordinate-transformation rules above are applied when visualizing

## Troubleshooting

### Common issues

1. **"No data could be loaded!" error**
   - Check that the data file path is correct
   - Check that the `ARCore_sensor_pose.txt` file exists

2. **"No point cloud data available" warning**
   - Short recordings may have little or no point cloud data
   - Try a longer recording

3. **Graphs do not display**
   - Check matplotlib's backend:
     ```python
     import matplotlib
     matplotlib.use('TkAgg')  # or 'Qt5Agg'
     ```

4. **Memory errors**
   - Large point cloud datasets can run out of memory
   - Try a shorter recording, or subsample the data

5. **Coordinate axes point in an unexpected direction**
   - The ARCore coordinate system is right-handed
   - Gravity is the Y axis; the camera direction is the negative Z axis
   - The coordinate system is fixed by the device's orientation at initialization

## Customization

The script is easy to customize:

```python
# Create an instance of the visualizer class
visualizer = ARCoreDataVisualizer('your_data_folder')

# Run individual visualization functions
visualizer.load_pose_data()
visualizer.plot_trajectory_3d()
visualizer.plot_pose_components()
```

## Example Output

Running the script produces output like this:

```
Loading ARCore data...
Loaded 66 pose samples
Warning: No point cloud data found!

==================================================
ARCore Data Summary Report
==================================================
Pose Data:
  - Number of samples: 66
  - Duration: 2.18 seconds
  - Average update rate: 30.3 Hz
  - Total distance traveled: 0.045 m
  - Position range:
    X: [-0.002, 0.003] m
    Y: [-0.001, 0.002] m
    Z: [-0.003, 0.001] m
==================================================

Generating trajectory visualization...
Generating pose component plots...
Visualization complete!
```

## License

This script is provided under the MIT license.
