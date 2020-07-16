import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Utiles {
    //create list from csv file
    public static List<String> readPointsFromCSV(File f) throws IOException {
        List<String> records = new LinkedList<>();
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line;
        while ((line = br.readLine()) != null) {
            String[] values = line.split("[^0-9,],");
            records.addAll(Arrays.asList(values));
        }
        return records;
    }
    //create list of points from list of records (the return value of readPointsFromCSV function)
    public static List<Point> convertStringToPoints(List<String> records) throws Exception{
        List<Point> pointList = new LinkedList<>();

        for (String a:records) {
            String s1 = a.split(",")[0];
            String s2 = a.split(",")[1];

            Point p = new Point(Integer.parseInt(s1.replaceAll("[\\D]", "")), Integer.parseInt(s2.replaceAll("[\\D]", "")));
            pointList.add(p);
        }
        return pointList;
    }
}
