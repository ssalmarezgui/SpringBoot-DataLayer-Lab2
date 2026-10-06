package com.sip.workshop2.configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.sip.workshop2.controllers.ArticleController;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Récupère le chemin absolu du dossier uploadArticles
        Path uploadDir = Paths.get(ArticleController.uploadDirectory);
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        // Lie l'URL /uploadArticles/** au dossier physique sur le disque
        registry.addResourceHandler("/uploadArticles/**")
        .addResourceLocations("file:///" + uploadPath.replace("\\", "/") + "/");
    }
}