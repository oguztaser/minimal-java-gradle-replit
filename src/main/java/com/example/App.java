package com.example;

import com.google.common.collect.ImmutableList;
import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.*;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class App {
  public static void main(String[] args) {
    String apiKey = System.getenv("GEMINI_API_KEY");

    if (apiKey == null || apiKey.isEmpty()) {
      System.err.println("Hata: GEMINI_API_KEY ortam değişkeni ayarlanmamış.");
      System.err.println("Lütfen Replit'teki 'Secrets' (Kilit simgesi) kısmından API anahtarınızı ekleyin.");
      System.exit(1);
    }

    Client client = Client.builder().apiKey(apiKey).build();

    List<Tool> tools = new ArrayList<>();
    tools.add(
      Tool.builder()
        .googleSearch(
          GoogleSearch.builder().build()
        )
        .build()
    );

    String model = "gemini-1.5-flash";
    List<Content> contents = ImmutableList.of(
      Content.builder()
        .role("user")
        .parts(ImmutableList.of(
          Part.fromText("Bana İstanbul hakkında 5 ilginç bilgi ver.")
        ))
        .build()
    );

    GenerateContentConfig config =
      GenerateContentConfig
      .builder()
      .temperature(0.7f)
      .thinkingConfig(
        ThinkingConfig
          .builder()
          .thinkingBudget(-1)
          .build()
      )
      .tools(tools)
      .build();

    System.out.println("Gemini API'sine istek gönderiliyor...");

    ResponseStream<GenerateContentResponse> responseStream = client.models.generateContentStream(model, contents, config);

    for (GenerateContentResponse res : responseStream) {
      Optional<List<Candidate>> candidatesOpt = res.candidates();
      if (candidatesOpt.isEmpty() || candidatesOpt.get().isEmpty()) {
        System.out.println("Yanıt adayları boş.");
        continue;
      }
      Optional<Content> contentOpt = candidatesOpt.get().get(0).content();
      if (contentOpt.isEmpty()) {
        System.out.println("Yanıt içeriği boş.");
        continue;
      }
      Optional<List<Part>> partsOpt = contentOpt.get().parts();
      if (partsOpt.isEmpty()) {
        System.out.println("Yanıt parçaları boş.");
        continue;
      }
      List<Part> parts = partsOpt.get();
      for (Part part : parts) {
        if (part.text().isPresent()) {
          System.out.print(part.text().get());
        } else if (part.functionCall().isPresent()) {
            System.out.println("\n--- Fonksiyon Çağrısı Algılandı ---");
            System.out.println("Fonksiyon Adı: " + part.functionCall().get().getName());
            System.out.println("Argümanlar: " + part.functionCall().get().getArgs());
            System.out.println("-----------------------------------\n");
        } else if (part.functionResponse().isPresent()) {
            System.out.println("\n--- Fonksiyon Yanıtı Algılandı ---");
            System.out.println("Yanıt: " + part.functionResponse().get().getResponse());
            System.out.println("-----------------------------------\n");
        }
      }
    }
    System.out.println("\nİstek tamamlandı.");
    responseStream.close();
  }
}
