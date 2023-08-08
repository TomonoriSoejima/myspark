import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
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
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.apache.commons.text.StringEscapeUtils.escapeHtml4;
import static spark.Spark.get;

public class MySpark {


    static String index_name = "hotel";
    static String serverUrl = "http://localhost:9200";
    static RestClient restClient = RestClient
            .builder(HttpHost.create(serverUrl))
            .setDefaultHeaders(new Header[]{
            })
            .build();

    // Create the transport with a Jackson mapper
    static ElasticsearchTransport transport = new RestClientTransport(
            restClient, new JacksonJsonpMapper());



    // And create the API client
    static ElasticsearchClient esClient = new ElasticsearchClient(transport);


    public static void main(String[] args) throws Exception {


        create("s","5");

        Desktop desktop = Desktop.getDesktop();
        desktop.browse(new URL("http://localhost:4567/readme").toURI());

        get("/readme", (req, res) -> readme());

        get("/create/:interval/:how_many", (request, response) -> create(request.params("interval"), request.params("how_many")));

        get("/bulk/:interval/:how_many", (request, response) -> bulk(request.params("interval"), request.params("how_many")));

    }


    public static String create(String interval, String numbers) throws Exception {

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
        File fileName = new File(home + "/Downloads/" + "data" + ".json");
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(sb.toString());

        writer.close();

        String response;
        response = "data.json is saved : " + fileName.toString() + "<br>" +
                "<br>" +
                "you can invoke bulk as below<br>" +
                "curl  -H 'Content-Type: application/x-ndjson' -XPOST 'localhost:9200/_bulk?pretty' --data-binary @" + fileName.toString();
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
        return "done";
    }
}