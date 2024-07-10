package com.generation.ecommerce.api;

import com.generation.ecommerce.cloud.S3Service;
import com.generation.ecommerce.model.Producto;
import com.generation.ecommerce.service.ProductoServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin("*")
@RestController
@AllArgsConstructor
@RequestMapping("/api/producto")
public class ProductoRestController {

    private final ProductoServiceImpl productoService;

    private final S3Service s3Service;


    @GetMapping("/lista")
    public ResponseEntity<List<Producto>> listaDeProductos() {
        return  new ResponseEntity<>(productoService.listaDeProductos(), HttpStatus.OK);
    }

    @PostMapping("/nuevo")
    public ResponseEntity<Producto> agregarProducto(@RequestPart("file") MultipartFile file,
                                                    @RequestPart("producto") Producto nuevoProducto) {

        try {
            String filename = file.getOriginalFilename();// Obtenemos el nombre del archivo
            String contentType = file.getContentType(); // Obtener el tipo de contenido del archivo

            // Verificar el tipo de contenido y establecer el tipo de contenido correspondiente
            String imageUrl;
            if (contentType != null && contentType.equals("image/jpeg")) {
                imageUrl = s3Service.uploadFile(filename, file.getInputStream(), "image/jpeg");
            } else if (contentType != null && contentType.equals("image/png")) {
                imageUrl = s3Service.uploadFile(filename, file.getInputStream(), "image/png");
            } else {
                // Tipo de contenido no compatible
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            nuevoProducto.setImagen(imageUrl); // Guardar la URL del archivo en el campo de imagen
            Producto productoGuardado = productoService.agregarProducto(nuevoProducto); // Guardar producto en la base de datos
            return new ResponseEntity(productoGuardado, HttpStatus.CREATED);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Manejar error de subida de imagen
        }
    }


    @PutMapping("/editar/{nombreProducto}")
    public ResponseEntity<Producto> editarProducto(@PathVariable String nombreProducto,
                                                   @RequestBody Producto productoEditado) {

            Producto productoActualizado = productoService.editarProducto(productoEditado, nombreProducto);
            return new ResponseEntity(productoActualizado, HttpStatus.OK);
    }

    @DeleteMapping("/borrar/{nombreProducto}")
    public ResponseEntity<String> eliminarProducto(@PathVariable String nombreProducto) {
        productoService.eliminarProducto(nombreProducto);
        return new ResponseEntity<>("Producto eliminado", HttpStatus.OK);
    }

    @GetMapping("/lista/imagenes")
    public ResponseEntity<List<String>> listaDeImagenes() {
        return new ResponseEntity<>(s3Service.getAllImageUrls(),HttpStatus.OK);
    }
}


