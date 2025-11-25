package knu.atoz.document.exception;

public class DocumentFileNotExistException extends DocumentException {
    public DocumentFileNotExistException() {
        super("파일을 첨부해야 합니다.");
    }
}
