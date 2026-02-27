package br.com.devisecenter.devise_center.modules.upload;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;

import br.com.devisecenter.devise_center.exceptions.exception.upload.ExternalServiceException;
import br.com.devisecenter.devise_center.exceptions.exception.upload.SizeLimitExceeded;
import br.com.devisecenter.devise_center.exceptions.exception.upload.UploadException;
import br.com.devisecenter.devise_center.modules.posts.service.PostService;
import br.com.devisecenter.devise_center.modules.users.service.UserService;

@ExtendWith(MockitoExtension.class)
class UploadFileServiceUnitTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private MultipartFile file;

    @Mock
    private PostService postService;

    @Mock
    private UserService userService;

    @InjectMocks
    private UploadFileService service;

    // =========================
    // UPLOAD FILE
    // =========================

    @Test
    @DisplayName("Should upload file - everthing is ok")
    void shouldUploadFileSuccessfully() throws Exception {

        when(cloudinary.uploader()).thenReturn(uploader);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getBytes()).thenReturn("data".getBytes());

        Map<String, Object> cloudResponse = new HashMap<>();
        cloudResponse.put("secure_url", "http://image");
        cloudResponse.put("public_id", "abc123");

        when(uploader.upload(any(), anyMap())).thenReturn(cloudResponse);

        Map<String, String> result = service.uploadFile(file, "/posts");

        assertEquals("http://image", result.get("url"));
        assertEquals("abc123", result.get("publicId"));
    }

    @Test
    @DisplayName("Should throw Exception: UploadException (unexpected file type)")
    void shouldThrowWhenFileTypeIsInvalid() {
        when(file.getContentType()).thenReturn("application/pdf");

        assertThrows(UploadException.class,
                () -> service.uploadFile(file, "/posts"));
    }

    @Test
    @DisplayName("Should throw Exception: ExternalServiceException (fail to connect to cloudinary)")
    void shouldThrowExternalServiceExceptionWhenCloudinaryFails() throws Exception {

        when(cloudinary.uploader()).thenReturn(uploader);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getBytes()).thenThrow(new IOException());

        assertThrows(ExternalServiceException.class,
                () -> service.uploadFile(file, "/posts"));
    }

    // =========================
    // UPDATE FILE
    // =========================

    @Test
    @DisplayName("Should update file - everthing is ok")
    void shouldUpdatePostFileSuccessfully() throws Exception {

        UUID postId = UUID.randomUUID();

        when(cloudinary.uploader()).thenReturn(uploader);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(1000L);
        when(file.getBytes()).thenReturn("data".getBytes());

        var postMock = mock(br.com.devisecenter.devise_center.modules.posts.dtos.PostDTO.class);
        when(postService.findById(postId)).thenReturn(postMock);
        when(postMock.getPublicId()).thenReturn("oldId");

        Map<String, Object> cloudResponse = new HashMap<>();
        cloudResponse.put("secure_url", "http://newimage");
        cloudResponse.put("public_id", "newId");

        when(uploader.upload(any(), anyMap())).thenReturn(cloudResponse);

        Map<String, String> result = service.updateFile(file, "/posts", "publicId");

        assertEquals("http://newimage", result.get("url"));
        assertEquals("newId", result.get("publicId"));

        verify(uploader).destroy(eq("oldId"), anyMap());
    }

    @Test
    @DisplayName("Should throw Exception: SizeLimitExceeded")
    void shouldThrowWhenFileExceedsSizeLimit() {
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(6 * 1024 * 1024L);

        assertThrows(SizeLimitExceeded.class,
                () -> service.updateFile(file, "/posts", "publicId"));
    }

    // =========================
    // DELETE FILE
    // =========================

    @Test
    @DisplayName("Should delete file - everthing is ok")
    void shouldDeleteFileSuccessfully() throws Exception {

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(anyString(), anyMap()))
                .thenReturn(new HashMap<>());

        service.deleteFile("abc123");

        verify(uploader).destroy(eq("abc123"), anyMap());
    }

    @Test
    @DisplayName("Should throw Exception: (Generic failure)")
    void shouldThrowRuntimeWhenDeleteFails() throws Exception {

        when(cloudinary.uploader()).thenReturn(uploader);
        doThrow(new IOException())
                .when(uploader)
                .destroy(anyString(), anyMap());

        assertThrows(RuntimeException.class,
                () -> service.deleteFile("abc123"));
    }

}