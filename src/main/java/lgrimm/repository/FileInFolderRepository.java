package lgrimm.repository;

import lgrimm.datamodel.*;
import org.springframework.core.io.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

@Repository
public class FileInFolderRepository {

    private Path root;

    public boolean init(String repositoryPath, boolean deleteAllFromStorage) {
        try {
            this.root = Paths.get(repositoryPath);
            if (deleteAllFromStorage) {
                FileSystemUtils.deleteRecursively(root.toFile());
            }
            Files.createDirectories(root);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public Optional<Resource> getByFilename(String filename) {
        try {
            Path path = root.resolve(filename);
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() || resource.isReadable()) {
                return Optional.of(resource);
            }
            else {
                return Optional.empty();
            }
        }
        catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<Resource> getAll() {
        try (Stream<Path> walk = Files.walk(this.root, 1)) {
            return walk
                    .filter(path -> !path.equals(this.root))
                    .map(this.root::relativize)
                    .map(path -> path.toFile().getName())
                    .map(this::getByFilename)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
        }
        catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public Optional<Path> findByFilename(String filename) {
        try {
            Path path = this.root.resolve(filename);
            if (path.toFile().exists() || path.toFile().isFile()) {
                return Optional.of(root.relativize(path));
            }
            return Optional.empty();
        }
        catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<Path> findAll() {
        try (Stream<Path> walk = Files.walk(this.root, 1)) {
            return walk
                    .filter(path -> !path.equals(this.root))
                    .map(this.root::relativize)
                    .toList();
        }
        catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public Optional<String> save(Multipart file) {
        try {
            Path path = this.root.resolve(file.getOriginalFilename());
            Files.copy(new ByteArrayInputStream(file.getContent()), path);
            return Optional.of(file.getOriginalFilename());
        }
        catch (Exception e) {
            return Optional.empty();
        }
    }

    public Stream<String> saveAll(List<Multipart> files) {
        return files.stream()
                .map(this::save)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    public boolean delete(String filename) {
        try {
            Path file = root.resolve(filename);
            return Files.deleteIfExists(file);
        }
        catch (Exception e) {
            return false;
        }
    }

    public long deleteAll() {
        try (Stream<Path> walk = Files.walk(this.root, 1)) {
            return walk
                    .filter(path -> !path.equals(this.root))
                    .map(Path::toFile)
                    .map(File::delete)
                    .filter(success -> success)
                    .count();
        }
        catch (Exception e) {
            return 0;
        }
    }

    public long count() {
        try (Stream<Path> walk = Files.walk(this.root, 1)) {
            return walk
                    .filter(path -> !path.equals(this.root))
                    .count();
        }
        catch (Exception e) {
            return -1;
        }
    }
}
