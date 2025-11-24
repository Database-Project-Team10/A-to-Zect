package knu.atoz.mbti.member;

import knu.atoz.mbti.MbtiDimension;
import knu.atoz.mbti.MbtiRepository;
import knu.atoz.mbti.exception.InvalidMbtiException;
import knu.atoz.mbti.exception.MbtiNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MemberMbtiService {

    private final MbtiRepository mbtiRepository;
    private final MemberMbtiRepository memberMbtiRepository;

    public MemberMbtiService(MbtiRepository mbtiRepository,  MemberMbtiRepository memberMbtiRepository) {
        this.mbtiRepository = mbtiRepository;
        this.memberMbtiRepository = memberMbtiRepository;
    }

    public List<MbtiDimension> getMbtiDimensions() {
        List<MbtiDimension> dimensions = mbtiRepository.findAllMbtiDimensions();
        if (dimensions == null || dimensions.isEmpty()) {
            throw new MbtiNotFoundException("MBTI 차원 정보를 불러올 수 없습니다.");
        }
        return dimensions;
    }

    public Map<Long, String> getMbtiMapByMemberId(Long memberId) {

        if (memberId == null) {
            throw new InvalidMbtiException("회원 ID가 필요합니다.");
        }

        Map<Long, String> map = memberMbtiRepository.findMbtiMapByMemberId(memberId);

        if (map == null) {
            throw new MbtiNotFoundException("해당 회원의 MBTI 정보를 찾을 수 없습니다.");
        }

        return map;
    }

    public void saveMyMbti(Long memberId, Map<Long, String> mbtiMap) {

        if (memberId == null) {
            throw new InvalidMbtiException("회원 ID가 필요합니다.");
        }

        if (mbtiMap == null || mbtiMap.isEmpty()) {
            throw new InvalidMbtiException("MBTI 정보가 비어있습니다.");
        }

        memberMbtiRepository.saveMemberMbti(memberId, mbtiMap);
    }
}