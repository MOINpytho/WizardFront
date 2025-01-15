package com.example.DataWizard.services;

import com.example.DataWizard.payload.Scrapper;
import lombok.SneakyThrows;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Service
public class ChatService {

    @Autowired
    private ChatModel chatModel;
    private final Scrapper scrapper = new Scrapper();



    //get the CDP name
    public Mono<String> getCDPName(String inputText){
        return Mono.fromCallable(() -> {
            String promptTemplate = loadPromptTemplate("prompts/platformSelect.txt");
            String actualPrompt = putValuesInPromptTemplate(promptTemplate, Map.of("inputText", inputText));
            ChatResponse chatResponse = chatModel.call(new Prompt(actualPrompt));
            return chatResponse.getResult().getOutput().getContent();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    //get the accurate link from diff cdp prompts
    public Mono<String> getLink(String inputText) {
        return getCDPName(inputText)
                .flatMap(cdp -> {
                    String templatePath = switch (cdp.toLowerCase().trim()) {
                        case "segment" -> "prompts/segment.txt";
                        case "mparticle" -> "prompts/mparticle.txt";
                        case "lytics" -> "prompts/lytics.txt";
                        case "zeotap" -> "prompts/zeotap.txt";
                        default -> null;
                    };
                    if (templatePath == null) {
                        return Mono.error(new IllegalArgumentException("Invalid CDP name: " + cdp));
                    }
                    String template = loadPromptTemplate(templatePath);

                    String prompt = putValuesInPromptTemplate(template, (Map<String, String>) Map.of("inputText",inputText));
                    String response = chatModel.call(prompt);
                    response.replaceAll("[<>]", "");
                    System.out.println(response);
                    return Mono.just(response);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    //get the scrapped data for generating response
    public Mono<String> plainText(String path) {
        return Mono.fromCallable(() -> scrapper.scrapeContent(path))
                .subscribeOn(Schedulers.boundedElastic());
    }

    //get the generated response of the input
    @SneakyThrows
    public Flux<String> generateResponse(String inputText) {
        return getLink(inputText)
                .flatMap(this::plainText)
                .flatMapMany(dataset ->{
                    String template = loadPromptTemplate("prompts/chat_prompt.txt");
                    String prompt = putValuesInPromptTemplate(template, (Map<String, String>) Map.of("dataset",dataset,"inputText",inputText));
                    return chatModel.stream(prompt);
                })
                //to handle errors and provide a fallback response
                .onErrorResume(e -> {
                    return Flux.just("Sorry, I can't answer Irrelevant Questions");
                });
    }

    //load prompt from class path
    public String loadPromptTemplate(String fileName) {
        try {
            Path path = new ClassPathResource(fileName).getFile().toPath();
            return Files.readString(path);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load prompt template: " + fileName, e);
        }
    }

    //put values to prompt

    public String putValuesInPromptTemplate(String template, Map<String,String> values){
        for(Map.Entry<String,String> entry: values.entrySet()){
            template = template.replace("{"+ entry.getKey() +"}", entry.getValue());
        }
        return template;
    }

}
