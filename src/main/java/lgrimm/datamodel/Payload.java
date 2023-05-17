package lgrimm.datamodel;

import java.util.*;

public class Payload {
    private String message;
    private List<String> messages;
    private List<FileInfo> fileInfos;

    public Payload() {
    }

    public Payload(String message, List<String> messages, List<FileInfo> fileInfos) {
        this.message = message;
        this.messages = messages;
        this.fileInfos = fileInfos;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public List<FileInfo> getFileInfos() {
        return fileInfos;
    }

    public void setFileInfos(List<FileInfo> fileInfos) {
        this.fileInfos = fileInfos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payload payload = (Payload) o;
        return Objects.equals(message, payload.message) &&
                Objects.equals(messages, payload.messages) &&
                Objects.equals(fileInfos, payload.fileInfos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, messages, fileInfos);
    }

    @Override
    public String toString() {
        return "Payload{" +
                "message='" + message + '\'' +
                ", messages=" + messages +
                ", fileInfos=" + fileInfos +
                '}';
    }
}
