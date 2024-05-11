package engine.core;

import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.KeyAdapter;

import javax.swing.JFrame;

import agents.human.Agent;
import engine.helper.GameStatus;
import engine.helper.MarioActions;
import java.io.*;
import javax.imageio.*;


public class MarioGame {
    /**
     * the maximum time that agent takes for each step
     */
    public static final long maxTime = 40;
    /**
     * extra time before reporting that the agent is taking more time that it should
     */
    public static final long graceTime = 10;
    /**
     * Screen width
     */
    public static final int width = 256;
    /**
     * Screen height
     */
    public static final int height = 256;
    /**
     * Screen width in tiles
     */
    public static final int tileWidth = width / 16;
    /**
     * Screen height in tiles
     */
    public static final int tileHeight = height / 16;
    /**
     * print debug details
     */
    public static final boolean verbose = false;

    /**
     * pauses the whole game at any moment
     */
    public boolean pause = false;

    /**
     * events that kills the player when it happens only care about type and param
     */
    private MarioEvent[] killEvents;

    //visualization
    private JFrame window = null;
    private MarioRender render = null;
    private MarioAgent agent = null;
    private MarioWorld world = null;

    /**
     * Create a mario game to be played
     */
    public MarioGame() {

    }

    /**
     * Create a mario game with a different forward model where the player on certain event
     *
     * @param killEvents events that will kill the player
     */
    public MarioGame(MarioEvent[] killEvents) {
        this.killEvents = killEvents;
    }

    private int getDelay(int fps) {
        if (fps <= 0) {
            return 0;
        }
        return 1000 / fps;
    }

    private void setAgent(MarioAgent agent) {
        this.agent = agent;
        if (agent instanceof KeyAdapter) {
            this.render.addKeyListener((KeyAdapter) this.agent);
        }
    }

    private MarioWorld initializeWorld(String level, int timer, int marioState, boolean visual, int fps, int lives, MarioWorld world) {
        world.visuals = visual;
        world.initializeLevel(level, 1000 * timer);
        if (visual) {
            world.initializeVisuals(this.render.getGraphicsConfiguration());
        }
        world.mario.isLarge = marioState > 0;
        world.mario.isFire = marioState > 1;
        world.update(new boolean[MarioActions.numberOfActions()]);
        return world;
    }

    public MarioResult playInteractive(Agent agent, String level, int timer, int lives) {
        return playInteractive(agent, level, timer, lives, null, null);
    }


    public MarioResult playInteractive(Agent agent, String level, int timer, int lives, String imageDir, String outputPath) {
        this.window = new JFrame("Mario AI Framework");
        this.render = new MarioRender(2);
        this.window.setContentPane(this.render);
        this.window.pack();
        this.window.setResizable(false);
        this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        if (imageDir != null && !imageDir.isEmpty()) {
            this.render.init(imageDir);
        } else {
            this.render.init();
        }
        this.window.setVisible(true);

        this.setAgent(agent);
        MarioResult result = null;
        MarioWorld world;
        for (int i = 0; i < lives; i++) {
            world = new MarioWorld(this.killEvents, lives);
            result = this.gameLoop(level, timer, 1, true, 30, lives, world, outputPath);
            if (!(result.getGameStatus() == GameStatus.WIN)) {
                lives--;
            }
            else {
                break;
            }
        }

        this.window.dispose();
        this.render = null;
        this.world = null;
        return result;
    }

    public MarioResult playAstar(agents.robinBaumgarten.Agent agent, String level, int timer, boolean render) {
        return playAstar(agent, level, timer, render, null, null);
    }

