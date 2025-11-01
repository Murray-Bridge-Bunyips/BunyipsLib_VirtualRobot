package au.edu.sa.mbhs.studentrobotics.deps;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Graphing {
    private static final double WINDOW_DURATION_SEC = 10.0;
    private static final double MIN_PERIOD_MS = 10.0;

    private static final long START_TIME = System.nanoTime();
    private static final Map<String, XYChart.Series<Number, Number>> seriesMap = new ConcurrentHashMap<>();
    private static final Map<String, ConcurrentLinkedQueue<DataPoint>> dataQueues = new ConcurrentHashMap<>();
    private static final Map<String, Long> lastAddTimeMap = new ConcurrentHashMap<>();
    private static LineChart<Number, Number> lineChart;
    private static NumberAxis xAxis;
    private static NumberAxis yAxis;
    private static boolean init;

    public static void plot(String channel, double value) {
        if (!init) {
            Platform.runLater(() -> {
                xAxis = new NumberAxis();
                yAxis = new NumberAxis();
                xAxis.setLabel("Time (s)");
                yAxis.setLabel("Value");

                xAxis.setAutoRanging(false);
                yAxis.setAutoRanging(false);
                System.err.close(); // if we graph excessively large data

                lineChart = new LineChart<>(xAxis, yAxis);
                lineChart.setAnimated(true);
                lineChart.setCreateSymbols(false);

                Stage stage = new Stage();
                stage.setScene(new Scene(lineChart, 800, 600));
                stage.show();

                AnimationTimer timer = new AnimationTimer() {
                    @Override
                    public void handle(long now) {
                        double currentTime = (System.nanoTime() - START_TIME) / 1_000_000_000.0;
                        if (lineChart == null) return;

                        Platform.runLater(() -> {
                            double minY = Double.MAX_VALUE;
                            double maxY = -Double.MAX_VALUE;

                            for (String channel : dataQueues.keySet()) {
                                XYChart.Series<Number, Number> series = seriesMap.computeIfAbsent(channel, k -> {
                                    XYChart.Series<Number, Number> s = new XYChart.Series<>();
                                    s.setName(channel);
                                    lineChart.getData().add(s);
                                    return s;
                                });

                                ConcurrentLinkedQueue<DataPoint> queue = dataQueues.get(channel);
                                DataPoint dp;
                                while ((dp = queue.poll()) != null) {
                                    series.getData().add(new XYChart.Data<>(dp.timeSec, dp.value));
                                }

                                series.getData().removeIf(p -> p.getXValue().doubleValue() < currentTime - WINDOW_DURATION_SEC);

                                for (XYChart.Data<Number, Number> p : series.getData()) {
                                    double y = p.getYValue().doubleValue();
                                    if (y < minY) minY = y;
                                    if (y > maxY) maxY = y;
                                }
                            }

                            xAxis.setLowerBound(Math.max(0, currentTime - WINDOW_DURATION_SEC));
                            xAxis.setUpperBound(currentTime);
                            if (minY != Double.MAX_VALUE && maxY != -Double.MAX_VALUE) {
                                yAxis.setLowerBound(minY);
                                yAxis.setUpperBound(maxY);
                            }
                        });
                    }
                };
                timer.start();
            });
        }
        init = true;

        long now = System.nanoTime();
        long last = lastAddTimeMap.getOrDefault(channel, 0L);
        long diffMs = (now - last) / 1_000_000;
        if (diffMs < MIN_PERIOD_MS) return;
        lastAddTimeMap.put(channel, now);

        double timeSec = (now - START_TIME) / 1_000_000_000.0;
        dataQueues.computeIfAbsent(channel, k -> new ConcurrentLinkedQueue<>())
                .add(new DataPoint(timeSec, value));
    }

    private record DataPoint(double timeSec, double value) {
    }
}
