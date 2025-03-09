package virtual_robot.controller;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Threads;
import com.qualcomm.robotcore.util.ThreadPool;
import dev.frozenmilk.sinister.SinisterImpl;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.concurrent.Future;

/**
 * For internal use only. Main class for the JavaFX application.
 */
public class VirtualRobotApplication extends Application {

    private static VirtualRobotController controllerHandle;
    public static Future<?> sinisterOperation;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("virtual_robot.fxml"));
        Parent root = (BorderPane)loader.load();
        controllerHandle = loader.getController();
        primaryStage.setTitle("Virtual Robot");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.setOnShowing(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                controllerHandle.setConfig(null);
            }
        });
        primaryStage.setOnCloseRequest((v) -> {
            Threads.stopAll();
            ThreadPool.getDefault().shutdownNow();
            System.exit(0);
        });
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (controllerHandle.executorService != null && !controllerHandle.executorService.isShutdown()) {
            controllerHandle.executorService.shutdownNow();
        }
        if (controllerHandle.gamePadExecutorService != null && !controllerHandle.gamePadExecutorService.isShutdown()) {
            controllerHandle.gamePadExecutorService.shutdownNow();
        }
        controllerHandle.gamePadHelper.quit();
    }

    public static VirtualRobotController getControllerHandle(){return controllerHandle;}


    public static void main(String[] args) {
        sinisterOperation = ThreadPool.getDefault().submit(SinisterImpl::doSinisterThings); // hacky classpath scanning
        launch(args);
    }
}
