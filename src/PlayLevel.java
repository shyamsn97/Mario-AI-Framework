import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import engine.core.MarioGame;
import engine.core.MarioResult;

public class PlayLevel {
    public static void printResults(MarioResult result) {
        System.out.println("****************************************************************");
        System.out.println("Game Status: " + result.getGameStatus().toString() +
                " Percentage Completion: " + result.getCompletionPercentage());
        System.out.println("Lives: " + result.getCurrentLives() + " Coins: " + result.getCurrentCoins() +
                " Remaining Time: " + (int) Math.ceil(result.getRemainingTime() / 1000f));
        System.out.println("Mario State: " + result.getMarioMode() +
                " (Mushrooms: " + result.getNumCollectedMushrooms() + " Fire Flowers: " + result.getNumCollectedFireflower() + ")");
        System.out.println("Total Kills: " + result.getKillsTotal() + " (Stomps: " + result.getKillsByStomp() +
                " Fireballs: " + result.getKillsByFire() + " Shells: " + result.getKillsByShell() +
                " Falls: " + result.getKillsByFall() + ")");
        System.out.println("Bricks: " + result.getNumDestroyedBricks() + " Jumps: " + result.getNumJumps() +
                " Max X Jump: " + result.getMaxXJump() + " Max Air Time: " + result.getMaxJumpAirTime());
        System.out.println("****************************************************************");
    }

    public static String getLevel(String filepath) {
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get(filepath)));
        } catch (IOException e) {
        }
        return content;
    }

    public static void playLevel(MarioGame game, String levelPath) {
        System.out.println("Running Interactive Play!");
        System.out.println("=========================");
        printResults(game.playInteractive(new agents.human.Agent(), getLevel(levelPath), 200, 999));
    }

    public static void playAstar(MarioGame game, String levelPath, boolean render) {
        System.out.println("Running Astar agent with render: " + render);
        System.out.println("===================================");
        printResults(game.playAstar(new agents.robinBaumgarten.Agent(), getLevel(levelPath), 200, render));
    }

    public static void main(String[] args) {
        MarioGame game = new MarioGame();
        String levelPath = args[0];
        if (args.length == 1) {
            playLevel(game, levelPath);
        } else {
            if (args[1].equals("astar")) {
                boolean render = false;
                if (args.length > 2 && args[2].equals("render")) {
                    render = true;
                }
                playAstar(game, levelPath, render);
            }
        }
    }
}
