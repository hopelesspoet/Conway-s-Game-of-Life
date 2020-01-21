import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.util.Timer;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
    private static final int BLOCK_SIZE = 20;
    private static final GameBoard gameboard = new GameBoard();

    private boolean isBeingPlayed = false;
    private int movesPerSecond = 200;
    private TimerTask task;
    private final Timer timer = new Timer("Stepper");

    private JMenuItem playItem;
    private JMenuItem stopItem;

    public static void main(String[] args) {
        final var game = new ConwaysGameOfLife();
        setupFrame(game);
    }

    private static void setupFrame(JFrame game) {
        final var screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        game.setTitle("Conway's Game of Life");
        game.setIconImage(new ImageIcon(ConwaysGameOfLife.class.getResource("/images/logo.png")).getImage());
        game.setSize(DEFAULT_WINDOW_SIZE);
        game.setMinimumSize(MINIMUM_WINDOW_SIZE);
        game.setLocation((screenSize.width - game.getWidth()) / 2, (screenSize.height - game.getHeight()) / 2);
        game.setVisible(true);
    }

    public ConwaysGameOfLife() {
        setupMenu();
        setupGameboard();
    }

    private void setupGameboard() {
        gameboard.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                gameboard.resizeToWindowSize();
            }
        });
        gameboard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                gameboard.addPoint(e.getPoint());
            }
        });
        gameboard.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                gameboard.addPoint(e.getPoint());
            }
        });
        add(gameboard);
    }

    private void setupMenu() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent event) {
                super.keyReleased(event);
                if (event.getKeyChar() == ' ') {
                    isBeingPlayed = !isBeingPlayed;
                    setGameBeingPlayed(isBeingPlayed);
                }
                if (e.getKeyCode() == KeyEvent.VK_KP_UP || e.getKeyCode() == KeyEvent.VK_UP) {
                    gameboard.i_movesPerSecond++;
                }
                if (e.getKeyCode() == KeyEvent.VK_KP_DOWN || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (gameboard.i_movesPerSecond >= 1) {
                        gameboard.i_movesPerSecond--;
                    }
                }
            }
        });
        final var menu = new JMenuBar();

        final var fileMenu = new JMenu("File");
        final var gameMenu = new JMenu("Game");
        final var helpMenu = new JMenu("Help");

        final var optionsItem = new JMenuItem("Options");
        optionsItem.addActionListener(event -> changeNumberOfMoves());

        final var exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(event -> System.exit(0));

        fileMenu.add(optionsItem);
        fileMenu.add(new JSeparator());
        fileMenu.add(exitItem);

        final var autofillItem = new JMenuItem("Autofill");
        autofillItem.addActionListener(ignored -> autoFillCells());

        playItem = new JMenuItem("Play");
        playItem.addActionListener(event -> setGameBeingPlayed(true));

        stopItem = new JMenuItem("Stop");
        stopItem.setEnabled(false);
        stopItem.addActionListener(ignored -> setGameBeingPlayed(false));

        final var resetItem = new JMenuItem("Reset");
        resetItem.addActionListener(event -> {
            gameboard.resetBoard();
            gameboard.repaint();
        });

        Stream.of(autofillItem, new JSeparator(), playItem, stopItem, resetItem).forEach(gameMenu::add);

        final var aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(event -> showAboutBox());

        final var sourceItem = new JMenuItem("Source");
        sourceItem.addActionListener(event -> navigateToSource());

        Stream.of(aboutItem, sourceItem).forEach(helpMenu::add);
        Stream.of(fileMenu, gameMenu, helpMenu).forEach(menu::add);

        setJMenuBar(menu);
    }

    public void setGameBeingPlayed(boolean isBeingPlayed) {
        if (isBeingPlayed) {
            playItem.setEnabled(false);
            stopItem.setEnabled(true);
            task = new TimerTask() {
                @Override
                public void run() {
                    gameboard.run();
                }
            };
            timer.schedule(task, 0, 1000 / movesPerSecond);
        } else {
            playItem.setEnabled(true);
            stopItem.setEnabled(false);
            task.cancel();
        }
    }

    public void navigateToSource() {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;

        try {
            desktop.browse(new URI("https://github.com/Burke9077/Conway-s-Game-of-Life"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Source is available on GitHub at:\nhttps://github.com/Burke9077/Conway-s-Game-of-Life",
                    "Source", JOptionPane.INFORMATION_MESSAGE);
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
        final var screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        final var autoFillFrame = new JFrame();
        autoFillFrame.setTitle("Autofill");
        autoFillFrame.setSize(360, 60);
        autoFillFrame.setLocation((screenSize.width - autoFillFrame.getWidth()) / 2,
                (screenSize.height - autoFillFrame.getHeight()) / 2);
        autoFillFrame.setResizable(false);

        final var autoFillPanel = new JPanel();
        autoFillPanel.setOpaque(false);

        final var percentageOptions = new JComboBox<>(new Integer[]{5, 10, 15, 20, 25, 30, 40, 50, 60, 70, 80, 90, 95});
        percentageOptions.addActionListener(e -> {
            if (percentageOptions.getSelectedIndex() > 0) {
                gameboard.resetBoard();
                gameboard.randomlyFillBoard((int) percentageOptions.getSelectedItem());
                autoFillFrame.dispose();
            }
        });

        autoFillFrame.add(autoFillPanel);
        autoFillPanel.add(new JLabel("What percentage should be filled? "));
        autoFillPanel.add(percentageOptions);

        autoFillFrame.setVisible(true);
    }

    private void changeNumberOfMoves() {
        final var screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        final var optionsScreen = new JFrame();
        optionsScreen.setTitle("Options");
        optionsScreen.setSize(300, 60);

        optionsScreen.setLocation((screenSize.width - optionsScreen.getWidth()) / 2,
                (screenSize.height - optionsScreen.getHeight()) / 2);
        optionsScreen.setResizable(true);

        final var optionsPanel = new JPanel();
        optionsPanel.setOpaque(false);

        final var generationsPerSecondOptions = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 10, 15, 20});
        generationsPerSecondOptions.setSelectedItem(movesPerSecond);
        generationsPerSecondOptions.addActionListener(ignored -> {
            movesPerSecond = generationsPerSecondOptions.getItemAt(generationsPerSecondOptions.getSelectedIndex());
            optionsScreen.dispose();
        });

        optionsPanel.add(new JLabel("Number of generations per second:"));
        optionsPanel.add(generationsPerSecondOptions);
        optionsScreen.add(optionsPanel);

        optionsScreen.setVisible(true);
    }

    private static class GameBoard extends JPanel implements Runnable {
        private static final Predicate<Integer> doesCellSurvive = surrounding -> (surrounding == 2) || (surrounding == 3);
        private static final Predicate<Integer> isCellBorn = surrounding -> surrounding == 2 || (surrounding == 3);
        private static final BiPredicate<Point, Dimension> isOutsideViewPort = (point, dimension) -> (point.x > dimension.width - 1) || point.y > dimension.height - 1;
        private static final Set<Point> points = new HashSet<>(0);
        private Dimension gameboardSize = new Dimension(getWidth() / BLOCK_SIZE - 2, getHeight() / BLOCK_SIZE - 2);
        private int i_movesPerSecond = 1;
        public void addPoint(int x, int y) {
            points.add(new Point(x, y));
            repaint();
        }

        public void addPoint(Point point) {
            int x = point.x / BLOCK_SIZE - 1;
            int y = point.y / BLOCK_SIZE - 1;
            if (isOutsideViewPort.negate().test(new Point(x, y), gameboardSize)) {
                addPoint(x, y);
            }
        }

        public void removePoint(Point point) {
            points.remove(point);
        }

        public void resetBoard() {
            points.clear();
        }

        public void randomlyFillBoard(int percent) {
            for (int i = 0; i < gameboardSize.width; i++) {
                for (int j = 0; j < gameboardSize.height; j++) {
                    if (Math.random() * 100 < percent) {
                        addPoint(i, j);
                    }
                }
            }
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.blue);

            try {
                points.forEach(newPoint -> drawScaledPoint(g, newPoint));
            } catch (ConcurrentModificationException ignored) {
            }

            setupGrid(g);
        }

        private void drawScaledPoint(Graphics g, Point newPoint) {
            g.fillRect(BLOCK_SIZE + (BLOCK_SIZE * newPoint.x),
                    BLOCK_SIZE + (BLOCK_SIZE * newPoint.y), BLOCK_SIZE, BLOCK_SIZE);
        }

        private void setupGrid(Graphics g) {
            g.setColor(Color.BLACK);

            for (int i = 0; i <= gameboardSize.width; i++) {
                drawScaledHorizontalLine(g, i);
            }

            for (int i = 0; i <= gameboardSize.height; i++) {
                drawVerticalLine(g, i);
            }
        }

        private void drawVerticalLine(Graphics g, int i) {
            g.drawLine(BLOCK_SIZE, ((i * BLOCK_SIZE) + BLOCK_SIZE),
                    BLOCK_SIZE * (gameboardSize.width + 1), ((i * BLOCK_SIZE) + BLOCK_SIZE));
        }

        private void drawScaledHorizontalLine(Graphics g, int i) {
            g.drawLine(((i * BLOCK_SIZE) + BLOCK_SIZE), BLOCK_SIZE,
                    (i * BLOCK_SIZE) + BLOCK_SIZE, BLOCK_SIZE + (BLOCK_SIZE * gameboardSize.height));
        }

        public void resizeToWindowSize() {
            gameboardSize = new Dimension(getWidth() / BLOCK_SIZE - 2,
                    getHeight() / BLOCK_SIZE - 2);

            points.removeAll(points.stream()
                    .filter(point -> isOutsideViewPort.test(point, gameboardSize))
                    .collect(Collectors.toSet()));

            repaint();
        }

        @Override
        public void run() {
            final var gameBoard = new boolean[gameboardSize.width + 2][gameboardSize.height + 2];
            final var survivingCells = new ArrayList<Point>(points.size());

            points.forEach(point -> gameBoard[point.x + 1][point.y + 1] = true);

            for (int i = 1; i < gameBoard.length - 1; i++) {
                for (int j = 1; j < gameBoard[0].length - 1; j++) {
                    final var surrounding = getSurrounding(gameBoard, i, j);
                    final var cellIsAlive = gameBoard[i][j];

                    if (cellIsAlive) {
                        if (doesCellSurvive.test(surrounding)) {
                            survivingCells.add(new Point(i - 1, j - 1));
                        }
                    } else {
                        if (isCellBorn.test(surrounding)) {
                            survivingCells.add(new Point(i - 1, j - 1));
                        }
                    }
                }
            }
            resetBoard();
            points.addAll(survivingCells);
            repaint();
        }

        private int getSurrounding(boolean[][] gameBoard, int i, int j) {
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

            return surrounding;
        }
    }
}