    public MarioResult playAstar(agents.robinBaumgarten.Agent agent, String level, int timer, boolean render, String imageDir, String outputPath) {
        if (render) {
            this.window = new JFrame("Mario AI Framework");
            this.render = new MarioRender(2);
            this.window.setContentPane(this.render);
            this.window.pack();
            this.window.setResizable(false);
            this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            if (imageDir != null && !imageDir.isEmpty()) {
                this.render.init(imageDir);
            } else {
                this.render.init();
            }
            this.window.setVisible(true);
        }

        this.setAgent(agent);
        MarioResult result = null;
        MarioWorld world = new MarioWorld(this.killEvents, 1);
        result = this.gameLoop(level, timer, 1, render, 30, 1, world, outputPath);

        if (render) {
            this.window.dispose();
            this.render = null;
            this.world = null;
        }
        return result;
    }


    private MarioResult gameLoop(String level, int timer, int marioState, boolean visual, int fps) {
        return gameLoop(level, timer, marioState, visual, fps, 1, null);
    }

    private MarioResult gameLoop(String level, int timer, int marioState, boolean visual, int fps, int lives, String outputPath) {
        world = new MarioWorld(this.killEvents, lives);
        return gameLoop(level, timer, marioState, visual, fps, lives, world, outputPath);
    }

