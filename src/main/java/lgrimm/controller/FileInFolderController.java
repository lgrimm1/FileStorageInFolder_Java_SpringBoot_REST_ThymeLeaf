package lgrimm.controller;

import jakarta.servlet.http.*;
import lgrimm.datamodel.*;
import lgrimm.service.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.ui.*;
import org.springframework.util.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.*;
import org.springframework.web.servlet.*;
import org.springframework.web.servlet.support.*;

import java.util.*;
import java.util.stream.*;

@RestController
@ControllerAdvice
public class FileInFolderController {

    private final FileInFolderService service;

    @Autowired
    public FileInFolderController(FileInFolderService service) {
        this.service = service;
    }

    @GetMapping("/files/new")
    public ModelAndView newFile(Model model) {
        model.asMap().clear();
        return new ModelAndView("upload", "payload", service.newFile());
    }

    @PostMapping("/files/upload/single")
    public ModelAndView uploadFile(@RequestParam("file") MultipartFile file,
                                   HttpServletRequest request,
                                   Model model) {
        model.asMap().clear();
        Payload payload = service.uploadFile(bindMultipartFileToMultipart(file), detectBaseUrl(request));
        return new ModelAndView("file_list", "payload", payload);
    }

    @PostMapping("/files/upload/multiple")
    public ModelAndView uploadFiles(@RequestParam("files") MultipartFile[] files,
                                    HttpServletRequest request,
                                    Model model) {
        model.asMap().clear();
        Payload payload = service.uploadFiles(bindMultipartFileArrayToMultipartList(files), detectBaseUrl(request));
        return new ModelAndView("file_list", "payload", payload);
    }

    @GetMapping({"/", "/files"})
    public ModelAndView getListFiles(HttpServletRequest request, Model model) {
        model.asMap().clear();
        Payload payload = service.getFileList(detectBaseUrl(request));
        return new ModelAndView("file_list", "payload", payload);
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable("filename") String filename) {
        Resource resource = service.getFile(filename);
        return resource == null ?
                ResponseEntity
                        .badRequest()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "")
                        .body(null) :
                ResponseEntity
                        .ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
    }

    @PostMapping("/files/delete/{filename:.+}")
    public ModelAndView deleteFile(@PathVariable("filename") String filename,
                                   HttpServletRequest request,
                                   Model model) {
        model.asMap().clear();
        Payload payload = service.deleteFile(filename, detectBaseUrl(request));
        return new ModelAndView("file_list", "payload", payload);
    }

    @PostMapping("/files/empty")
    public ModelAndView deleteAllFiles(HttpServletRequest request, Model model) {
        model.asMap().clear();
        Payload payload = service.deleteAllFiles(detectBaseUrl(request));
        return new ModelAndView("file_list", "payload", payload);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView handleMaxSizeException(MaxUploadSizeExceededException e,
                                               HttpServletRequest request,
                                               Model model) {
        model.asMap().clear();
        Payload payload = service.handleMaxSizeException(detectBaseUrl(request));
        return new ModelAndView("file_list", "payload", payload);
    }

    private Multipart bindMultipartFileToMultipart(MultipartFile file) {
        try {
            return new Multipart(file.getName(),
                    StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())),
                    Objects.requireNonNull(file.getContentType()),
                    file.getBytes());
        }
        catch (Exception e) {
            return null;
        }
    }

    private List<Multipart> bindMultipartFileArrayToMultipartList(MultipartFile[] files) {
        if (files == null) {
            return null;
        }
        return Arrays.stream(files)
                .map(this::bindMultipartFileToMultipart)
                .collect(Collectors.toList());
    }

    private String detectBaseUrl(HttpServletRequest request) {
        return ServletUriComponentsBuilder
                .fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();
    }
}
