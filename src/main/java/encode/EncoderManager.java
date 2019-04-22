package encode;

import com.bitmovin.api.BitmovinApi;
import com.bitmovin.api.encoding.AclEntry;
import com.bitmovin.api.encoding.AclPermission;
import com.bitmovin.api.encoding.EncodingOutput;
import com.bitmovin.api.encoding.InputStream;
import com.bitmovin.api.encoding.codecConfigurations.AACAudioConfig;
import com.bitmovin.api.encoding.codecConfigurations.H264VideoConfiguration;
import com.bitmovin.api.encoding.codecConfigurations.enums.ProfileH264;
import com.bitmovin.api.encoding.encodings.Encoding;
import com.bitmovin.api.encoding.encodings.conditions.AbstractCondition;
import com.bitmovin.api.encoding.encodings.conditions.AndConjunction;
import com.bitmovin.api.encoding.encodings.conditions.Condition;
import com.bitmovin.api.encoding.encodings.conditions.ConditionAttribute;
import com.bitmovin.api.encoding.encodings.muxing.FMP4Muxing;
import com.bitmovin.api.encoding.encodings.muxing.MuxingStream;
import com.bitmovin.api.encoding.encodings.streams.Stream;
import com.bitmovin.api.encoding.enums.CloudRegion;
import com.bitmovin.api.encoding.enums.StreamSelectionMode;
import com.bitmovin.api.encoding.inputs.S3Input;
import com.bitmovin.api.encoding.outputs.Output;
import com.bitmovin.api.encoding.outputs.S3Output;
import com.bitmovin.api.exceptions.BitmovinApiException;
import com.bitmovin.api.http.RestException;
import com.bitmovin.api.webhooks.Webhook;
import com.bitmovin.api.webhooks.enums.WebhookHttpMethod;
import com.bitmovin.api.webhooks.enums.WebhookType;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class EncoderManager
{
    // Raul Key
    private static String API_KEY = "INSERT_YOUR_APIKEY";

    // INPUT
    private static String S3_INPUT_ACCESSKEY = "INSERT_YOUR_S3_ACCESSKEY";
    private static String S3_INPUT_SECRETKEY = "INSERT_YOUR_S3_SECRETKEY";
    private static String S3_INPUT_BUCKETNAME = "";
    private static String S3_INPUT_PATH = "";

    // OUTPUT
    private static String S3_OUTPUT_ACCESSKEY = "INSERT_YOUR_S3_APIKEY";
    private static String S3_OUTPUT_SECRET_KEY = "INSERT_YOUR_S3_SECRETKEY";
    private static String S3_OUTPUT_BUCKET_NAME = "INSERT_YOUR_BUCKET_NAME";
    private static String OUTPUT_BASE_PATH = "output/watchfolder/";

    // AWS LAMBDA: API Gateway endpoint (Status monitoring, manifest)
    private static String NOTIFICATION_URL = "INSERT_YOUR_BITMOVIN_WEBHOOK_NOTIFICATION_URL";
    private static String PRE_FIX_ENCODE_NAME = "watchfolder: ";
    private static CloudRegion CLOUD_REGION = CloudRegion.AWS_EU_WEST_1;

    private static BitmovinApi bitmovinApi;

    public void createAndRunEncode(String s3InputBucketName, String s3InputPath) throws IOException, BitmovinApiException, UnirestException, URISyntaxException, RestException, InterruptedException
    {

        // Update OUTPUT_BASE_PATH
        updateOutputPath(s3InputPath);

        S3_INPUT_BUCKETNAME = s3InputBucketName;
        S3_INPUT_PATH = s3InputPath;

        Encoding encoding = createBitmovinApi(s3InputPath);

        S3Input input = new S3Input();
        input.setBucketName(S3_INPUT_BUCKETNAME);
        input.setAccessKey(S3_INPUT_ACCESSKEY);
        input.setSecretKey(S3_INPUT_SECRETKEY);
        input.setName("S3 Input");
        input = bitmovinApi.input.s3.create(input);

        S3Output output = new S3Output();
        output.setAccessKey(S3_OUTPUT_ACCESSKEY);
        output.setSecretKey(S3_OUTPUT_SECRET_KEY);
        output.setBucketName(S3_OUTPUT_BUCKET_NAME);
        output = bitmovinApi.output.s3.create(output);

        // Add Audio Configuration
        AACAudioConfig aacConfiguration = getAacAudioConfig(120000L,48000);

        // Add H264 configuration for encode
        H264VideoConfiguration videoConfiguration240p = getH264VideoConfiguration(240, 400000L);
        H264VideoConfiguration videoConfiguration480p = getH264VideoConfiguration(480, 800000L);
        H264VideoConfiguration videoConfiguration720p = getH264VideoConfiguration(720, 4000000L);
        H264VideoConfiguration videoConfiguration1080p = getH264VideoConfiguration(1080, 6500000L);

        InputStream inputStreamVideo = getInputStream(input, StreamSelectionMode.VIDEO_RELATIVE);
        InputStream inputStreamAudio = getInputStream(input, StreamSelectionMode.AUDIO_RELATIVE);

        // Add streams and add the condition for every stream that the input height must be >= than the height for the specific representation
        Stream videoStream240p = getVideoStream(encoding, videoConfiguration240p, inputStreamVideo, "240");
        Stream videoStream480p = getVideoStream(encoding, videoConfiguration480p, inputStreamVideo, "480");
        Stream videoStream720p = getVideoStream(encoding, videoConfiguration720p, inputStreamVideo, "720");
        Stream videoStream1080p = getVideoStream(encoding, videoConfiguration1080p, inputStreamVideo, "1080");

        Stream audioStream = getAudioStream(encoding, aacConfiguration, inputStreamAudio);

        // ffmp4 muxing
        this.createFMP4Muxing(encoding, output, "/video/dash/h264/240p/", videoStream240p);
        this.createFMP4Muxing(encoding, output, "/video/dash/h264/480p/", videoStream480p);
        this.createFMP4Muxing(encoding, output, "/video/dash/h264/720p/", videoStream720p);
        this.createFMP4Muxing(encoding, output, "/video/dash/h264/1080p/", videoStream1080p);
        this.createFMP4Muxing(encoding, output, "/audio/dash/", audioStream);

        // Start Encoding Job
        System.out.println("Starting Encoding Job");
        bitmovinApi.encoding.start(encoding);

        System.out.println("Added webhook notification");
        this.createWebHook(encoding);

    }

    private AACAudioConfig getAacAudioConfig(long bitrate, float rate) throws BitmovinApiException, UnirestException, IOException, URISyntaxException
    {
        AACAudioConfig aacConfiguration = new AACAudioConfig();
        aacConfiguration.setBitrate(bitrate);
        aacConfiguration.setRate(rate);
        aacConfiguration = bitmovinApi.configuration.audioAAC.create(aacConfiguration);
        return aacConfiguration;
    }

    private Stream getAudioStream(Encoding encoding, AACAudioConfig aacConfiguration, InputStream inputStreamAudio) throws BitmovinApiException, IOException, RestException, URISyntaxException, UnirestException
    {
        Stream audioStream = new Stream();
        audioStream.setCodecConfigId(aacConfiguration.getId());
        audioStream.setInputStreams(Collections.singleton(inputStreamAudio));
        audioStream = bitmovinApi.encoding.stream.addStream(encoding, audioStream);
        return audioStream;
    }

    private void updateOutputPath(String s3InputPath)
    {
        String pattern = "^[a-zA-Z_\\-0-9]*";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(s3InputPath);
        m.find();
        String fileName = m.group(0);

        OUTPUT_BASE_PATH = OUTPUT_BASE_PATH + fileName;
    }

    private Stream getVideoStream(Encoding encoding, H264VideoConfiguration videoConfiguration, InputStream inputStreamVideo, String conditionHeight) throws BitmovinApiException, IOException, RestException, URISyntaxException, UnirestException
    {
        Stream videoStream = new Stream();
        videoStream.setCodecConfigId(videoConfiguration.getId());
        videoStream.setInputStreams(Collections.singleton(inputStreamVideo));
        AndConjunction andConjunction = new AndConjunction();
        andConjunction.setConditions(new ArrayList<AbstractCondition>()
        {{
            add(new Condition(ConditionAttribute.HEIGHT, ">=", conditionHeight));
        }});
        videoStream.setConditions(andConjunction);
        videoStream = bitmovinApi.encoding.stream.addStream(encoding, videoStream);
        return videoStream;
    }

    private InputStream getInputStream(S3Input input, StreamSelectionMode videoRelative)
    {
        InputStream inputStreamVideo = new InputStream();
        inputStreamVideo.setInputPath(S3_INPUT_PATH);
        inputStreamVideo.setInputId(input.getId());
        inputStreamVideo.setSelectionMode(videoRelative);
        inputStreamVideo.setPosition(0);
        return inputStreamVideo;
    }

    private H264VideoConfiguration getH264VideoConfiguration(int height, long bitrate) throws BitmovinApiException, UnirestException, IOException, URISyntaxException
    {
        H264VideoConfiguration videoConfiguration = new H264VideoConfiguration();
        videoConfiguration.setHeight(height);
        videoConfiguration.setBitrate(bitrate);
        videoConfiguration.setProfile(ProfileH264.HIGH);
        videoConfiguration = bitmovinApi.configuration.videoH264.create(videoConfiguration);
        return videoConfiguration;
    }

    private Encoding createBitmovinApi(String s3InputPath) throws IOException, BitmovinApiException, UnirestException, URISyntaxException
    {
        bitmovinApi = new BitmovinApi(API_KEY);
        Encoding encoding = new Encoding();
        encoding.setName(PRE_FIX_ENCODE_NAME + s3InputPath);
        encoding.setCloudRegion(CLOUD_REGION);
        encoding = bitmovinApi.encoding.create(encoding);
        return encoding;
    }

    private void createFMP4Muxing(Encoding encoding, Output output, String path, Stream videoStream) throws BitmovinApiException, IOException, RestException, URISyntaxException, UnirestException
    {
        EncodingOutput encodingOutput = new EncodingOutput();
        encodingOutput.setOutputId(output.getId());
        encodingOutput.setOutputPath(OUTPUT_BASE_PATH + path);
        encodingOutput.setAcl(new ArrayList<AclEntry>()
        {{
            this.add(new AclEntry(AclPermission.PUBLIC_READ));
        }});
        FMP4Muxing fmp4Muxing = new FMP4Muxing();
        fmp4Muxing.setSegmentLength(4.0);
        fmp4Muxing.setOutputs(Collections.singletonList(encodingOutput));
        List<MuxingStream> muxingStreams = new ArrayList<>();
        MuxingStream muxingStreamVideo = new MuxingStream();
        muxingStreamVideo.setStreamId(videoStream.getId());
        muxingStreams.add(muxingStreamVideo);
        fmp4Muxing.setStreams(muxingStreams);
        bitmovinApi.encoding.muxing.addFmp4MuxingToEncoding(encoding, fmp4Muxing);
    }

    private void createWebHook(Encoding encoding) throws URISyntaxException, BitmovinApiException, RestException, UnirestException, IOException
    {
        Webhook webhook = new Webhook();
        webhook.setUrl(NOTIFICATION_URL);
        webhook.setMethod(WebhookHttpMethod.POST);
        bitmovinApi.notifications.webhooks.create(webhook, WebhookType.ENCODING_FINISHED, encoding.getId());
        bitmovinApi.notifications.webhooks.create(webhook, WebhookType.ENCODING_ERROR, encoding.getId());

    }
}
