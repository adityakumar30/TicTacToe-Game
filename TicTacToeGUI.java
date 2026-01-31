
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import java.util.Random;

public class TicTacToeGUI extends JFrame implements ActionListener {
    private JButton[][] buttons = new JButton[3][3];
    private char currentPlayer = 'X';
    private JLabel statusLabel, scoreLabel;
    private int scoreX = 0, scoreO = 0;
    private Timer animationTimer;
    private int animFrame = 0;
    private JComboBox<String> themeSelector, modeSelector;
    private boolean singlePlayer = false;
    private boolean darkTheme = false;
    private Random rand = new Random();

    public TicTacToeGUI() {
        setTitle("Tic Tac Toe 🎮");
        setSize(420, 540);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel boardPanel = new JPanel(new GridLayout(3, 3));
        boardPanel.setBackground(Color.decode("#f0f0f0"));
        Font font = new Font("Arial", Font.BOLD, 50);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j] = new JButton("");
                buttons[i][j].setFont(font);
                buttons[i][j].setFocusPainted(false);
                buttons[i][j].addActionListener(this);
                boardPanel.add(buttons[i][j]);
            }
        }

        statusLabel = new JLabel("Player X's Turn 😎", JLabel.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        scoreLabel = new JLabel("Score - X: 0 | O: 0", JLabel.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        JButton playAgainBtn = new JButton("🔄 Play Again");
        playAgainBtn.setFont(new Font("Arial", Font.BOLD, 16));
        playAgainBtn.addActionListener(e -> resetBoard(false));

        themeSelector = new JComboBox<>(new String[]{"Light Theme", "Dark Theme"});
        themeSelector.addActionListener(e -> {
            darkTheme = themeSelector.getSelectedIndex() == 1;
            updateTheme();
        });

        modeSelector = new JComboBox<>(new String[]{"2 Player Mode", "Single Player (Easy)"});
        modeSelector.addActionListener(e -> {
            singlePlayer = modeSelector.getSelectedIndex() == 1;
            resetBoard(false);
        });

        JPanel bottomPanel = new JPanel(new GridLayout(5, 1));
        bottomPanel.setBackground(Color.decode("#ffffff"));
        bottomPanel.add(statusLabel);
        bottomPanel.add(scoreLabel);
        bottomPanel.add(playAgainBtn);
        bottomPanel.add(modeSelector);
        bottomPanel.add(themeSelector);

        add(boardPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        JButton btn = (JButton) e.getSource();
        if (!btn.getText().equals("")) return;

        btn.setText(String.valueOf(currentPlayer));
        btn.setForeground(currentPlayer == 'X' ? Color.BLUE : Color.RED);
        playMoveTone(currentPlayer);

        if (checkWin()) {
            statusLabel.setText("🎉 Player " + currentPlayer + " Wins!");
            playTone(1000, 400);
            if (currentPlayer == 'X') scoreX++;
            else scoreO++;
            updateScore();
            highlightWinningButtons();
            disableBoard();
            startWinAnimation();
        } else if (isBoardFull()) {
            statusLabel.setText("😐 It's a Draw!");
            playTone(400, 300);
        } else {
            switchPlayer();
            statusLabel.setText("Player " + currentPlayer + "'s Turn " + (currentPlayer == 'X' ? "😎" : "😇"));
            if (singlePlayer && currentPlayer == 'O') makeComputerMove();
        }
    }

    private void makeComputerMove() {
        Timer aiDelay = new Timer(500, e -> {
            int row, col;
            do {
                row = rand.nextInt(3);
                col = rand.nextInt(3);
            } while (!buttons[row][col].getText().equals(""));
            buttons[row][col].doClick();
        });
        aiDelay.setRepeats(false);
        aiDelay.start();
    }

    private void switchPlayer() {
        currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
    }

    private void playMoveTone(char player) {
        if (player == 'X') playTone(600, 150);
        else playTone(800, 150);
    }

    private void playTone(int hz, int durationMs) {
        float sampleRate = 44100;
        byte[] buf = new byte[(int) (sampleRate * durationMs / 1000)];
        for (int i = 0; i < buf.length; i++) {
            double angle = i / (sampleRate / hz) * 2.0 * Math.PI;
            buf[i] = (byte) (Math.sin(angle) * 127);
        }
        try {
            AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();
            line.write(buf, 0, buf.length);
            line.drain();
            line.stop();
            line.close();
        } catch (Exception e) {
            System.out.println("Error playing tone: " + e.getMessage());
        }
    }

    private boolean checkWin() {
        for (int i = 0; i < 3; i++) {
            if (equal(buttons[i][0], buttons[i][1], buttons[i][2])) return true;
            if (equal(buttons[0][i], buttons[1][i], buttons[2][i])) return true;
        }
        return equal(buttons[0][0], buttons[1][1], buttons[2][2]) ||
               equal(buttons[0][2], buttons[1][1], buttons[2][0]);
    }

    private boolean equal(JButton b1, JButton b2, JButton b3) {
        return !b1.getText().equals("") &&
               b1.getText().equals(b2.getText()) &&
               b2.getText().equals(b3.getText());
    }

    private boolean isBoardFull() {
        for (JButton[] row : buttons)
            for (JButton btn : row)
                if (btn.getText().equals("")) return false;
        return true;
    }

    private void disableBoard() {
        for (JButton[] row : buttons)
            for (JButton btn : row)
                btn.setEnabled(false);
    }

    private void highlightWinningButtons() {
        for (int i = 0; i < 3; i++) {
            if (equal(buttons[i][0], buttons[i][1], buttons[i][2])) {
                setGreen(buttons[i][0], buttons[i][1], buttons[i][2]);
                return;
            }
            if (equal(buttons[0][i], buttons[1][i], buttons[2][i])) {
                setGreen(buttons[0][i], buttons[1][i], buttons[2][i]);
                return;
            }
        }
        if (equal(buttons[0][0], buttons[1][1], buttons[2][2])) {
            setGreen(buttons[0][0], buttons[1][1], buttons[2][2]);
        } else if (equal(buttons[0][2], buttons[1][1], buttons[2][0])) {
            setGreen(buttons[0][2], buttons[1][1], buttons[2][0]);
        }
    }

    private void setGreen(JButton... btns) {
        for (JButton b : btns) b.setBackground(Color.GREEN);
    }

    private void startWinAnimation() {
        if (animationTimer != null && animationTimer.isRunning()) animationTimer.stop();
        animFrame = 0;
        animationTimer = new Timer(200, e -> {
            for (JButton[] row : buttons) {
                for (JButton btn : row) {
                    if (btn.getBackground() == Color.GREEN) {
                        btn.setFont(new Font("Arial", Font.BOLD, animFrame % 2 == 0 ? 60 : 45));
                    }
                }
            }
            animFrame++;
            if (animFrame > 6) animationTimer.stop();
        });
        animationTimer.start();
    }

    private void resetBoard(boolean fullReset) {
        for (JButton[] row : buttons)
            for (JButton btn : row) {
                btn.setText("");
                btn.setEnabled(true);
                btn.setBackground(null);
                btn.setFont(new Font("Arial", Font.BOLD, 50));
            }
        currentPlayer = 'X';
        statusLabel.setText("Player X's Turn 😎");
        if (fullReset) {
            scoreX = 0;
            scoreO = 0;
            updateScore();
        }
    }

    private void updateScore() {
        scoreLabel.setText("Score - X: " + scoreX + " | O: " + scoreO);
    }

    private void updateTheme() {
        Color bg = darkTheme ? Color.DARK_GRAY : Color.decode("#f0f0f0");
        for (JButton[] row : buttons)
            for (JButton btn : row)
                btn.setBackground(bg);
        getContentPane().setBackground(bg);
    }

    public static void main(String[] args) {
        new TicTacToeGUI();
    }
}
