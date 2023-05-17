package lgrimm.repository;

import lgrimm.datamodel.*;
import org.junit.jupiter.api.*;
import org.springframework.core.io.*;
import org.springframework.http.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.*;

class FileInFolderRepositoryTest {

    FileInFolderRepository repository;
    AtomicInteger testFolderNumber = new AtomicInteger();
    String filename1, filename2;

    @BeforeEach
    void setUp() {
        repository = new FileInFolderRepository();
        filename1 = "file1.txt";
        filename2 = "file2.txt";
    }

    @Test
    void fileNameSeparator() {
        Assertions.assertTrue(File.separator.equals("/") || File.separator.equals("\\"));
    }

    @Test
    void init_Failed() {
        String root = "." + File.separator + "init\ntest" + testFolderNumber.getAndIncrement();
        Assertions.assertFalse(repository.init(root, false));
        Assertions.assertFalse(repository.init(root, true));
    }

    @Test
    void init_NoDirectory() {
        String root = "." + File.separator + "init_test" + testFolderNumber.getAndIncrement();
        File rootFolder = Paths.get(root).toFile();
        if (rootFolder.exists()) {
            Assertions.assertTrue(deleteAllFromDirectory(rootFolder));
            Assertions.assertTrue(rootFolder.delete());
        }

        Assertions.assertTrue(repository.init(root, false));
        Assertions.assertTrue(rootFolder.exists());
        Assertions.assertEquals(0, Objects.requireNonNull(rootFolder.listFiles()).length);
        Assertions.assertTrue(rootFolder.delete());

        Assertions.assertTrue(repository.init(root, true));
        Assertions.assertTrue(rootFolder.exists());
        Assertions.assertEquals(0, Objects.requireNonNull(rootFolder.listFiles()).length);
        Assertions.assertTrue(rootFolder.delete());
    }

    @Test
    void init_EmptyDirectory() {
        String root = "." + File.separator + "init_test" + testFolderNumber.getAndIncrement();
        File rootFolder = Paths.get(root).toFile();
        if (rootFolder.exists()) {
            Assertions.assertTrue(deleteAllFromDirectory(rootFolder));
        }
        else {
            Assertions.assertTrue(rootFolder.mkdir());
        }

        Assertions.assertTrue(repository.init(root, false));
        Assertions.assertTrue(rootFolder.exists());
        Assertions.assertEquals(0, Objects.requireNonNull(rootFolder.listFiles()).length);

        Assertions.assertTrue(repository.init(root, true));
        Assertions.assertTrue(rootFolder.exists());
        Assertions.assertEquals(0, Objects.requireNonNull(rootFolder.listFiles()).length);
        Assertions.assertTrue(rootFolder.delete());
    }

    @Test
    void init_FilesInDirectory() {
        String root = "." + File.separator + "init_test" + testFolderNumber.getAndIncrement();
        File rootFolder = Paths.get(root).toFile();
        if (rootFolder.exists()) {
            Assertions.assertTrue(deleteAllFromDirectory(rootFolder));
        }
        else {
            Assertions.assertTrue(rootFolder.mkdir());
        }
        File file1 = new File(root + File.separator + filename1);
        Assertions.assertDoesNotThrow(file1::createNewFile);
        File file2 = new File(root + File.separator + filename2);
        Assertions.assertDoesNotThrow(file2::createNewFile);
        File[] files = rootFolder.listFiles();

        Assertions.assertTrue(repository.init(root, false));
        Assertions.assertTrue(rootFolder.exists());
        Assertions.assertArrayEquals(files, rootFolder.listFiles());

        Assertions.assertTrue(repository.init(root, true));
        Assertions.assertTrue(rootFolder.exists());
        Assertions.assertEquals(0, Objects.requireNonNull(rootFolder.listFiles()).length);
        Assertions.assertTrue(deleteAllFromDirectory(rootFolder));
        Assertions.assertTrue(rootFolder.delete());
    }

    @Test
    void getByFilename_WrongFilename() {
        String root = "." + File.separator + "init_test" + testFolderNumber.getAndIncrement();
        File rootFolder = Paths.get(root).toFile();
        Assertions.assertTrue(repository.init(root, true));
        String filename = "file1\ntxt";

        Assertions.assertTrue(repository.getByFilename(filename).isEmpty());
        Assertions.assertTrue(rootFolder.delete());
    }

