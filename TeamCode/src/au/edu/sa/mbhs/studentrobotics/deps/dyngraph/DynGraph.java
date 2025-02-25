package au.edu.sa.mbhs.studentrobotics.deps.dyngraph;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DynGraph {
    private static final XYChart.Series<Number, Number> series = new XYChart.Series<>();
    private static final Queue<XYChart.Data<Number, Number>> dataQueue = new LinkedList<>();
    private static int xValue = 0;

    public static void open() {
        Platform.runLater(() -> {
            Stage stage = new Stage();
            stage.setTitle("DynGraph");

            final NumberAxis xAxis = new NumberAxis();
            final NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("X Axis");
            yAxis.setLabel("Y Axis");

            final LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
            lineChart.setTitle("DynGraph");
            series.setName("Data Series");

            lineChart.getData().add(series);

            Scene scene = new Scene(lineChart, 800, 600);
            stage.setScene(scene);
            stage.show();
        });

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> Platform.runLater(() -> {
            synchronized (dataQueue) {
                series.getData().addAll(dataQueue);
            }
        }), 0, 100, TimeUnit.MILLISECONDS);
    }

    public static void addDataPoint(double functionYValue) {
        synchronized (dataQueue) {
            dataQueue.add(new XYChart.Data<>(xValue++, functionYValue));
            if (dataQueue.size() > 100) {
                dataQueue.poll();
            }
        }
    }
}