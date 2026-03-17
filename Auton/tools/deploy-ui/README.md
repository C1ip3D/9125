# Java Deploy UI (Swing)

This app provides a Java-based UI to:

- Configure deploy target/team/connection
- Configure speed limits
- Configure driver/operator joystick settings
- Save config to `src/main/deploy/layouts/deploy-config.json`
- Run real Gradle commands (`deploy`, `clean deploy`, `simulateJava`)

## Run

From repository root:

```bash
javac tools/deploy-ui/src/DeployManagerApp.java
java -cp tools/deploy-ui/src DeployManagerApp
```

Or use:

```bash
./tools/deploy-ui/run.sh
```

If `javac` is missing on Ubuntu/Linux:

```bash
sudo apt update
sudo apt install -y openjdk-17-jdk-headless
```

## Notes

- This app executes `./gradlew ...` in your selected workspace path.
- If `gradlew` does not exist yet, first create a WPILib project in this repository.
- For real robot deploy, connect robot network and use target `roborio` with your team number configured in WPILib project settings.