    @Test
    void getByFilename_NoSuchFile() {
        String root = "." + File.separator + "init_test" + testFolderNumber.getAndIncrement();
        File rootFolder = Paths.get(root).toFile();
        Assertions.assertTrue(repository.init(root, true));

        Assertions.assertTrue(repository.getByFilename(filename1).isEmpty());
        Assertions.assertTrue(rootFolder.delete());
    }

    @Test
    void getByFilename_FileExists() {
        String root = "." + File.separator + "init_test" + testFolderNumber.getAndIncrement();
        File rootFolder = Paths.get(root).toFile();
        Assertions.assertTrue(repository.init(root, true));
        File file1 = new File(root + File.separator + filename1);
        Assertions.assertDoesNotThrow(file1::createNewFile);

        Optional<Resource> resource = repository.getByFilename(filename1);
        Assertions.assertTrue(resource.isPresent());
        Assertions.assertEquals(file1.getName(), resource.get().getFilename());
        Assertions.assertTrue(deleteAllFromDirectory(rootFolder));
        Assertions.assertTrue(rootFolder.delete());
    }

    @Test
    void getAll() {
        String root = "." + File.separator + "init_test" + testFolderNumber.getAndIncrement();
        File rootFolder = Paths.get(root).toFile();
        Assertions.assertTrue(repository.init(root, true));
        File file1 = new File(root + File.separator + filename1);
        Assertions.assertDoesNotThrow(file1::createNewFile);
        File file2 = new File(root + File.separator + filename2);
        Assertions.assertDoesNotThrow(file2::createNewFile);

        List<Resource> resources = repository.getAll();
        Assertions.assertEquals(2, resources.size());
        Assertions.assertEquals(file1.getName(), resources.get(0).getFilename());
        Assertions.assertEquals(file2.getName(), resources.get(1).getFilename());
        Assertions.assertTrue(deleteAllFromDirectory(rootFolder));
        Assertions.assertTrue(rootFolder.delete());
    }

    @Test
    void findByFilename_WrongFilename() {
        String root = "." + File.separator + "init_test" + testFolderNumber.getAndIncrement();
        File rootFolder = Paths.get(root).toFile();
        Assertions.assertTrue(repository.init(root, true));
        File file1 = new File(root + File.separator + filename1);
        Assertions.assertDoesNotThrow(file1::createNewFile);
        File file2 = new File(root + File.separator + filename2);
        Assertions.assertDoesNotThrow(file2::createNewFile);

        Assertions.assertTrue(repository.findByFilename("file1\ntxt").isEmpty());
        Assertions.assertTrue(deleteAllFromDirectory(rootFolder));
        Assertions.assertTrue(rootFolder.delete());
    }

    @Test
    void findByFilename_NoSuchFile() {
        String root = "." + File.separator + "init_test" + testFolderNumber.getAndIncrement();
        File rootFolder = Paths.get(root).toFile();
        Assertions.assertTrue(repository.init(root, true));
        File file1 = new File(root + File.separator + filename1);
        Assertions.assertDoesNotThrow(file1::createNewFile);
        File file2 = new File(root + File.separator + filename2);
        Assertions.assertDoesNotThrow(file2::createNewFile);

        Assertions.assertTrue(repository.findByFilename("file3.txt").isEmpty());
        Assertions.assertTrue(deleteAllFromDirectory(rootFolder));
        Assertions.assertTrue(rootFolder.delete());
    }

    @Test
    void findByFilename_FileExists() {
        String root = "." + File.separator + "init_test" + testFolderNumber.getAndIncrement();
        File rootFolder = Paths.get(root).toFile();
        Assertions.assertTrue(repository.init(root, true));
        File file1 = new File(root + File.separator + filename1);
        Assertions.assertDoesNotThrow(file1::createNewFile);
        File file2 = new File(root + File.separator + filename2);
        Assertions.assertDoesNotThrow(file2::createNewFile);

        Optional<Path> path = repository.findByFilename(filename2);
        Assertions.assertTrue(path.isPresent());
        Assertions.assertEquals(filename2, path.get().toFile().getName());
        Assertions.assertTrue(deleteAllFromDirectory(rootFolder));
        Assertions.assertTrue(rootFolder.delete());
    }

    @Test
    void findAll() {
        String root = "." + File.separator + "init_test" + testFolderNumber.getAndIncrement();
        File rootFolder = Paths.get(root).toFile();
        Assertions.assertTrue(repository.init(root, true));
        File file1 = new File(root + File.separator + filename1);
        Assertions.assertDoesNotThrow(file1::createNewFile);
        File file2 = new File(root + File.separator + filename2);
        Assertions.assertDoesNotThrow(file2::createNewFile);

        List<Path> paths = repository.findAll();
        Assertions.assertEquals(2, paths.size());
        Assertions.assertEquals(filename1, paths.get(0).toFile().getName());
        Assertions.assertEquals(filename2, paths.get(1).toFile().getName());
        Assertions.assertTrue(deleteAllFromDirectory(rootFolder));
        Assertions.assertTrue(rootFolder.delete());
    }

