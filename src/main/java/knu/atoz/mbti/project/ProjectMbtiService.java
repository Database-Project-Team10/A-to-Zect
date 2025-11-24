package knu.atoz.mbti.project;

import knu.atoz.mbti.MbtiDimension;
import knu.atoz.mbti.MbtiRepository;
import knu.atoz.mbti.exception.InvalidMbtiException;
import knu.atoz.mbti.exception.MbtiNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProjectMbtiService {

    private final MbtiRepository mbtiRepository;
    private final ProjectMbtiRepository projectMbtiRepository;

    public ProjectMbtiService(MbtiRepository mbtiRepository,  ProjectMbtiRepository projectMbtiRepository) {
        this.mbtiRepository = mbtiRepository;
        this.projectMbtiRepository = projectMbtiRepository;
    }

    public List<MbtiDimension> getMbtiDimensions() {
        List<MbtiDimension> dimensions = mbtiRepository.findAllMbtiDimensions();
        if (dimensions == null || dimensions.isEmpty()) {
            throw new MbtiNotFoundException("MBTI 차원 정보를 불러올 수 없습니다.");
        }
        return dimensions;
    }

    public Map<Long, String> getMbtiMapByProjectId(Long projectId) {
        if (projectId == null) {
            throw new InvalidMbtiException("프로젝트 ID가 필요합니다.");
        }

        Map<Long, String> map = projectMbtiRepository.findMbtiMapByProjectId(projectId);

        if (map == null) {
            throw new MbtiNotFoundException("해당 프로젝트의 MBTI 정보를 찾을 수 없습니다.");
        }

        return map;
    }

    public void saveProjectMbti(Long projectId, Map<Long, String> mbtiMap) {
        if (projectId == null) {
            throw new InvalidMbtiException("프로젝트 ID가 필요합니다.");
        }

        if (mbtiMap == null || mbtiMap.isEmpty()) {
            throw new InvalidMbtiException("MBTI 정보가 비어있습니다.");
        }

        projectMbtiRepository.saveProjectMbti(projectId, mbtiMap);
    }
}