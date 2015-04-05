package quickmotion.desktop;

import quickmotion.shared.*;

import javax.swing.*;
import javax.swing.Box;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * QuickMotion
 * By Alex Klen, March 2013
 *
 */


/**
 * TODO:
 * Save animation length in file
 * Fix bad state when you press back and go back in
 *
 * If there's time:
 * Play/Pause/Stop/Replay/Step icons
 * Mouse cursor changes when holding control
 * Default saving/loading to start file dialog at working directory, and then directory of last file opened/saved
 *
 * Accelerator keys for timeline actions (as well as menu for 'timeline')
 * Make timeline increase before reaching end OR have label with maximum time listed, to show that it's still recording
 * Have recording indicator
 * Lasso add/subtract selection
 * Layers
 *
 * Enhancements:
 *
 * Bugs:
 * Bug in some cases of transform superposition.
 *
 *
 */


public class QuickMotion extends JComponent implements AbstractView {

    private static QuickMotion singleton;
    final private List<DrawnLine> lines;
    final private List<Animation> animations;
    private Tool tool;
    private DocumentManager documentManager;
    private TimeLine timeline;
    private boolean exitTime = false;
    private InputController inputController;
    private HashMap<Tool, Cursor> cursors = new HashMap<Tool, Cursor>();
    private HashMap<Tool, Image> toolImages = new HashMap<Tool, Image>();

    // Components
    private JFrame frame;
    private JMenuBar menuBar;
    private JPanel timelinePanel;
    private JPanel mainPanel;
    private JSlider timelineSlider;
    private JButton playPauseButton;
    private JColorChooser colourChooser;

    private static final int TIMELINE_SLIDER_TICKS = 300;

    public static final int DESIRED_FPS = 40;   // Target number of updates per second

    public static final Color BACKGROUND_COLOUR = Color.white;

    public static final int SCREEN_WIDTH = 1024;
    public static final int SCREEN_HEIGHT = 735;
    private boolean preventSliderChange = false;
    private boolean exportingGif = false;

    public QuickMotion() {
        MatAffineTransform.injectFactory(); // Important

        documentManager = new DocumentManager();
        lines = new LinkedList<DrawnLine>();
        animations = new ArrayList<Animation>();
        Tool.setView(this);
        tool = Tool.MARKER;
        timeline = new TimeLine();
        inputController = new InputController(this);
        setupToolImages();
        this.setCursor(cursors.get(tool));
    }

    public void run() {
        setupScreen();

        Thread mainthread = new Thread() {
            @Override
            public void run() {
                long lastTime = System.currentTimeMillis();
                long curTime;
                Delta delta;

                do {
                    curTime = System.currentTimeMillis();
                    delta = new Delta(curTime - lastTime);

                    tick(delta); // Execute one game step
                    repaint();

                    try {
                        while (exportingGif) {
                            Thread.sleep(1000);
                        }
                        long timeTaken = System.currentTimeMillis() - lastTime;
                        long delayTime = 1000 / DESIRED_FPS - timeTaken;

                        if (delayTime > 0) {
                            Thread.sleep(delayTime);
                        }
                    } catch (InterruptedException ignored) {
                    }

                    lastTime = curTime;

                } while (!exitTime);

                quitProgram();
            }
        };

        mainthread.start();
    }

