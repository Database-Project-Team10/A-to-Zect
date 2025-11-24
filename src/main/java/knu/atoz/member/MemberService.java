package knu.atoz.member;

import knu.atoz.member.dto.MemberInfoResponseDto;
import knu.atoz.member.dto.MemberUpdateRequestDto;
import knu.atoz.member.dto.PasswordUpdateRequestDto;
import knu.atoz.member.dto.SignupRequestDto;
import knu.atoz.member.exception.DuplicateEmailException;
import knu.atoz.member.exception.InvalidCredentialsException;
import knu.atoz.member.exception.MemberNotFoundException;
import knu.atoz.member.exception.PasswordMismatchException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public Member signUp(SignupRequestDto signupRequestDto) {

        if (memberRepository.findByEmail(signupRequestDto.getEmail()) != null) {
            throw new DuplicateEmailException();
        }

        if (!signupRequestDto.getPassword().equals(signupRequestDto.getConfirmPassword())) {
            throw new PasswordMismatchException();
        }

        return memberRepository.save(signupRequestDto.toEntity());
    }

    public Member login(String email, String password) {

        Member member = memberRepository.findByEmail(email);

        if (member == null || !member.getPassword().equals(password)) {
            throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

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
    } 

    public Member getCurrentUser(){
        return new Member(1L, "1", "1", "1", LocalDate.now(), LocalDateTime.now());
    } 

    public Member updateMember(Long memberId, MemberUpdateRequestDto dto) {

        Member member = memberRepository.findById(memberId);
        if (member == null) {
            throw new MemberNotFoundException();
        }

        if (!member.getEmail().equals(dto.getEmail())) {
            if (memberRepository.findByEmail(dto.getEmail()) != null) {
                throw new DuplicateEmailException();
            }
        }

        if (!member.getPassword().equals(dto.getPassword())) {
            throw new InvalidCredentialsException("비밀번호가 올바르지 않습니다.");
        }

        Member updatedMember = new Member(
                member.getId(),
                dto.getEmail(),
                dto.getPassword(),
                dto.getName(),
                dto.getBirthDate(),
                member.getCreatedAt()
        );

        memberRepository.update(updatedMember);

        return updatedMember;
    }
}