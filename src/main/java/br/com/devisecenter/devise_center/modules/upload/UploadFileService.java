package br.com.devisecenter.devise_center.modules.upload;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import br.com.devisecenter.devise_center.exceptions.exception.upload.ExternalServiceException;
import br.com.devisecenter.devise_center.exceptions.exception.upload.SizeLimitExceeded;
import br.com.devisecenter.devise_center.exceptions.exception.upload.UploadException;

@Service
public class UploadFileService {

    private Cloudinary cloudinary;

    public UploadFileService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public Map<String, String> uploadFile(MultipartFile file, String folder) {

        final List<String> ALLOWED_TYPES = List.of(
                "image/jpeg",
                "image/png",
                "image/webp");

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new UploadException("Tipo de imagem não permitido");
        }

        Map<String, String> fileInfo = new HashMap<>();
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of("folder", folder));
            fileInfo.put("url", uploadResult.get("secure_url").toString());
            fileInfo.put("publicId", uploadResult.get("public_id").toString());
            return fileInfo;
        } catch (IOException e) {
            throw new ExternalServiceException("Erro de comunicação com serviço da Cloundinary.");
        }
    }

    public Map<String, String> updateFile(MultipartFile file, String folder, String previousPublicId) {

        final List<String> ALLOWED_TYPES = List.of(
                "image/jpeg",
                "image/png",
                "image/webp");

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new UploadException("Tipo de imagem não permitido");
        }

        final long MAX_SIZE = 5 * 1024 * 1024;

        if (file.getSize() > MAX_SIZE) {
            throw new SizeLimitExceeded("O arquivo enviado excede o limite de 5MB");
        }

        deleteFile(previousPublicId);
        return uploadFile(file, folder);
    }

    public void deleteFile(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("Erro ao deletar arquivo: ", e);
        }
    }

}
