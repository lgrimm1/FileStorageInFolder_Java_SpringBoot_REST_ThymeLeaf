package lgrimm;

import lgrimm.controller.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FileStorageApplicationTest {

    @Autowired
    FileInFolderController fileInFolderController;

    @Test
    public void contextLoads() throws Exception {
        Assertions.assertNotNull(fileInFolderController);
    }
}