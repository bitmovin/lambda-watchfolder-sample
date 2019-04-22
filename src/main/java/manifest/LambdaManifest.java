package manifest;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;


public class LambdaManifest
{

    public void handleRequest (InputStream inputStream, OutputStream outputStream, Context context) throws  IOException, ParseException {
        LambdaLogger logger = context.getLogger();
        logger.log("Loading lambda manifest function...");

        // Processing inputStream
        JSONObject bodyJson = getJsonObject(inputStream);

        String eventType = (String) bodyJson.get("eventType");
        logger.log("Event Status: \n");
        logger.log(eventType);

        JSONObject encodingJson = (JSONObject)bodyJson.get("encoding");
        String encodingId = (String) encodingJson.get("id");
        logger.log("encodingId: \n");
        logger.log(encodingId);

        // Check Bitmovin Encoding Status
        if (eventType.equals("ENCODING_FINISHED")) {

            logger.log("Starting Manifest ...");
            ManifestManager manifest = new ManifestManager();

            try {
                manifest.createAndRunDashManifest(encodingId);
            } catch (Exception e) {
                System.out.println("Exception raised by lambdaManifest");
                System.out.println(e.toString());
            }

            logger.log("Manifest Finished ...");

        } else {
            logger.log("Encoding Failed...");
        }

        // Response
        response(outputStream);

    }

    private JSONObject getJsonObject(InputStream inputStream) throws IOException, ParseException
    {
        JSONParser parser = new JSONParser();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        JSONObject bitmovinWebhookJson = (JSONObject)parser.parse(reader);
        return (JSONObject)parser.parse((String)bitmovinWebhookJson.get("body"));
    }

    private void response(OutputStream outputStream) throws IOException
    {
        String responseCode = "200";

        JSONObject responseBody = new JSONObject();
        responseBody.put("message", "test message");

        JSONObject headerJson = new JSONObject();
        headerJson.put("contentType", "application/json");

        JSONObject responseJson = new JSONObject();
        responseJson.put("isBase64Encoded", false);
        responseJson.put("statusCode", responseCode);
        responseJson.put("headers", headerJson);
        responseJson.put("body", responseBody.toString());

        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write(responseJson.toJSONString());
        writer.close();
    }

}
