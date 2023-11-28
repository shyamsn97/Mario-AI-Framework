import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import engine.core.MarioGame;
import engine.core.MarioResult;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class PlayAstar {

		public static void printCoords(ArrayList<Point2D> agentCoords) {
        for (int i = 0; i < agentCoords.size();i++){ 		      
	          System.out.println(agentCoords.get(i).getX() + " , " +  agentCoords.get(i).getY()); 		
        }
    }

		
    public static void printResults(MarioResult result) {
        System.out.println("****************************************************************");
        System.out.println("Game Status: " + result.getGameStatus().toString());
        System.out.println("Percentage Completion: " + result.getCompletionPercentage());
        System.out.println("Lives: " + result.getCurrentLives());
				System.out.println("Coins: " + result.getCurrentCoins()); 
        System.out.println("Remaining Time: " + (int) Math.ceil(result.getRemainingTime() / 1000f));
        System.out.println("Mario State: " + result.getMarioMode());
        System.out.println("Mushrooms collected: " + result.getNumCollectedMushrooms());
				System.out.println("Fire Flowers collected: " + result.getNumCollectedFireflower());
        System.out.println("Total Kills: " + result.getKillsTotal());
				System.out.println("Kills by Stomps: " + result.getKillsByStomp());
				System.out.println("Kills by Fireballs: " + result.getKillsByFire());
				System.out.println("Kills by Shells: " + result.getKillsByShell());
				System.out.println("Kills by Falls: " + result.getKillsByFall());
				System.out.println("Bricks: " + result.getNumDestroyedBricks());
				System.out.println("Jumps: " + result.getNumJumps());
				System.out.println("Max X Jump: " + result.getMaxXJump());
				System.out.println("Max Air Time: " + result.getMaxJumpAirTime());
        System.out.println("****************************************************************");
				printCoords(result.agentCoords);	
    }



    public static String getLevel(String filepath) {
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get(filepath)));
        } catch (IOException e) {
        }
        return content;
    }

    public static void playAstar(MarioGame game, String levelPath, boolean render) {
        System.out.println("Running Astar agent with render: " + render);
        System.out.println("=========================");
        printResults(game.playAstar(new agents.robinBaumgarten.Agent(), getLevel(levelPath), 200, render));
    }
    public static void playAstar(MarioGame game, String levelPath, boolean render, String imgDir) {
        System.out.println("Running Astar agent with render: " + render);
        System.out.println("=========================");
        printResults(game.playAstar(new agents.robinBaumgarten.Agent(), getLevel(levelPath), 200, render, imgDir));
    }

    public static void main(String[] args) {
        MarioGame game = new MarioGame();
        String levelPath = args[0];
        String renderString = args[1];
        boolean render = false;

        if (args[1].equals("human")) {
            render = true;
        }
        if (args.length > 2) {
            // this means we passed in an imageDir
            String imgDir = args[2];
            playAstar(game, levelPath, render, imgDir);
        } else {
            playAstar(game, levelPath, render);
        }
    }
}