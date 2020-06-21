import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Map {
    private Point[][] matrix; //matrix of the map

    public static final Integer REGULAR = 1; //point that didn't reach yes
    public static final Integer BEEN_HERE = 2; //point that already reached
    public static final Integer BORDER = 3; //point that agent cant step on, like the border of the map.
    public static final Integer AGENT = 9; //agent location
    public static final Integer INTERES = 8;
    public static HashMap<Integer, Boolean> hash_map = new HashMap<>(); //using to check if the the number (REGULAR/BEEN_HERE/BORDER/AGENT) is correct


    private Map(Point[][] matrix) {
        this.matrix = matrix;
    }


    //file start with  height, width and  and then matrix of integer as defined above
    public static Map CreateMap(File file) throws IOException {
        String str;
        BufferedReader br;
        br = new BufferedReader(new FileReader(file));
        str = br.readLine();
        List<String> size = Arrays.asList(str.trim().split(",")); //get height and width
        Integer height = Integer.parseInt(size.get(0));
        Integer width = Integer.parseInt(size.get(1));
        Point matrix[][] = new Point[height][width];
        for (int i = 0; i < height; ++i) {
            str = br.readLine();
            List<String> info = Arrays.asList(str.trim().split(" ")); //get map information
            for (int j = 0; j < width; ++j) {
                if (!Map.isValidNumber(Integer.parseInt(info.get(j)))) {
                    throw new IOException("Invalid number");
                }
                Point p = new Point(i, j);
                p.setValue(Integer.parseInt(info.get(j))); //set value
                matrix[i][j] = p; //add to matrix
            }
        }
        br.close();
        return new Map(matrix);
    }

    public Point[][] getMatrix() {
        return this.matrix;
    }
    public int getRowsNumber() {
        return matrix.length;
    }

    public int getColumnsNumber() {
        return matrix[0].length;
    }

    //to check if a number is describe a correct value
    private static Boolean isValidNumber(Integer i) {
        Map.createValidationMap();
        return Map.hash_map.containsKey(i);
    }

    private static void createValidationMap() {
        if (!Map.hash_map.isEmpty()) {
            return;
        }
        Map.hash_map.put(Map.REGULAR, true);
        Map.hash_map.put(Map.AGENT, true);
        Map.hash_map.put(Map.BEEN_HERE, true);
        Map.hash_map.put(Map.BORDER, true);
    }
}
