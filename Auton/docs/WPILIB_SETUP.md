# WPILib + PathPlanner Setup Steps

This repository contains robot code sources. For a complete runnable project, generate a season project in WPILib and ensure vendor dependencies are installed.

## Generate project

1. Open VS Code with WPILib extension installed.
2. `Ctrl+Shift+P` -> `WPILib: Create a new project`
3. Template -> Java -> Swerve (or your drivetrain)
4. Team Number -> your team number
5. Location -> this repository folder (`frc-2026`)

If WPILib created files already exist, keep generated build files and merge `src/main/java` from this repo.

## Add vendor dependencies

From VS Code:

1. `Ctrl+Shift+P` -> `WPILib: Manage Vendor Libraries`
2. Install latest PathPlannerLib
3. Install latest LimelightLib (if your team uses its helper library; this code also supports raw NetworkTables)

## Create the path file

In PathPlanner, create and save:

- `ToCenterTag.path`

The code loads this path in autonomous.

## Run simulation

From VS Code command palette:

- `WPILib: Simulate Robot Code`

Validate that:

- pose estimator updates from odometry
- vision measurement updates are accepted
- auto command follows path without oscillation

## Run WPILib mock autonomous from UI layout

1. In mock UI, choose `Run Engine = WPILib Mock (Java)`
2. Click `Run Mock Auto` to download `ui-current-layout.json`
3. Copy file to `src/main/deploy/layouts/ui-current-layout.json`
4. Set `Constants.Mock.ENABLE_WPILIB_MOCK_AUTONOMOUS = true`
5. Run simulation again with `WPILib: Simulate Robot Code`

## Deploy to robot

1. Ensure roboRIO image matches season WPILib
2. Connect via USB/Ethernet
3. `WPILib: Deploy Robot Code`
4. Confirm Driver Station communication and FMS-safe behavior
