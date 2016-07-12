package de.ponsen.speechrecognizer;

/**
 * Created by crockettj on 7/11/16.
 */

import android.app.Application;
import android.content.res.AssetManager;

import com.google.cloud.speech.v1.AudioRequest;
import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;

/*
 * AudioRequestFactory takes a URI as an input and creates an AudioRequest. The URI can point to a
 * local file or a file on Google Cloud Storage.
 */
public class AudioRequestFactory {

    private static final String FILE_SCHEME = "file";
    private static final String GS_SCHEME   = "gs";

    /**
     * Takes an input URI of form $scheme:// and converts to audio request.
     *
     * @return AudioRequest audio request
     */
    public static AudioRequest createRequest(File file)
            throws IOException {
            return audioFromBytes(readFile(file));
    }

    public static byte[] convertFileToByteArray(File f) {
        byte[] byteArray = null;
        try {
            InputStream inputStream = new FileInputStream(f);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024 * 8];
            int bytesRead = 0;

            while ((bytesRead = inputStream.read(b)) != -1) {
                bos.write(b, 0, bytesRead);
            }

            byteArray = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArray;
    }

    public static byte[] readFile(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }

    /**
     * Convert bytes to AudioRequest.
     *
     * @param bytes input bytes
     * @return AudioRequest audio request
     */
    private static AudioRequest audioFromBytes(byte[] bytes) {
        return AudioRequest.newBuilder()
                .setContent(ByteString.copyFrom(bytes))
                .build();
    }
}
