package com.example.DataWizard.payload;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class Scrapper {
    public String scrapeContent(String url)throws IOException {
        Document document = Jsoup.connect(url).get();
        String data = document.body().text();
        return data;
    }
}
