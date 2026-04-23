import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.*;


public class Main {
    public static void main(String[] args) throws Exception {
        //scoreFilePath("d_pet_pictures.txt", true, false); // c_memorable_moments.txt, b_lovely_landscapes.txt, e_shiny_selfies.txt
        testAll();
    }

    private static void testAll() throws Exception {
        String[] targets = {
                "a_example.txt",
                "b_lovely_landscapes.txt",
                "c_memorable_moments.txt",
                "d_pet_pictures.txt",
                "e_shiny_selfies.txt"
        };

        int total = 0;
        for (String target : targets) {
            total += scoreFilePath(target, false, false);
        }

        System.out.println("Total Score: " + total);
    }

    private static String convertTimeInMsToString(long elapsedMs) {
        long hours = elapsedMs / 3_600_000;
        long minutes = (elapsedMs % 3_600_000) / 60_000;
        long seconds = (elapsedMs % 60_000) / 1000;
        long millis = elapsedMs % 1000;

        StringBuilder time = new StringBuilder();

        if (hours > 0) time.append(hours).append("h ");
        if (minutes > 0 || hours > 0) time.append(minutes).append("m ");
        if (seconds > 0 || minutes > 0 || hours > 0) time.append(seconds).append("s ");
        time.append(millis).append("ms");

        return time.toString();
    }

    private static int scoreFilePath(String filepath, boolean verbose, boolean use_tag_clipping) throws Exception {
        long start = System.nanoTime();

        SlideGenerator generator = new SlideGenerator(filepath, verbose, use_tag_clipping); // c_memorable_moments.txt, b_lovely_landscapes.txt, e_shiny_selfies.txt
        List<Photo> slideshow = generator.generateSlideShow();

        long end = System.nanoTime();

        long elapsedMs = (end - start) / 1_000_000;
        String timeTaken = convertTimeInMsToString(elapsedMs);

        int score = SlideGenerator.scoreSlideShow(slideshow);
        System.out.println(Character.toUpperCase(filepath.charAt(0)) + " | Score: " + score + ", Taking: " + timeTaken);
        return score;
    }
}