    private MarioResult gameLoop(String level, int timer, int marioState, boolean visual, int fps, int lives, MarioWorld world, String outputPath) {

        long currentTime = System.currentTimeMillis();

        this.world = initializeWorld(level, timer, marioState, visual, fps, lives, world);

        //initialize graphics
        int count = 0;
        BufferedImage img;
        VolatileImage renderTarget = null;
        Graphics backBuffer = null;
        Graphics currentBuffer = null;
        if (visual) {
            renderTarget = this.render.createVolatileImage(MarioGame.width, MarioGame.height);
            backBuffer = this.render.getGraphics();
            currentBuffer = renderTarget.getGraphics();
            this.render.addFocusListener(this.render);
        }

        MarioTimer agentTimer = new MarioTimer(MarioGame.maxTime);
        System.out.println("WORLD");
        System.out.println(this.world);
        this.agent.initialize(new MarioForwardModel(this.world.clone()), agentTimer);

        MarioForwardModel forwardModel;
        ArrayList<MarioEvent> gameEvents = new ArrayList<>();
        ArrayList<MarioAgentEvent> agentEvents = new ArrayList<>();
        int[][] observations;
        while (this.world.gameStatus == GameStatus.RUNNING) {
            if (!this.pause) {
                //get actions
                forwardModel = new MarioForwardModel(this.world.clone());
                agentTimer = new MarioTimer(MarioGame.maxTime);
                observations = forwardModel.getScreenCompleteObservation(0,2);
                // try {
                //     // to sleep 10 seconds
                //     Thread.sleep(2000);
                // } catch (InterruptedException e) {
                //     // recommended because catching InterruptedException clears interrupt flag
                //     Thread.currentThread().interrupt();
                //     // you probably want to quit if the thread is interrupted
                // }
                boolean[] actions = this.agent.getActions(forwardModel, agentTimer);
                if (MarioGame.verbose) {
                    if (agentTimer.getRemainingTime() < 0 && Math.abs(agentTimer.getRemainingTime()) > MarioGame.graceTime) {
                        System.out.println("The Agent is slowing down the game by: "
                                + Math.abs(agentTimer.getRemainingTime()) + " msec.");
                    }
                }
                // update world
                this.world.update(actions);
                gameEvents.addAll(this.world.lastFrameEvents);
                agentEvents.add(new MarioAgentEvent(actions, this.world.mario.x,
                        this.world.mario.y, (this.world.mario.isLarge ? 1 : 0) + (this.world.mario.isFire ? 1 : 0),
                        this.world.mario.onGround, this.world.currentTick, observations));
            }

            //render world
            if (visual) {
                this.render.renderWorld(this.world, renderTarget, backBuffer, currentBuffer);
                if (outputPath != null && !outputPath.isEmpty()) {
                    img = convertToBufferedImage(renderTarget);
                    saveToPNG(img, outputPath + "/images/" + "img" + count + ".png");
                }
                count++;
            }
            //check if delay needed
            if (this.getDelay(fps) > 0) {
                try {
                    currentTime += this.getDelay(fps);
                    Thread.sleep(Math.max(0, currentTime - System.currentTimeMillis()));
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
        return new MarioResult(this.world, gameEvents, agentEvents);
    }

    private BufferedImage convertToBufferedImage(VolatileImage volatileImage) {
        BufferedImage bufferedImage = new BufferedImage(volatileImage.getWidth(), volatileImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = bufferedImage.getGraphics();
        g.drawImage(volatileImage, 0, 0, null);
        g.dispose();
        return bufferedImage;
    }

    private void saveToPNG(BufferedImage image, String filePath) {
        try {
            File file = new File(filePath);
            ImageIO.write(image, "png", file);
            System.out.println("Image saved to: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // /**
    //  * Play a certain mario level
    //  *
    //  * @param level a string that constitutes the mario level, it uses the same representation as the VGLC but with more details. for more details about each symbol check the json file in the levels folder.
    //  * @param timer number of ticks for that level to be played. Setting timer to anything &lt;=0 will make the time infinite
    //  * @return statistics about the current game
    //  */
    // public MarioResult playGame(String level, int timer) {
    //     return this.runGame(new Agent(), level, timer, 0, true, 30, 2);
    // }

    // /**
    //  * Play a certain mario level
    //  *
    //  * @param level      a string that constitutes the mario level, it uses the same representation as the VGLC but with more details. for more details about each symbol check the json file in the levels folder.
    //  * @param timer      number of ticks for that level to be played. Setting timer to anything &lt;=0 will make the time infinite
    //  * @param marioState the initial state that mario appears in. 0 small mario, 1 large mario, and 2 fire mario.
    //  * @return statistics about the current game
    //  */
    // public MarioResult playGame(String level, int timer, int marioState) {
    //     return this.runGame(new Agent(), level, timer, marioState, true, 30, 2);
    // }

    // /**
    //  * Play a certain mario level
    //  *
    //  * @param level      a string that constitutes the mario level, it uses the same representation as the VGLC but with more details. for more details about each symbol check the json file in the levels folder.
    //  * @param timer      number of ticks for that level to be played. Setting timer to anything &lt;=0 will make the time infinite
    //  * @param marioState the initial state that mario appears in. 0 small mario, 1 large mario, and 2 fire mario.
    //  * @param fps        the number of frames per second that the update function is following
    //  * @return statistics about the current game
    //  */
    // public MarioResult playGame(String level, int timer, int marioState, int fps) {
    //     return this.runGame(new Agent(), level, timer, marioState, true, fps, 2);
    // }

    // /**
    //  * Play a certain mario level
    //  *
    //  * @param level      a string that constitutes the mario level, it uses the same representation as the VGLC but with more details. for more details about each symbol check the json file in the levels folder.
    //  * @param timer      number of ticks for that level to be played. Setting timer to anything &lt;=0 will make the time infinite
    //  * @param marioState the initial state that mario appears in. 0 small mario, 1 large mario, and 2 fire mario.
    //  * @param fps        the number of frames per second that the update function is following
    //  * @param scale      the screen scale, that scale value is multiplied by the actual width and height
    //  * @return statistics about the current game
    //  */
    // public MarioResult playGame(String level, int timer, int marioState, int fps, float scale) {
    //     return this.runGame(new Agent(), level, timer, marioState, true, fps, scale);
    // }

    // /**
    //  * Run a certain mario level with a certain agent
    //  *
    //  * @param agent the current AI agent used to play the game
    //  * @param level a string that constitutes the mario level, it uses the same representation as the VGLC but with more details. for more details about each symbol check the json file in the levels folder.
    //  * @param timer number of ticks for that level to be played. Setting timer to anything &lt;=0 will make the time infinite
    //  * @return statistics about the current game
    //  */
    // public MarioResult runGame(MarioAgent agent, String level, int timer) {
    //     return this.runGame(agent, level, timer, 0, false, 0, 2);
    // }

    // /**
    //  * Run a certain mario level with a certain agent
    //  *
    //  * @param agent      the current AI agent used to play the game
    //  * @param level      a string that constitutes the mario level, it uses the same representation as the VGLC but with more details. for more details about each symbol check the json file in the levels folder.
    //  * @param timer      number of ticks for that level to be played. Setting timer to anything &lt;=0 will make the time infinite
    //  * @param marioState the initial state that mario appears in. 0 small mario, 1 large mario, and 2 fire mario.
    //  * @return statistics about the current game
    //  */
    // public MarioResult runGame(MarioAgent agent, String level, int timer, int marioState) {
    //     return this.runGame(agent, level, timer, marioState, false, 0, 2);
    // }

    // /**
    //  * Run a certain mario level with a certain agent
    //  *
    //  * @param agent      the current AI agent used to play the game
    //  * @param level      a string that constitutes the mario level, it uses the same representation as the VGLC but with more details. for more details about each symbol check the json file in the levels folder.
    //  * @param timer      number of ticks for that level to be played. Setting timer to anything &lt;=0 will make the time infinite
    //  * @param marioState the initial state that mario appears in. 0 small mario, 1 large mario, and 2 fire mario.
    //  * @param visuals    show the game visuals if it is true and false otherwise
    //  * @return statistics about the current game
    //  */
    // public MarioResult runGame(MarioAgent agent, String level, int timer, int marioState, boolean visuals) {
    //     return this.runGame(agent, level, timer, marioState, visuals, visuals ? 30 : 0, 2);
    // }

    // /**
    //  * Run a certain mario level with a certain agent
    //  *
    //  * @param agent      the current AI agent used to play the game
    //  * @param level      a string that constitutes the mario level, it uses the same representation as the VGLC but with more details. for more details about each symbol check the json file in the levels folder.
    //  * @param timer      number of ticks for that level to be played. Setting timer to anything &lt;=0 will make the time infinite
    //  * @param marioState the initial state that mario appears in. 0 small mario, 1 large mario, and 2 fire mario.
    //  * @param visuals    show the game visuals if it is true and false otherwise
    //  * @param fps        the number of frames per second that the update function is following
    //  * @return statistics about the current game
    //  */
    // public MarioResult runGame(MarioAgent agent, String level, int timer, int marioState, boolean visuals, int fps) {
    //     return this.runGame(agent, level, timer, marioState, visuals, fps, 2);
    // }

    // /**
    //  * Run a certain mario level with a certain agent
    //  *
    //  * @param agent      the current AI agent used to play the game
    //  * @param level      a string that constitutes the mario level, it uses the same representation as the VGLC but with more details. for more details about each symbol check the json file in the levels folder.
    //  * @param timer      number of ticks for that level to be played. Setting timer to anything &lt;=0 will make the time infinite
    //  * @param marioState the initial state that mario appears in. 0 small mario, 1 large mario, and 2 fire mario.
    //  * @param visuals    show the game visuals if it is true and false otherwise
    //  * @param fps        the number of frames per second that the update function is following
    //  * @param scale      the screen scale, that scale value is multiplied by the actual width and height
    //  * @return statistics about the current game
    //  */
    // public MarioResult runGame(MarioAgent agent, String level, int timer, int marioState, boolean visuals, int fps, float scale, int lives) {
    //     if (visuals) {
    //         this.window = new JFrame("Mario AI Framework");
    //         this.render = new MarioRender(scale);
    //         this.window.setContentPane(this.render);
    //         this.window.pack();
    //         this.window.setResizable(false);
    //         this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //         this.render.init();
    //         this.window.setVisible(true);
    //     }
    //     this.setAgent(agent);
    //     MarioResult result = this.gameLoop(level, timer, marioState, visuals, fps, lives);
    //     this.window.dispose();
    //     this.render = null;
    //     this.world = null;
    //     return result;
    // }

    // public MarioResult runGame(MarioAgent agent, String level, int timer, int marioState, boolean visuals, int fps, float scale) {
    //     return runGame(agent, level, timer, marioState, visuals, fps, scale, 1);
    // }
}
