import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

import engine.core.MarioGame;
import engine.core.MarioResult;
import java.util.*;
import java.nio.file.Path;

public class PlayAstar {
    public static void printResults(MarioResult result, String outputPath) {
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
        System.out.println("Actions: " + result.getActionString());
        System.out.println("Coords: " + result.getCoordString());
        System.out.println("****************************************************************");


        ArrayList<String> actions = new ArrayList<>();
        ArrayList<String> observations = new ArrayList<>();
        int[][] agentObs;

        for (int idx = 0; idx < result.getAgentEvents().size(); idx++) {
            actions.add(result.getAgentEvents().get(idx).getActionString());
            agentObs = result.getAgentEvents().get(idx).getObservations();

            String[][] s = new String[agentObs.length][];
            for(int i = 0; i < agentObs.length; i++){
                s[i] = new String[agentObs[i].length];
                for(int j=0; j<agentObs[i].length; j++){
                    s[i][j] = Integer.toString(agentObs[i][j]); 
                }
            }
            observations.add(Arrays.deepToString(s));
        }
        writeStrings(outputPath + "/" + "actions.txt", actions);
        writeStrings(outputPath + "/" + "observations.txt", observations);

        // String message;
        // JSONObject json = new JSONObject();
        // json.put("name", "student");

        // JSONArray array = new JSONArray();
        // JSONObject item = new JSONObject();
        // item.put("information", "test");
        // item.put("id", 3);
        // item.put("name", "course1");
        // array.put(item);

        // json.put("course", array);

        // message = json.toString();
    }

    public static void writeStrings(String filename, List<String> lines) {
        try {
            Path file = Paths.get(filename);
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        System.out.println("Saving output to: ./");
        System.out.println("=========================");
        printResults(game.playAstar(new agents.robinBaumgarten.Agent(), getLevel(levelPath), 200, render), ".");
    }

    public static void playAstar(MarioGame game, String levelPath, boolean render, String imgDir, String outputPath) {
        System.out.println("Running Astar agent with render: " + render);
        System.out.println("Saving output to: " + outputPath);
        System.out.println("=========================");
        printResults(game.playAstar(new agents.robinBaumgarten.Agent(), getLevel(levelPath), 200, render, imgDir, outputPath), outputPath);
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
            String outputPath = args[3];
            if (outputPath == "NONE") {
                playAstar(game, levelPath, render, imgDir, ".");
            } else {
                playAstar(game, levelPath, render, imgDir, outputPath);
            }
        } else {
            playAstar(game, levelPath, render);
        }
    }
}

    // public static void playAstar(MarioGame game, String levelPath, boolean render) {
    //     System.out.println("Running Astar agent with render: " + render);
    //     System.out.println("===================================");
    //     printResults(game.playAstar(new agents.robinBaumgarten.Agent(), getLevel(levelPath), 200, render));
    // }

    // public static void main(String[] args) {
    //     MarioGame game = new MarioGame();
    //     String levelPath = args[0];
    //     if (args.length == 2) {
    //         playLevel(game, levelPath);
    //     } else {
    //         if (args[1].equals("astar")) {
    //             boolean render = false;
    //             if (args.length > 2 && args[2].equals("render")) {
    //                 render = true;
    //             }
    //             playAstar(game, levelPath, render);
    //         }
    //     }
    // }