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
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ProviderRepository providerRepository;

    public Article saveArticle(Article article, long providerId, MultipartFile fileImage, 
    		MultipartFile filePdf, MultipartFile fileExcel) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid provider Id:" + providerId));
        article.setProvider(provider);

        long time = new Date().getTime();

        // Gestion de l'Image
        if (fileImage != null && !fileImage.isEmpty()) {
            String imageName = time + "_" + fileImage.getOriginalFilename();
            saveFileToDisk(fileImage, imageName);
            article.setPicture(imageName);
        }

        // Gestion du Devis PDF
        if (filePdf != null && !filePdf.isEmpty()) {
            String pdfName = time + "_" + filePdf.getOriginalFilename();
            if (pdfName.toLowerCase().endsWith(".pdf")) {
                saveFileToDisk(filePdf, pdfName);
                article.setPdfFile(pdfName);
            } else {
                throw new IllegalArgumentException("Le devis doit être un fichier PDF");
            }
        }

        // Gestion du fichier Excel
        if (fileExcel != null && !fileExcel.isEmpty()) {
            String excelName = time + "_" + fileExcel.getOriginalFilename();
            if (excelName.toLowerCase().endsWith(".xlsx") || excelName.toLowerCase().endsWith(".xls")) {
                saveFileToDisk(fileExcel, excelName);
                article.setExcelFile(excelName);
            } else {
                throw new IllegalArgumentException("L'estimation doit être un fichier Excel (.xlsx ou .xls)");
            }
        }

        return articleRepository.save(article);
    }

    // Méthode utilitaire pour écrire un fichier sur le disque
    private void saveFileToDisk(MultipartFile file, String filename) {
        try {
            Path path = Paths.get(ArticleController.uploadDirectory, filename);
            Files.write(path, file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public List<Article> listArticle() {
        return (List<Article>) articleRepository.findAll();
    }

    public Optional<Article> findArticleById(long id) {
        return articleRepository.findById(id);
    }

    public void deleteArticle(long id) {
        Optional<Article> oa = findArticleById(id);
        if (oa.isPresent()) {
            Article article = oa.get();

            // 1. Suppression physique de l'Image
            deleteFileFromDisk(article.getPicture());

            // 2. Suppression physique du PDF
            deleteFileFromDisk(article.getPdfFile());

            // 3. Suppression physique du Fichier Excel
            deleteFileFromDisk(article.getExcelFile());

            // 4. Suppression dand la BDD
            articleRepository.deleteById(id);
        }
    }

    // suppression sur le disque
    private void deleteFileFromDisk(String fileName) {
        if (fileName != null && !fileName.trim().isEmpty()) {
            try {
                Path path = Paths.get(ArticleController.uploadDirectory, fileName);
                boolean deleted = Files.deleteIfExists(path);
                if (deleted) {
                    System.out.println("Fichier supprimé avec succès : " + fileName);
                } else {
                    System.out.println("Fichier introuvable sur le disque : " + fileName);
                }
            } catch (IOException e) {
                System.err.println("Erreur lors de la suppression du fichier " + fileName + " : " + e.getMessage());
            }
        }
    }

    public Article updateArticle(Article article, long providerId, 
            MultipartFile imageFile, 
            MultipartFile pdfFile, 
            MultipartFile excelFile) {
		Provider provider = providerRepository.findById(providerId)
		.orElseThrow(() -> new IllegalArgumentException("Invalid provider Id:" + providerId));
		article.setProvider(provider);
		
		long time = new Date().getTime();
		
		// 1. Mise à jour de l'image (si un nouveau fichier a été sélectionné)
		if (imageFile != null && !imageFile.isEmpty()) {
			deleteFileIfExists(article.getPicture());
			String imageName = time + "_" + imageFile.getOriginalFilename();
			saveFileToDisk(imageFile, imageName);
			article.setPicture(imageName);
		}
		
		// 2. Mise à jour du PDF (si un nouveau fichier a été sélectionné)
		if (pdfFile != null && !pdfFile.isEmpty()) {
			deleteFileIfExists(article.getPdfFile());
			String pdfName = time + "_" + pdfFile.getOriginalFilename();
			saveFileToDisk(pdfFile, pdfName);
			article.setPdfFile(pdfName);
		}
		
		// 3. Mise à jour du fichier Excel (si un nouveau fichier a été sélectionné)
		if (excelFile != null && !excelFile.isEmpty()) {
			deleteFileIfExists(article.getExcelFile());
			String excelName = time + "_" + excelFile.getOriginalFilename();
			saveFileToDisk(excelFile, excelName);
			article.setExcelFile(excelName);
		}
		
		return articleRepository.save(article);
		}
		
		//Méthode utilitaire pour supprimer l'ancien fichier
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
}