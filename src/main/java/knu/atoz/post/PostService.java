package knu.atoz.post;

import knu.atoz.post.dto.PostRequestDto;
import knu.atoz.post.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    // 목록 조회
    public List<Post> getPostList(Long projectId) {
        if (projectId == null) throw new InvalidPostException("프로젝트 ID가 필요합니다.");
        return postRepository.findAllPostsByProjectId(projectId);
    }

    // 단건 조회
    public Post getPost(Long postId) {
        Post post = postRepository.findById(postId);
        if (post == null) throw new PostNotFoundException();
        return post;
    }

    // 생성
    public void createPost(Long projectId, Long memberId, PostRequestDto requestDto) {
        if (requestDto.getTitle() == null || requestDto.getTitle().trim().isEmpty()) {
            throw new InvalidPostException("제목을 입력해주세요.");
        }
        Post post = new Post(projectId, memberId, requestDto.getTitle(), requestDto.getContent());
        postRepository.save(post);
    }

    // 수정
    public void updatePost(Long postId, Long memberId, PostRequestDto requestDto) {
        Post original = postRepository.findById(postId);
        if (original == null) throw new PostNotFoundException();

        if (!original.getMemberId().equals(memberId)) {
            throw new UnauthorizedPostAccessException();
        }

        if (requestDto.getTitle() == null || requestDto.getTitle().trim().isEmpty()) {
            throw new InvalidPostException("제목을 입력해주세요.");
        }

        // 내용 변경 및 수정 시간 갱신은 엔티티 내부나 여기서 처리
        Post updatePost = new Post(
                original.getId(),
                original.getProjectId(),
                memberId,
                requestDto.getTitle(),
                requestDto.getContent(),
                original.getCreatedAt(),
                java.time.LocalDateTime.now() // 수정 시간 갱신
        );

        postRepository.update(updatePost);
    }

    // 삭제
    public void deletePost(Long postId, Long memberId) {
        Post original = postRepository.findById(postId);
        if (original == null) throw new PostNotFoundException();

        if (!original.getMemberId().equals(memberId)) {
            throw new UnauthorizedPostAccessException();
        }
        postRepository.delete(postId);
    }
}