package com.kmkbe.modules;

import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.repository.OtpRepository;
import com.kmkbe.core.service.FileStorageService;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.core.utils.RedisUtils;
import com.kmkbe.modules.customer.service.CustomerSeederService;
import com.kmkbe.modules.remote.request.ExistingCustomerRequest;
import com.kmkbe.modules.remote.service.CustomerRemoteService;
import com.kmkbe.modules.user.service.UserInternalSeederService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/testing")
public class TestingController {
    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ListOperations<String, Object> listOperations;
    private final OtpRepository otpRepository;
    private final FileStorageService fileStorageService;
    private final CustomerSeederService seederService;
    private final RedisUtils redisUtils;
    private final CustomerRemoteService customerRemoteService;
    private final UserInternalSeederService userInternalSeederService;


    private final Path fileStorageLocation = Paths.get("uploads")
            .toAbsolutePath().normalize();

    @GetMapping("/get")
    public CommonResult<Object> get() {
        //var a = redisUtils.getValue("khesatoken");
       /* Cache cache = cacheManager.getCache("refreshToken");
        if (cache != null) {
            String res = cache.get("khesatoken", Object.class).toString();
            return new CommonResult<>().success(res, "ok");
        }*/

        //refreshTokenService.create(UUID.randomUUID(), "1111");

        /*List<Object> list = new ArrayList<>();
        List<String> keys = redisUtils.getAllKeys();
        for (String key : keys) {
            list.add(redisUtils.getValue(key));
        }*/

        var a = customerRemoteService.validateExisting(
                ExistingCustomerRequest.builder()
                        .args(ExistingCustomerRequest.Args.builder()
                                .key("IdNo")
                                .operator("EQ")
                                .value("010002509057000")
                                .build()
                        )
                        .includeProperties(new ArrayList<>())
                        .requestDateTime(DateTimeUtils.SDF_STANDARD_DATE.format(new Date()))
                        .build()
        );
        return new CommonResult<>().success(a, "ok");
    }

    @GetMapping("/getAll")
    public CommonResult<Object> getAll() {
        var b = cacheManager.getCacheNames()
                .stream()
                .parallel()
                .map((cacheName) -> {
                    Cache cache = cacheManager.getCache(cacheName);

                    return cache.get("khesatoken", Object.class).toString();
                })
                .collect(Collectors.toCollection(ArrayList::new));

        return new CommonResult<>().success(null, "ok");
    }

    @GetMapping("/add")
    public CommonResult<Object> add() {
        redisUtils.putValue("khesatoken", "1111");
        //seederService.seed();
        return new CommonResult<>().success(null, "failed, cache null");
    }

    @GetMapping("/load-img")
    public CommonResult<Object> testingCount() {
        return new CommonResult<>().success(fileStorageService.load("Banner Jawa Tengah.jpg"));
    }

    @GetMapping("/seed")
    public CommonResult<Object> seedUserInternal() {
        userInternalSeederService.seed();
        return new CommonResult<>().success(
                null
        );
    }


    @PostMapping("/uploadFile")
    public CommonResult<Object> uploadFile(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request
    ) throws IOException {
        /*String fileName = storeFile(file);*/


        String uploadPath = request.getServletContext().getRealPath("resources/");
        System.out.println(uploadPath);
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = file.getOriginalFilename();
        fileName = UUID.randomUUID() + "." + fileName.substring(fileName.lastIndexOf("."));
        Files.copy(file.getInputStream(), Paths.get(uploadPath, fileName));
        System.out.println(uploadPath + File.separator + fileName);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();

        return new CommonResult<>().success(fileDownloadUri);
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<?> download(
            @PathVariable String id
//            ,@RequestParam(value = "token", defaultValue = "") String token
    ) throws Exception {
        Path filePath = Paths.get("uploads", id);

        InputStream inputStream = new FileInputStream(filePath.toFile());
        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

        HttpHeaders headers = new HttpHeaders();
        String mimeType = Files.probeContentType(filePath);

        if (mimeType == null) {
            mimeType = "application/octet-stream"; // Fallback MIME type
        }

        headers.setContentType(MediaType.parseMediaType(mimeType));
        headers.setContentLength(Files.size(filePath));
        headers.setContentDispositionFormData("attachment", id);

        return new ResponseEntity<>(inputStreamResource, headers, HttpStatus.OK);
    }


    public String storeFile(MultipartFile file) {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not create the directory where the uploaded files will be stored.", ex);
        }

        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new IllegalStateException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new IllegalStateException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new IllegalStateException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("File not found " + fileName, ex);
        }
    }
}
