package com.sip.workshop2.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sip.workshop2.controllers.ArticleController;
import com.sip.workshop2.entities.Article;
import com.sip.workshop2.entities.Provider;
import com.sip.workshop2.repositories.ArticleRepository;
import com.sip.workshop2.repositories.ProviderRepository;

@Service
public class ProviderService {

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private ArticleRepository articleRepository;

    public void deleteProvider(long id) {
        Optional<Provider> op = providerRepository.findById(id);
        if (op.isPresent()) {
            Provider provider = op.get();

            // 1. Suppression physique de la facture du Provider
            deleteFileFromDisk(provider.getInvoiceFile());

            // 2. Nettoyage des fichiers des articles de ce provider
            List<Article> articles = articleRepository.findByProviderId(id);
            for (Article article : articles) {
                deleteFileFromDisk(article.getPicture());
                deleteFileFromDisk(article.getPdfFile());
                deleteFileFromDisk(article.getExcelFile());
            }

            // 3. Suppression du Provider en BDD
            providerRepository.deleteById(id);
        }
    }

    // suppression sur disque
    private void deleteFileFromDisk(String fileName) {
        if (fileName != null && !fileName.trim().isEmpty()) {
            try {
                Path path = Paths.get(ArticleController.uploadDirectory, fileName);
                Files.deleteIfExists(path);
            } catch (IOException e) {
                System.err.println("Erreur suppression fichier Provider : " + e.getMessage());
            }
        }
    }
	
	public Provider updateProvider(Provider provider, MultipartFile invoiceFile) {
	    // Si un nouveau fichier a été choisi, supprimer l'ancien et enregistrer le nouveau
	    if (invoiceFile != null && !invoiceFile.isEmpty()) {
	        deleteFileIfExists(provider.getInvoiceFile());
	        String fileName = invoiceFile.getOriginalFilename();
	        
	        try {
	            Path path = Paths.get(ArticleController.uploadDirectory, fileName);
	            Files.write(path, invoiceFile.getBytes());
	            provider.setInvoiceFile(fileName);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    return providerRepository.save(provider);
	}

	private void deleteFileIfExists(String fileName) {
	    if (fileName != null && !fileName.isEmpty()) {
	        try {
	            Path path = Paths.get(ArticleController.uploadDirectory, fileName);
	            Files.deleteIfExists(path);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}
	
	public Provider addProvider(Provider provider, MultipartFile invoiceFile) {
	    if (invoiceFile != null && !invoiceFile.isEmpty()) {
	        String originalFilename = invoiceFile.getOriginalFilename();
	        String lowerName = originalFilename.toLowerCase();

	        // Vérification de l'extension PDF ou Excel
	        if (lowerName.endsWith(".pdf") || lowerName.endsWith(".xlsx") || lowerName.endsWith(".xls")) {
	            long time = new Date().getTime();
	            String fileName = time + "_" + originalFilename;

	            // Enregistrement dans le dossier uploadArticles
	            Path path = Paths.get(ArticleController.uploadDirectory, fileName);
	            try {
	                Files.write(path, invoiceFile.getBytes());
	                provider.setInvoiceFile(fileName);
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        } else {
	            throw new IllegalArgumentException("La facture doit être au format PDF ou Excel (.xlsx, .xls)");
	        }
	    }
	    return providerRepository.save(provider);
	}
	public List<Provider> listProvider(){
		return (List<Provider>) providerRepository.findAll();
	}
	
	
	public Optional<Provider> findProviderById(long id){
		return providerRepository.findById(id);
	}
	public List<Article> findArticlesByProvider(long id){
		return providerRepository.findArticlesByProvider(id);
	}
}
