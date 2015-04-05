package cranesim.game;


/**
 * TODO:
 *
 *
 * Maybe:
 * Stuck block colliding with crane body and arms
 * Center of mass check for placing blocks
 *
 *
 * Bugs:
 * Arms sometimes flash at strange angles on startup
 * Boxes sometimes flash at top left
 *
 */

import cranesim.tools.Box;
import cranesim.tools.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * CraneSim
 * By Alex Klen, February 2013
 *
 */
public class CraneSim extends JComponent {
    // Debug flags
    public static final boolean DEBUG_CRANEARM_MOUSE_TEST = false;
    public static final boolean DEBUG_INPUT = false;
    public static final boolean DEBUG_COLLISION = false;
    public static final boolean DEBUG_CANDYBOX_CORNERS = false;
    public static final boolean DEBUG_PERFECT = false;

    public static Box debugCollideMagnet, debugCollideMagnetField;
    public static List<Vector2f[]> debugCollideCandyBoxes = new ArrayList<Vector2f[]>();

    private static enum GameState {MENU, PLAYING, INSTRUCTIONS, GAMEOVER}

    private static CraneSim singleton;
    public static final String[] requiredFiles = {};
    private List<Entity> entities;
    private CraneMagnet theMagnet;
    private SceneNode sceneGraph;
    private int moves = 0;
    private int breaks = 0;
    private int movedBoxes = 0;
    public static GameState gameState;
    private boolean exittime = false;

    // Components
    private JFrame frame;
    private JPanel menuPanel;
    private JPanel instructionPanel;
    private JPanel gameOverPanel;
    private JLabel scoreLabel;
    private JMenuBar menuBar;

    public static final int DESIRED_FPS = 60;   // Target number of updates per second
    public static float actualFPS;

    public static final Color BACKGROUND_COLOUR = new Color(32, 136, 176); //new Color (0.1f, 0.9f, 0.35f, 0.2f);
    public static final Color TITLE_COLOUR = new Color(198, 24, 183); //new Color (0.1f, 0.9f, 0.35f, 0.2f);
    public static final Color BAD_MANIP_COLOUR = new Color(175, 50, 75);

    public static final int SCREEN_WIDTH = 1024;
    public static final int SCREEN_HEIGHT = 735;
    public static Font titleFont = new Font("Ariel", Font.BOLD, 30);
    public static Font subtitleFont = new Font("Ariel", Font.BOLD, 24);
    public static Font instructionFont = new Font("Ariel", Font.PLAIN, 18);
    public static Font hudFont = new Font("Trebuchet MS", Font.PLAIN, 18);

    public CraneSim() {
        // Setup
        checkRequiredFiles();
    }

    public void run() {
        setupScreen();
        changeGameState(GameState.MENU);

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

//                    if (InputController.Key.QUIT.isPressedThisFrame()) {
//                        changeGameState(GameState.MENU);
//                    }

                    try {
                        long timeTaken = System.currentTimeMillis() - lastTime;
                        long delayTime = 1000 / DESIRED_FPS - timeTaken;

                        if (delayTime > 0) {
//                            actualFPS = DESIRED_FPS;
                            Thread.sleep(delayTime);
//                        } else {
//                            actualFPS = 1000.0f / timeTaken;
                        }
                    } catch (InterruptedException ex) {
                    }

                    lastTime = curTime;

                } while (!exittime);


