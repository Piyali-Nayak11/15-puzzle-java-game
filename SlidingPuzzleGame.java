import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SlidingPuzzleGame extends JFrame implements ActionListener {

    private final int SIZE = 4; // 4x4 grid (15-puzzle)
    private JButton[][] buttons = new JButton[SIZE][SIZE];
    private int[][] board = new int[SIZE][SIZE];
    private int emptyRow, emptyCol;
    private int moves = 0;

    private JLabel statusLabel;
    private JButton resetButton;

    public SlidingPuzzleGame() {
        setTitle("15-Puzzle Sliding Game");
        setSize(420, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        setLayout(new BorderLayout(10, 10));

        // Top Panel: Move Counter & New Game Button
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));

        statusLabel = new JLabel("Moves: 0");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        resetButton = new JButton("New Game");
        resetButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(e -> initializeBoard());

        topPanel.add(statusLabel, BorderLayout.WEST);
        topPanel.add(resetButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Panel: 4x4 Puzzle Grid
        JPanel gridPanel = new JPanel(new GridLayout(SIZE, SIZE, 6, 6));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        gridPanel.setBackground(new Color(44, 62, 80));

        Font tileFont = new Font("SansSerif", Font.BOLD, 28);

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                buttons[r][c] = new JButton();
                buttons[r][c].setFont(tileFont);
                buttons[r][c].setFocusPainted(false);
                buttons[r][c].setActionCommand(r + "," + c);
                buttons[r][c].addActionListener(this);
                gridPanel.add(buttons[r][c]);
            }
        }

        add(gridPanel, BorderLayout.CENTER);
        initializeBoard();
    }

    // Generate a guaranteed solvable shuffled board
    private void initializeBoard() {
        moves = 0;
        statusLabel.setText("Moves: 0");

        List<Integer> tiles = new ArrayList<>();
        for (int i = 1; i < SIZE * SIZE; i++) {
            tiles.add(i);
        }
        tiles.add(0); // 0 represents the empty space

        do {
            Collections.shuffle(tiles);
        } while (!isSolvable(tiles) || isAlreadySolved(tiles));

        int index = 0;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                board[r][c] = tiles.get(index++);
                if (board[r][c] == 0) {
                    emptyRow = r;
                    emptyCol = c;
                }
            }
        }
        updateUI();
    }

    // Check mathematical solvability for a 4x4 sliding puzzle
    private boolean isSolvable(List<Integer> tiles) {
        int inversions = 0;
        int blankRowFromBottom = 0;

        for (int i = 0; i < tiles.size(); i++) {
            int val = tiles.get(i);
            if (val == 0) {
                blankRowFromBottom = SIZE - (i / SIZE);
                continue;
            }
            for (int j = i + 1; j < tiles.size(); j++) {
                int nextVal = tiles.get(j);
                if (nextVal != 0 && val > nextVal) {
                    inversions++;
                }
            }
        }

        // For even grid width (4): solvable if (blank on even row from bottom & odd inversions)
        // OR (blank on odd row from bottom & even inversions)
        if (blankRowFromBottom % 2 == 0) {
            return inversions % 2 != 0;
        } else {
            return inversions % 2 == 0;
        }
    }

    private boolean isAlreadySolved(List<Integer> tiles) {
        for (int i = 0; i < tiles.size() - 1; i++) {
            if (tiles.get(i) != i + 1) return false;
        }
        return true;
    }

    // Refresh button text and colors
    private void updateUI() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                int val = board[r][c];
                if (val == 0) {
                    buttons[r][c].setText("");
                    buttons[r][c].setBackground(new Color(52, 73, 94));
                    buttons[r][c].setEnabled(false);
                } else {
                    buttons[r][c].setText(String.valueOf(val));
                    buttons[r][c].setEnabled(true);
                    // Highlight tile green if it is in its correct final position
                    int expectedVal = r * SIZE + c + 1;
                    if (val == expectedVal) {
                        buttons[r][c].setBackground(new Color(46, 204, 113));
                        buttons[r][c].setForeground(Color.WHITE);
                    } else {
                        buttons[r][c].setBackground(new Color(236, 240, 241));
                        buttons[r][c].setForeground(new Color(44, 62, 80));
                    }
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String[] coords = e.getActionCommand().split(",");
        int r = Integer.parseInt(coords[0]);
        int c = Integer.parseInt(coords[1]);

        // Check if clicked tile is adjacent (up, down, left, right) to the empty space
        if ((Math.abs(r - emptyRow) == 1 && c == emptyCol) ||
            (Math.abs(c - emptyCol) == 1 && r == emptyRow)) {

            // Swap tile with empty slot
            board[emptyRow][emptyCol] = board[r][c];
            board[r][c] = 0;
            emptyRow = r;
            emptyCol = c;

            moves++;
            statusLabel.setText("Moves: " + moves);
            updateUI();

            if (checkWin()) {
                JOptionPane.showMessageDialog(
                    this,
                    "Congratulations! You solved the puzzle in " + moves + " moves!",
                    "Puzzle Solved!",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        }
    }

    private boolean checkWin() {
        int count = 1;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (r == SIZE - 1 && c == SIZE - 1) {
                    return board[r][c] == 0;
                }
                if (board[r][c] != count++) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SlidingPuzzleGame game = new SlidingPuzzleGame();
            game.setVisible(true);
        });
    }
}