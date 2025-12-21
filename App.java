package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        // 1. Create the Axes for the Chart
        final CategoryAxis xAxis = new CategoryAxis(); // Time (X-axis)
        final NumberAxis yAxis = new NumberAxis();     // Price (Y-axis)
        xAxis.setLabel("Time");
        yAxis.setLabel("Stock Price ($)");

        // 2. Create the Line Chart
        final LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Dow Jones Industrial Average (^DJI)");
        
        // 3. Create a Data Series to hold the points
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Live Data");
        lineChart.getData().add(series);

        // 4. Setup the Scene (The Window)
        Scene scene = new Scene(lineChart, 800, 600);
        stage.setScene(scene);
        stage.show();

        // 5. Start the Background Loop to fetch data
        startDataFetcher(series);
    }

    private void startDataFetcher(XYChart.Series<String, Number> series) {
        // We run this in a separate thread so the Window doesn't freeze
        Thread dataThread = new Thread(() -> {
            while (true) {
                try {
                    // Fetch Data
                    Stock dowJones = YahooFinance.get("^DJI");
                    
                    if (dowJones != null && dowJones.getQuote() != null) {
                        BigDecimal price = dowJones.getQuote().getPrice();
                        
                        // Get current time as a string (e.g., "10:30:05")
                        String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());

                        // UPDATE THE UI (Must happen on the JavaFX Thread)
                        Platform.runLater(() -> {
                            series.getData().add(new XYChart.Data<>(timeStamp, price));
                        });
                    }

                    // Wait 5 seconds
                    Thread.sleep(5000);

                } catch (IOException | InterruptedException e) {
                    System.out.println("Error fetching data: " + e.getMessage());
                }
            }
        });
        
        // Ensure the thread stops when we close the window
        dataThread.setDaemon(true);
        dataThread.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}