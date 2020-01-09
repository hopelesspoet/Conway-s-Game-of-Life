import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.*;

/**
 * Conway's game of life is a cellular automaton devised by the
 * mathematician John Conway.
 */
public class ConwaysGameOfLife extends JFrame {
    private static final Dimension DEFAULT_WINDOW_SIZE = new Dimension(800, 600);
    private static final Dimension MINIMUM_WINDOW_SIZE = new Dimension(400, 400);
    private static final int BLOCK_SIZE = 10;
    private boolean isBeingPlayed = false;

    private JMenuItem playItem;
    private JMenuItem stopItem;
    private int i_movesPerSecond = 3;
    private GameBoard gameBoard;
    private Thread game;

    public static void main(String[] args) {
        // Setup the swing specifics
        JFrame game = new ConwaysGameOfLife();
        setupFrame(game);
    }

    private static void setupFrame(JFrame game) {
        game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        game.setTitle("Conway's Game of Life");
        game.setIconImage(new ImageIcon(ConwaysGameOfLife.class.getResource("/images/logo.png")).getImage());
        game.setSize(DEFAULT_WINDOW_SIZE);
        game.setMinimumSize(MINIMUM_WINDOW_SIZE);
        game.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - game.getWidth()) / 2,
                (Toolkit.getDefaultToolkit().getScreenSize().height - game.getHeight()) / 2);
        game.setVisible(true);
    }

    public ConwaysGameOfLife() {
        SetupMenu();
        SetupGameBoard();
    }

    private void SetupGameBoard() {
        gameBoard = new GameBoard();
        gameBoard.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                gameBoard.resizeToWindowSize();
            }
        });
        gameBoard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                gameBoard.addPoint(e);
            }
        });
        gameBoard.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                gameBoard.addPoint(e);
            }
        });
        add(gameBoard);
    }

    private void SetupMenu() {
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if (e.getKeyChar() == ' ') {
                    isBeingPlayed = !isBeingPlayed;
                    setGameBeingPlayed(isBeingPlayed);
                }
                if (e.getKeyCode() == KeyEvent.VK_KP_UP || e.getKeyCode() == KeyEvent.VK_UP) {
                    gameBoard.i_movesPerSecond++;
                }
                if (e.getKeyCode() == KeyEvent.VK_KP_DOWN || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (gameBoard.i_movesPerSecond >= 1) {
                        gameBoard.i_movesPerSecond--;
                    }
                }
            }
        });
        JMenuBar menu = new JMenuBar();
        setJMenuBar(menu);
        JMenu fileMenu = new JMenu("File");
        menu.add(fileMenu);
        JMenu gameMenu = new JMenu("Game");
        menu.add(gameMenu);
        JMenu helpMenu = new JMenu("Help");
        menu.add(helpMenu);

        JMenuItem optionsItem = new JMenuItem("Options");
        optionsItem.addActionListener(event -> changeNumberOfMoves());

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(event -> System.exit(0));

        fileMenu.add(optionsItem);
        fileMenu.add(new JSeparator());
        fileMenu.add(exitItem);

        JMenuItem autofillItem = new JMenuItem("Autofill");
        autofillItem.addActionListener(event -> autoFillCells());

        playItem = new JMenuItem("Play");
        playItem.addActionListener(event -> setGameBeingPlayed(true));

        stopItem = new JMenuItem("Stop");
        stopItem.setEnabled(false);
        stopItem.addActionListener(event -> setGameBeingPlayed(false));

        JMenuItem resetItem = new JMenuItem("Reset");
        resetItem.addActionListener(event -> {
            gameBoard.resetBoard();
            gameBoard.repaint();
        });

        gameMenu.add(autofillItem);
        gameMenu.add(new JSeparator());
        gameMenu.add(playItem);
        gameMenu.add(stopItem);
        gameMenu.add(resetItem);

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(event -> showAboutBox());
        JMenuItem sourceItem = new JMenuItem("Source");
        sourceItem.addActionListener(event -> navigateToSource());
        helpMenu.add(aboutItem);
        helpMenu.add(sourceItem);
    }

    public void setGameBeingPlayed(boolean isBeingPlayed) {
        if (isBeingPlayed) {
            playItem.setEnabled(false);
            stopItem.setEnabled(true);
            game = new Thread(gameBoard);
            game.start();
        } else {
            playItem.setEnabled(true);
            stopItem.setEnabled(false);
            game.interrupt();
        }
    }

    public void navigateToSource() {

        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        try {
            desktop.browse(new URI("https://github.com/Burke9077/Conway-s-Game-of-Life"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Source is available on GitHub at:\nhttps://github.com/Burke9077/Conway-s-Game-of-Life", "Source", JOptionPane.INFORMATION_MESSAGE);
        }

    }

    private void showAboutBox() {
        JOptionPane.showMessageDialog(null, """
                Conway's game of life was a cellular animation devised by the mathematician John Conway.
                This Java, swing based implementation was created by Matthew Burke.

                http://burke9077.com
                Burke9077@gmail.com
                @burke9077

                Creative Commons Attribution 4.0 International""");
    }

    private void autoFillCells() {
        final JFrame f_autoFill = new JFrame();
        f_autoFill.setTitle("Autofill");
        f_autoFill.setSize(360, 60);
        f_autoFill.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - f_autoFill.getWidth()) / 2,
                (Toolkit.getDefaultToolkit().getScreenSize().height - f_autoFill.getHeight()) / 2);
        f_autoFill.setResizable(false);
        JPanel p_autoFill = new JPanel();
        p_autoFill.setOpaque(false);
        f_autoFill.add(p_autoFill);
        p_autoFill.add(new JLabel("What percentage should be filled? "));
        Integer[] percentageOptions = {5, 10, 15, 20, 25, 30, 40, 50, 60, 70, 80, 90, 95};
        final var cb_percent = new JComboBox<>(percentageOptions);
        p_autoFill.add(cb_percent);
        cb_percent.addActionListener(e -> {
            if (cb_percent.getSelectedIndex() > 0) {
                gameBoard.resetBoard();
                gameBoard.randomlyFillBoard((int) cb_percent.getSelectedItem());
                f_autoFill.dispose();
            }
        });
        f_autoFill.setVisible(true);
    }

    private void changeNumberOfMoves() {
        final JFrame f_options = new JFrame();
        f_options.setTitle("Options");
        f_options.setSize(300, 60);
        f_options.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - f_options.getWidth()) / 2,
                (Toolkit.getDefaultToolkit().getScreenSize().height - f_options.getHeight()) / 2);
        f_options.setResizable(false);
        JPanel p_options = new JPanel();
        p_options.setOpaque(false);
        f_options.add(p_options);
        p_options.add(new JLabel("Number of moves per second:"));
        Integer[] secondOptions = {1, 2, 3, 4, 5, 10, 15, 20};
        final JComboBox cb_seconds = new JComboBox(secondOptions);
        p_options.add(cb_seconds);
        cb_seconds.setSelectedItem(i_movesPerSecond);
        cb_seconds.addActionListener(ignored -> {
            i_movesPerSecond = (Integer) cb_seconds.getSelectedItem();
            f_options.dispose();
        });
        f_options.setVisible(true);
    }

    private static class GameBoard extends JPanel implements Runnable {
        private Dimension gameBoardSize = new Dimension(getWidth() / BLOCK_SIZE - 2, getHeight() / BLOCK_SIZE - 2);
        private final Set<Point> points = new HashSet<>(0);
        private int i_movesPerSecond = 1;

        private void updateArraySize() {
            ArrayList<Point> removeList = new ArrayList<>(0);
            for (Point current : points) {
                if ((current.x > gameBoardSize.width - 1) || (current.y > gameBoardSize.height - 1)) {
                    removeList.add(current);
                }
            }
            points.removeAll(removeList);
            repaint();
        }

        public void addPoint(int x, int y) {
            points.add(new Point(x, y));
            repaint();
        }

        private Set<Integer[]> mousePoints = new HashSet();

        public void addPoint(MouseEvent me) {

            int x = me.getPoint().x / BLOCK_SIZE - 1;
            int y = me.getPoint().y / BLOCK_SIZE - 1;
            mousePoints.add(new Integer[]{x, y});
            if (me.getButton() != 0) {

                for (Integer[] point : mousePoints) {
                    x = point[0];
                    y = point[1];
                    if ((x >= 0) && (x < gameBoardSize.width) && (y >= 0) && (y < gameBoardSize.height)) {
                        if (me.getButton() == MouseEvent.BUTTON1) {
                            addPoint(x, y);
                        }
                        if (me.getButton() == MouseEvent.BUTTON3) {
                            removePoint(x, y);
                        }
                    }
                }
                mousePoints.clear();
            }


        }

        public void removePoint(int x, int y) {
            Set<Point> pointsToRemove = this.points.stream().filter(p -> p.x == x && p.y == y).collect(Collectors.toSet());
            points.removeAll(pointsToRemove);
            repaint();
        }

        public void resetBoard() {
            points.clear();
        }

        public void randomlyFillBoard(int percent) {
            for (int i = 0; i < gameBoardSize.width; i++) {
                for (int j = 0; j < gameBoardSize.height; j++) {
                    if (Math.random() * 100 < percent) {
                        addPoint(i, j);
                    }
                }
            }
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            try {
                for (Point newPoint : points) {
                    // Draw new point
                    g.setColor(Color.blue);
                    g.fillRect(BLOCK_SIZE + (BLOCK_SIZE * newPoint.x), BLOCK_SIZE + (BLOCK_SIZE * newPoint.y), BLOCK_SIZE, BLOCK_SIZE);
                }
            } catch (ConcurrentModificationException ignored) {
            }
            // Setup grid
            g.setColor(Color.BLACK);
            for (int i = 0; i <= gameBoardSize.width; i++) {
                g.drawLine(((i * BLOCK_SIZE) + BLOCK_SIZE), BLOCK_SIZE, (i * BLOCK_SIZE) + BLOCK_SIZE, BLOCK_SIZE + (BLOCK_SIZE * gameBoardSize.height));
            }
            for (int i = 0; i <= gameBoardSize.height; i++) {
                g.drawLine(BLOCK_SIZE, ((i * BLOCK_SIZE) + BLOCK_SIZE), BLOCK_SIZE * (gameBoardSize.width + 1), ((i * BLOCK_SIZE) + BLOCK_SIZE));
            }
        }

        public void resizeToWindowSize() {
            gameBoardSize = new Dimension(getWidth() / BLOCK_SIZE - 2, getHeight() / BLOCK_SIZE - 2);
            updateArraySize();
        }

        @Override
        public void run() {
            boolean[][] gameBoard = new boolean[gameBoardSize.width + 2][gameBoardSize.height + 2];
            for (Point current : points) {
                gameBoard[current.x + 1][current.y + 1] = true;
            }
            ArrayList<Point> survivingCells = new ArrayList<>(0);
            // Iterate through the array, follow game of life rules
            for (int i = 1; i < gameBoard.length - 1; i++) {
                for (int j = 1; j < gameBoard[0].length - 1; j++) {
                    int surrounding = 0;
                    if (gameBoard[i - 1][j - 1]) {
                        surrounding++;
                    }
                    if (gameBoard[i - 1][j]) {
                        surrounding++;
                    }
                    if (gameBoard[i - 1][j + 1]) {
                        surrounding++;
                    }
                    if (gameBoard[i][j - 1]) {
                        surrounding++;
                    }
                    if (gameBoard[i][j + 1]) {
                        surrounding++;
                    }
                    if (gameBoard[i + 1][j - 1]) {
                        surrounding++;
                    }
                    if (gameBoard[i + 1][j]) {
                        surrounding++;
                    }
                    if (gameBoard[i + 1][j + 1]) {
                        surrounding++;
                    }
                    if (gameBoard[i][j]) {
                        // Cell is alive, Can the cell live? (2-3)
                        if ((surrounding == 2) || (surrounding == 3)) {
                            survivingCells.add(new Point(i - 1, j - 1));
                        }
                    } else {
                        // Cell is dead, will the cell be given birth? (3)
                        if (surrounding == 3) {
                            survivingCells.add(new Point(i - 1, j - 1));
                        }
                    }
                }
            }
            resetBoard();
            points.addAll(survivingCells);
            repaint();
            try {
                Thread.sleep(1000 / i_movesPerSecond);
                run();
            } catch (InterruptedException ex) {
            }
        }
    }
}
