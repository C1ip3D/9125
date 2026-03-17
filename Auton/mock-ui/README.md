# Mock Field UI

This is a lightweight UI to test autonomous field layouts by placing:

- AprilTags
- Obstacles

## Open the UI

Open `mock-ui/index.html` in a browser.

If you prefer serving via an HTTP server, use any static server tool from this folder.

Recommended quick launch from repo root:

`python -m http.server 4173 --directory mock-ui`

Then open:

`http://localhost:4173`

## Controls

UI is split into two tabs:

- `Simulation` tab: place robot/tags/obstacles and run mock movement
- `Deployment` tab: set team/deploy/joystick config and export JSON files

- `Select`: pick and drag objects
- `Place AprilTag`: click canvas to add a tag
- `Place Obstacle`: click canvas to add a rectangle obstacle
- `Place Robot`: click canvas to place the test robot start pose
- `Run Mock Auto`: runs a simulated robot drive to target AprilTag
- `Run Engine`: choose `Browser JS Mock` or `WPILib Mock (Java)`
- `Deploy`: set team number, target, and network connection
- `Joysticks`: define driver/operator device type, port, and attached state
- `Stop`: stops the running simulation
- `Delete Selected` or keyboard `Delete`
- Edit object properties in the right panel
- Configure field dimensions and origin frame for meter export
- `Export`: serialize current layout to JSON
- `Import`: paste JSON to restore layout
- `Export Deploy Config`: download deploy+joystick config JSON

## Mock run sequence

1. Place at least one AprilTag
2. Place robot using `Place Robot`
3. Optional: set `Target Tag ID` (leave empty for nearest)
4. Click `Run Mock Auto`
5. Robot animates toward the target with obstacle-aware detours

## WPILib mock run sequence

1. Set `Run Engine` to `WPILib Mock (Java)`
2. Click `Run Mock Auto`
3. UI downloads `ui-current-layout.json`
4. Place file at `src/main/deploy/layouts/ui-current-layout.json`
5. In robot code set `Constants.Mock.ENABLE_WPILIB_MOCK_AUTONOMOUS = true`
6. Run `WPILib: Simulate Robot Code`

This runs the Java command-based WPILib mock planner (`RunWpilibMockAutoCommand`) rather than the browser animation.

## Deploy + joystick config

The layout JSON now includes a `deploy` block:

- `teamNumber`
- `target` (`sim` | `wpilib-mock` | `roborio`)
- `connection` (`usb` | `ethernet` | `wifi`)
- `joysticks.driver` and `joysticks.operator` with `type`, `port`, and `attached`

## Important boundary

The browser UI does not directly push code to roboRIO.

Use WPILib tooling for actual deploy:

- `WPILib: Deploy Robot Code`
- or `./gradlew deploy`

## Export modes

- `Pixel Layout`: canvas coordinates for pure UI editing
- `WPILib Meters`: field-relative coordinates (`meters`) intended for robot/sim data pipelines

## JSON format

```json
{
  "mode": "meters",
  "frame": { "origin": "blue-left", "units": "meters" },
  "mockRun": { "targetTagId": 1, "speedMetersPerSec": 1.2, "planner": "A_STAR_GRID" },
  "fieldMeters": { "width": 16.54, "height": 8.21 },
  "robot": { "x": 1.2, "y": 1.1, "rotationDeg": 0 },
  "tags": [
    { "id": 1, "x": 1.35, "y": 2.25, "rotationDeg": 0 }
  ],
  "obstacles": [
    { "id": 1, "x": 5.2, "y": 3.1, "width": 0.8, "height": 0.6, "rotationDeg": 0 }
  ]
}
```
