package knu.atoz.member;

import knu.atoz.member.dto.*;
import knu.atoz.member.exception.*;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
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

        String encryptedPassword = BCrypt.hashpw(signupRequestDto.getPassword(), BCrypt.gensalt());
        signupRequestDto.setPassword(encryptedPassword);

        return memberRepository.save(signupRequestDto.toEntity());
    }

    public Member login(String email, String password) {

        Member member = memberRepository.findByEmail(email);

        if (member == null || !BCrypt.checkpw(password, member.getPassword())) {
            throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return member;
    }

    public void editPassword(Long memberId, PasswordUpdateRequestDto dto) {

        Member member = memberRepository.findById(memberId);
        if (member == null){
            throw new MemberNotFoundException();
        }

        if (!BCrypt.checkpw(dto.getCurrentPassword(), member.getPassword())) {
            throw new InvalidCredentialsException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new PasswordMismatchException();
        }

        if (BCrypt.checkpw(dto.getNewPassword(), member.getPassword())) {
            throw new RuntimeException("새 비밀번호는 현재 비밀번호와 다르게 설정해야 합니다.");
        }

        String newEncryptedPassword = BCrypt.hashpw(dto.getNewPassword(), BCrypt.gensalt());
        memberRepository.updatePassword(memberId, newEncryptedPassword);
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

    public String getMemberName(Long memberId) {
        Member member = memberRepository.findById(memberId);
        if (member == null){
            throw new MemberNotFoundException();
        }
        return member.getName();
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

        if (!BCrypt.checkpw(dto.getPassword(), member.getPassword())) {
            throw new InvalidCredentialsException("비밀번호가 올바르지 않습니다.");
        }

        Member updatedMember = new Member(
                member.getId(),
                dto.getEmail(),
                member.getPassword(),
                dto.getName(),
                dto.getBirthDate(),
                member.getCreatedAt()
        );

        memberRepository.update(updatedMember);

        return updatedMember;
    }

    public void delete(Long memberId) {
        Member member = memberRepository.findById(memberId);
        if (member == null) {
            throw new MemberNotFoundException();
        }
        memberRepository.delete(memberId);
    }
}