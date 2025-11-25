package knu.atoz.document;

import knu.atoz.document.dto.DocumentRequestDto;
import knu.atoz.document.exception.DocumentAccessException;
import knu.atoz.document.exception.DocumentFileNotExistException;
import knu.atoz.document.exception.DocumentNotFoundException;
import knu.atoz.document.exception.InvalidDocumentInputException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;

    // 파일 저장 기본 경로 (프로젝트 루트/uploads)
    private final String uploadDir = System.getProperty("user.dir") + "/uploads";

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    /**
     * 문서 생성
     */
    public void createDocument(Long projectId, DocumentRequestDto dto) {
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new InvalidDocumentInputException("문서 제목을 입력해주세요.");
        }

        try {
            String savedPath = saveFile(dto.getFile());
            if (savedPath == null) {
                throw new DocumentFileNotExistException();
            }

            Document document = new Document(projectId, dto.getTitle(), savedPath);
            documentRepository.save(document);

        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    public List<Document> getDocumentsByProject(Long projectId) {
        return documentRepository.findByProjectId(projectId);
    }

    public Document getDocument(Long documentId) {
        Document document = documentRepository.findById(documentId);
        if (document == null) {
            throw new DocumentNotFoundException();
        }
        return document;
    }

    /**
     * 문서 수정 (기존 파일 삭제 로직 강화)
     */
    public void updateDocument(Long documentId, Long projectId, DocumentRequestDto requestDto) {
        Document targetDocument = documentRepository.findById(documentId);
        if (targetDocument == null) throw new DocumentNotFoundException();

        if (!targetDocument.getProjectId().equals(projectId)) {
            throw new DocumentAccessException("해당 문서는 이 프로젝트에 속하지 않아 수정할 수 없습니다.");
        }

        if (requestDto.getTitle() == null || requestDto.getTitle().trim().isEmpty()) {
            throw new InvalidDocumentInputException("제목은 비워둘 수 없습니다.");
        }

        try {
            // [중요] 기존 경로를 별도 변수에 확실하게 백업
            String oldPath = targetDocument.getLocation();
            String newPath = oldPath; // 기본값: 변경 없음

            // 새 파일이 업로드되었는지 확인
            if (requestDto.getFile() != null && !requestDto.getFile().isEmpty()) {
                // 1. 새 파일 저장
                newPath = saveFile(requestDto.getFile());

                // 2. 기존 파일 물리적 삭제 (백업해둔 oldPath 사용)
                System.out.println("[Update] 기존 파일 삭제 시도: " + oldPath);
                deletePhysicalFile(oldPath);
            }

            // 3. DB 업데이트
            Document updateDoc = new Document(documentId, projectId, requestDto.getTitle(), newPath);
            documentRepository.update(updateDoc);

        } catch (IOException e) {
            throw new RuntimeException("파일 수정 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 문서 삭제
     */
    public void deleteDocument(Long documentId, Long expectedProjectId) {
        Document targetDocument = documentRepository.findById(documentId);
        if (targetDocument == null) throw new DocumentNotFoundException();

        if (!targetDocument.getProjectId().equals(expectedProjectId)) {
            throw new DocumentAccessException("해당 문서는 이 프로젝트에 속하지 않아 삭제할 수 없습니다.");
        }

        // [중요] 삭제할 경로 백업
        String pathToDelete = targetDocument.getLocation();

        // 1. DB 삭제
        documentRepository.delete(documentId);

        // 2. 물리적 파일 삭제
        System.out.println("[Delete] 파일 삭제 시도: " + pathToDelete);
        deletePhysicalFile(pathToDelete);
    }

    /**
     * 다운로드용 파일 객체 반환
     */
    public File getPhysicalFile(Long documentId) {
        Document document = getDocument(documentId);
        File file = getFileFromPath(document.getLocation());

        if (!file.exists()) {
            throw new DocumentFileNotExistException();
        }
        return file;
    }

    // --- Private Helper Methods ---

    // 파일 저장
    private String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;

        File directory = new File(uploadDir);
        if (!directory.exists()) directory.mkdirs();

        UUID uuid = UUID.randomUUID();
        String originalFilename = file.getOriginalFilename();
        // 파일명에 한글이 있을 경우 안전하게 처리
        if (originalFilename == null) originalFilename = "unknown_file";

        String fileName = uuid + "_" + originalFilename;
        File saveFile = new File(directory, fileName);

        file.transferTo(saveFile);

        return "/uploads/" + fileName;
    }

    // 물리적 파일 삭제
    private void deletePhysicalFile(String dbPath) {
        if (dbPath == null || dbPath.isEmpty()) return;

        try {
            File file = getFileFromPath(dbPath);

            // 디버깅용 로그
            System.out.println("   -> 삭제 대상 경로: " + file.getAbsolutePath());
            System.out.println("   -> 파일 존재 여부: " + file.exists());

            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    System.out.println("   -> [성공] 파일이 삭제되었습니다.");
                } else {
                    System.err.println("   -> [실패] 파일 삭제에 실패했습니다. (권한 또는 사용 중)");
                }
            } else {
                System.out.println("   -> [무시] 삭제할 파일이 이미 없습니다.");
            }
        } catch (Exception e) {
            System.err.println("   -> [에러] 파일 삭제 중 예외 발생: " + e.getMessage());
        }
    }

    // DB 경로 -> 실제 File 객체 변환
    private File getFileFromPath(String dbPath) {
        // dbPath 예시: "/uploads/uuid_파일명.pdf"
        // 윈도우/맥 경로 구분자 차이 해결을 위해 replace 사용
        String cleanPath = dbPath.replace("\\", "/");

        // 마지막 슬래시 뒤의 파일명만 추출
        String fileName = cleanPath.substring(cleanPath.lastIndexOf("/") + 1);

        // URL 디코딩 (파일명에 공백이나 특수문자가 있을 경우 대비)
        fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);

        return new File(uploadDir, fileName);
    }
}