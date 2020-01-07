import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import javax.swing.*;

/**
 * Conway's game of life is a cellular automaton devised by the
 * mathematician John Conway.
 */
public class ConwaysGameOfLife extends JFrame implements ActionListener {
    private static final Dimension DEFAULT_WINDOW_SIZE = new Dimension(800, 600);
    private static final Dimension MINIMUM_WINDOW_SIZE = new Dimension(400, 400);
    private static final int BLOCK_SIZE = 10;

    private JMenuBar menu;
    private JMenu fileMenu, gameMenu, helpMenu;
    private JMenuItem optionsItem, exitItem;
    private JMenuItem autofillItem, playItem, stopItem, resetItem;
    private JMenuItem aboutItem, sourceItem;
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
        add(gameBoard);
    }

    private void SetupMenu() {
        menu = new JMenuBar();
        setJMenuBar(menu);
        fileMenu = new JMenu("File");
        menu.add(fileMenu);
        gameMenu = new JMenu("Game");
        menu.add(gameMenu);
        helpMenu = new JMenu("Help");
        menu.add(helpMenu);

        optionsItem = new JMenuItem("Options");
        optionsItem.addActionListener(this);

        exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(this);

        fileMenu.add(optionsItem);
        fileMenu.add(new JSeparator());
        fileMenu.add(exitItem);

        autofillItem = new JMenuItem("Autofill");
        autofillItem.addActionListener(this);

        playItem = new JMenuItem("Play");
        playItem.addActionListener(this);

        stopItem = new JMenuItem("Stop");
        stopItem.setEnabled(false);
        stopItem.addActionListener(this);

        resetItem = new JMenuItem("Reset");
        resetItem.addActionListener(this);

        gameMenu.add(autofillItem);
        gameMenu.add(new JSeparator());
        gameMenu.add(playItem);
        gameMenu.add(stopItem);
        gameMenu.add(resetItem);

        aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(this);
        sourceItem = new JMenuItem("Source");
        sourceItem.addActionListener(this);
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

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource().equals(exitItem)) {
            // Exit the game
            System.exit(0);
        } else if (ae.getSource().equals(optionsItem)) {
            // Put up an options panel to change the number of moves per second
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
            cb_seconds.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    i_movesPerSecond = (Integer) cb_seconds.getSelectedItem();
                    f_options.dispose();
                }
            });
            f_options.setVisible(true);
        } else if (ae.getSource().equals(autofillItem)) {
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
            Object[] percentageOptions = {"Select", 5, 10, 15, 20, 25, 30, 40, 50, 60, 70, 80, 90, 95};
            final JComboBox cb_percent = new JComboBox(percentageOptions);
            p_autoFill.add(cb_percent);
            cb_percent.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (cb_percent.getSelectedIndex() > 0) {
                        gameBoard.resetBoard();
                        gameBoard.randomlyFillBoard((Integer) cb_percent.getSelectedItem());
                        f_autoFill.dispose();
                    }
                }
            });
            f_autoFill.setVisible(true);
        } else if (ae.getSource().equals(resetItem)) {
            gameBoard.resetBoard();
            gameBoard.repaint();
        } else if (ae.getSource().equals(playItem)) {
            setGameBeingPlayed(true);
        } else if (ae.getSource().equals(stopItem)) {
            setGameBeingPlayed(false);
        } else if (ae.getSource().equals(sourceItem)) {
            Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
            try {
                desktop.browse(new URI("https://github.com/Burke9077/Conway-s-Game-of-Life"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Source is available on GitHub at:\nhttps://github.com/Burke9077/Conway-s-Game-of-Life", "Source", JOptionPane.INFORMATION_MESSAGE);
            }
        } else if (ae.getSource().equals(aboutItem)) {
            JOptionPane.showMessageDialog(null, "Conway's game of life was a cellular animation devised by the mathematician John Conway.\nThis Java, swing based implementation was created by Matthew Burke.\n\nhttp://burke9077.com\nBurke9077@gmail.com\n@burke9077\n\nCreative Commons Attribution 4.0 International");
        }
    }

    private class GameBoard extends JPanel implements ComponentListener, MouseListener, MouseMotionListener, Runnable {
        private Dimension d_gameBoardSize = null;
        private ArrayList<Point> point = new ArrayList<Point>(0);

        public GameBoard() {
            // Add resizing listener
            addComponentListener(this);
            addMouseListener(this);
            addMouseMotionListener(this);
        }

        private void updateArraySize() {
            ArrayList<Point> removeList = new ArrayList<Point>(0);
            for (Point current : point) {
                if ((current.x > d_gameBoardSize.width - 1) || (current.y > d_gameBoardSize.height - 1)) {
                    removeList.add(current);
                }
            }
            point.removeAll(removeList);
            repaint();
        }

        public void addPoint(int x, int y) {
            if (!point.contains(new Point(x, y))) {
                point.add(new Point(x, y));
            }
            repaint();
        }

        public void addPoint(MouseEvent me) {
            int x = me.getPoint().x / BLOCK_SIZE - 1;
            int y = me.getPoint().y / BLOCK_SIZE - 1;
            if ((x >= 0) && (x < d_gameBoardSize.width) && (y >= 0) && (y < d_gameBoardSize.height)) {
                addPoint(x, y);
            }
        }

        public void removePoint(int x, int y) {
            point.remove(new Point(x, y));
        }

        public void resetBoard() {
            point.clear();
        }

        public void randomlyFillBoard(int percent) {
            for (int i = 0; i < d_gameBoardSize.width; i++) {
                for (int j = 0; j < d_gameBoardSize.height; j++) {
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
                for (Point newPoint : point) {
                    // Draw new point
                    g.setColor(Color.blue);
                    g.fillRect(BLOCK_SIZE + (BLOCK_SIZE * newPoint.x), BLOCK_SIZE + (BLOCK_SIZE * newPoint.y), BLOCK_SIZE, BLOCK_SIZE);
                }
            } catch (ConcurrentModificationException cme) {
            }
            // Setup grid
            g.setColor(Color.BLACK);
            for (int i = 0; i <= d_gameBoardSize.width; i++) {
                g.drawLine(((i * BLOCK_SIZE) + BLOCK_SIZE), BLOCK_SIZE, (i * BLOCK_SIZE) + BLOCK_SIZE, BLOCK_SIZE + (BLOCK_SIZE * d_gameBoardSize.height));
            }
            for (int i = 0; i <= d_gameBoardSize.height; i++) {
                g.drawLine(BLOCK_SIZE, ((i * BLOCK_SIZE) + BLOCK_SIZE), BLOCK_SIZE * (d_gameBoardSize.width + 1), ((i * BLOCK_SIZE) + BLOCK_SIZE));
            }
        }

        @Override
        public void componentResized(ComponentEvent e) {
            // Setup the game board size with proper boundries
            d_gameBoardSize = new Dimension(getWidth() / BLOCK_SIZE - 2, getHeight() / BLOCK_SIZE - 2);
            updateArraySize();
        }

        @Override
        public void componentMoved(ComponentEvent e) {
        }

        @Override
        public void componentShown(ComponentEvent e) {
        }

        @Override
        public void componentHidden(ComponentEvent e) {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            // Mouse was released (user clicked)
            addPoint(e);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            // Mouse is being dragged, user wants multiple selections
            addPoint(e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }

        @Override
        public void run() {
            boolean[][] gameBoard = new boolean[d_gameBoardSize.width + 2][d_gameBoardSize.height + 2];
            for (Point current : point) {
                gameBoard[current.x + 1][current.y + 1] = true;
            }
            ArrayList<Point> survivingCells = new ArrayList<Point>(0);
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
            point.addAll(survivingCells);
            repaint();
            try {
                Thread.sleep(1000 / i_movesPerSecond);
                run();
            } catch (InterruptedException ex) {
            }
        }
    }
}