    @Test
    void save_WrongFilename() {
        String root = "." + File.separator + "init_test" + testFolderNumber.getAndIncrement();
        File rootFolder = Paths.get(root).toFile();
        Assertions.assertTrue(repository.init(root, true));
        String filename = "file\ntxt";
        String content = "content";
        Multipart file = new Multipart("file", filename, MediaType.TEXT_PLAIN_VALUE, content.getBytes());

        Assertions.assertTrue(repository.save(file).isEmpty());
        List<Path> paths = repository.findAll();
        Assertions.assertEquals(0, paths.size());
        Assertions.assertTrue(rootFolder.delete());
    }

    @Test
    void save_ExistingFile() {
        String root = "." + File.separator + "init_test" + testFolderNumber.getAndIncrement();
        File rootFolder = Paths.get(root).toFile();
        Assertions.assertTrue(repository.init(root, true));
        String filename3 = "file2.txt";
        String content3 = "content";
        Multipart file = new Multipart("file", filename3, MediaType.TEXT_PLAIN_VALUE, content3.getBytes());
        File file1 = new File(root + File.separator + filename1);
        Assertions.assertDoesNotThrow(file1::createNewFile);
        File file2 = new File(root + File.separator + filename2);
        Assertions.assertDoesNotThrow(file2::createNewFile);

        Assertions.assertTrue(repository.save(file).isEmpty());
        List<Path> paths = repository.findAll();
        Assertions.assertEquals(2, paths.size());
        Assertions.assertEquals(filename1, paths.get(0).toFile().getName());
        Assertions.assertEquals(filename2, paths.get(1).toFile().getName());
        Assertions.assertTrue(deleteAllFromDirectory(rootFolder));
        Assertions.assertTrue(rootFolder.delete());
    }

    @Test
    void save_NoSuchFile() {
        String root = "." + File.separator + "init_test" + testFolderNumber.getAndIncrement();
        File rootFolder = Paths.get(root).toFile();
        Assertions.assertTrue(repository.init(root, true));
        String filename3 = "file3.txt";
        String content3 = "content";
        Multipart file = new Multipart("file", filename3, MediaType.TEXT_PLAIN_VALUE, content3.getBytes());
        File file1 = new File(root + File.separator + filename1);
        Assertions.assertDoesNotThrow(file1::createNewFile);
        File file2 = new File(root + File.separator + filename2);
        Assertions.assertDoesNotThrow(file2::createNewFile);

        Optional<String> savedFilename = repository.save(file);
        Assertions.assertTrue(savedFilename.isPresent());
        Assertions.assertEquals(filename3, savedFilename.get());
        List<Path> paths = repository.findAll();
        Assertions.assertEquals(3, paths.size());
        Assertions.assertEquals(filename1, paths.get(0).toFile().getName());
        Assertions.assertEquals(filename2, paths.get(1).toFile().getName());
        Assertions.assertEquals(filename3, paths.get(2).toFile().getName());
        Assertions.assertTrue(deleteAllFromDirectory(rootFolder));
        Assertions.assertTrue(rootFolder.delete());
    }

    @Test
    void saveAll() {
        String root = "." + File.separator + "init_test" + testFolderNumber.getAndIncrement();
        File rootFolder = Paths.get(root).toFile();
        Assertions.assertTrue(repository.init(root, true));
        String content1 = "content";
        Multipart file1 = new Multipart("file", filename1, MediaType.TEXT_PLAIN_VALUE, content1.getBytes());
        String content2 = "content";
        Multipart file2 = new Multipart("file", filename2, MediaType.TEXT_PLAIN_VALUE, content2.getBytes());
        List<Multipart> files = List.of(file1, file2);

        List<String> savedFilenames = repository.saveAll(files).toList();
        Assertions.assertEquals(2, savedFilenames.size());
        Assertions.assertEquals(filename1, savedFilenames.get(0));
        Assertions.assertEquals(filename2, savedFilenames.get(1));
        List<Path> paths = repository.findAll();
        Assertions.assertEquals(2, paths.size());
        Assertions.assertEquals(filename1, paths.get(0).toFile().getName());
        Assertions.assertEquals(filename2, paths.get(1).toFile().getName());
        Assertions.assertTrue(deleteAllFromDirectory(rootFolder));
        Assertions.assertTrue(rootFolder.delete());
    }

