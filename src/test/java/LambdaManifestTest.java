import com.bitmovin.api.exceptions.BitmovinApiException;
import com.bitmovin.api.http.RestException;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;

import static manifest.ManifestManager.createAndRunDashManifest;

public class LambdaManifestTest
{
    //@Test
    public void lambdaPackageTest() throws UnirestException, IOException, BitmovinApiException, URISyntaxException, RestException, InterruptedException, ParseException
    {
        // Replace example JSON object below with the JSON payload from Bitmovin webhook notification
        System.out.println("LambdaManifest Test");
        String inputTextJson =
                "{\n" +
                        "    \"resource\": \"/watchfolder-dtc-webhook\",\n" +
                        "    \"path\": \"/watchfolder-dtc-webhook\",\n" +
                        "    \"httpMethod\": \"POST\",\n" +
                        "    \"headers\": {\n" +
                        "        \"Accept\": \"text/plain, application/json, application/*+json, */*\",\n" +
                        "        \"Accept-Charset\": \"big5, big5-hkscs, cesu-8, euc-jp, euc-kr, gb18030, gb2312, gbk, ibm-thai, ibm00858, ibm01140, ibm01141, ibm01142, ibm01143, ibm01144, ibm01145, ibm01146, ibm01147, ibm01148, ibm01149, ibm037, ibm1026, ibm1047, ibm273, ibm277, ibm278, ibm280, ibm284, ibm285, ibm290, ibm297, ibm420, ibm424, ibm437, ibm500, ibm775, ibm850, ibm852, ibm855, ibm857, ibm860, ibm861, ibm862, ibm863, ibm864, ibm865, ibm866, ibm868, ibm869, ibm870, ibm871, ibm918, iso-2022-cn, iso-2022-jp, iso-2022-jp-2, iso-2022-kr, iso-8859-1, iso-8859-13, iso-8859-15, iso-8859-2, iso-8859-3, iso-8859-4, iso-8859-5, iso-8859-6, iso-8859-7, iso-8859-8, iso-8859-9, jis_x0201, jis_x0212-1990, koi8-r, koi8-u, shift_jis, tis-620, us-ascii, utf-16, utf-16be, utf-16le, utf-32, utf-32be, utf-32le, utf-8, windows-1250, windows-1251, windows-1252, windows-1253, windows-1254, windows-1255, windows-1256, windows-1257, windows-1258, windows-31j, x-big5-hkscs-2001, x-big5-solaris, x-compound_text, x-euc-jp-linux, x-euc-tw, x-eucjp-open, x-ibm1006, x-ibm1025, x-ibm1046, x-ibm1097, x-ibm1098, x-ibm1112, x-ibm1122, x-ibm1123, x-ibm1124, x-ibm1166, x-ibm1364, x-ibm1381, x-ibm1383, x-ibm300, x-ibm33722, x-ibm737, x-ibm833, x-ibm834, x-ibm856, x-ibm874, x-ibm875, x-ibm921, x-ibm922, x-ibm930, x-ibm933, x-ibm935, x-ibm937, x-ibm939, x-ibm942, x-ibm942c, x-ibm943, x-ibm943c, x-ibm948, x-ibm949, x-ibm949c, x-ibm950, x-ibm964, x-ibm970, x-iscii91, x-iso-2022-cn-cns, x-iso-2022-cn-gb, x-iso-8859-11, x-jis0208, x-jisautodetect, x-johab, x-macarabic, x-maccentraleurope, x-maccroatian, x-maccyrillic, x-macdingbat, x-macgreek, x-machebrew, x-maciceland, x-macroman, x-macromania, x-macsymbol, x-macthai, x-macturkish, x-macukraine, x-ms932_0213, x-ms950-hkscs, x-ms950-hkscs-xp, x-mswin-936, x-pck, x-sjis_0213, x-utf-16le-bom, x-utf-32be-bom, x-utf-32le-bom, x-windows-50220, x-windows-50221, x-windows-874, x-windows-949, x-windows-950, x-windows-iso2022jp\",\n" +
                        "        \"Content-Type\": \"application/json;charset=UTF-8\",\n" +
                        "        \"Host\": \"l5lntvnxj7.execute-api.eu-central-1.amazonaws.com\",\n" +
                        "        \"User-Agent\": \"BitWebHook/1.0\",\n" +
                        "        \"X-Amzn-Trace-Id\": \"Root=1-5c180261-bf18352a3cf4d032387bb02c\",\n" +
                        "        \"X-Forwarded-For\": \"xx.xx.xx.xx\",\n" +
                        "        \"X-Forwarded-Port\": \"443\",\n" +
                        "        \"X-Forwarded-Proto\": \"https\"\n" +
                        "    },\n" +
                        "    \"multiValueHeaders\": {\n" +
                        "        \"Accept\": [\n" +
                        "            \"text/plain, application/json, application/*+json, */*\"\n" +
                        "        ],\n" +
                        "        \"Accept-Charset\": [\n" +
                        "            \"big5, big5-hkscs, cesu-8, euc-jp, euc-kr, gb18030, gb2312, gbk, ibm-thai, ibm00858, ibm01140, ibm01141, ibm01142, ibm01143, ibm01144, ibm01145, ibm01146, ibm01147, ibm01148, ibm01149, ibm037, ibm1026, ibm1047, ibm273, ibm277, ibm278, ibm280, ibm284, ibm285, ibm290, ibm297, ibm420, ibm424, ibm437, ibm500, ibm775, ibm850, ibm852, ibm855, ibm857, ibm860, ibm861, ibm862, ibm863, ibm864, ibm865, ibm866, ibm868, ibm869, ibm870, ibm871, ibm918, iso-2022-cn, iso-2022-jp, iso-2022-jp-2, iso-2022-kr, iso-8859-1, iso-8859-13, iso-8859-15, iso-8859-2, iso-8859-3, iso-8859-4, iso-8859-5, iso-8859-6, iso-8859-7, iso-8859-8, iso-8859-9, jis_x0201, jis_x0212-1990, koi8-r, koi8-u, shift_jis, tis-620, us-ascii, utf-16, utf-16be, utf-16le, utf-32, utf-32be, utf-32le, utf-8, windows-1250, windows-1251, windows-1252, windows-1253, windows-1254, windows-1255, windows-1256, windows-1257, windows-1258, windows-31j, x-big5-hkscs-2001, x-big5-solaris, x-compound_text, x-euc-jp-linux, x-euc-tw, x-eucjp-open, x-ibm1006, x-ibm1025, x-ibm1046, x-ibm1097, x-ibm1098, x-ibm1112, x-ibm1122, x-ibm1123, x-ibm1124, x-ibm1166, x-ibm1364, x-ibm1381, x-ibm1383, x-ibm300, x-ibm33722, x-ibm737, x-ibm833, x-ibm834, x-ibm856, x-ibm874, x-ibm875, x-ibm921, x-ibm922, x-ibm930, x-ibm933, x-ibm935, x-ibm937, x-ibm939, x-ibm942, x-ibm942c, x-ibm943, x-ibm943c, x-ibm948, x-ibm949, x-ibm949c, x-ibm950, x-ibm964, x-ibm970, x-iscii91, x-iso-2022-cn-cns, x-iso-2022-cn-gb, x-iso-8859-11, x-jis0208, x-jisautodetect, x-johab, x-macarabic, x-maccentraleurope, x-maccroatian, x-maccyrillic, x-macdingbat, x-macgreek, x-machebrew, x-maciceland, x-macroman, x-macromania, x-macsymbol, x-macthai, x-macturkish, x-macukraine, x-ms932_0213, x-ms950-hkscs, x-ms950-hkscs-xp, x-mswin-936, x-pck, x-sjis_0213, x-utf-16le-bom, x-utf-32be-bom, x-utf-32le-bom, x-windows-50220, x-windows-50221, x-windows-874, x-windows-949, x-windows-950, x-windows-iso2022jp\"\n" +
                        "        ],\n" +
                        "        \"Content-Type\": [\n" +
                        "            \"application/json;charset=UTF-8\"\n" +
                        "        ],\n" +
                        "        \"Host\": [\n" +
                        "            \"l5lntvnxj7.execute-api.eu-central-1.amazonaws.com\"\n" +
                        "        ],\n" +
                        "        \"User-Agent\": [\n" +
                        "            \"BitWebHook/1.0\"\n" +
                        "        ],\n" +
                        "        \"X-Amzn-Trace-Id\": [\n" +
                        "            \"Root=1-5c180261-bf18352a3cf4d032387bb02c\"\n" +
                        "        ],\n" +
                        "        \"X-Forwarded-For\": [\n" +
                        "            \"35.233.22.56\"\n" +
                        "        ],\n" +
                        "        \"X-Forwarded-Port\": [\n" +
                        "            \"443\"\n" +
                        "        ],\n" +
                        "        \"X-Forwarded-Proto\": [\n" +
                        "            \"https\"\n" +
                        "        ]\n" +
                        "    },\n" +
                        "    \"queryStringParameters\": \"None\",\n" +
                        "    \"multiValueQueryStringParameters\": \"None\",\n" +
                        "    \"pathParameters\": \"None\",\n" +
                        "    \"stageVariables\": \"None\",\n" +
                        "    \"requestContext\": {\n" +
                        "        \"resourceId\": \"lwfgc9\",\n" +
                        "        \"resourcePath\": \"/watchfolder-dtc-webhook\",\n" +
                        "        \"httpMethod\": \"POST\",\n" +
                        "        \"extendedRequestId\": \"SEVPPHeHliAFbfA=\",\n" +
                        "        \"requestTime\": \"17/Dec/2018:20:09:05 +0000\",\n" +
                        "        \"path\": \"/default/watchfolder-dtc-webhook\",\n" +
                        "        \"accountId\": \"577045664692\",\n" +
                        "        \"protocol\": \"HTTP/1.1\",\n" +
                        "        \"stage\": \"default\",\n" +
                        "        \"domainPrefix\": \"l5lntvnxj7\",\n" +
                        "        \"requestTimeEpoch\": 1545077345511,\n" +
                        "        \"requestId\": \"9acd402c-0237-11e9-8e0e-45c024eeca7d\",\n" +
                        "        \"identity\": {\n" +
                        "            \"cognitoIdentityPoolId\": \"None\",\n" +
                        "            \"accountId\": \"None\",\n" +
                        "            \"cognitoIdentityId\": \"None\",\n" +
                        "            \"caller\": \"None\",\n" +
                        "            \"sourceIp\": \"xx.xx.xx.xx\",\n" +
                        "            \"accessKey\": \"None\",\n" +
                        "            \"cognitoAuthenticationType\": \"None\",\n" +
                        "            \"cognitoAuthenticationProvider\": \"None\",\n" +
                        "            \"userArn\": \"None\",\n" +
                        "            \"userAgent\": \"BitWebHook/1.0\",\n" +
                        "            \"user\": \"None\"\n" +
                        "        },\n" +
                        "        \"domainName\": \"l5lntvnxj7.execute-api.eu-central-1.amazonaws.com\",\n" +
                        "        \"apiId\": \"l5lntvnxj7\"\n" +
                        "    },\n" +
                        "    \"body\": {\n" +
                        "        \"id\": \"851295d1-33b5-4e3c-a155-2747f4a42a09\",\n" +
                        "        \"createdAt\": \"2018-12-17T20:09:05Z\",\n" +
                        "        \"webhookId\": \"0239ca55-15af-4f57-83b8-f419c9c844f5\",\n" +
                        "        \"eventId\": \"94cf92c8-09ad-4f97-bdac-45037f86272f\",\n" +
                        "        \"eventType\": \"ENCODING_FINISHED\",\n" +
                        "        \"encode\": {\n" +
                        "            \"id\": \"6bd756e9-af4f-45a1-a8f2-5c8f131fa35d\",\n" +
                        "            \"type\": \"VOD\",\n" +
                        "            \"name\": \"watchfolder-dtc-java cinepolis/Fifa_30sec.mp4\",\n" +
                        "            \"encoderVersion\": \"1.44.0\",\n" +
                        "            \"cloudRegion\": \"AWS_EU_WEST_1\"\n" +
                        "        }\n" +
                        "    },\n" +
                        "    \"isBase64Encoded\": \"None\"\n" +
                        "}\n";

        JSONParser parser = new JSONParser();
        JSONObject bitmovinWebhookJson = (JSONObject) parser.parse(inputTextJson);
        JSONObject bodyJson = (JSONObject) bitmovinWebhookJson.get("body");
        System.out.println("Json Object: ");
        System.out.println(bodyJson.toString());
        String eventType = (String) bodyJson.get("eventType");
        System.out.println("EventType: ");
        System.out.println(eventType);
        JSONObject encodingJson = (JSONObject) bodyJson.get("encoding");
        String encodingId = (String) encodingJson.get("id");
        System.out.println("Encoding ID: ");
        System.out.println(encodingId);
        createAndRunDashManifest(encodingId);
    }

}


