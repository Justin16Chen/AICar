package aicar.dataRecording;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
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

    public void addEntry(String timestamp, double[] values) {
        if (1 + values.length != cols)
            throw new IllegalArgumentException(values.length + " values not match expected header count of " + cols);
        
        String entry = timestamp + "," + IntStream.range(0, values.length)
            .mapToObj(i -> df.format(values[i]))
            .collect(Collectors.joining(","));
        dataPoints.add(entry.substring(0, entry.length() - 1));

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

    public int getNumEntries() {
        return dataPoints.size();
    }

    // append dataPoints to end of CSV file
    public void saveToFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.write(path, dataPoints, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(filePath + " does not exist");
        }
        dataPoints.clear();
    }
}
