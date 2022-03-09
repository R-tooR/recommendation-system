import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import properties.PropertiesNames;
import updater.DatabaseUpdater;
import recommendation.Recommender;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Properties;

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



    public static void main(String[] args) throws IOException {
        ParametersResolver resolver = new ParametersResolver(args);
        Properties appProperties = new Properties();
        appProperties.load(new InputStreamReader(new FileInputStream(resolver.paramsMap().get(ParametersResolver.applicationConfig()).get())));
        String hostname = "127.0.0.1";
        int port = Integer.parseInt(appProperties.getProperty(PropertiesNames.appPort(), "6666"));
        DatabaseUpdater updater = null;
        ObjectMapper mapper = new ObjectMapper();
        Recommender recommender = new Recommender(Integer.parseInt((String)appProperties.computeIfAbsent(PropertiesNames.targetInvestor(), x -> 0)), appProperties);
        boolean isUpdaterEnabled = Boolean.parseBoolean((String)appProperties.computeIfAbsent(PropertiesNames.updaterEnabled(), x -> false));
        if(isUpdaterEnabled) {
            updater = new DatabaseUpdater(Double.parseDouble((String)appProperties.computeIfAbsent(PropertiesNames.updaterChangeRatio(), x -> 0.1)));
            updater.initialize();
        }
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
                if (isUpdaterEnabled) updater.update();
            }

        } catch (UnknownHostException ex) {

            System.out.println("Server not found: " + ex.getMessage());

        } catch (IOException ex) {

            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}
