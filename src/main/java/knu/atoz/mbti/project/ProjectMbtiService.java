package knu.atoz.mbti.project;

import knu.atoz.mbti.MbtiDimension;
import knu.atoz.mbti.MbtiRepository;
import knu.atoz.mbti.exception.InvalidMbtiException;
import knu.atoz.mbti.exception.MbtiNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProjectMbtiService {

    private final MbtiRepository mbtiRepository;
    private final ProjectMbtiRepository projectMbtiRepository;

    public List<MbtiDimension> getMbtiDimensions() {
        List<MbtiDimension> dimensions = mbtiRepository.findAllMbtiDimensions();
        if (dimensions == null || dimensions.isEmpty()) {
            throw new MbtiNotFoundException("MBTI 차원 정보를 불러올 수 없습니다.");
        }
        return dimensions;
    }

    public Map<Long, String> getMbtiMapByProjectId(Long projectId) {
        if (projectId == null) throw new InvalidMbtiException("프로젝트 ID가 필요합니다.");

        Map<Long, String> map = projectMbtiRepository.findMbtiMapByProjectId(projectId);
        // 맵이 없으면 빈 맵 반환 (수정 폼에서 에러 안 나게)
        return map != null ? map : new java.util.HashMap<>();
    }

    public void saveProjectMbti(Long projectId, Map<Long, String> mbtiMap) {
        if (projectId == null) throw new InvalidMbtiException("프로젝트 ID가 필요합니다.");
        if (mbtiMap == null || mbtiMap.isEmpty()) throw new InvalidMbtiException("MBTI 정보가 비어있습니다.");

        projectMbtiRepository.saveProjectMbti(projectId, mbtiMap);
    }
}