package encode;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.json.JSONObject;

import java.util.Map;

public class LambdaEncode
{

    public String handleRequest(Map<String, Object> input, Context context)
    {
        LambdaLogger logger = context.getLogger();
        logger.log("Loading lambda function...");

        // Process Input
        context.getLogger().log("Input: " + input);

        JSONObject inputJson = new JSONObject(input);

        JSONObject s3Json = inputJson.getJSONArray("Records").getJSONObject(0).getJSONObject("s3");
        String inputBucketName = s3Json.getJSONObject("bucket").getString("name");
        String inputFileName = s3Json.getJSONObject("object").getString("key");
        String awsRegion = inputJson.getJSONArray("Records").getJSONObject(0).getString("awsRegion");

        logger.log(String.format("New input upload detected: Bucket: %s, File: %s, AWS Region: %s", inputBucketName, inputFileName, awsRegion));

        // Create and run encode job
        logger.log("Initiating Encode ...");
        EncoderManager encode = new EncoderManager();
        try
        {
            encode.createAndRunEncode(inputBucketName, inputFileName);
        }
        catch (Exception e)
        {
            logger.log("Exception raised by EncoderManager");
        }
        logger.log("Lambda Finish ...");

        return "{\"statusCode\": 200}";
    }

}