                quitgame();
            }
        };

        mainthread.start();
    }

    private void checkRequiredFiles() {
        for (int i = 0; i < requiredFiles.length; i++) {
            if (!new File(requiredFiles[i]).exists()) {
                javax.swing.JOptionPane.showMessageDialog(null, "Missing file: " + requiredFiles[i]);
                quitgame();
            }
        }
    }

    private void setupScreen() {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        frame = new JFrame("Crane Simulation", gc);
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
                // So player doesn't keep walking
                InputController.clearInput();
            }

        });
        InputController.setupInputComponent(this);

        this.setSize(SCREEN_WIDTH - 100, SCREEN_HEIGHT - 100);
        frame.setContentPane(this);

        // Action listener for all buttons and menu items!!
        ActionListener unifiedActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (actionEvent.getActionCommand().equals("play")) {
                    changeGameState(GameState.PLAYING);
                } else if (actionEvent.getActionCommand().equals("instructions")) {
                    changeGameState(GameState.INSTRUCTIONS);
                } else if (actionEvent.getActionCommand().equals("quit")) {
                    exittime = true;
                } else if (actionEvent.getActionCommand().equals("menu")) {
                    changeGameState(GameState.MENU);
                } else if (actionEvent.getActionCommand().equals("reset")) {
                    // Should already be in playing state
                    setupGame();
                } else if (actionEvent.getActionCommand().equals("grade")) {
                    calculateScore();
                    changeGameState(GameState.GAMEOVER);
                }
            }
        };

        // Menu Panel setup
        menuPanel = new JPanel();
        menuPanel.setBackground(BACKGROUND_COLOUR);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.X_AXIS));
        JPanel midPanel = new JPanel();
        midPanel.setBackground(BACKGROUND_COLOUR);
        midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.Y_AXIS));
        midPanel.add(javax.swing.Box.createVerticalGlue());

        JLabel label = new JLabel("Crane Simulator");
        label.setFont(titleFont);
        label.setForeground(TITLE_COLOUR);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        midPanel.add(label);
        label = new JLabel("by Alex Klen");
        label.setFont(subtitleFont);
        label.setForeground(TITLE_COLOUR);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        midPanel.add(label);

        midPanel.add(javax.swing.Box.createVerticalStrut(100));
        JButton button = new JButton("Play");
        button.setActionCommand("play");
        button.addActionListener(unifiedActionListener);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        midPanel.add(button);
        midPanel.add(javax.swing.Box.createVerticalStrut(10));
        button = new JButton("Instructions");
        button.setActionCommand("instructions");
        button.addActionListener(unifiedActionListener);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        midPanel.add(button);
        midPanel.add(javax.swing.Box.createVerticalStrut(10));
        button = new JButton("Quit");
        button.setActionCommand("quit");
        button.addActionListener(unifiedActionListener);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        midPanel.add(button);
        midPanel.add(javax.swing.Box.createVerticalGlue());
        menuPanel.add(javax.swing.Box.createHorizontalGlue());
        menuPanel.add(midPanel);
        menuPanel.add(javax.swing.Box.createHorizontalGlue());


        // Instruction Panel setup
        instructionPanel = new JPanel();
        instructionPanel.setBackground(BACKGROUND_COLOUR);
        instructionPanel.setLayout(new BoxLayout(instructionPanel, BoxLayout.X_AXIS));
        midPanel = new JPanel();
        midPanel.setBackground(BACKGROUND_COLOUR);
        midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.Y_AXIS));
        midPanel.add(javax.swing.Box.createVerticalGlue());

        label = new JLabel("Instructions");
        label.setFont(titleFont);
        label.setForeground(TITLE_COLOUR);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        midPanel.add(label);
        midPanel.add(javax.swing.Box.createVerticalStrut(20));
        label = new JLabel("<html>- Use the mouse to click and drag the crane and its arms.<br/>- Position the crane magnet parallel and close to a candy block<br/>and click on the magnet to pick it up.<br/>- Move the block to a desired location and align it parallel and close to the surface,<br/>then click on the magnet to drop it.<br/>- If the block falls too far or the angle is not close to parallel,<br/>it will break and be reset in the simulator.<br/>- Try to create the structure outlined with the blocks provided.<br/>- Select the 'Grade' option under the 'Game' menu to have your work evaluated.</html> ");
        label.setFont(instructionFont);
        label.setForeground(Color.white);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel failPanel = new JPanel();
        failPanel.setBackground(BACKGROUND_COLOUR);
        failPanel.add(javax.swing.Box.createHorizontalGlue());
        failPanel.add(label);
        failPanel.add(javax.swing.Box.createHorizontalGlue());
        midPanel.add(failPanel);
        midPanel.add(javax.swing.Box.createVerticalStrut(20));

        button = new JButton("Back to Menu");
        button.setActionCommand("menu");
        button.addActionListener(unifiedActionListener);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        midPanel.add(button);
        midPanel.add(javax.swing.Box.createVerticalGlue());
        instructionPanel.add(javax.swing.Box.createHorizontalGlue());
        instructionPanel.add(midPanel);
        instructionPanel.add(javax.swing.Box.createHorizontalGlue());


        // Game over panel setup
        gameOverPanel = new JPanel();
        gameOverPanel.setBackground(BACKGROUND_COLOUR);
        gameOverPanel.setLayout(new BoxLayout(gameOverPanel, BoxLayout.X_AXIS));
        midPanel = new JPanel();
        midPanel.setBackground(BACKGROUND_COLOUR);
        midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.Y_AXIS));
        midPanel.add(javax.swing.Box.createVerticalGlue());

        label = new JLabel("Grading Results:");
        label.setFont(titleFont);
        label.setForeground(TITLE_COLOUR);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        midPanel.add(label);
        midPanel.add(javax.swing.Box.createVerticalStrut(20));
        scoreLabel = new JLabel("");
        scoreLabel.setFont(instructionFont);
        scoreLabel.setForeground(Color.white);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        failPanel = new JPanel();
        failPanel.setBackground(BACKGROUND_COLOUR);
        failPanel.add(javax.swing.Box.createHorizontalGlue());
        failPanel.add(scoreLabel);
        failPanel.add(javax.swing.Box.createHorizontalGlue());
        midPanel.add(failPanel);
        midPanel.add(javax.swing.Box.createVerticalStrut(20));

        button = new JButton("Back to Menu");
        button.setActionCommand("menu");
        button.addActionListener(unifiedActionListener);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        midPanel.add(button);
        midPanel.add(javax.swing.Box.createVerticalGlue());
        gameOverPanel.add(javax.swing.Box.createHorizontalGlue());
        gameOverPanel.add(midPanel);
        gameOverPanel.add(javax.swing.Box.createHorizontalGlue());


        // Setup game menu bar
        menuBar = new JMenuBar();
        JMenu menuMenu = new JMenu("Navigation");
        JMenuItem menuItem = new JMenuItem("Back to Menu");
        menuItem.setActionCommand("menu");
        menuItem.addActionListener(unifiedActionListener);
        menuMenu.add(menuItem);
        menuItem = new JMenuItem("Quit");
        menuItem.setActionCommand("quit");
        menuItem.setAccelerator(KeyStroke.getKeyStroke('q'));
        menuItem.addActionListener(unifiedActionListener);
        menuMenu.add(menuItem);
        menuBar.add(menuMenu);

        menuMenu = new JMenu("Game");
        menuItem = new JMenuItem("Grade");
        menuItem.setActionCommand("grade");
        menuItem.addActionListener(unifiedActionListener);
        menuMenu.add(menuItem);
        menuItem = new JMenuItem("Reset");
        menuItem.setActionCommand("reset");
        menuItem.addActionListener(unifiedActionListener);
        menuMenu.add(menuItem);
        menuBar.add(menuMenu);

        frame.setJMenuBar(menuBar);
        menuBar.setVisible(true);


        centerFrame(frame);
        frame.setVisible(true); // Actually show window
    }

    private void setupGame() {
        moves = 0;
        movedBoxes = 0;
        breaks = 0;

        entities = new LinkedList<Entity>();
        ArrayList<Entity> rootEntities = new ArrayList<Entity>(5);

        final int GROUND_HEIGHT = 85;

        // Player
        rootEntities.add(new Crane(new Vector2f(400, SCREEN_HEIGHT - GROUND_HEIGHT - Crane.HEIGHT - Crane.TRACK_WIDTH/2)));

        // Ground
        rootEntities.add(new Ground(new Vector2f(0, SCREEN_HEIGHT - GROUND_HEIGHT), Box.boxFromPositionAndSize(new Vector2f(), new Vector2f(SCREEN_WIDTH, GROUND_HEIGHT))));

        // NOTE: data points are defined in first-quadrant.
        //  To find actual points, do SCREEN_HEIGHT - GROUND_HEIGHT - point.y - point.h
        //  They are also defined at 0 offset horizontally - so shift them!

        // Outlines
        final int OUTLINE_SHIFT = 50;
        int[][] outlineData = {
                {0, 0, 75, 120},
                {225, 0, 75, 120},
                {100, 0, 100, 75},
                {125, 75, 50, 50},
                {125, 125, 50, 50},
                {125, 175, 50, 50},
                {25, 120, 75, 150},
                {200, 120, 75, 150},
                {50, 270, 200, 100},
                {100, 370, 100, 100}
        };

        // Boxes
        int BOX_SHIFT = 625;
        int[][] boxData = {
                {0, 0, 100, 75},
                {101, 0, 75, 120},
                {101, 120, 75, 120},
                {177, 0, 200, 100},
                {177, 100, 50, 50},
                {177, 150, 50, 50},
                {177, 200, 50, 50},
                {277, 100, 100, 100},
                {228, 200, 150, 75},
                {228, 275, 150, 75}
        };
        if (DEBUG_PERFECT) {
            boxData = outlineData;
            BOX_SHIFT = OUTLINE_SHIFT;
        }

        for (int[] b : outlineData) {
            rootEntities.add(new Outline(new Vector2f(OUTLINE_SHIFT + b[0], SCREEN_HEIGHT - GROUND_HEIGHT - b[1] - b[3]), Box.boxFromPositionAndSize(new Vector2f(), new Vector2f(b[2], b[3]))));
        }
        for (int[] b : boxData) {
            rootEntities.add(new CandyBox(new Vector2f(BOX_SHIFT + b[0], SCREEN_HEIGHT - GROUND_HEIGHT - b[1] - b[3]), Box.boxFromPositionAndSize(new Vector2f(), new Vector2f(b[2], b[3]))));
        }
//        rootEntities.add(new CandyBox(new Vector2f(750, SCREEN_HEIGHT - GROUND_HEIGHT - 100), Box.boxFromPositionAndSize(new Vector2f(), new Vector2f(100, 100))));
//        rootEntities.add(new CandyBox(new Vector2f(600, SCREEN_HEIGHT - GROUND_HEIGHT - 80), Box.boxFromPositionAndSize(new Vector2f(), new Vector2f(150, 80))));
//        rootEntities.add(new CandyBox(new Vector2f(740, SCREEN_HEIGHT - GROUND_HEIGHT - 100 - 50), Box.boxFromPositionAndSize(new Vector2f(), new Vector2f(50, 50))));

        // Add entities to scene graph
        sceneGraph = new SceneNode();
        for (Entity b : rootEntities) {
            sceneGraph.addChildNode(b);
        }

        entities.addAll(rootEntities);
    }

    private void tick(Delta delta) { // Main game Loop
        InputController.update(delta);
        if (gameState == GameState.PLAYING) {
            DirectManipManager.getInstance().update(delta);
            sceneGraph.update(delta);
        }
    }

    private void paintPlaying(Graphics g) {
        g.setColor(BACKGROUND_COLOUR);
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        // Debug Drawing -----------------------------------------------------------
        if (DEBUG_COLLISION && debugCollideMagnet != null) {
            g.translate(100, 100);
            g.drawRect(Math.round(debugCollideMagnet.getLeft()), Math.round(debugCollideMagnet.getTop()), Math.round(debugCollideMagnet.getWidth()), Math.round(debugCollideMagnet.getHeight()));
            if (debugCollideMagnetField != null) {
                g.drawRect(Math.round(debugCollideMagnetField.getLeft()), Math.round(debugCollideMagnetField.getTop()), Math.round(debugCollideMagnetField.getWidth()), Math.round(debugCollideMagnetField.getHeight()));
            }
            ArrayList<Vector2f[]> debugCollideRotBoxCopy;
            synchronized (debugCollideCandyBoxes) {
                debugCollideRotBoxCopy = new ArrayList<Vector2f[]>(debugCollideCandyBoxes);
            }
            for (Vector2f[] vects : debugCollideRotBoxCopy) {
                for (int i = 0; i < 4; i++) {
                    int j = (i + 1) % 4;
                    g.drawLine(Math.round(vects[i].getX()), Math.round(vects[i].getY()), Math.round(vects[j].getX()), Math.round(vects[j].getY()));
                }
            }
            g.translate(-100, -100);
        }
        // ---------------------------------------------------------------------------

        // Draw scene
        sceneGraph.paint(g);

        // Draw other information
        g.setColor(Color.white);
        g.setFont(hudFont);
        g.drawString("Number of Moves:  " + String.valueOf(moves), 30, 20);
        g.drawString("Moved Boxes:         " + String.valueOf(movedBoxes), 30, 45);
        g.drawString("Broken Boxes:        " + String.valueOf(breaks), 30, 70);


        if (DEBUG_INPUT) {
            InputController.debugDraw(g);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (gameState == GameState.PLAYING) {
            paintPlaying(g);
        }
    }

    private void quitgame() {
        frame.dispose(); //This will also quit the program
        System.exit(0);
    }

    public void changeGameState(GameState gs) {
        if (gs == GameState.PLAYING) {
            frame.setContentPane(this);
            this.requestFocus();
            menuBar.setVisible(true);
            setupGame();
        } else if (gs == GameState.MENU) {
            frame.setContentPane(menuPanel);
            menuPanel.requestFocus();
            menuBar.setVisible(false);
        } else if (gs == GameState.INSTRUCTIONS) {
            frame.setContentPane(instructionPanel);
            instructionPanel.requestFocus();
            menuBar.setVisible(false);
        } else if (gs == GameState.GAMEOVER) {
            frame.setContentPane(gameOverPanel);
            gameOverPanel.requestFocus();
            menuBar.setVisible(false);
        }
        frame.validate();
        gameState = gs;
    }

    public static SceneNode getSceneGraph() {
        return singleton.sceneGraph;
    }

    public static Vector2d getCanvasOffset() {
        return new Vector2d(singleton.getLocation().x, singleton.getLocation().y);
    }

    public static List<Entity> getEntities() {
        return singleton.entities;
    }

    public static CraneMagnet getTheMagnet() {
        return singleton.theMagnet;
    }

    public static boolean addEntity(Entity e) {
        if (e instanceof CraneMagnet) {
            singleton.theMagnet = (CraneMagnet)e;
        }
        return singleton.entities.add(e);
    }

    public static boolean removeEntity(Entity e) {
        return singleton.entities.remove(e);
    }

    public static void clearDebugCollideBoxes() {
        if (DEBUG_COLLISION) {
            synchronized (debugCollideCandyBoxes) {
                debugCollideCandyBoxes.clear();
            }
        }
    }

    public static void setDebugCollideMagnet(Box box, boolean field) {
        if (field) {
            debugCollideMagnetField = box;
        } else {
            debugCollideMagnet = box;
        }
    }

    public static boolean transformedBoxCollision(Box a, AffineTransform aTransform, Box b, AffineTransform bTransform) {
        try {
            // Find transform from other to this
            AffineTransform relativeTransform = new AffineTransform(bTransform);
            relativeTransform.preConcatenate(aTransform.createInverse());

            // Transform 4 vertices of other to this space
            Vector2f transformedVertices[] = {
                    b.getTopLeft().clone(),
                    b.getTopRight().clone(),
                    b.getBottomRight().clone(),
                    b.getBottomLeft().clone()
            };
            Vector2f.transformVectorsWithAffineTransform(relativeTransform, false, transformedVertices[0], transformedVertices[1], transformedVertices[2], transformedVertices[3]);

            if (DEBUG_COLLISION) {
                debugCollideCandyBoxes.add(transformedVertices);
            }

            // Create line segments for all 8 lines and check for any intersections
            LineSegment aLines[] = {
                    new LineSegment(a.getTopLeft(), a.getTopRight()),
                    new LineSegment(a.getTopRight(), a.getBottomRight()),
                    new LineSegment(a.getBottomRight(), a.getBottomLeft()),
                    new LineSegment(a.getBottomLeft(), a.getTopLeft())
            };
            LineSegment bLines[] = {
                    new LineSegment(transformedVertices[0], transformedVertices[1]),
                    new LineSegment(transformedVertices[1], transformedVertices[2]),
                    new LineSegment(transformedVertices[2], transformedVertices[3]),
                    new LineSegment(transformedVertices[3], transformedVertices[0])
            };

            // Check if any lines intersect
            for (LineSegment al : aLines) {
                for (LineSegment bl : bLines) {
                    if (al.intersection(bl) != null) {
                        return true;
                    }
                }
            }

            // Check if a is contained within b
            if (a.isPointWithin(transformedVertices[0])) {
                return true;
            }
            // Check if b is contained within a
            Vector2f aPoint = a.getTopLeft().clone();
            Vector2f.transformVectorsWithAffineTransform(relativeTransform, true, aPoint);
            if (b.isPointWithin(aPoint)) {
                return true;
            }


        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static float findTransformAngle(AffineTransform t) {
        Vector2f a = new Vector2f(0, 0);
        Vector2f b = new Vector2f(1, 0);
        Vector2f.transformVectorsWithAffineTransform(t, false, a, b);
        return b.subtract(a).angle();
    }


    public static final int ANGLE_OUTSIDE_THRESHOLD = -10;
    /**
     * Find the closest relative right angle between these two transforms
     * @return closest right angle is return value * Math.PI/2. If none found, ANGLE_OUTSIDE_THRESHOLD is returned;
     */
    public static int closestRightAngleWithingThresholdWithFixes(Entity a, Entity b, float THRESHOLD, float[] angleFix, Vector2f translateFixPoint) {
        float currentAngle = CraneSim.findTransformAngle(a.getTotalTransform());
        float boxAngle = CraneSim.findTransformAngle(b.getTotalTransform());
        float angleDifference = currentAngle - boxAngle;
        if (angleDifference < -Math.PI) {
            angleDifference += 2*Math.PI;
        } else if (angleDifference > Math.PI) {
            angleDifference -= 2*Math.PI;
        }
        angleFix[0] = angleDifference;
        Box candyBound = b.getBoundingBox();
        if (Math.abs(angleDifference) < THRESHOLD) {
            translateFixPoint.set(candyBound.getTopLeft());
            return 0;
        } else if(Math.abs(angleDifference - Math.PI/2f) < THRESHOLD) {
            angleFix[0] -= (float)Math.PI/2f;
            translateFixPoint.set(candyBound.getTopRight());
            return 1;
        } else if(Math.abs(angleDifference + Math.PI/2f) < THRESHOLD) {
            angleFix[0] += (float)Math.PI/2f;
            translateFixPoint.set(candyBound.getBottomLeft());
            return -1;
        } else if (Math.abs(angleDifference - Math.PI) < THRESHOLD) {
            angleFix[0] -= (float)Math.PI;
            translateFixPoint.set(candyBound.getBottomRight());
            return 2;
        } else if(Math.abs(angleDifference + Math.PI) < THRESHOLD) {
            angleFix[0] += (float)Math.PI;
            translateFixPoint.set(candyBound.getBottomRight());
            return -2;
        }
        return ANGLE_OUTSIDE_THRESHOLD;
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

    public static void brokeBox() {
        singleton.breaks++;
    }

    public static void finishedMove() {
        singleton.moves++;
    }

    public static void movedBox() {
        singleton.movedBoxes++;
    }

    public static boolean floatEquals(float a, float b) {
        final float T = 0.0001f;
        return a - T < b && a + T > b;
    }

    public static boolean sameDimensions(Entity a, Entity b) {
        return floatEquals(a.getBoundingBox().getWidth(), b.getBoundingBox().getWidth())
                && floatEquals(a.getBoundingBox().getHeight(), b.getBoundingBox().getHeight())
                || floatEquals(a.getBoundingBox().getWidth(), b.getBoundingBox().getHeight())
                && floatEquals(a.getBoundingBox().getHeight(), b.getBoundingBox().getWidth());
    }

    public int calculateScore() {
        final int MOVE_BOX_ALLOWANCE = 10;
        final int MOVE_ALLOWANCE = 50;
        int efficiencyScore = Math.max(0, 3000 - Math.max(0, movedBoxes - breaks - MOVE_BOX_ALLOWANCE) * 100 - breaks * 250 - Math.max(0, moves - MOVE_ALLOWANCE)*25);
        int structureScore = 0;
        ArrayList<CandyBox> candyBoxes = new ArrayList<CandyBox>();
        ArrayList<Outline> outlines = new ArrayList<Outline>();
        for (Entity e : entities) {
            if (e instanceof CandyBox) {
                candyBoxes.add((CandyBox)e);
            } else if (e instanceof Outline) {
                outlines.add((Outline)e);
            }
        }
        for (Outline outline : outlines) {
            // Find closest box of the same size
            Vector2f outlinePos = outline.findTransformedBounds().getTopLeft();
            float minDistance = SCREEN_HEIGHT + SCREEN_WIDTH;
            CandyBox minBox = null;
            for (CandyBox b : candyBoxes) {
                if (sameDimensions(outline, b)) {
                    Vector2f bPos = b.findTransformedBounds().getTopLeft();
                    float d = bPos.subtract(outlinePos).magnitude(); // Note: overwriting bPos
                    if (d < minDistance) {
                        minDistance = d;
                        minBox = b;
                    }
                }
            }
            if (minBox != null) candyBoxes.remove(minBox);
            structureScore += minDistance;
        }
        System.out.println(structureScore);
        structureScore = Math.max(0, 7000 - structureScore);

        int finalScore = efficiencyScore + structureScore;
        scoreLabel.setText("<html>Moves: " + String.valueOf(moves) + "<br/>" +
                "Moved boxes: " + String.valueOf(movedBoxes - breaks) + "<br/>" +
                "Broken boxes: " + String.valueOf(breaks) + "<br/>" +
                "Efficiency Score: " + String.valueOf(efficiencyScore) + "<br/>" +
                "Structure Score: " + String.valueOf(structureScore) + "<br/>" +
                "Final Score: " + String.valueOf(finalScore) + "<br/><br/>" +
                (finalScore >= 10000 ? "Perfect!" : "Nice job!") +
                "</html>");
        return finalScore;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        singleton = new CraneSim();
        singleton.run();
    }
}
