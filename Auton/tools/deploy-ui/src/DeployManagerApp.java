import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class DeployManagerApp {
  private final JTextField workspaceField = new JTextField(".", 30);
  private final JTextField teamField = new JTextField("0", 8);
  private final JTextField maxLinearSpeedField = new JTextField("4.0", 8);
  private final JTextField maxAngularSpeedField = new JTextField("3.14", 8);

  private final JComboBox<String> targetBox = new JComboBox<>(new String[] {"sim", "wpilib-mock", "roborio"});
  private final JComboBox<String> connectionBox = new JComboBox<>(new String[] {"usb", "ethernet", "wifi"});

  private final JComboBox<String> driverTypeBox =
      new JComboBox<>(new String[] {"xbox", "joystick", "ps", "custom"});
  private final JTextField driverPortField = new JTextField("0", 5);
  private final JCheckBox driverAttachedBox = new JCheckBox("Attached", true);

  private final JComboBox<String> operatorTypeBox =
      new JComboBox<>(new String[] {"xbox", "joystick", "ps", "none"});
  private final JTextField operatorPortField = new JTextField("1", 5);
  private final JCheckBox operatorAttachedBox = new JCheckBox("Attached", true);

  private final JTextArea outputArea = new JTextArea();

  private static final String DEPLOY_CONFIG_RELATIVE_PATH = "src/main/deploy/layouts/deploy-config.json";

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> new DeployManagerApp().show());
  }

  private void show() {
    JFrame frame = new JFrame("FRC Deploy Manager");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLayout(new BorderLayout(10, 10));

    JPanel form = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(6, 6, 6, 6);
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridy = 0;

    addRow(form, c, "Workspace Path", workspaceField);
    addRow(form, c, "Team Number", teamField);
    addRow(form, c, "Deploy Target", targetBox);
    addRow(form, c, "Connection", connectionBox);
    addRow(form, c, "Max Linear Speed (m/s)", maxLinearSpeedField);
    addRow(form, c, "Max Angular Speed (rad/s)", maxAngularSpeedField);

    addRow(form, c, "Driver Joystick Type", driverTypeBox);
    addRow(form, c, "Driver Port", driverPortField);
    addRow(form, c, "Driver", driverAttachedBox);

    addRow(form, c, "Operator Joystick Type", operatorTypeBox);
    addRow(form, c, "Operator Port", operatorPortField);
    addRow(form, c, "Operator", operatorAttachedBox);

    JButton saveConfigButton = new JButton("Save Config JSON");
    JButton deployButton = new JButton("Run ./gradlew deploy");
    JButton cleanDeployButton = new JButton("Run ./gradlew clean deploy");
    JButton simulateButton = new JButton("Run ./gradlew simulateJava");

    JPanel actions = new JPanel();
    actions.add(saveConfigButton);
    actions.add(deployButton);
    actions.add(cleanDeployButton);
    actions.add(simulateButton);

    outputArea.setEditable(false);
    outputArea.setLineWrap(true);
    outputArea.setWrapStyleWord(true);

    JScrollPane outputPane = new JScrollPane(outputArea);
    outputPane.setPreferredSize(new Dimension(900, 280));

    saveConfigButton.addActionListener(event -> saveConfigOnly());
    deployButton.addActionListener(event -> runGradleCommand(List.of("./gradlew", "deploy")));
    cleanDeployButton.addActionListener(event -> runGradleCommand(List.of("./gradlew", "clean", "deploy")));
    simulateButton.addActionListener(event -> runGradleCommand(List.of("./gradlew", "simulateJava")));

    frame.add(form, BorderLayout.NORTH);
    frame.add(actions, BorderLayout.CENTER);
    frame.add(outputPane, BorderLayout.SOUTH);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  private void addRow(JPanel panel, GridBagConstraints c, String label, java.awt.Component component) {
    c.gridx = 0;
    c.weightx = 0;
    panel.add(new JLabel(label), c);
    c.gridx = 1;
    c.weightx = 1;
    panel.add(component, c);
    c.gridy += 1;
  }

  private void saveConfigOnly() {
    try {
      Path configPath = writeConfig();
      appendOutput("Saved deploy config to: " + configPath);
    } catch (Exception ex) {
      showError("Failed to save config: " + ex.getMessage());
    }
  }

  private void runGradleCommand(List<String> command) {
    try {
      Path workspacePath = getWorkspacePath();
      Path gradlew = workspacePath.resolve("gradlew");
      if (!Files.exists(gradlew)) {
        showError("Could not find gradlew in workspace path: " + workspacePath);
        return;
      }

      Path configPath = writeConfig();
      appendOutput("Saved deploy config to: " + configPath);
      appendOutput("Running: " + String.join(" ", command));

      new Thread(
              () -> {
                try {
                  ProcessBuilder pb = new ProcessBuilder(command);
                  pb.directory(workspacePath.toFile());
                  pb.redirectErrorStream(true);
                  Process process = pb.start();

                  byte[] bytes = process.getInputStream().readAllBytes();
                  String output = new String(bytes, StandardCharsets.UTF_8);
                  int exit = process.waitFor();

                  appendOutput(output);
                  appendOutput("Process exited with code: " + exit);
                } catch (Exception ex) {
                  appendOutput("Command failed: " + ex.getMessage());
                }
              })
          .start();
    } catch (Exception ex) {
      showError("Could not start deploy: " + ex.getMessage());
    }
  }

  private Path writeConfig() throws IOException {
    Path workspacePath = getWorkspacePath();
    Path configPath = workspacePath.resolve(DEPLOY_CONFIG_RELATIVE_PATH);
    Files.createDirectories(configPath.getParent());
    Files.writeString(configPath, buildConfigJson(), StandardCharsets.UTF_8);
    return configPath;
  }

  private Path getWorkspacePath() {
    return Path.of(workspaceField.getText().trim()).toAbsolutePath().normalize();
  }

  private String buildConfigJson() {
    int teamNumber = parseInt(teamField.getText(), 0);
    double maxLinear = parseDouble(maxLinearSpeedField.getText(), 4.0);
    double maxAngular = parseDouble(maxAngularSpeedField.getText(), 3.14);

    int driverPort = parseInt(driverPortField.getText(), 0);
    int operatorPort = parseInt(operatorPortField.getText(), 1);

    List<String> lines = new ArrayList<>();
    lines.add("{");
    lines.add("  \"format\": \"frc-deploy-config-v1\",");
    lines.add("  \"generatedAt\": \"" + Instant.now() + "\",");
    lines.add("  \"deploy\": {");
    lines.add("    \"teamNumber\": " + teamNumber + ",");
    lines.add("    \"target\": \"" + targetBox.getSelectedItem() + "\",");
    lines.add("    \"connection\": \"" + connectionBox.getSelectedItem() + "\", ");
    lines.add("    \"speed\": {");
    lines.add("      \"maxLinearMps\": " + round4(maxLinear) + ",");
    lines.add("      \"maxAngularRadPerSec\": " + round4(maxAngular));
    lines.add("    },");
    lines.add("    \"joysticks\": {");
    lines.add("      \"driver\": {");
    lines.add("        \"type\": \"" + driverTypeBox.getSelectedItem() + "\",");
    lines.add("        \"port\": " + driverPort + ",");
    lines.add("        \"attached\": " + driverAttachedBox.isSelected());
    lines.add("      },");
    lines.add("      \"operator\": {");
    lines.add("        \"type\": \"" + operatorTypeBox.getSelectedItem() + "\",");
    lines.add("        \"port\": " + operatorPort + ",");
    lines.add("        \"attached\": " + operatorAttachedBox.isSelected());
    lines.add("      }");
    lines.add("    }");
    lines.add("  }");
    lines.add("}");

    return String.join(System.lineSeparator(), lines) + System.lineSeparator();
  }

  private int parseInt(String text, int fallback) {
    try {
      return Integer.parseInt(text.trim());
    } catch (Exception ignored) {
      return fallback;
    }
  }

  private double parseDouble(String text, double fallback) {
    try {
      return Double.parseDouble(text.trim());
    } catch (Exception ignored) {
      return fallback;
    }
  }

  private String round4(double value) {
    return String.format("%.4f", value);
  }

  private void appendOutput(String text) {
    SwingUtilities.invokeLater(
        () -> {
          outputArea.append(text + System.lineSeparator());
          outputArea.setCaretPosition(outputArea.getDocument().getLength());
        });
  }

  private void showError(String message) {
    JOptionPane.showMessageDialog(null, message, "Deploy Manager Error", JOptionPane.ERROR_MESSAGE);
  }
}
