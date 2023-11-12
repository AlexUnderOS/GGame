import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
public class SimpleGUI extends JFrame {

    private JRadioButton easyRadioButton = new JRadioButton("Easy");
    private JRadioButton normalRadioButton = new JRadioButton("Normal");
    private JRadioButton hardRadioButton = new JRadioButton("Hard");

    private ButtonGroup difficultyGroup = new ButtonGroup();
    private JButton startButton = new JButton("Start Game");

    private final Map<String, ImageIcon[]> carImagesMap = new HashMap<>();
    private final Map<String, String[]> carNamesMap = new HashMap<>();

    public SimpleGUI() {
        super("GAME - guess the car brand");
        initializeCarData();

        setSize(400, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        setLocationRelativeTo(null);

        JLabel label = new JLabel("Select the game difficulty level:");
        add(label);

        difficultyGroup.add(easyRadioButton);
        difficultyGroup.add(normalRadioButton);
        difficultyGroup.add(hardRadioButton);

        add(easyRadioButton);
        add(normalRadioButton);
        add(hardRadioButton);

        add(startButton);

        startButton.addActionListener(e -> openGameWindow());
    }

    private void initializeCarData() {
        initializeCarDataForDifficulty("easy");
        initializeCarDataForDifficulty("normal");
        initializeCarDataForDifficulty("hard");
        initializeCarNames();
    }

    private void initializeCarDataForDifficulty(String difficultyLevel) {
        String folderPath = "logos/" + difficultyLevel;
        URL folderUrl = getClass().getClassLoader().getResource(folderPath);

        if (folderUrl != null) {
            File folder = new File(folderUrl.getFile());

            File[] files = folder.listFiles();

            if (files != null) {
                List<ImageIcon> carImagesList = new ArrayList<>();
                List<String> carNamesList = new ArrayList<>();

                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".png")) {
                        ImageIcon imageIcon = new ImageIcon(file.getAbsolutePath());
                        carImagesList.add(imageIcon);
                        carNamesList.add(file.getName().replace(".png", "").toLowerCase());
                    }
                }

                ImageIcon[] carImages = carImagesList.toArray(new ImageIcon[0]);
                String[] carNames = carNamesList.toArray(new String[0]);

                carImagesMap.put(difficultyLevel, carImages);
                carNamesMap.put(difficultyLevel, carNames);

                System.out.println("Loaded images for difficulty level: " + difficultyLevel);
            }
        }
    }



    private void initializeCarNames() {
        carNamesMap.forEach((difficultyLevel, carNames) -> {
            System.out.println("Car names for difficulty level: " + difficultyLevel);
            for (String carName : carNames) {
                System.out.println(carName);
            }
        });
    }

    private void openGameWindow() {
        if (easyRadioButton.isSelected() || normalRadioButton.isSelected() || hardRadioButton.isSelected()) {
            String difficultyLevel = getSelectedDifficultyLevel();
            LevelGameWindow gameWindow = new LevelGameWindow(this, difficultyLevel);
            gameWindow.setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Please select a difficulty level.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getSelectedDifficultyLevel() {
        if (easyRadioButton.isSelected()) {
            return "easy";
        } else if (normalRadioButton.isSelected()) {
            return "normal";
        } else if (hardRadioButton.isSelected()) {
            return "hard";
        } else {
            return null;
        }
    }

    public void openDifficultySelectionWindow() {
        SwingUtilities.invokeLater(() -> {
            SimpleGUI simpleGUI = new SimpleGUI();
            simpleGUI.setVisible(true);
        });
    }

    public Map<String, ImageIcon[]> getCarImagesMap() {
        return carImagesMap;
    }

    public Map<String, String[]> getCarNamesMap() {
        return carNamesMap;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimpleGUI app = new SimpleGUI();
            app.setVisible(true);
        });
    }
}

class LevelGameWindow extends JFrame {

    private SimpleGUI parent;
    private String difficultyLevel;
    private int currentIndex = 0;
    private int correctAnswers = 0;

    private JLabel carLabel;
    private JTextField inputField;
    private JButton submitButton;

    private Map<String, ImageIcon[]> carImagesMap;
    private ImageIcon[] carImages;

    public LevelGameWindow(SimpleGUI parent, String difficultyLevel) {
        super(getWindowTitle(difficultyLevel));
        this.parent = parent;
        this.difficultyLevel = difficultyLevel;
        this.carImagesMap = parent.getCarImagesMap();
        this.carImages = carImagesMap.get(difficultyLevel);

        initializeUI();

        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new GridLayout(2, 1));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeGameWindow();
            }
        });
    }

    private void initializeUI() {
        if (carImages != null && carImages.length > 0) {
            carLabel = new JLabel();
            updateCarLabel();
            carLabel.setHorizontalAlignment(JLabel.CENTER);

            JScrollPane scrollPane = new JScrollPane(carLabel);
            add(scrollPane);

            JLabel answerLabel = new JLabel();
            answerLabel.setHorizontalAlignment(JLabel.CENTER);
            add(answerLabel);

            inputField = new JTextField();

            // Set the font size for the input field
            Font inputFont = inputField.getFont();
            inputField.setFont(new Font(inputFont.getName(), Font.BOLD, 40));

            add(inputField);

            submitButton = new JButton("Submit Answer");
            submitButton.addActionListener(e -> checkAnswer(answerLabel));
            add(submitButton);
        } else {
            JOptionPane.showMessageDialog(this, "No car images found for the selected difficulty level.", "Error", JOptionPane.ERROR_MESSAGE);
            closeGameWindow();
        }
    }


    private void updateCarLabel() {
        if (currentIndex < carImages.length) {
            ImageIcon currentImageIcon = carImages[currentIndex];
            Image scaledImage = currentImageIcon.getImage().getScaledInstance(350, 200, Image.SCALE_DEFAULT);
            carLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            carLabel.setIcon(null);
        }
    }

    private void checkAnswer(JLabel answerLabel) {
        String userAnswer = inputField.getText().trim().toLowerCase();
        String[] carNames = parent.getCarNamesMap().get(difficultyLevel);

        if (currentIndex < carNames.length) {
            String correctAnswer = carNames[currentIndex];

            if (userAnswer.equals(correctAnswer)) {
                correctAnswers++;
            }

            answerLabel.setText("Correct Answer: " + correctAnswer);
            currentIndex++;

            if (currentIndex < carNames.length) {
                updateCarLabel();
                inputField.setText("");
            } else {
                showResults();
            }
        }
    }


    private void showResults() {
        double accuracy = ((double) correctAnswers / parent.getCarNamesMap().get(difficultyLevel).length) * 100;
        DecimalFormat numberFormat = new DecimalFormat("#.00");
        JOptionPane.showMessageDialog(this, "Results:\nCorrect Answers: " + correctAnswers + "\nAccuracy: " + numberFormat.format(accuracy) + "%", "Game Over", JOptionPane.INFORMATION_MESSAGE);
        closeGameWindow();
    }

    private static String getWindowTitle(String difficultyLevel) {
        return difficultyLevel.substring(0, 1).toUpperCase() + difficultyLevel.substring(1) + " Game Window";
    }

    private void closeGameWindow() {
        parent.openDifficultySelectionWindow();
        dispose();
    }
}

