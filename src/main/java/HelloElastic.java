import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;
import static spark.Spark.get;

public class HelloElastic {


    static final String index_name = "hotel";


    static final RestHighLevelClient client = new RestHighLevelClient(
            RestClient.builder(
                    new HttpHost("localhost", 9200, "http"),
                    new HttpHost("localhost", 9201, "http"),
                    new HttpHost("localhost", 9202, "http"),
                    new HttpHost("localhost", 9203, "http")));



    public static void main(String[] args) throws Exception {




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


        List<Hotel> list = HelloElastic.getHotels(interval, numbers);


        int i = 1;
        for (Hotel hotel : list) {
            String header = "{\"index\":{\"_index\":\"" + index_name + "\",\"_id\":" + i +"}} " + "\n";
            sb.append(header);
            String json = mapper.writeValueAsString(hotel);
            sb.append(json + "\n");
            i++;
        }


        String home = System.getProperty("user.home");
        File fileName = new File(home+"/Downloads/" + "data" + ".json");
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(sb.toString());

        writer.close();

        StringBuilder response = new StringBuilder();
        response.append("data.json is saved : " + fileName.toString() + "<br>");
        response.append("<br>");
        response.append("you can invoke bulk as below<br>");
        response.append("curl  -H 'Content-Type: application/x-ndjson' -XPOST 'localhost:9200/_bulk?pretty' --data-binary @" + fileName.toString());
        return response.toString();

    }


    public static String readme() throws Exception {
        StringBuilder sb = new StringBuilder();
        String resource_path = "/readme.txt";
        InputStream in = HelloElastic.class.getResourceAsStream(resource_path);
        if ( in == null )
            throw new Exception("resource not found: " + resource_path);

        Reader reader = new InputStreamReader(in, "utf-8");

        BufferedReader bufferedReader = new BufferedReader(reader);


        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(escapeHtml4(line ) + "<br>");
        }

        return sb.toString();
    }


    private static List<Hotel> getHotels(String interval, String numbers) {
        int number = Integer.parseInt(numbers);


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
            default : // Optional
                inter = Helper.INTERVAL.SECONDLY;
        }

        List<Hotel> list = Hotel.make_hotels(number, inter);
        return list;

    }

    public static String bulk(String interval, String numbers) throws Exception {


        Header header = new BasicHeader("a", "a");


        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);


        int i = 1;
        List<Hotel> list = HelloElastic.getHotels(interval, numbers);

        try {
            BulkRequest bulkRequest = new BulkRequest();
            for (Hotel hotel : list) {
                Map<String, Object> map = mapper.convertValue(hotel, Map.class);

                String id_str = Integer.toString(i);
                IndexRequest request = new IndexRequest(index_name, id_str)
                        .source(map);

                bulkRequest.add(request);
                i++;

            }


            BulkResponse bulkResponse = client.bulk(bulkRequest, header);


            for (BulkItemResponse bulkItemResponse : bulkResponse) {
                DocWriteResponse itemResponse = bulkItemResponse.getResponse();

                if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.INDEX
                        || bulkItemResponse.getOpType() == DocWriteRequest.OpType.CREATE) {
                    IndexResponse indexResponse = (IndexResponse) itemResponse;
                }

            }


        } catch (Exception e)
        {
            return "error";
        }

        return "done";
    }
}