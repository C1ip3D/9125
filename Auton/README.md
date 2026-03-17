# FRC 2026 Starter (Limelight + PathPlanner + Sim-First)

This repository provides a practical starting point for:

1. AprilTag localization via Limelight
2. Trajectory following with PathPlanner
3. Simulation-first validation before deploying to a real robot

## 1) One-time machine setup

- Install latest WPILib for the current season
- Install PathPlanner
- Install Git and connect this repo to GitHub
- Install NI FRC Game Tools on a Windows driver-station laptop (for Driver Station + roboRIO imaging)

## 2) Project setup in VS Code (WPILib)

Use WPILib's project generator first, then copy this source tree over if needed.

1. `Ctrl+Shift+P` -> `WPILib: Create a new project`
2. Select `Template` -> `Java` -> your drivetrain base
3. Set team number and project folder
4. Open generated project in VS Code
5. Merge/update files from this repo into generated project

## 3) Vision and path flow implemented here

- `VisionSubsystem` reads Limelight NetworkTables (`botpose_wpiblue`)
- `DriveSubsystem` owns robot pose estimate and fuses vision updates
- `RobotContainer` configures PathPlanner `AutoBuilder` and exposes `getAutonomousCommand`
- `FollowPathToTagCommand` shows the basic command flow to seed pose and follow a path

## 4) Required constants to tune for your robot

Update values in `src/main/java/frc/robot/Constants.java`:

- drivetrain kinematics geometry
- max linear/angular speed
- Limelight camera pose on robot
- AprilTag pose uncertainty (vision standard deviations)

## 5) Simulation-first checklist

1. Run simulation from VS Code (`WPILib: Simulate Robot Code`)
2. Verify odometry updates without vision
3. Inject/replay Limelight tag measurements and validate pose convergence
4. Run PathPlanner auto and verify tracking error is stable

## 6) Real-robot bring-up checklist

1. Image roboRIO with current season image
2. Validate CAN IDs, inversion, gyro yaw sign, wheelbase dimensions
3. Confirm Limelight mount transform and pipeline configuration
4. Start with conservative speed/accel limits
5. Validate straight, arc, then full autonomous routines

## 7) Mock UI for AprilTag + obstacle testing

A lightweight layout editor is included at:

- `mock-ui/index.html`

See usage details in:

- `mock-ui/README.md`

Sample exported layout for robot-side testing is included at:

- `src/main/deploy/layouts/test-layout.json`

WPILib mock runner support files:

- `src/main/java/frc/robot/commands/RunWpilibMockAutoCommand.java`
- `src/main/java/frc/robot/sim/WpilibMockScenario.java`

Java-based deploy UI app:

- `tools/deploy-ui/src/DeployManagerApp.java`
- `tools/deploy-ui/README.md`

To enable WPILib mock autonomous from exported UI layout, set:

- `Constants.Mock.ENABLE_WPILIB_MOCK_AUTONOMOUS = true`

Expected layout path in deploy directory:

- `src/main/deploy/layouts/ui-current-layout.json`
