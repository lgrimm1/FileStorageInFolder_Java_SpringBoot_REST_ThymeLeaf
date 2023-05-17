package lgrimm.service;

import lgrimm.datamodel.*;
import lgrimm.repository.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.core.io.*;
import org.springframework.stereotype.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

@Service
public class FileInFolderService {
    private final FileInFolderRepository repository;

    @Autowired
    public FileInFolderService(FileInFolderRepository repository) {
        this.repository = repository;
        if (!repository.init("." + File.separator + "uploads", false)) {
            throw new RuntimeException("Could not initialize the file storage!");
        }
    }

    public Payload newFile() {
        return new Payload(
                null,
                null,
                null);
    }

    public Payload uploadFile(Multipart file, String baseUrl) {
        if (file == null) {
            return new Payload(
                    "No file was given.",
                    null,
                    convertPathListToFileInfoList(repository.findAll(), baseUrl)
            );
        }
        String message = repository.save(file)
                .map(filename -> filename + " file has been successfully uploaded.")
                .orElseGet(() -> "Could not upload the file: " + file.getOriginalFilename());
        return new Payload(
                message,
                null,
                convertPathListToFileInfoList(repository.findAll(), baseUrl)
        );
    }

    public Payload uploadFiles(List<Multipart> files, String baseUrl) {
        if (files == null || files.size() == 0) {
            return new Payload(
                    null,
                    List.of("No files were given."),
                    convertPathListToFileInfoList(repository.findAll(), baseUrl)
            );
        }
        files = files.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<String> saved = repository.saveAll(files).toList();
        List<String> messages = files.stream()
                .map(Multipart::getOriginalFilename)
                .map(filename -> {
                    if (saved.contains(filename)) {
                        return filename + ": [Success]";
                    }
                    else {
                        return filename + ": [Failed]";
                    }
                })
                .collect(Collectors.toList());
        messages.add(0, "Results:");
        return new Payload(
                null,
                messages,
                convertPathListToFileInfoList(repository.findAll(), baseUrl)
        );
    }

    public Payload getFileList(String baseUrl) {
        return new Payload(
                null,
                null,
                convertPathListToFileInfoList(repository.findAll(), baseUrl)
        );
    }

    public Resource getFile(String filename) {
        if (filename == null || filename.isBlank()) {
            return null;
        }
        return repository.getByFilename(filename)
                .orElse(null);
    }

    public Payload deleteFile(String filename, String baseUrl) {
        if (filename == null || filename.isBlank()) {
            return new Payload(
                    "No file was given.",
                    null,
                    convertPathListToFileInfoList(repository.findAll(), baseUrl)
            );
        }
        String message;
        try {
            boolean existed = repository.delete(filename);
            if (existed) {
                message = filename + " file has been deleted.";
            }
            else {
                message = filename + " file does not exist!";
            }
        }
        catch (Exception e) {
            message = "Could not delete the file: " + filename + ". Error: " + e.getMessage();
        }
        return new Payload(
                message,
                null,
                convertPathListToFileInfoList(repository.findAll(), baseUrl)
        );
    }

    public Payload deleteAllFiles(String baseUrl) {
        long count = repository.count();
        long deleted = repository.deleteAll();
        return new Payload(
                deleted + " of " + count + " file(s) has been deleted.",
                null,
                convertPathListToFileInfoList(repository.findAll(), baseUrl)
        );
    }

    public Payload handleMaxSizeException(String baseUrl) {
        return new Payload(
                "The selected file (or one of them) is too large!",
                null,
                convertPathListToFileInfoList(repository.findAll(), baseUrl)
        );
    }

    private List<FileInfo> convertPathListToFileInfoList(List<Path> pathList, String baseUrl) {
        return pathList.stream()
                .map(path -> {
                    String filename = path.getFileName().toString();
                    String url = baseUrl + "/files/" + filename;
                    return new FileInfo(filename, url);
                })
                .toList();
    }
}
