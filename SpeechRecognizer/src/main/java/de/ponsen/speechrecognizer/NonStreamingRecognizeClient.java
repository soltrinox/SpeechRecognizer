package de.ponsen.speechrecognizer;

/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Client that sends audio to Speech.NonStreamingRecognize via gRPC and returns transcription.
//
// Uses a service account for OAuth2 authentication, which you may obtain at
// https://console.developers.google.com
// API Manager > Google Cloud Speech API > Enable
// API Manager > Credentials > Create credentials > Service account key > New service account.
//
// Then set environment variable GOOGLE_APPLICATION_CREDENTIALS to the full path of that file.

import android.content.res.AssetManager;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.AudioRequest;
import com.google.cloud.speech.v1.InitialRecognizeRequest;
import com.google.cloud.speech.v1.InitialRecognizeRequest.AudioEncoding;
import com.google.cloud.speech.v1.NonStreamingRecognizeResponse;
import com.google.cloud.speech.v1.RecognizeRequest;
import com.google.cloud.speech.v1.SpeechGrpc;
import com.google.protobuf.TextFormat;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.auth.ClientAuthInterceptor;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client that sends audio to Speech.NonStreamingRecognize and returns transcript.
 */
public class NonStreamingRecognizeClient {

  private static final Logger logger =
      Logger.getLogger(NonStreamingRecognizeClient.class.getName());

  private static final List<String> OAUTH2_SCOPES =
      Arrays.asList("https://www.googleapis.com/auth/cloud-platform");

  private final String host;
  private final int port;
  private final File input;
  private final int samplingRate;

  private final ManagedChannel channel;
  private final SpeechGrpc.SpeechBlockingStub blockingStub;

  /**
   * Construct client connecting to Cloud Speech server at {@code host:port}.
   */
  public NonStreamingRecognizeClient(String host, int port, File input, int samplingRate)
      throws IOException {
    this.host = host;
    this.port = port;
    this.input = input;
    this.samplingRate = samplingRate;
    final String API_KEY = "AIzaSyDDhyXsfjknBf1tDRT6Ofb3C6-eLIOUMvk";
    final String client_id = "896945449984-s5g19onj14k82tmaqvpo6c2aih3tc6vp.apps.googleusercontent.com";

    InputStream resourceAsStream = MainActivity.class.getClassLoader().getResourceAsStream("Google-Play-Android-Developer.json");
    GoogleCredentials creds = GoogleCredentials.fromStream(resourceAsStream);
    creds = creds.createScoped(OAUTH2_SCOPES);
    channel = NettyChannelBuilder.forAddress(host, port)
        .negotiationType(NegotiationType.TLS)
        .intercept(new ClientAuthInterceptor(creds, Executors.newSingleThreadExecutor()))
        .build();
    blockingStub = SpeechGrpc.newBlockingStub(channel);
    logger.info("Created blockingStub for " + host + ":" + port);
  }

  private AudioRequest createAudioRequest(File file) throws IOException {
    return AudioRequestFactory.createRequest(file);
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  /** Send a non-streaming-recognize request to server. */
  public void recognize() {
    AudioRequest audio;
    try {
      audio = createAudioRequest(input);
    } catch (IOException e) {
      logger.log(Level.WARNING, "Failed to read audio uri input: " + input + ":" + e.getMessage());
      return;
    }
    logger.info("Sending " + audio.getContent().size() + " bytes from audio uri input: " + input);
    InitialRecognizeRequest initial = InitialRecognizeRequest.newBuilder()
        .setEncoding(AudioEncoding.LINEAR16)
        .setSampleRate(samplingRate)
        .build();
    RecognizeRequest request = RecognizeRequest.newBuilder()
        .setInitialRequest(initial)
        .setAudioRequest(audio)
        .build();
    NonStreamingRecognizeResponse response;
    try {
      response = blockingStub.nonStreamingRecognize(request);
      logger.info("Received response: " + TextFormat.printToString(response));
    } catch (StatusRuntimeException e) {
      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
      return;
    }
  }

  public static void start(final File file) throws IOException, InterruptedException {
    Thread background=new Thread(new Runnable() {
      public void run() {
        try {
          startRecognize(file);
        } catch (IOException e) {
          e.printStackTrace();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

    });
    background.start();
  }

  public static void startRecognize(File file) throws IOException, InterruptedException {
    String audioFile = "file:///assets/audio.raw";
    String host = "speech.googleapis.com";
    Integer port = 443;
    Integer sampling = 16000;

    NonStreamingRecognizeClient client =
            new NonStreamingRecognizeClient(host, port, file, sampling);
    try {
      client.recognize();
    } finally {
      client.shutdown();
    }
  }
}