    private void setupScreen() {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        frame = new JFrame("Quick Motion", gc);

        // Double buffering
        this.setDoubleBuffered(true);

        frame.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        frame.setFocusCycleRoot(false);
        frame.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                super.windowGainedFocus(e);
                singleton.grabFocus();
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                super.windowLostFocus(e);
                inputController.clearInput();
            }

        });
        inputController.setupInputComponent(this);

        this.setSize(SCREEN_WIDTH - 100, SCREEN_HEIGHT - 100);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(this, BorderLayout.CENTER);

        frame.setContentPane(mainPanel);

        // Action listener for all buttons and menu items!!
        ActionListener toolActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (actionEvent.getActionCommand().equals("marker")) {
                    tool = Tool.MARKER;
                } else if (actionEvent.getActionCommand().equals("eraser")) {
                    tool = Tool.ERASER;
                } else if (actionEvent.getActionCommand().equals("lasso")) {
                    tool = Tool.LASSO;
                } else if (actionEvent.getActionCommand().equals("rotate")) {
                    tool = Tool.ROTATE;
                } else if (actionEvent.getActionCommand().equals("scale")) {
                    tool = Tool.SCALE;
                } else if (actionEvent.getActionCommand().equals("addtime")) {
                    String duration = JOptionPane.showInputDialog(frame, "How many seconds would you like to insert?", "1.0");
                    if (duration == null || duration.length() == 0) return;
                    try {
                        float d = Float.valueOf(duration);
                        timeline.insertTime(d);
                        for (Animation a : animations) {
                            a.insertTime(timeline.getTime(), d);
                        }
                    } catch (NumberFormatException ignored) {}
                    return;
                }
                Tool.notifyToolChanged();
                QuickMotion.this.setCursor(cursors.get(tool));
                timeline.setPlaying(false);
                QuickMotion.this.requestFocus();
            }
        };

        // Menu Panel setup
        JPanel toolBoxPanel = new JPanel();
        toolBoxPanel.setLayout(new GridLayout(2, 3));
        toolBoxPanel.setBackground(BACKGROUND_COLOUR);
        // Row 1
        JButton toolButton = new JButton("", new ImageIcon(toolImages.get(Tool.MARKER)));
        toolButton.setActionCommand("marker");
        toolButton.addActionListener(toolActionListener);
        toolBoxPanel.add(toolButton);
        toolButton = new JButton("", new ImageIcon(toolImages.get(Tool.LASSO)));
        toolButton.setActionCommand("lasso");
        toolButton.addActionListener(toolActionListener);
        toolBoxPanel.add(toolButton);
        toolButton = new JButton("", new ImageIcon(toolImages.get(Tool.ERASER)));
        toolButton.setActionCommand("eraser");
        toolButton.addActionListener(toolActionListener);
        toolBoxPanel.add(toolButton);
        toolButton = new JButton("", new ImageIcon(loadImage("addtime.png", 50, 32)));
        toolButton.setActionCommand("addtime");
        toolButton.addActionListener(toolActionListener);
        toolBoxPanel.add(toolButton);
        // Row 2
        toolButton = new JButton("", new ImageIcon(toolImages.get(Tool.ROTATE)));
        toolButton.setActionCommand("rotate");
        toolButton.addActionListener(toolActionListener);
        toolBoxPanel.add(toolButton);
        toolButton = new JButton("", new ImageIcon(toolImages.get(Tool.SCALE)));
        toolButton.setActionCommand("scale");
        toolButton.addActionListener(toolActionListener);
        toolBoxPanel.add(toolButton);

        // Setup colour chooser
        colourChooser = new JColorChooser();
        colourChooser.setChooserPanels(new AbstractColorChooserPanel[]{colourChooser.getChooserPanels()[0]});
        colourChooser.setPreviewPanel(new JPanel());
        colourChooser.getSelectionModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                Tool.setColour(colourChooser.getColor().getRGB());
            }
        });
        colourChooser.setColor(Color.blue);
        colourChooser.setVisible(true);

        JPanel northPanel = new JPanel();
        northPanel.add(toolBoxPanel);

        northPanel.add(Box.createHorizontalStrut(50));
        northPanel.add(colourChooser);
        northPanel.add(Box.createHorizontalGlue());

        mainPanel.add(northPanel, BorderLayout.NORTH);

        ActionListener timelineActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (actionEvent.getActionCommand().equals("play")) {
                    timeline.setPlaying(!timeline.isPlaying());
                } else if (actionEvent.getActionCommand().equals("stop")) {
                    timeline.setPlaying(false);
                    timeline.setTime(0f);
                    notifyTimelineUpdated();
                } else if (actionEvent.getActionCommand().equals("left")) {
                    timeline.decrement();
                    notifyTimelineUpdated();
                } else if (actionEvent.getActionCommand().equals("right")) {
                    timeline.increment();
                    notifyTimelineUpdated();
                }

                playPauseButton.setText(timeline.isPlaying() ? "Pause" : "Play");
                Tool.resetLasso();
                QuickMotion.this.requestFocus();
            }
        };

        timelinePanel = new JPanel(new FlowLayout());
        playPauseButton = new JButton("Play");
        playPauseButton.setActionCommand("play");
        playPauseButton.addActionListener(timelineActionListener);
        timelinePanel.add(playPauseButton);
        JButton timelineButton = new JButton("Stop");
        timelineButton.setActionCommand("stop");
        timelineButton.addActionListener(timelineActionListener);
        timelinePanel.add(timelineButton);
        timelineButton = new JButton("<");
        timelineButton.setActionCommand("left");
        timelineButton.addActionListener(timelineActionListener);
        timelinePanel.add(timelineButton);
        timelineButton = new JButton(">");
        timelineButton.setActionCommand("right");
        timelineButton.addActionListener(timelineActionListener);
        timelinePanel.add(timelineButton);

        timelineSlider = new JSlider();
        timelineSlider.setMaximum(TIMELINE_SLIDER_TICKS);
        timelineSlider.setValue(0);
        timelineSlider.setPreferredSize(new Dimension(710, 50));
        timelineSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                if (preventSliderChange) return;
                timeline.setPlaying(false);
                playPauseButton.setText("Play");
                Tool.resetLasso();
                JSlider slider = (JSlider) changeEvent.getSource();
                int newTime = slider.getValue();
                timeline.setTime(sliderToTimeline(newTime)); // TODO - check synchronization?
                QuickMotion.this.requestFocus();
            }
        });
        timelinePanel.add(timelineSlider);
        mainPanel.add(timelinePanel, BorderLayout.SOUTH);

        // Setup menu bar
        menuBar = new JMenuBar();
        JMenu menuMenu = new JMenu("File");
        JMenuItem menuItem = new JMenuItem("New");
        menuItem.setActionCommand("new");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                synchronized (lines) {
                    lines.clear();
                }
                synchronized (animations) {
                    animations.clear();
                }
                Tool.resetLasso();
                timeline.reset();
                notifyTimelineUpdated();
            }
        });
        menuMenu.add(menuItem);

        menuItem = new JMenuItem("Save...");
        menuItem.setActionCommand("save");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                saveToFile();
            }
        });
        menuMenu.add(menuItem);

        menuItem = new JMenuItem("Load...");
        menuItem.setActionCommand("load");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                loadFromFile();
            }
        });

        menuMenu.add(menuItem);
        menuItem = new JMenuItem("Export as animated gif...");
        menuItem.setActionCommand("gif");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                exportGif();
            }
        });
        menuMenu.add(menuItem);
        menuItem = new JMenuItem("Quit");
        menuItem.setActionCommand("quit");
        menuItem.setAccelerator(KeyStroke.getKeyStroke('q'));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                quitProgram();
            }
        });
        menuMenu.add(menuItem);
        menuBar.add(menuMenu);

        menuMenu = new JMenu("Tools");
        menuItem = new JMenuItem("Marker");
        menuItem.setActionCommand("marker");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0));
        menuItem.addActionListener(toolActionListener);
        menuMenu.add(menuItem);
        menuItem = new JMenuItem("Eraser");
        menuItem.setActionCommand("eraser");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0));
        menuItem.addActionListener(toolActionListener);
        menuMenu.add(menuItem);
        menuItem = new JMenuItem("Lasso");
        menuItem.setActionCommand("lasso");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, 0));
        menuItem.addActionListener(toolActionListener);
        menuMenu.add(menuItem);
        menuItem = new JMenuItem("Rotate");
        menuItem.setActionCommand("rotate");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0));
        menuItem.addActionListener(toolActionListener);
        menuMenu.add(menuItem);
        menuItem = new JMenuItem("Scale");
        menuItem.setActionCommand("scale");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0));
        menuItem.addActionListener(toolActionListener);
        menuMenu.add(menuItem);
        menuItem = new JMenuItem("Add Time");
        menuItem.setActionCommand("addtime");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, 0));
        menuItem.addActionListener(toolActionListener);
        menuMenu.add(menuItem);
        menuBar.add(menuMenu);
        menuBar.setVisible(true);
        frame.setJMenuBar(menuBar);

        centerFrame(frame);
        frame.validate();
        frame.setVisible(true); // Actually show window
    }

    private void saveToFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnCode = fileChooser.showSaveDialog(frame);
        if (returnCode != JFileChooser.APPROVE_OPTION) return;

        final File f = fileChooser.getSelectedFile();
        if (f == null) return;

        documentManager.save(f, SyncTools.synchronizedCopy(lines));
    }

    private void loadFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnCode = fileChooser.showOpenDialog(frame);
        if (returnCode != JFileChooser.APPROVE_OPTION) return;

        final File f = fileChooser.getSelectedFile();
        if (f == null) return;

        lines.clear();
        animations.clear();
        boolean successful = documentManager.load(f, lines, animations);
        // TODO - popup if not successful
    }

    private void exportGif() {
        JFileChooser fileChooser = new JFileChooser();
        int returnCode = fileChooser.showSaveDialog(frame);
        if (returnCode != JFileChooser.APPROVE_OPTION) return;

        final File f = fileChooser.getSelectedFile();
        if (f == null) return;

        exportingGif = true;

        final JDialog popup = new JDialog(frame);
        JLabel label = new JLabel("Exporting Animated Gif... Please Wait.");

        label.setVisible(true);
        popup.add(label);
        popup.setLayout(new FlowLayout());
        popup.setModal(true);
        popup.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        popup.setResizable(false);
        popup.setBounds(frame.getX() + frame.getWidth() / 3, frame.getY() + frame.getHeight() / 3, frame.getWidth() / 3, 100);
        popup.validate();

        Thread exportThread = new Thread() {
            @Override
            public void run() {
                float saveTime = timeline.getTime();

                float maxTime = timeline.getMaxObservedTime();
                final float SCALE = 0.5f;
                final float STEP = 0.05f;

                AnimatedGifEncoder gifEncoder = new AnimatedGifEncoder();
                try {
                    gifEncoder.start(new FileOutputStream(f));
                } catch (FileNotFoundException e) {
                    popup.setVisible(false);
                    exportingGif = false;
                    return;
                }
                gifEncoder.setDelay(Math.round(STEP*1000));
                gifEncoder.setRepeat(0);

                final int W = Math.round(QuickMotion.this.getWidth()*SCALE);
                final int H = Math.round(QuickMotion.this.getHeight()*SCALE);
                BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
                Graphics g = img.getGraphics();

                ((Graphics2D)g).scale(SCALE, SCALE);

                for (float t = 0f; t <= maxTime; t += STEP) {
                    g.clearRect(0, 0, W, H);
                    timeline.setTime(t);
                    myPaint(g);
                    gifEncoder.addFrame(img);
                }

                gifEncoder.finish();

                popup.setVisible(false);

                timeline.setTime(saveTime);

                popup.setVisible(false);
                exportingGif = false;
            }
        };
        exportThread.start();

        popup.setVisible(true);
    }

    private static float getTimelineScale() {
        return TIMELINE_SLIDER_TICKS/singleton.timeline.getMaxObservedTime();
    }

    private static int timeLineToSlider(float time) {
        return Math.round(time * getTimelineScale());
    }

    private static float sliderToTimeline(int time) {
        return time/getTimelineScale();
    }

    @Override
    public void notifyTimelineUpdated() {
        singleton.preventSliderChange = true;
        singleton.timelineSlider.setValue(timeLineToSlider(singleton.timeline.getTime()));
        singleton.preventSliderChange = false;
    }

    private void tick(Delta delta) { // Main Loop
        inputController.update(delta);
        timeline.update(delta);
        if (timeline.isPlaying()) {
            notifyTimelineUpdated();
        } else {
            playPauseButton.setText("Play");
            tool.update(delta, timeline.getTime());
        }
    }

    private void myPaint(Graphics g) {
        float time = timeline.getTime();

        g.setColor(BACKGROUND_COLOUR);
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        for (DrawnLine l : SyncTools.synchronizedCopy(lines)) {
            DrawnLineRenderer.paint(l, (Graphics2D) g, time);
        }
        ToolRenderer.paint(tool, (Graphics2D) g);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        myPaint(g);
    }

    private void quitProgram() {
        frame.dispose(); //This will also quit the program
        System.exit(0);
    }

    @Override
    public Vector2d getCanvasOffset() {
        return new Vector2d(singleton.getLocation().x, singleton.getLocation().y);
    }

    @Override
    public boolean addLine(DrawnLine line) {
        synchronized (lines) {
            return lines.add(line);
        }
    }

    @Override
    public boolean shouldAnimate() {
        return true; // Not used on desktop
    }

    @Override
    public int getBackgroundColour() {
        return Color.white.getRGB();
    }

    @Override
    public List<DrawnLine> getLines() {
        return (List<DrawnLine>)SyncTools.synchronizedCopy(singleton.lines);
    }

    @Override
    public Tool getTool() {
        return tool;
    }

    @Override
    public boolean addAnimation(Animation anim) {
        synchronized (animations) {
            return animations.add(anim);
        }
    }

    @Override
    public TimeLine getTimeLine() {
        return timeline;
    }

    @Override
    public AbstractController getController() {
        return inputController;
    }

    public void setupToolImages() {
        Image image;
        Cursor cursor = null;
        String img;
        Vector2d p;
        for (Tool t : Tool.values()) {
            img = t.getImage();
            image = null;
            p = tool.getCursorCenter();
            if (img != null && p != null) {
                Toolkit tk = Toolkit.getDefaultToolkit();

                Dimension best = tk.getBestCursorSize(p.x, p.y);
                image = QuickMotion.loadImage(img, 32, 32);
                cursor = tk.createCustomCursor(image, new Point(best.width - 1, best.height - 1), t.name());
            }
            if (cursor == null) {
                cursor = Cursor.getDefaultCursor();
            }
            cursors.put(t, cursor);
            toolImages.put(t, image);
        }
    }

    public static void centerFrame(javax.swing.JFrame frame) {
        //Put the JFrame in the center of the screen
        //Get the size of the screen
        java.awt.Dimension dim = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        //Use width of JFrame to find center position
        int w = frame.getSize().width;
        int h = frame.getSize().height;
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;
        //Move the JFrame
        frame.setLocation(x, y);
    }

    public static Image loadImage(String img, int w, int h) {
        Toolkit tk = Toolkit.getDefaultToolkit();
        return tk.getImage("resources/" + img).getScaledInstance(w, h, 0);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        singleton = new QuickMotion();
        singleton.run();
    }

}
