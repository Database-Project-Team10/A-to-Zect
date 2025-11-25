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
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    /**
     * 문서 생성 (파일 업로드 필수)
     */
    public void createDocument(Long projectId, DocumentRequestDto dto) {
        // 제목 검증
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new InvalidDocumentInputException("문서 제목을 입력해주세요.");
        }

        try {
            // 1. 파일 저장 수행 (파일 없으면 null 반환)
            String savedPath = saveFile(dto.getFile());

            // 생성 시에는 파일이 필수
            if (savedPath == null) {
                throw new DocumentFileNotExistException();
            }

            // 2. DB 저장 (Document 생성자 순서: id=null, projectId, title, location)
            // (Repository의 save 메서드 시그니처나 Entity 생성자에 맞춰 조정 필요)
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
     * 문서 수정 (파일은 선택 사항)
     * - 새 파일이 있으면 교체
     * - 없으면 기존 파일 유지
     */
    public void updateDocument(Long documentId, Long projectId, DocumentRequestDto requestDto) {
        // 1. 기존 문서 조회
        Document targetDocument = documentRepository.findById(documentId);
        if (targetDocument == null) {
            throw new DocumentNotFoundException();
        }

        // 2. 권한/소속 체크
        if (!targetDocument.getProjectId().equals(projectId)) {
            throw new DocumentAccessException("해당 문서는 이 프로젝트에 속하지 않아 수정할 수 없습니다.");
        }

        // 3. 제목 검증
        if (requestDto.getTitle() == null || requestDto.getTitle().trim().isEmpty()) {
            throw new InvalidDocumentInputException("제목은 비워둘 수 없습니다.");
        }

        try {
            // 4. 파일 처리 로직
            String newLocation = targetDocument.getLocation(); // 기본값: 기존 경로 유지

            // 사용자가 새 파일을 업로드했는지 확인
            if (requestDto.getFile() != null && !requestDto.getFile().isEmpty()) {
                newLocation = saveFile(requestDto.getFile()); // 새 경로로 덮어쓰기

                // (선택사항) 여기서 기존 파일(targetDocument.getLocation())을 디스크에서 삭제하는 로직을 추가할 수도 있음
            }

            // 5. 업데이트 실행 (id, projectId, title, location)
            Document updateDoc = new Document(documentId, projectId, requestDto.getTitle(), newLocation);
            documentRepository.update(updateDoc);

        } catch (IOException e) {
            throw new RuntimeException("파일 수정 중 오류가 발생했습니다.", e);
        }
    }

    public void deleteDocument(Long documentId, Long expectedProjectId) {
        Document targetDocument = documentRepository.findById(documentId);

        if (targetDocument == null) {
            throw new DocumentNotFoundException();
        }

        if (!targetDocument.getProjectId().equals(expectedProjectId)) {
            throw new DocumentAccessException("해당 문서는 이 프로젝트에 속하지 않아 삭제할 수 없습니다.");
        }

        // (선택사항) 여기서 실제 파일(targetDocument.getLocation())을 삭제하는 로직 추가 가능

        documentRepository.delete(documentId);
    }

    // 파일 저장 헬퍼 메서드
    private String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 1. 저장할 폴더 지정 (프로젝트 루트/uploads)
        String projectPath = System.getProperty("user.dir") + "/uploads";
        File directory = new File(projectPath);

        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 2. 파일명 중복 방지 (UUID)
        UUID uuid = UUID.randomUUID();
        String fileName = uuid + "_" + file.getOriginalFilename();

        // 3. 실제 저장
        File saveFile = new File(directory, fileName);
        file.transferTo(saveFile);

        // 4. DB 저장용 경로 반환
        return "/uploads/" + fileName;
    }

}