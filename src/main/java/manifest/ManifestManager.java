package manifest;

import com.bitmovin.api.BitmovinApi;
import com.bitmovin.api.encoding.EncodingOutput;
import com.bitmovin.api.encoding.encodings.Encoding;
import com.bitmovin.api.encoding.encodings.muxing.Muxing;
import com.bitmovin.api.encoding.encodings.streams.Stream;
import com.bitmovin.api.encoding.enums.DashMuxingType;
import com.bitmovin.api.encoding.manifest.dash.AudioAdaptationSet;
import com.bitmovin.api.encoding.manifest.dash.DashFmp4Representation;
import com.bitmovin.api.encoding.manifest.dash.DashManifest;
import com.bitmovin.api.encoding.manifest.dash.Period;
import com.bitmovin.api.encoding.manifest.dash.VideoAdaptationSet;
import com.bitmovin.api.encoding.outputs.S3Output;
import com.bitmovin.api.enums.Status;
import com.bitmovin.api.exceptions.BitmovinApiException;
import com.bitmovin.api.http.RestException;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManifestManager
{

    // Raul
    private static String API_KEY = "INSERT_YOUR_APIKEY";

    // OUTPUT
    private static String S3_OUTPUT_ACCESSKEY = "INSERT_YOUR_S3_ACCESSKEY";
    private static String S3_OUTPUT_SECRET_KEY = "INSERT_YOUR_S3_SECRETKEY";
    private static String S3_OUTPUT_BUCKET_NAME = "INSERT_YOUR_BUCKET_NAME";
    private static String OUTPUT_BASE_PATH = "output/watchfolder/";

    private static BitmovinApi bitmovinApi;


    public static String createAndRunDashManifest(String encodingId) throws UnirestException, IOException, BitmovinApiException, URISyntaxException, RestException, InterruptedException
    {

        System.out.println("Initiating Manifest Job");

        System.out.println(encodingId);

        bitmovinApi = new BitmovinApi(API_KEY);

        S3Output output = new S3Output();
        output.setAccessKey(S3_OUTPUT_ACCESSKEY);
        output.setSecretKey(S3_OUTPUT_SECRET_KEY);
        output.setBucketName(S3_OUTPUT_BUCKET_NAME);
        output = bitmovinApi.output.s3.create(output);

        Encoding encoding = bitmovinApi.encoding.get(encodingId);

        Muxing muxingOutput = bitmovinApi.encoding.muxing.getMuxings(encoding).get(0);

        // Update OUTPUT_BASE_PATH
        updateOutputPath(muxingOutput);

        // Start Dash Manifest Creation
        System.out.println("Creating Dash Manifest for Encoding ID: " + encoding.getId());

        DashManifest dashManifest = new DashManifest();
        dashManifest.setName("stream.mpd");
        dashManifest.setOutputs(Collections.singletonList(
                new EncodingOutput(
                        output.getId(),
                        OUTPUT_BASE_PATH
                )
        ));
        dashManifest = bitmovinApi.manifest.dash.create(dashManifest);

        Period period = new Period();
        period = bitmovinApi.manifest.dash.createPeriod(dashManifest, period);

        VideoAdaptationSet videoAdaptationSet = new VideoAdaptationSet();
        videoAdaptationSet = bitmovinApi.manifest.dash.addVideoAdaptationSetToPeriod(dashManifest, period, videoAdaptationSet);


        List<Muxing> perTitleMuxings = bitmovinApi.encoding.muxing.getMuxings(encoding);
        for (Muxing muxing : perTitleMuxings)
        {
            String tmp = muxing.getOutputs().get(0).getOutputPath();
            if (muxing.getOutputs().get(0).getOutputPath().indexOf("audio") > -1)
            {
                AudioAdaptationSet audioAdaptationSet = new AudioAdaptationSet();
                audioAdaptationSet.setLang("en");
                audioAdaptationSet = bitmovinApi.manifest.dash.addAudioAdaptationSetToPeriod(dashManifest, period,
                        audioAdaptationSet);
                String segmentsPath = StringUtils.removeStart(muxing.getOutputs().get(0).getOutputPath(), OUTPUT_BASE_PATH);
                segmentsPath = StringUtils.removeStart(segmentsPath, "/");
                DashFmp4Representation representation = new DashFmp4Representation();
                representation.setMuxingId(muxing.getId());
                representation.setStreamId(muxing.getStreams().get(0).getStreamId());
                representation.setEncodingId(encoding.getId());
                representation.setSegmentPath(segmentsPath);
                representation.setType(DashMuxingType.TEMPLATE);

                bitmovinApi.manifest.dash.addRepresentationToAdaptationSet(
                        dashManifest,
                        period,
                        audioAdaptationSet,
                        representation
                );

                continue;
            }

            if (CollectionUtils.isEmpty(muxing.getStreams()))

            {
                System.out.println(
                        String.format("Found muxing with id %s that does not contain a stream...", muxing.getId())
                );
                continue;
            }

            Stream stream = bitmovinApi.encoding.stream.getStream(encoding, muxing.getStreams().get(0).getStreamId());
            if (stream == null)
            {
                System.out.println(
                        String.format("Could not get stream with id %s", muxing.getStreams().get(0).getStreamId())
                );
                continue;
            }

            if (CollectionUtils.isEmpty(muxing.getOutputs()))
            {
                System.out.println(
                        String.format(
                                "Could not get encode output of muxing with id %s",
                                muxing.getId()
                        )
                );
                continue;
            }

            if (StringUtils.isBlank(muxing.getOutputs().get(0).getOutputPath()))
            {
                System.out.println(
                        String.format(
                                "Could not determine segment path because output path is not set for muxing with id %s",
                                muxing.getId()
                        )
                );
                continue;
            }

            String segmentsPath = StringUtils.removeStart(muxing.getOutputs().get(0).getOutputPath(), OUTPUT_BASE_PATH);


            if (segmentsPath.contains("{bitrate}"))
            {
                continue;
            }
            segmentsPath = StringUtils.removeStart(segmentsPath, "/");

            DashFmp4Representation representation = new DashFmp4Representation();
            representation.setMuxingId(muxing.getId());
            representation.setStreamId(muxing.getStreams().get(0).getStreamId());
            representation.setEncodingId(encoding.getId());
            representation.setSegmentPath(segmentsPath);
            representation.setType(DashMuxingType.TEMPLATE);

            bitmovinApi.manifest.dash.addRepresentationToAdaptationSet(
                    dashManifest,
                    period,
                    videoAdaptationSet,
                    representation
            );
        }

        // Start Manifest Job
        System.out.println("Starting Manifest Job");

        bitmovinApi.manifest.dash.startGeneration(dashManifest);
        System.out.println("Manifest Id: " + dashManifest.getId());
        Status manifestStatus;
        do
        {
            Thread.sleep(2500);
            manifestStatus = bitmovinApi.manifest.dash.getGenerationStatus(dashManifest);
        }
        while (manifestStatus != Status.FINISHED && manifestStatus != Status.ERROR);

        if (manifestStatus == Status.ERROR)
        {
            System.out.println("Error creating manifest...");
            return "Ending Lambda Manifest Function with ERROR";
        }

        System.out.println("Successfully finished encode and created manifest!");
        return "Ending Manifest Successfully";
    }

    private static void updateOutputPath(Muxing muxingOutput)
    {
        String outputPath = StringUtils.removeStart(muxingOutput.getOutputs().get(0).getOutputPath(), OUTPUT_BASE_PATH);

        String pattern = "^[a-zA-Z_\\-0-9]*\\/";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(outputPath);
        m.find();
        String fileName = m.group(0);

        OUTPUT_BASE_PATH = OUTPUT_BASE_PATH + fileName;
    }

}