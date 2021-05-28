import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import moveItToDataServer.DatabaseUpdater;
import recommendation.Recommender;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

public class Client {
//    val extractor = new DataExtractor(new Properties())
//
//    val df = extractor.get(new GetInvestorsQuery(0))
//    val target = extractor.get(new GetTargetInvestorQuery(0))
//    println(df.show(50))
//
//    val data = new InvestorsDataProcessor().get(df.limit(50), target) map collection2DToRealMatrix
//
//    val e = new recommendation.Engine
//    val r = e.getTopNEmbeddings(e.calculateConsensusEmbedding(e.createConsensusEmbedding(data)), data)



    public static void main(String[] args) {
        String hostname = "127.0.0.1";
        int port = Integer.parseInt("6666");
        ObjectMapper mapper = new ObjectMapper();
        Recommender recommender = new Recommender(0);
        DatabaseUpdater updater = new DatabaseUpdater(0.1);
        updater.initialize();
        TypeReference<HashMap<String, Object>> typeRef
                = new TypeReference<HashMap<String, Object>>() {};
        try (Socket socket = new Socket(hostname, port)) {

            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            while(true) {
                String time = reader.readLine();
                System.out.println(mapper.readValue(time, typeRef));
                var x = recommender.step(mapper.readValue(time, typeRef));
//                System.out.println(time);
                System.out.println("Result is: " + x);
                updater.update();
            }

        } catch (UnknownHostException ex) {

            System.out.println("Server not found: " + ex.getMessage());

        } catch (IOException ex) {

            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}