    @Test
    void delete_WrongFilename() {
        String root = "." + File.separator + "init_test" + testFolderNumber.getAndIncrement();
        File rootFolder = Paths.get(root).toFile();
        Assertions.assertTrue(repository.init(root, true));
        File file1 = new File(root + File.separator + filename1);
        Assertions.assertDoesNotThrow(file1::createNewFile);
        File file2 = new File(root + File.separator + filename2);
        Assertions.assertDoesNotThrow(file2::createNewFile);

        Assertions.assertFalse(repository.delete("file1\ntxt"));
        List<Path> paths = repository.findAll();
        Assertions.assertEquals(2, paths.size());
        Assertions.assertEquals(filename1, paths.get(0).toFile().getName());
        Assertions.assertEquals(filename2, paths.get(1).toFile().getName());
        Assertions.assertTrue(deleteAllFromDirectory(rootFolder));
        Assertions.assertTrue(rootFolder.delete());
    }

    @Test
    void delete_NoSuchFile() {
        String root = "." + File.separator + "init_test" + testFolderNumber.getAndIncrement();
        File rootFolder = Paths.get(root).toFile();
        Assertions.assertTrue(repository.init(root, true));
        File file1 = new File(root + File.separator + filename1);
        Assertions.assertDoesNotThrow(file1::createNewFile);
        File file2 = new File(root + File.separator + filename2);
        Assertions.assertDoesNotThrow(file2::createNewFile);

        Assertions.assertFalse(repository.delete("file3.txt"));
        List<Path> paths = repository.findAll();
        Assertions.assertEquals(2, paths.size());
        Assertions.assertEquals(filename1, paths.get(0).toFile().getName());
        Assertions.assertEquals(filename2, paths.get(1).toFile().getName());
        Assertions.assertTrue(deleteAllFromDirectory(rootFolder));
        Assertions.assertTrue(rootFolder.delete());
    }

    @Test
    void delete_FileExists() {
        String root = "." + File.separator + "init_test" + testFolderNumber.getAndIncrement();
        File rootFolder = Paths.get(root).toFile();
        Assertions.assertTrue(repository.init(root, true));
        File file1 = new File(root + File.separator + filename1);
        Assertions.assertDoesNotThrow(file1::createNewFile);
        File file2 = new File(root + File.separator + filename2);
        Assertions.assertDoesNotThrow(file2::createNewFile);

        Assertions.assertTrue(repository.delete(filename2));
        List<Path> paths = repository.findAll();
        Assertions.assertEquals(1, paths.size());
        Assertions.assertEquals(filename1, paths.get(0).toFile().getName());
        Assertions.assertTrue(deleteAllFromDirectory(rootFolder));
        Assertions.assertTrue(rootFolder.delete());
    }

    @Test
    void DeleteAll() {
        String root = "." + File.separator + "init_test" + testFolderNumber.getAndIncrement();
        File rootFolder = Paths.get(root).toFile();
        Assertions.assertTrue(repository.init(root, true));
        File file1 = new File(root + File.separator + filename1);
        Assertions.assertDoesNotThrow(file1::createNewFile);
        File file2 = new File(root + File.separator + filename2);
        Assertions.assertDoesNotThrow(file2::createNewFile);

        Assertions.assertEquals(2, repository.deleteAll());
        List<Path> paths = repository.findAll();
        Assertions.assertEquals(0, paths.size());
        Assertions.assertTrue(rootFolder.delete());
    }

    @Test
    void count() {
        String root = "." + File.separator + "init_test" + testFolderNumber.getAndIncrement();
        File rootFolder = Paths.get(root).toFile();
        Assertions.assertTrue(repository.init(root, true));
        File file1 = new File(root + File.separator + filename1);
        Assertions.assertDoesNotThrow(file1::createNewFile);
        File file2 = new File(root + File.separator + filename2);
        Assertions.assertDoesNotThrow(file2::createNewFile);

        Assertions.assertEquals(2, repository.count());
        Assertions.assertTrue(deleteAllFromDirectory(rootFolder));
        Assertions.assertTrue(rootFolder.delete());
    }

    private boolean deleteAllFromDirectory(File root) {
        File[] files = root.listFiles();
        if (files == null) {
            return false;
        }
        if (files.length == 0) {
            return true;
        }
        return Arrays.stream(files)
                .filter(File::delete)
                .count() == files.length;
    }
}