package com.sip.workshop2.controllers;

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
import com.sip.workshop2.entities.Provider;
import com.sip.workshop2.services.ProviderService;

@Controller
@RequestMapping("/providers")
public class ProviderController {

    private final ProviderService service;

    public ProviderController(ProviderService service) {
        this.service = service;
    }
    
    @GetMapping("/show/{id}")
    public String showProvider(@PathVariable("id") long id, Model model) {
        Provider provider = service.findProviderById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid provider Id:" + id));

        List<Article> articles = service.findArticlesByProvider(id);
        
        model.addAttribute("articles", articles);
        model.addAttribute("provider", provider);
        return "provider/showProvider";
    }

    @GetMapping("/list")
    public String list(Model model) {
        List<Provider> res = service.listProvider();
        if (res.isEmpty()) {
            res = null;
        }
        model.addAttribute("providers", res);
        return "provider/listProviders";
    }

    @GetMapping("/add")
    public String showAddProviderForm(Model model) {
        Provider provider = new Provider();
        model.addAttribute("provider", provider);
        return "provider/addProvider";
    }

    @PostMapping("/add")
    public String addProvider(@Valid Provider provider, 
                              BindingResult result,
                              @RequestParam(name = "invoiceFile", required = false) MultipartFile invoiceFile,
                              Model model) {
        if (result.hasErrors()) {
            return "provider/addProvider";
        }
        try {
            service.addProvider(provider, invoiceFile);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("fileError", ex.getMessage());
            return "provider/addProvider";
        }
        return "redirect:list";
    }

    @GetMapping("/delete/{id}")
    public String deleteProvider(@PathVariable("id") long id) {
        try {
            service.findProviderById(id)
                   .orElseThrow(() -> new IllegalArgumentException("Invalid provider Id:" + id));
            service.deleteProvider(id);
        } catch (IllegalArgumentException ex) {
            return "provider/500";
        }
        return "redirect:../list";
    }

    @GetMapping("/edit/{id}")
    public String showProviderFormToUpdate(@PathVariable("id") long id, Model model) {
        try {
            Provider provider = service.findProviderById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid provider Id:" + id));
            model.addAttribute("provider", provider);
            return "provider/updateProvider";
        } catch (IllegalArgumentException ex) {
            return "provider/500";
        }
    }

    @PostMapping("/update")
    public String updateProvider(@Valid Provider provider, 
                                 BindingResult result,
                                 @RequestParam(name = "newInvoiceFile", required = false) MultipartFile invoiceFile) {
        if (result.hasErrors()) {
            return "provider/updateProvider";
        }
        service.updateProvider(provider, invoiceFile);
        return "redirect:list";
    }
}