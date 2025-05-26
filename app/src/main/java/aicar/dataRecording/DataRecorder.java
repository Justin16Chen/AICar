package aicar.dataRecording;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.io.FileWriter;
import java.io.IOException;

public class DataRecorder {

    private int cols;
    private DecimalFormat df; // used for rounding entries
    private ArrayList<String> dataPoints;

    public DataRecorder(int cols, DecimalFormat df) {
        this.cols = cols;
        this.df = df;
        dataPoints = new ArrayList<>();
    }

    public void clearDataPoints() {
        dataPoints.clear();
    }

    public void addDataPoint(String timestamp, double[] distances, double[] responses) {
        if (1 + distances.length + responses.length != cols)
            throw new IllegalArgumentException(distances.length + " distances and " + responses.length + " responses do not match expected header count of " + cols);
        String entry = timestamp + ",";
        for (int i=0; i<distances.length; i++) 
            entry += df.format(distances[i]) + ",";
        for (double response : responses)
            entry += df.format(response) + ",";
        dataPoints.add(entry.substring(0, entry.length() - 1));
    }

    // append dataPoints to end of CSV file
    public void saveToFile(String folderPath, String fileName) {
        try {
            // Create directory if it doesn't exist
            Path path = Paths.get(folderPath, fileName);
            Files.write(path, dataPoints, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
        dataPoints.clear();
    }
}
