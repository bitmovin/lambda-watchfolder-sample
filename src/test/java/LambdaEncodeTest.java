import com.bitmovin.api.exceptions.BitmovinApiException;
import com.bitmovin.api.http.RestException;
import com.mashape.unirest.http.exceptions.UnirestException;
import encode.EncoderManager;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class LambdaEncodeTest
{
    //@Test
    public void lambdaEncodeTest() throws UnirestException, IOException, BitmovinApiException, URISyntaxException, RestException, InterruptedException
    {
        System.out.println("LambdaEncode Test");
        // Replace example JSON object below with the JSON payload from AWS S3 create event
        JSONObject obj = new JSONObject("{\n" +
                "  \"Records\": [\n" +
                "    {\n" +
                "      \"eventVersion\": \"2.1\",\n" +
                "      \"eventSource\": \"aws:s3\",\n" +
                "      \"awsRegion\": \"eu-central-1\",\n" +
                "      \"eventTime\": \"2018-12-10T18:41:17.445Z\",\n" +
                "      \"eventName\": \"ObjectCreated:Put\",\n" +
                "      \"userIdentity\": {\n" +
                "        \"principalId\": \"AWS:AROAJ2ZKBVYFOJ5R4OAYU:rvecchione\"\n" +
                "      },\n" +
                "      \"requestParameters\": {\n" +
                "        \"sourceIPAddress\": \"71.229.130.169\"\n" +
                "      },\n" +
                "      \"responseElements\": {\n" +
                "        \"x-amz-request-id\": \"88363E1A62CF232D\",\n" +
                "        \"x-amz-id-2\": \"q8Q2t+CKc4QWA+S9qYB8r1K/Ij5NrwJvWTi8F/W/B+Qy5skAaCfGQocwN6TR3BG8cugk2D6m3tA=\"\n" +
                "      },\n" +
                "      \"s3\": {\n" +
                "        \"s3SchemaVersion\": \"1.0\",\n" +
                "        \"configurationId\": \"40da1803-5c88-49aa-9762-bd2497a0e1e0\",\n" +
                "        \"bucket\": {\n" +
                "          \"name\": \"dtc-wfa\",\n" +
                "          \"ownerIdentity\": {\n" +
                "            \"principalId\": \"AYIILULS2D9N0\"\n" +
                "          },\n" +
                "          \"arn\": \"arn:aws:s3:::dtc-wfb\"\n" +
                "        },\n" +
                "        \"object\": {\n" +
                "          \"key\": \"Fifa_30sec_1.mp4\",\n" +
                "          \"size\": 702,\n" +
                "          \"eTag\": \"67e2f84388336e55f061201283151469\",\n" +
                "          \"sequencer\": \"005C0EB34C3ECDD232\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}");
        JSONObject s3Json = obj.getJSONArray("Records").getJSONObject(0).getJSONObject("s3");
        String inputBucketName = s3Json.getJSONObject("bucket").getString("name");
        String inputFileName = s3Json.getJSONObject("object").getString("key");
        String awsRegion = obj.getJSONArray("Records").getJSONObject(0).getString("awsRegion");

        System.out.println(String.format("Following input Detected: Bucket: %s, File: %s, AWS Region: %s", inputBucketName, inputFileName, awsRegion));

        System.out.println("Encoding ...");
        EncoderManager encode = new EncoderManager();
        try
        {
            encode.createAndRunEncode(inputBucketName, inputFileName);

        }
        catch (Exception e)
        {
            System.out.println("Exception raised by EncoderManager");
        }
        System.out.println("Finish ...");
    }
}
