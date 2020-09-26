import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static Point parsePoint(String s){
        //parse (x,y) to Point
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(s);
        m.find();
        int x=Integer.parseInt(m.group());
        m.find();
        int y=Integer.parseInt(m.group());
        return  new Point(x,y);
    }

    public static ImagePattern margeImages(BufferedImage img1, BufferedImage img2) {
        int offset  = 5;
        int wid = img1.getWidth()+img2.getWidth()+offset;
        int height = Math.max(img1.getHeight(),img2.getHeight())+offset;

        BufferedImage newImage = new BufferedImage(wid,height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = newImage.createGraphics();

        g2.fillRect(0,0,wid, height);

        g2.drawImage(img1, null, 0,0);
        g2.drawImage(img2, null, img1.getWidth()+offset, 0);
        g2.dispose();

        return new ImagePattern(SwingFXUtils.toFXImage(newImage, null));
    }

    public static List<Point> getPointsFromReport(List<Report> list, int start, int end) {
        List<Point> pointList = new LinkedList<>();
        for (int i = start; i <= end; i++) {
            if(i == list.size()) break;
            pointList.add(list.get(i).getLocation());
        }
        return pointList;
    }
}
