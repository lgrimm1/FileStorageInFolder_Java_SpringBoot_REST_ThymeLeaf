package lgrimm.service;

import lgrimm.datamodel.*;
import lgrimm.repository.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.core.io.*;
import org.springframework.http.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import static org.mockito.Mockito.*;

class FileInFolderServiceTest {

    FileInFolderRepository repository;
    FileInFolderService service;
    String filename1, filename2, content1, content2, baseUrl;
    Path path1, path2;
    List<Path> paths;
    FileInfo fileInfo1, fileInfo2;
    List<FileInfo> fileInfoList;
    Multipart file1, file2;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(FileInFolderRepository.class);
        when(repository.init("." + File.separator + "uploads", false))
                .thenReturn(true);
        service = new FileInFolderService(repository);
        baseUrl = "localhost:8080";
        filename1 = "file1.txt";
        filename2 = "file2.txt";
        path1 = new File(filename1).toPath();
        path2 = new File(filename2).toPath();
        paths = List.of(path1, path2);
        when(repository.findAll())
                .thenReturn(paths);
        fileInfo1 = new FileInfo(filename1, baseUrl + "/files/" + filename1);
        fileInfo2 = new FileInfo(filename2, baseUrl + "/files/" + filename2);
        fileInfoList = List.of(fileInfo1, fileInfo2);
        content1 = "content1";
        content2 = "content2";
        file1 = new Multipart("file", filename1, MediaType.TEXT_PLAIN_VALUE, content1.getBytes());
        file2 = new Multipart("file", filename2, MediaType.TEXT_PLAIN_VALUE, content2.getBytes());
    }

    @Test
    void init_Fail() {
        String root = "." + File.separator + "uploads";
        when(repository.init(root, false))
                .thenReturn(false);
        when(repository.init(root, true))
                .thenReturn(false);

        Exception e = Assertions.assertThrows(Exception.class, () -> new FileInFolderService(repository));
        Assertions.assertEquals("Could not initialize the file storage!", e.getMessage());
    }

    @Test
    void init_Success() {
        String root = "." + File.separator + "uploads";
        when(repository.init(root, false))
                .thenReturn(true);
        when(repository.init(root, true))
                .thenReturn(true);

        service = Assertions.assertDoesNotThrow(() -> new FileInFolderService(repository));
        Assertions.assertNotNull(service);
    }

    @Test
    void newFile() {
        Payload expectedPayload = new Payload(
                null,
                null,
                null
        );
        Assertions.assertEquals(expectedPayload, service.newFile());
    }

    @Test
    void uploadFile_NullFile() {
        Payload expectedPayload = new Payload(
                "No file was given.",
                null,
                fileInfoList
        );
        Assertions.assertEquals(expectedPayload, service.uploadFile(null, baseUrl));
    }

    @Test
    void uploadFile_WrongFilename() {
        String filename3 = "file3\ntxt";
        String content3 = "content3";
        Multipart file3 = new Multipart("file", filename3, MediaType.TEXT_PLAIN_VALUE, content3.getBytes());
        when(repository.save(file3))
                .thenReturn(Optional.empty());
        Payload expectedPayload = new Payload(
                "Could not upload the file: " + filename3,
                null,
                fileInfoList
        );
        Assertions.assertEquals(expectedPayload, service.uploadFile(file3, baseUrl));
    }

    @Test
    void uploadFile_FileExists() {
        when(repository.save(file1))
                .thenReturn(Optional.empty());
        Payload expectedPayload = new Payload(
                "Could not upload the file: " + filename1,
                null,
                fileInfoList
        );
        Assertions.assertEquals(expectedPayload, service.uploadFile(file1, baseUrl));
    }

    @Test
    void uploadFile_NoSuchFile() {
        when(repository.save(file1))
                .thenReturn(Optional.of(filename1));
        Payload expectedPayload = new Payload(
                filename1 + " file has been successfully uploaded.",
                null,
                fileInfoList
        );
        Assertions.assertEquals(expectedPayload, service.uploadFile(file1, baseUrl));
    }

    @Test
    void uploadFiles_NullFiles() {
        Payload expectedPayload = new Payload(
                null,
                List.of("No files were given."),
                fileInfoList
        );
        Assertions.assertEquals(expectedPayload, service.uploadFiles(null, baseUrl));
    }

    @Test
    void uploadFiles_EmptyFiles() {
        Payload expectedPayload = new Payload(
                null,
                List.of("No files were given."),
                fileInfoList
        );
        Assertions.assertEquals(expectedPayload, service.uploadFiles(new ArrayList<>(), baseUrl));
    }

    @Test
    void uploadFiles_GivenFiles() {
        List<Multipart> filesWithoutNull = new ArrayList<>();
        filesWithoutNull.add(file1);
        filesWithoutNull.add(file2);
        List<Multipart> filesWithNull = new ArrayList<>(filesWithoutNull);
        filesWithNull.add(null);
        when(repository.saveAll(filesWithoutNull))
                .thenReturn(Stream.of(filename1, filename2));
        Payload expectedPayload = new Payload(
                null,
                List.of("Results:", filename1 + ": [Success]", filename2 + ": [Success]"),
                fileInfoList
        );
        Assertions.assertEquals(expectedPayload, service.uploadFiles(filesWithNull, baseUrl));
    }

    @Test
    void getFileList() {
        Payload expectedPayload = new Payload(
                null,
                null,
                fileInfoList
        );
        Assertions.assertEquals(expectedPayload, service.getFileList(baseUrl));
    }

    @Test
    void getFile_NullFilename() {
        Assertions.assertNull(service.getFile(null));
    }

    @Test
    void getFile_BlankFilename() {
        Assertions.assertNull(service.getFile("  "));
    }

    @Test
    void getFile_WrongFilename() {
        String filename3 = "file\ntxt";
        when(repository.getByFilename(filename3))
                .thenReturn(Optional.empty());
        Assertions.assertNull(service.getFile(filename3));
    }

    @Test
    void getFile_NoSuchFile() {
        when(repository.getByFilename(filename1))
                .thenReturn(Optional.empty());
        Assertions.assertNull(service.getFile(filename1));
    }

    @Test
    void getFile_ExistingFile() throws MalformedURLException {
        Resource expectedResource = new UrlResource(path1.toUri());
        when(repository.getByFilename(filename1))
                .thenReturn(Optional.of(expectedResource));
        Assertions.assertEquals(expectedResource, service.getFile(filename1));
    }

    @Test
    void deleteFile_NullFilename() {
        Payload expectedPayload = new Payload(
                "No file was given.",
                null,
                fileInfoList
        );
        Assertions.assertEquals(expectedPayload, service.deleteFile(null, baseUrl));
    }

    @Test
    void deleteFile_BlankFilename() {
        Payload expectedPayload = new Payload(
                "No file was given.",
                null,
                fileInfoList
        );
        Assertions.assertEquals(expectedPayload, service.deleteFile("  ", baseUrl));
    }

    @Test
    void deleteFile_WrongFilename() {
        String filename3 = "file\ntxt";
        when(repository.delete(filename3))
                .thenReturn(false);
        Payload expectedPayload = new Payload(
                filename3 + " file does not exist!",
                null,
                fileInfoList
        );
        Assertions.assertEquals(expectedPayload, service.deleteFile(filename3, baseUrl));
    }

    @Test
    void deleteFile_NoSuchFile() {
        when(repository.delete(filename1))
                .thenReturn(false);
        Payload expectedPayload = new Payload(
                filename1 + " file does not exist!",
                null,
                fileInfoList
        );
        Assertions.assertEquals(expectedPayload, service.deleteFile(filename1, baseUrl));
    }

    @Test
    void deleteFile_FileExists() {
        when(repository.delete(filename1))
                .thenReturn(true);
        Payload expectedPayload = new Payload(
                filename1 + " file has been deleted.",
                null,
                fileInfoList
        );
        Assertions.assertEquals(expectedPayload, service.deleteFile(filename1, baseUrl));
    }

    @Test
    void deleteAllFiles() {
        when(repository.count())
                .thenReturn(2L);
        when(repository.deleteAll())
                .thenReturn(1L);
        paths = List.of(path1);
        when(repository.findAll())
                .thenReturn(paths);
        fileInfoList = List.of(fileInfo1);
        Payload expectedPayload = new Payload(
                "1 of 2 file(s) has been deleted.",
                null,
                fileInfoList
        );
        Assertions.assertEquals(expectedPayload, service.deleteAllFiles(baseUrl));
    }

    @Test
    void handleMaxSizeException() {
        Payload expectedPayload = new Payload(
                "The selected file (or one of them) is too large!",
                null,
                fileInfoList
        );
        Assertions.assertEquals(expectedPayload, service.handleMaxSizeException(baseUrl));
    }
}