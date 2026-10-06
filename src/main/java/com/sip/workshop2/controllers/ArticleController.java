package com.sip.workshop2.controllers;

import java.io.File;
import java.util.List;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.sip.workshop2.entities.Article;
import com.sip.workshop2.services.ArticleService;
import com.sip.workshop2.services.ProviderService;

@Controller
@RequestMapping("/articles/")
public class ArticleController {

    private final ArticleService articleService;
    private final ProviderService providerService;

    // Emplacement où seront stockées les images uploadées
    public static String uploadDirectory = System.getProperty("user.dir") + "/src/main/resources/static/uploadArticles";

    public ArticleController(ArticleService articleService, ProviderService providerService) {
        this.articleService = articleService;
        this.providerService = providerService;
        
        // Crée le dossier uploadArticles s'il n'existe pas encore
        File dir = new File(uploadDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @GetMapping("list")
    public String listArticles(Model model) {
        List<Article> articles = articleService.listArticle();
        if (articles.isEmpty()) {
            articles = null;
        }
        model.addAttribute("articles", articles);
        return "article/listArticles";
    }

    @GetMapping("add")
    public String showAddArticleForm(Model model) {
        model.addAttribute("providers", providerService.listProvider());
        model.addAttribute("article", new Article());
        return "article/addArticle";
    }

    @PostMapping("add")
    public String addArticle(Model model, 
                             Article article, 
                             BindingResult result,
                             @RequestParam(name = "providerId", required = false) Long p, 
                             @RequestParam("imageFile") MultipartFile imageFile,
                             @RequestParam("pdfFile") MultipartFile pdfFile,
                             @RequestParam("excelFile") MultipartFile excelFile) {
        
        // On vérifie juste si le provider a bien été sélectionné
        if (p == null) {
            model.addAttribute("providers", providerService.listProvider());
            return "article/addArticle";
        }
        
        try {
            articleService.saveArticle(article, p, imageFile, pdfFile, excelFile);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("fileError", ex.getMessage());
            model.addAttribute("providers", providerService.listProvider());
            return "article/addArticle";
        }
        return "redirect:list";
    }

    @GetMapping("delete/{id}")
    public String deleteArticle(@PathVariable("id") long id) {
        try {
            articleService.findArticleById(id)
                          .orElseThrow(() -> new IllegalArgumentException("Invalid article Id:" + id));
            articleService.deleteArticle(id);
        } catch (IllegalArgumentException ex) {
            return "provider/500";
        }
        return "redirect:../list";
    }

    @GetMapping("edit/{id}")
    public String showArticleFormToUpdate(@PathVariable("id") long id, Model model) {
        try {
            Article article = articleService.findArticleById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid article Id:" + id));
            model.addAttribute("article", article);
            model.addAttribute("providers", providerService.listProvider());
            return "article/updateArticle";
        } catch (IllegalArgumentException ex) {
            return "provider/500";
        }
    }

    @PostMapping("edit")
    public String updateArticle(@Valid Article article, 
                                BindingResult result, 
                                Model model,
                                @RequestParam(name = "providerId", required = false) Long p, 
                                @RequestParam(name = "newImageFile", required = false) MultipartFile imageFile,
                                @RequestParam(name = "newPdfFile", required = false) MultipartFile pdfFile,
                                @RequestParam(name = "newExcelFile", required = false) MultipartFile excelFile) {
        
        if (result.hasErrors() || p == null) {
            result.getAllErrors().forEach(e -> System.out.println("Validation Error: " + e.getDefaultMessage()));
            model.addAttribute("providers", providerService.listProvider());
            return "article/updateArticle";
        }
        
        articleService.updateArticle(article, p, imageFile, pdfFile, excelFile);
        return "redirect:list";
    }

    @GetMapping("show/{id}")
    public String showArticleDetails(@PathVariable("id") long id, Model model) {
        try {
            Article article = articleService.findArticleById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid article Id:" + id));
            model.addAttribute("article", article);
            return "article/showArticle";
        } catch (IllegalArgumentException ex) {
            return "provider/500";
        }
    }
}