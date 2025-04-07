import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.client.RestClient;
import org.apache.http.Header;
import org.apache.http.HttpHost;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import java.util.logging.Logger;
import java.util.logging.Level;

import static org.apache.commons.text.StringEscapeUtils.escapeHtml4;
import static spark.Spark.get;
import static spark.Spark.port;

public class MySpark {

    private static final Logger logger = Logger.getLogger(MySpark.class.getName());

    static String index_name = "hotel";
    static String serverUrl;

    static {
        // Determine the Elasticsearch server URL
        if (isRunningInContainer()) {
            serverUrl = System.getenv().getOrDefault("ELASTICSEARCH_URL", "http://host.docker.internal:9200");
            logger.info("Running inside a container. Using server URL: " + serverUrl);
        } else {
            serverUrl = System.getenv().getOrDefault("ELASTICSEARCH_URL", "http://localhost:9200");
            logger.info("Running outside a container. Using server URL: " + serverUrl);
        }

        // Load java.util.logging configuration
        try (InputStream configFile = MySpark.class.getResourceAsStream("/logging.properties")) {
            java.util.logging.LogManager.getLogManager().readConfiguration(configFile);
        } catch (IOException e) {
            System.err.println("Could not load logging configuration: " + e.getMessage());
        }
        logger.info("Attempting to connect to Elasticsearch at: " + serverUrl);
    }

    static RestClient restClient = RestClient
            .builder(HttpHost.create(serverUrl))
            .setDefaultHeaders(new Header[]{})
            .build();

    // Create the transport with a Jackson mapper
    static ElasticsearchTransport transport = new RestClientTransport(
            restClient, new JacksonJsonpMapper());


    // And create the API client
    static ElasticsearchClient esClient = new ElasticsearchClient(transport);

    public static void main(String[] args)  {
        logger.info("Starting application...");
        logger.info("Elasticsearch server URL: " + serverUrl); // Explicitly log the server URL

        // Set the port for Spark
        int sparkPort = Integer.parseInt(System.getenv().getOrDefault("SPARK_PORT", "4567"));
        port(sparkPort);
        logger.info("Spark server running on port: " + sparkPort);

        openWebpage("http://localhost:" + sparkPort + "/readme");
        get("/readme", (req, res) -> readme());

        get("/create/:interval/:how_many", (request, response) -> create(request.params("interval"), request.params("how_many")));

        get("/bulk/:interval/:how_many", (request, response) -> bulk(request.params("interval"), request.params("how_many")));

    }


    public static void openWebpage(String urlString) {
        try {
            URI uri = new URI(urlString);

            // Check if Desktop is supported
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();

                // Check if BROWSE action is supported
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(uri);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to open webpage: " + urlString, e);
        }
    }
    public static String create(String interval, String numbers) throws Exception {

        logger.info("Creating data with interval: " + interval + " and numbers: " + numbers);

        StringBuilder sb = new StringBuilder();

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        
        List<Hotel> hotels = MySpark.getHotels(interval, numbers);
        
        int i = 1;
        for (Hotel hotel : hotels) {
            String header = "{\"index\":{\"_index\":\"" + index_name + "\",\"_id\":" + i +"}} " + "\n";
            sb.append(header);
            String json = mapper.writeValueAsString(hotel);
            sb.append(json).append("\n");
            i++;
        }
        
        String home = System.getProperty("user.home");
        File fileName = new File(home + "/" + "data" + ".json");
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(sb.toString());

        writer.close();

        String response;
        response = "data.json is saved : " + fileName + "<br>" +
                "<br>" +
                "you can invoke bulk as below<br>" +
                "curl  -H 'Content-Type: application/x-ndjson' -XPOST 'localhost:9200/_bulk?pretty' --data-binary @" + fileName;
        return response;

    }


    public static String readme() throws Exception {
        StringBuilder sb = new StringBuilder();
        String resource_path = "/readme.txt";
        InputStream in = MySpark.class.getResourceAsStream(resource_path);
        if ( in == null )
            throw new Exception("resource not found: " + resource_path);

        Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);

        BufferedReader bufferedReader = new BufferedReader(reader);

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(escapeHtml4(line)).append("<br>");
        }

        return sb.toString();
    }


    private static List<Hotel> getHotels(String interval, String numbers) {
        int number = Integer.parseInt(numbers);
        
//        Helper.INTERVAL inter = null;
        Helper.INTERVAL inter = null;
        switch(interval) {
            case "s" :
                inter = Helper.INTERVAL.SECONDLY;
                break;

            case "m" :
                inter = Helper.INTERVAL.MINUTELY;
                break;

            case "h" :
                inter = Helper.INTERVAL.HOURLY;
                break;

            case "d" :
                inter = Helper.INTERVAL.DAILY;
                break;
        }

        return Hotel.make_hotels(number, inter);

    }

    public static String bulk(String interval, String numbers) throws Exception {
        
        logger.info("Performing bulk operation with interval: " + interval + " and numbers: " + numbers);

        List<Hotel> hotels = MySpark.getHotels(interval, numbers);
        BulkRequest.Builder br = new BulkRequest.Builder();

        for (Hotel hotel : hotels) {
            br.operations(op -> op
                    .index(idx -> idx
                            .index(index_name)
                            .id(hotel.getHotel_name())
                            .document(hotel)
                    )
            );
            
        }

        BulkResponse result = esClient.bulk(br.build());

        if (result.errors()) {
            logger.severe("Bulk had errors");
            for (BulkResponseItem item: result.items()) {
                if (item.error() != null) {
                    logger.severe(item.error().reason());
                }
            }
        }
        return "done";
    }

    private static boolean isRunningInContainer() {
        File cgroupFile = new File("/proc/1/cgroup");
        if (cgroupFile.exists()) {
            logger.info("Detected container environment based on the existence of /proc/1/cgroup.");
            return true;
        }
        logger.info("No container environment detected.");
        return false;
    }
}