package knu.atoz.member;

import knu.atoz.member.dto.SignupRequestDto;
import knu.atoz.member.dto.MemberInfoResponseDto;
import knu.atoz.member.dto.PasswordUpdateRequestDto;
import knu.atoz.member.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public Member signUp(SignupRequestDto signupRequestDto) {
        // isLoggedIn() 체크 로직 삭제 (웹에서는 불필요)

        if (memberRepository.findByEmail(signupRequestDto.getEmail()) != null) {
            throw new DuplicateEmailException();
        }

        if (!signupRequestDto.getPassword().equals(signupRequestDto.getConfirmPassword())) {
            throw new PasswordMismatchException();
        }

        return memberRepository.save(signupRequestDto.toEntity());
    }

    public Member login(String email, String password) {
        // isLoggedIn() 체크 로직 삭제

        Member member = memberRepository.findByEmail(email);

        if (member == null || !member.getPassword().equals(password)) {
            throw new InvalidCredentialsException();
        }

        // loggedInUser = member; <-- 이 줄 삭제!
        return member;
    }

    public void editPassword(Long memberId, PasswordUpdateRequestDto dto) {

        Member member = memberRepository.findById(memberId);
        if (member == null){
            throw new MemberNotFoundException();
        }

        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new PasswordMismatchException();
        }

        memberRepository.updatePassword(memberId, dto.getNewPassword());
    }

    public MemberInfoResponseDto getAllInfo(Long memberId) {
        return memberRepository.getAllInfoById(memberId);
    }

    public boolean isLoggedIn() {
        return true;
    } // 컴파일 에러 방지 임시 코드

    public Member getCurrentUser(){
        return new Member(1L, "1", "1", "1", LocalDate.now(), LocalDateTime.now());
    } // 컴파일 에러 방지 임시 코드
}