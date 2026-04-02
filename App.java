import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

import org.knowm.xchart.XYChart;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;


public class StockTickerApp {

    private static BigDecimal getStockPrice(String stockName) throws Exception {
        
        //the URL for the API stored as a string and adding the inputted stock into the endpoint
        String urlStr = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE" + "&symbol=" + stockName + "&apikey=P5VES4L2SPKGVVZP";

        //storing the api endpoint as an actual url to be used later
        URL apiEndpoint = new URL(urlStr);
        //creating the connection object and typecasting it to httpurl so http methods can be called on it
        HttpURLConnection connection = (HttpURLConnection)apiEndpoint.openConnection();
        //setting the api method to GET to grab the data from the api
        connection.setRequestMethod("GET");

        //creating a new buffered reader object to read the chars read/translated from bytes by the
        //input stream reader which is set to work on the connection's stream of bytes from the API
        BufferedReader buffReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        
        //string for a single line that gets read by the buffered reader
        String singleLine;
        StringBuilder wholeObject = new StringBuilder();

        //loop that runs while the buffered reader keeps receiving new lines
        while ((singleLine = buffReader.readLine()) != null){
            //creating a string builder version of the full data received from the API
            wholeObject.append(singleLine);
        }

        //closing the reader and connection
        buffReader.close();
        connection.disconnect();

        //turning the string builder into a full on json object
        JSONObject jsonFile = new JSONObject(wholeObject.toString());
        //retrieving the global quote dictionary from the full json file
        JSONObject globalQuote = jsonFile.getJSONObject("Global Quote");
        //retrieving the stock's price from the global quote object
        String stockPrice = globalQuote.getString("05. price");

        //returning the BigDecimal version of the stock price so that it doesn't get rounded
        return new BigDecimal(stockPrice);
    }

    public static void main(String[] args) {

        //Stock symbol for the Dow Jones Industrial Average
        String symbol = "DJI";

        //Wait time in milliseconds between queries
        int waitTimeMs = 5000;

        //Queue for containing timestamps and stock price
        Queue<ArrayList<Object>> stockDataQueue = new LinkedList<>();

        //This is the loop for querying data
        while (true) {

            //Try to query the stock information
            try {
                //get the current stock price
                BigDecimal price = StockTickerApp.getStockPrice(symbol);
                // Record the timestamp for the query
                Date timestamp = new Date();

                //Add the stockData to the queue, in the form (timestamp, price)
                ArrayList<Object> stockData = new ArrayList<Object>();
                stockData.add(timestamp);
                stockData.add(price);
                stockDataQueue.add(stockData);

                //runs every 20 data points
                if (stockDataQueue.size() % 20 == 0) { 
                    //creating array lists for the x and y axis
                    java.util.List<Date> xData = new java.util.ArrayList<>();
                    java.util.List<Double> yData = new java.util.ArrayList<>();
                    
                    //iterating through the queue holding the stock data
                    for (ArrayList<Object> data : stockDataQueue) {
                        //adding the date into the xdata list
                        xData.add((Date)data.get(0));
                        //adding the stock price into the ydata list
                        yData.add(((BigDecimal)data.get(1)).doubleValue());
                    }
                    
                    //creating the chart displaying the stock data
                    XYChart chart = QuickChart.getChart(
                            (symbol + " Prices"), "Time", "Price", symbol,
                            xData, yData
                    );
                
                    //opening a new window with the chart
                    new SwingWrapper<>(chart).displayChart(symbol + " Live Chart");
                }
            }

            //Catch exception if there is a connection or JSON parsing error
            catch(Exception e) {
                System.out.println("Failure to connect or parse JSON. Trying again.");
            }

            //Wait before repeating the query
            try {
                Thread.sleep(waitTimeMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

