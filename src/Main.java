import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;


public class Main extends Application {
    Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("view.fxml")
        );
        Pane pane = (Pane) loader.load();
        Controller controller = loader.<Controller>getController();
        primaryStage.setTitle("Coverage");
        primaryStage.setScene(new Scene(pane, 800, 600));
        primaryStage.show();
        this.primaryStage = primaryStage;

        List<String> parametres = getParameters().getRaw();
        if (parametres.size() > 0) {
            controller.loadMap(new File(parametres.get(0)));
            controller.loadInteresPoints(new File(parametres.get(1)));
            Integer i = 0;
            while (!controller.getBlockForward()) {
                controller.takeSS(parametres.get(2)+"\\"+i.toString());
                controller.mapForward(null);
                i++;
            }
            //the last one is aleardy blocked
            controller.takeSS(parametres.get(2)+"\\"+i.toString());
            controller.exitFunc();
        }


    }


    public static void main(String[] args) {
        launch(args);
    }
}
