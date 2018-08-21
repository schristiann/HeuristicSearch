package app;

/*
 * Sam Christian and Brad Mitchell
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import view.GridController;

import java.io.IOException;

public class GridTest extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(
                getClass().getResource("/view/grid.fxml"));
        AnchorPane root = loader.load();
        GridController gridController = loader.getController();
        //sets the size of the grid
        gridController.makeGrid(101);

        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Grid Test");
        primaryStage.show();
    }


}