import com.sun.webkit.network.Util;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.beans.XMLDecoder;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.System.exit;
import static java.lang.System.setOut;

public class Controller implements Initializable {
    public GridPane mainGrid;
    GridPane mapGrid;
    Map map;
    List<Map> mapslog;
    int currentMap = -1;
    Thread mapChangingThread;
    BooleanProperty blockPointsAdd = new SimpleBooleanProperty(); //block adding points option
    BooleanProperty blockBackward = new SimpleBooleanProperty(); //block backward button
    BooleanProperty blockForward = new SimpleBooleanProperty(); //block forward button
    BooleanProperty blockPlay = new SimpleBooleanProperty(); //block play button
    BooleanProperty blockRandom = new SimpleBooleanProperty(); //block random points option
    Boolean agentLoaded = false;
    List<List<Point>> pathToInsteres;

    java.util.Map<String, ImagePattern> imgFile = new HashMap<>();


    public boolean isBlockRandom() {
        return blockRandom.get();
    }

    public BooleanProperty blockRandomProperty() {
        return blockRandom;
    }

    public void setBlockRandom(boolean blockRandom) {
        this.blockRandom.set(blockRandom);
    }


    public boolean getBlockBackward() {
        return blockBackward.get();
    }

    public BooleanProperty blockBackwardProperty() {
        return blockBackward;
    }

    public void setBlockBackward(boolean blockBackward) {
        this.blockBackward.set(blockBackward);
    }

    public boolean getBlockForward() {
        return blockForward.get();
    }

    public BooleanProperty blockForwardProperty() {
        return blockForward;
    }

    public void setBlockForward(boolean blockForward) {
        this.blockForward.set(blockForward);
    }

    public boolean getBlockPlay() {
        return blockPlay.get();
    }

    public BooleanProperty blockPlayProperty() {
        return blockPlay;
    }

    public void setBlockPlay(boolean blockPlay) {
        this.blockPlay.set(blockPlay);
    }


    public boolean getBlockPointsAdd() {
        return blockPointsAdd.get();
    }

    public BooleanProperty blockPointsAddProperty() {
        return blockPointsAdd;
    }

    public void setBlockPointsAdd(boolean blockPointsAdd) {
        this.blockPointsAdd.set(blockPointsAdd);
    }

    //file chooser dialog
    public File getFile(String title) {
        Window theStage = mainGrid.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        return fileChooser.showOpenDialog(theStage);
    }

    //when use choose to load map file
    public void loadMapFile(ActionEvent actionEvent) {
        File f = this.getFile("Open Map file");
        if (f == null) return; //if user didn't choose file
        this.clearThread();
        try {
            this.map = Map.CreateMap(f);
            GridPane newMapGrid = this.createMapGrid(this.map); //create map grid
            setMapGrid(newMapGrid); //show the map on screen
            this.blockPointsAdd.set(false); //now the user can add points file
            this.blockRandom.set(false); //now user can create random points
        } catch (Exception e) {
            AlertBox.display("can't open map file");
        }
    }

    //show grid argument on screen
    private void setMapGrid(GridPane grid) {
        GridPane.setConstraints(grid, 0, 1);
        GridPane.setColumnSpan(grid, GridPane.REMAINING);
        if (mapGrid != null) {
            mainGrid.getChildren().remove(this.mapGrid); //clean previous map
        }
        this.mainGrid.getChildren().add(grid);
        this.mapGrid = grid;
    }

    //stop the thread (the "playing" thread)
    private void clearThread() {
        if (this.mapChangingThread == null) return;
        this.mapChangingThread.interrupt();
        this.mapChangingThread = null;
    }

    public void exitFunc() {
        exit(0);
    }

