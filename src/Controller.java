import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
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
    Thread mapChaingingThread;
    BooleanProperty blockPointsAdd = new SimpleBooleanProperty();
    BooleanProperty blockBack = new SimpleBooleanProperty();
    BooleanProperty blockForwerd = new SimpleBooleanProperty();
    BooleanProperty blockPlay = new SimpleBooleanProperty();


    public boolean isBlockBack() {
        return blockBack.get();
    }

    public BooleanProperty blockBackProperty() {
        return blockBack;
    }

    public void setBlockBack(boolean blockBack) {
        this.blockBack.set(blockBack);
    }

    public boolean isBlockForwerd() {
        return blockForwerd.get();
    }

    public BooleanProperty blockForwerdProperty() {
        return blockForwerd;
    }

    public void setBlockForwerd(boolean blockForwerd) {
        this.blockForwerd.set(blockForwerd);
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

    public File getFile(String title) {
        Window theStage = mainGrid.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        return fileChooser.showOpenDialog(theStage);
    }


    public void loadMapFile(ActionEvent actionEvent) {
        File f = this.getFile("Open Map file");
        if (f == null) return; //if user didn't choose file
        this.clearThread();
        try {
            this.map = Map.CreateMap(f);
            GridPane newMapGrid = this.createMapGrid(this.map);
            setMapGrid(newMapGrid);
            this.blockPointsAdd.set(false);
        } catch (Exception e) {
            AlertBox.display("can't open map file");
        }
    }

    private void setMapGrid(GridPane grid) {
        GridPane.setConstraints(grid, 0,1);
        GridPane.setColumnSpan(grid, GridPane.REMAINING);
        if (mapGrid != null) {
            mainGrid.getChildren().remove(this.mapGrid);
        }
        this.mainGrid.getChildren().add(grid);
        this.mapGrid = grid;
    }

    private void clearThread() {
        if(this.mapChaingingThread == null) return;
        this.mapChaingingThread.interrupt();
        this.mapChaingingThread = null;
    }

    public void exitFunc() {
        exit(0);
    }

    private GridPane createMapGrid(Map inMap) {
        int pad = 2;
        int menuBarSize = 25;
        GridPane mapGrid = new GridPane();
        int rows = inMap.getRowsNumber();
        int columns = inMap.getColumnsNumber();
        mapGrid.setGridLinesVisible(true);
        Scene scene = this.mainGrid.getScene();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                Rectangle rec = new ResizableRectangle();
                //width
                rec.widthProperty().bind(scene.widthProperty().divide(columns).add(-2 * pad));
                //height
                rec.heightProperty().bind(scene.heightProperty().add(-menuBarSize).divide(rows).add(-2 * pad));
                Point p = inMap.getMatrix()[i][j];

                rec.fillProperty().bind(Bindings.createObjectBinding(() -> IntToColor.getColor(p.valueProperty().get()), p.valueProperty()));

                GridPane.setConstraints(rec, j, i);
                GridPane.setMargin(rec, new Insets(pad, pad, pad, pad));
                mapGrid.getChildren().add(rec);

            }
        }
        GridPane.setConstraints(mapGrid, 0, 1);
        return mapGrid;
    }

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

            cleanMap();

            for (Point p : pointList) {
                mat[p.getX()][p.getY()].setValue(Map.INTERES);
            }
        } catch (Exception e) {
            AlertBox.display("Point file is incorrect");
        }

    }


    public void loadMapLog(ActionEvent actionEvent) throws Exception {
        File f = getFile("Open Map Log file");
        if (f == null) return; //if user didn't choose file

        this.clearThread();

        BufferedReader reader = new BufferedReader(new FileReader(f));

        String line = reader.readLine();

        List<Map> maplist = new LinkedList<>();


        while(line!= null) {
            List<String> list = new LinkedList<>();
            while((line= reader.readLine()) !=null && !line.equals("")) {
                list.add(line);
            }
            maplist.add(Map.CreateMapFromLog(list));

        }
        this.mapslog = maplist;
        if(this.mapslog.size() > 0) {
            this.blockForwerd.set(false);
            this.blockPlay.set(false);
            this.changeMap(0);
        }

    }

    private synchronized void changeMap(int n) {
        if(n >= this.mapslog.size() ) return;
        this.setMapGrid(createMapGrid(this.mapslog.get(n)));
        this.currentMap = n;
        if (this.mapslog.size()-1 == n) {
            this.blockForwerd.set(true);
        } else { this.blockForwerd.set(false); }
        if (n == 0) {
            this.blockBack.set(true);
        } else { this.blockBack.set(false); }
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
        this.blockBack.set(true);
        this.blockForwerd.set(true);
        this.blockPlay.set(true);
    }

    public void mapBack(ActionEvent actionEvent) {
        this.changeMap(this.currentMap-1);

    }

    public void mapforwerd(ActionEvent actionEvent) {
        this.changeMap(this.currentMap+1);
    }

    public void mapPlay(ActionEvent actionEvent) {
        if(this.mapChaingingThread == null) {
            Thread t = new Thread(() -> {
                while (this.currentMap < this.mapslog.size()) {
                    Platform.runLater(() -> mapforwerd(actionEvent));
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                this.mapChaingingThread = null;
            });
            this.mapChaingingThread = t;
            t.start();
        } else { //stop the map change
            this.mapChaingingThread.interrupt();
        }
    }
}
