package com.generation.ecommerce.cloud;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
public class S3Service {


    private final S3Client s3Client;
    private final String bucketName;

    public S3Service(@Value("${aws.accessKeyId}") String accessKey,
                     @Value("${aws.secretKey}") String secretKey,
                     @Value("${aws.region}") String region,
                     @Value("${aws.s3.bucketName}") String bucketName) {
        this.bucketName = bucketName;
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(() -> AwsBasicCredentials.create(accessKey, secretKey))
                .build();
    }

    // Subir
    public String uploadFile(String key, InputStream inputStream, String contentType) throws IOException {
        byte[] bytes = inputStream.readAllBytes();
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build(), RequestBody.fromInputStream(byteArrayInputStream, bytes.length));
        }
        // Construir y retornar la URL del archivo subido
        String fileUrl = s3Client.utilities().getUrl(GetUrlRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build())
                .toExternalForm();
        return fileUrl;
    }

    // Descargar
    public InputStream downloadFile(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        // Obtener el objeto de S3 como un flujo de entrada
        ResponseInputStream<GetObjectResponse> responseInputStream = s3Client.getObject(getObjectRequest);

        // Obtener el cuerpo del objeto como un flujo de entrada (InputStream)
        return responseInputStream;
    }

    //Borrar por nombre
    public void deleteFile(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
    }

    // Obtener nombre de la URL
    private String getKeyFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath();
            // La clave es la parte del path después del primer '/'
            return path.substring(1);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Borrar con la URL
    public void deleteFileByUrl(String fileUrl) {
        String key = getKeyFromUrl(fileUrl); // Extraer la clave (key) del URL
        if (key != null) {
            deleteFile(key); // Eliminar el archivo usando la clave
        }
    }

    public List<String> getAllImageUrls() {
        List<String> imageUrls = new ArrayList<>();

        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

        ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
        List<S3Object> objects = listResponse.contents();

        for (S3Object object : objects) {
            String key = object.key();
            String fileUrl = s3Client.utilities().getUrl(GetUrlRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build())
                    .toExternalForm();

                imageUrls.add(fileUrl);

        }
        return imageUrls;
    }

}
