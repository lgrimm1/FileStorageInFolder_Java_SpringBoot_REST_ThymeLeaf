package lgrimm.controller;

import lgrimm.datamodel.FileInfo;
import lgrimm.datamodel.Multipart;
import lgrimm.datamodel.Payload;
import lgrimm.service.FileInFolderService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.*;

import java.io.File;
import java.nio.file.*;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileInFolderController.class)
class FileInFolderControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private FileInFolderService service;
    String filename1, filename2, content1, content2, baseUrl;
    FileInfo fileInfo1, fileInfo2;
    Multipart multipartOfFile1, multipartOfFiles1, multipartOfFiles2;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost";
        filename1 = "file1.txt";
        filename2 = "file2.txt";
        content1 = "content1";
        content2 = "content2";
        fileInfo1 = new FileInfo(filename1, baseUrl + "/files/" + filename1);
        fileInfo2 = new FileInfo(filename2, baseUrl + "/files/" + filename2);
        multipartOfFile1 = new Multipart("file", filename1, MediaType.TEXT_PLAIN_VALUE, content1.getBytes());
        multipartOfFiles1 = new Multipart("files", filename1, MediaType.TEXT_PLAIN_VALUE, content1.getBytes());
        multipartOfFiles2 = new Multipart("files", filename2, MediaType.TEXT_PLAIN_VALUE, content2.getBytes());
    }

    @Test
    void newFile() throws Exception {
        Payload sentPayload = new Payload(
                null,
                null,
                null
        );
        when(service.newFile())
                .thenReturn(sentPayload);

        mockMvc
                .perform(
                        get("/files/new")
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(view().name("upload"))
                .andExpect(model().size(1))
                .andExpect(model().attribute("payload", sentPayload));
    }

    @Test
    void uploadFile() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "file",
                filename1,
                MediaType.TEXT_PLAIN_VALUE,
                content1.getBytes()
        );
        List<FileInfo> fileInfoList = List.of(fileInfo1);
        Payload sentPayload = new Payload(
                filename1 + " file has been successfully uploaded.",
                null,
                fileInfoList);
        when(service.uploadFile(multipartOfFile1, baseUrl))
                .thenReturn(sentPayload);

        mockMvc.perform(
                        multipart("/files/upload/single")
                                .file(file1)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(view().name("file_list"))
                .andExpect(model().size(1))
                .andExpect(model().attribute("payload", sentPayload));
    }

    @Test
    void uploadFiles() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                filename1,
                MediaType.TEXT_PLAIN_VALUE,
                content1.getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                filename2,
                MediaType.TEXT_PLAIN_VALUE,
                content2.getBytes()
        );
        List<FileInfo> fileInfoList = List.of(fileInfo1, fileInfo2);
        List<String> messages = List.of("messages");
        Payload sentPayload = new Payload(
                null,
                messages,
                fileInfoList);
        List<Multipart> multiparts = List.of(multipartOfFiles1, multipartOfFiles2);
        when(service.uploadFiles(multiparts, baseUrl))
                .thenReturn(sentPayload);

        mockMvc.perform(
                        multipart("/files/upload/multiple")
                                .file(file1)
                                .file(file2)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(view().name("file_list"))
                .andExpect(model().size(1))
                .andExpect(model().attribute("payload", sentPayload));
    }

    @Test
    void getListFiles() throws Exception {
        List<FileInfo> fileInfoList = List.of(fileInfo1, fileInfo2);
        Payload sentPayload = new Payload(
                null,
                null,
                fileInfoList
        );
        when(service.getFileList(baseUrl))
                .thenReturn(sentPayload);

        mockMvc
                .perform(
                        get("/")
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(view().name("file_list"))
                .andExpect(model().size(1))
                .andExpect(model().attribute("payload", sentPayload));

        mockMvc
                .perform(
                        get("/files")
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(view().name("file_list"))
                .andExpect(model().size(1))
                .andExpect(model().attribute("payload", sentPayload));
    }

    @Test
    void getFile() throws Exception {
        String rootName = "." + File.separator + "init_test100";
        Path rootPath = Paths.get(rootName);
        File rootFolder = rootPath.toFile();
        Assertions.assertEquals(rootPath, Files.createDirectories(rootPath));
        Path path = rootPath.resolve(filename1);
        File file = path.toFile();
        file.delete();
        Assertions.assertEquals(path, Files.write(path, content1.getBytes()));
        Resource resource = new UrlResource(path.toUri());
        when(service.getFile(filename1))
                .thenReturn(resource);

        MvcResult result = mockMvc
                .perform(
                        get("/files/" + filename1)
                                .queryParam("filename", filename1)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        Assertions.assertEquals(content1, result.getResponse().getContentAsString());
        Assertions.assertTrue(file.delete());
        Assertions.assertTrue(rootFolder.delete());
    }

    @Test
    void deleteFile() throws Exception {
        List<FileInfo> fileInfoList = List.of(fileInfo2);
        Payload sentPayload = new Payload(
                filename1 + " file has been deleted.",
                null,
                fileInfoList
        );
        when(service.deleteFile(filename1, baseUrl))
                .thenReturn(sentPayload);

        mockMvc
                .perform(
                        post("/files/delete/" + filename1)
                                .queryParam("filename", filename1)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(view().name("file_list"))
                .andExpect(model().size(1))
                .andExpect(model().attribute("payload", sentPayload));
    }

    @Test
    void deleteAllFiles() throws Exception {
        List<FileInfo> fileInfoList = List.of();
        Payload sentPayload = new Payload(
                "2 of 2 file(s) has been deleted.",
                null,
                fileInfoList
        );
        when(service.deleteAllFiles(baseUrl))
                .thenReturn(sentPayload);

        mockMvc
                .perform(
                        post("/files/empty")
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(view().name("file_list"))
                .andExpect(model().size(1))
                .andExpect(model().attribute("payload", sentPayload));
    }
}