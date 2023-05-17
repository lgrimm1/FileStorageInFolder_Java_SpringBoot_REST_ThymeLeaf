package lgrimm.datamodel;

import org.springframework.lang.*;

import java.util.*;

public class Multipart {

    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final byte[] content;

    public Multipart(@NonNull String name,
                     @NonNull String originalFilename,
                     @NonNull String contentType,
                     @NonNull byte[] content) {
        this.name = name;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Multipart multipart = (Multipart) o;
        return Objects.equals(name, multipart.name) &&
                Objects.equals(originalFilename, multipart.originalFilename) &&
                Objects.equals(contentType, multipart.contentType) &&
                Arrays.equals(content, multipart.content);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name, originalFilename, contentType);
        result = 31 * result + Arrays.hashCode(content);
        return result;
    }

    @Override
    public String toString() {
        return "Multipart{" +
                "name='" + name + '\'' +
                ", originalFilename='" + originalFilename + '\'' +
                ", contentType='" + contentType + '\'' +
                ", content=" + Arrays.toString(content) +
                '}';
    }
}