    //create grid object from map object
    private GridPane createMapGrid(Map newMap) {
        int pad = 2;
        int menuBarSize = 25;
        GridPane mapGrid = new GridPane();
        int rows = newMap.getRowsNumber();
        int columns = newMap.getColumnsNumber();
        mapGrid.setGridLinesVisible(true);
        Scene scene = this.mainGrid.getScene();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                Rectangle rec = new ResizableRectangle();
                //bind to width and columns so the user can change the windows size
                //width
                rec.widthProperty().bind(scene.widthProperty().divide(columns).add(-2 * pad));
                //height
                rec.heightProperty().bind(scene.heightProperty().add(-menuBarSize).divide(rows).add(-2 * pad));
                Point p = newMap.getMatrix()[i][j];
                //bind rectangle color to value with IntToColor function
                rec.fillProperty().bind(Bindings.createObjectBinding(() -> IntToColor.getColor(p.valueProperty().get()), p.valueProperty()));
                p.setRec(rec);
                GridPane.setConstraints(rec, j, i);
                GridPane.setMargin(rec, new Insets(pad, pad, pad, pad));
                mapGrid.getChildren().add(rec);
            }
        }
        GridPane.setConstraints(mapGrid, 0, 1);
        return mapGrid;
    }

    //when user click on adding interes points
    public void loadInteresPoints(ActionEvent actionEvent) {
        File f = getFile("Open Points file");
        if (f == null) return; //if user didn't choose file

        if (this.map == null) {
            AlertBox.display("Please load map file first.");
            return;
        }

        final String POINT = "Point";
        final String LOCATION = "location";
        final String INTERESTING = "interesting";

        List<Report> pointList = new LinkedList<>();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(f);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName(POINT);
            for (int i = 0; i < nList.getLength(); ++i) {
                Element element = (Element) nList.item(i);
                pointList.add(new Report(Utiles.parsePoint(element.getAttribute(LOCATION)), Boolean.valueOf(element.getAttribute(INTERESTING))));
            }
        } catch (Exception e) {
            AlertBox.display("Points file is incorrect");
            return;
        }

        List<List<Point>> pathToInteres = pathToInsteres(pointList);
        this.pathToInsteres = pathToInteres;
        this.agentLoaded = true;

        List<Map> maplist = new LinkedList<>();
        for (List<Point> list : pathToInteres) {
            maplist.add(new Map(this.map));
        }
        this.mapslog = maplist;

        this.blockRandom.set(true);
        this.blockPointsAdd.set(true);
        this.mapslog = maplist;
        if (this.mapslog.size() > 0) { //let user click on play and forward buttons
            this.blockForward.set(false);
            this.blockPlay.set(false);
            this.changeMap(0);
        }
    }

    public void drawPath(int n) {
        ImagePattern img = null;
        for (int i = 0; i < this.pathToInsteres.get(n).size()-1; i++) {
            Point p = this.pathToInsteres.get(n).get(i);
            String direction = fromDirection(p, this.pathToInsteres.get(n).get(i+1));
            if (this.mapslog.get(n).getMatrix()[p.getX()][p.getY()].getRec().getFill() instanceof ImagePattern) {
                ImagePattern a = (ImagePattern) this.mapslog.get(n).getMatrix()[p.getX()][p.getY()].getRec().getFill();
                try {
                    img = Utiles.margeImages(SwingFXUtils.fromFXImage(a.getImage(), null), SwingFXUtils.fromFXImage(this.imgFile.get(direction).getImage(), null));
                } catch (Exception e) {
                    System.err.println("Error while loading images files");
                }
            } else {
                img = this.imgFile.get(direction);
            }

            this.mapslog.get(n).getMatrix()[p.getX()][p.getY()].getRec().fillProperty().unbind();
            this.mapslog.get(n).getMatrix()[p.getX()][p.getY()].getRec().setFill(img);
        }
    }

    //check the direction from a to b
    private String fromDirection(Point a, Point b) {
        if (a.getX() == b.getX()) {
            if(a.getY() < b.getY()) return "e"; //east
            if(a.getY() > b.getY()) return "w"; //west
        }
        if(a.getX() > b.getX()) {
            if(a.getY() == b.getY()) return "n"; //north
            if(a.getY() < b.getY()) return "ne"; //north-east
            return "wn"; //west-north
        }
        if (a.getX() < b.getX()) {
            if(a.getY() == b.getY()) return "s"; //south
            if(a.getY() < b.getY()) return "es"; //east-south
            return "ws"; //south-west
        }
        return null;

    }

    //return list that the last point is interest
    public List<List<Point>> pathToInsteres(List<Report> reportList) {
        List<List<Point>> pointsList = new LinkedList<>();
        for (int i = 0; i < reportList.size(); ++i) {
            if (reportList.get(i).getIntersting()) {
                pointsList.add(Utiles.getPointsFromReport(reportList, 0, i));
            }
        }
        return pointsList;
    }

    //when user click on loading map log file
    public void loadMapLog(ActionEvent actionEvent) throws Exception {
        File f = getFile("Open Map Log file");
        if (f == null) return; //if user didn't choose file
        this.clearThread(); //clear playing thread is exist (another file already loaded and play)
        BufferedReader reader = new BufferedReader(new FileReader(f));
        String line = reader.readLine();
        List<Map> maplist = new LinkedList<>();
        while (line != null) {
            List<String> list = new LinkedList<>();
            while ((line = reader.readLine()) != null && !line.equals("")) {
                list.add(line);
            }
            maplist.add(Map.CreateMapFromLog(list)); //adding maps
        }
        this.blockRandom.set(true);
        this.blockPointsAdd.set(true);
        this.mapslog = maplist;
        if (this.mapslog.size() > 0) { //let user click on play and forward buttons
            this.blockForward.set(false);
            this.blockPlay.set(false);
            this.changeMap(0);
        }
    }

    //change the map that use see
    private synchronized void changeMap(int n) {
        if (n >= this.mapslog.size()) return; //check if the n'th map is valid
        this.setMapGrid(createMapGrid(this.mapslog.get(n))); //set the new map
        if(this.agentLoaded) this.drawPath(n);
        this.currentMap = n;
        //block and dis-block button according to the n'th
        if (this.mapslog.size() - 1 == n) {
            this.blockForward.set(true);
            this.blockPlay.set(true);
        } else {
            this.blockForward.set(false);
            this.blockPlay.set(false);
        }
        if (n == 0) {
            this.blockBackward.set(true);
        } else {
            this.blockBackward.set(false);
        }
    }

    //clean the values of the points, keep only regular and borders
    private void cleanMap() {
        Point[][] mat = this.map.getMatrix();
        for (int i = 0; i < this.map.getRowsNumber(); i++) {
            for (int j = 0; j < this.map.getColumnsNumber(); j++) {
                if (!(mat[i][j].getValue() == Map.BORDER) && !(mat[i][j].getValue() == Map.REGULAR)) {
                    mat[i][j].setValue(Map.REGULAR);
                }
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.blockPointsAdd.set(true);
        this.blockBackward.set(true);
        this.blockForward.set(true);
        this.blockPlay.set(true);
        this.blockRandom.set(true);
        this.imgFile.put("n", new ImagePattern(new Image("file:images/n.png"))); // north
        this.imgFile.put("ne", new ImagePattern(new Image("file:images/ne.png"))); // north-east
        this.imgFile.put("e", new ImagePattern(new Image("file:images/e.png"))); // east
        this.imgFile.put("es", new ImagePattern(new Image("file:images/es.png"))); // east-south
        this.imgFile.put("s", new ImagePattern(new Image("file:images/s.png"))); // south
        this.imgFile.put("sw", new ImagePattern(new Image("file:images/sw.png"))); // south-west
        this.imgFile.put("w", new ImagePattern(new Image("file:images/w.png"))); // west
        this.imgFile.put("wn", new ImagePattern(new Image("file:images/wn.png"))); // west-north
    }

    //when user click on the backward button
    public void mapBackward(ActionEvent actionEvent) {
        this.changeMap(this.currentMap - 1);
    }

    //when user click on the forward button
    public void mapForward(ActionEvent actionEvent) {
        this.changeMap(this.currentMap + 1);
    }

    //when user click on the play/pause button
    public void mapPlay(ActionEvent actionEvent) {
        if (this.mapChangingThread == null) { //check if
            Thread t = new Thread(() -> {
                while (this.currentMap < this.mapslog.size() - 1) { //while we didn't finish
                    Platform.runLater(() -> mapForward(actionEvent)); //move forward
                    try {
                        Thread.sleep(500); //wait 0.5 sec
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                this.mapChangingThread = null;
            });
            this.mapChangingThread = t;
            t.start();
        } else { //stop the map change
            this.mapChangingThread.interrupt();
        }
    }

    //when user click on random points option
    public void random(ActionEvent actionEvent) {
        final float pr = 0.20F; //probability to choose a points as interesting point
        for (int i = 0; i < this.map.getRowsNumber(); i++) {
            for (int j = 0; j < this.map.getColumnsNumber(); j++) {
                if (this.map.getMatrix()[i][j].getValue() == Map.BORDER)
                    continue; //don't let border to be interesting point
                if (Math.random() <= pr) {
                    this.map.getMatrix()[i][j].setValue(Map.INTERES);
                } else {
                    this.map.getMatrix()[i][j].setValue(Map.REGULAR);
                }
            }
        }
    }

    //when user click in screen shot option
    public void screenshot(ActionEvent actionEvent) {
        if (this.mapGrid == null) return;
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(new Date()); //file name format - time stamp
        WritableImage snapshot = this.mapGrid.snapshot(null, null);
        File file = new File(timeStamp + ".png");
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
        } catch (IOException e) {
            System.err.println("Error while saving screen shot");
        }
    }
}
