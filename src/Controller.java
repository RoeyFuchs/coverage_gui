import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import static java.lang.System.exit;

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
        List<String> records;
        List<Point> pointList;

        try {
            records = Utiles.readPointsFromCSV(f);
            pointList = Utiles.convertStringToPoints(records);
            Point[][] mat = this.map.getMatrix();
            cleanMap(); //clean map so only border will left
            for (Point p : pointList) {
                mat[p.getX()][p.getY()].setValue(Map.INTERES);
            }
        } catch (Exception e) {
            AlertBox.display("Point file is incorrect");
        }

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
