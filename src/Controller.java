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

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static java.lang.System.exit;

public class Controller implements Initializable {
    public GridPane mainGrid;
    Map map;
    BooleanProperty blockPointsAdd = new SimpleBooleanProperty();


    StringProperty mapPath = new SimpleStringProperty();


    public boolean getBlockPointsAdd() {
        return blockPointsAdd.get();
    }

    public BooleanProperty blockPointsAddProperty() {
        return blockPointsAdd;
    }

    public void setBlockPointsAdd(boolean blockPointsAdd) {
        this.blockPointsAdd.set(blockPointsAdd);
    }


    public void loadMapFile(ActionEvent actionEvent) {
        Window theStage = mainGrid.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Map file");
        File f = fileChooser.showOpenDialog(theStage);

        if (f == null) return; //if user didn't choose file
        try {
            this.map = Map.CreateMap(f);
            this.createMapGrid();
            this.blockPointsAdd.set(false);
        } catch (Exception e) {
            AlertBox.display("can't open map file");
        }
    }

    public void exitFunc() {
        exit(0);
    }

    private void createMapGrid() {
        int pad = 2;
        int menuBarSize = 25;
        GridPane mapGrid = new GridPane();
        int rows = map.getRowsNumber();
        int columns = map.getColumnsNumber();
        mapGrid.setGridLinesVisible(true);
        Scene scene = this.mainGrid.getScene();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                Rectangle rec = new ResizableRectangle();
                //width
                rec.widthProperty().bind(scene.widthProperty().divide(columns).add(-2 * pad));
                //height
                rec.heightProperty().bind(scene.heightProperty().add(-menuBarSize).divide(rows).add(-2 * pad));
                Point p = map.getMatrix()[i][j];

                rec.fillProperty().bind(Bindings.createObjectBinding(() -> IntToColor.getColor(p.valueProperty().get()), p.valueProperty()));

                GridPane.setConstraints(rec, j, i);
                GridPane.setMargin(rec, new Insets(pad, pad, pad, pad));
                mapGrid.getChildren().add(rec);

            }
        }
        GridPane.setConstraints(mapGrid, 0, 1);
        this.mainGrid.getChildren().add(mapGrid);
    }

    public void loadInteresPoints(ActionEvent actionEvent) {
        Window theStage = mainGrid.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Points file");
        File f = fileChooser.showOpenDialog(theStage);

        if (f==null) return; //if user didn't choose file

        if(this.map == null) {
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
    }
}
