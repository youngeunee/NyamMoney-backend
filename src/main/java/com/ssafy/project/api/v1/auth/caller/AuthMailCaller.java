package com.ssafy.project.api.v1.auth.caller;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;

@Component
public class AuthMailCaller {

    private final JavaMailSender mailSender;
    private final SecureRandom random = new SecureRandom();

    public AuthMailCaller(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // 인증번호 생성
    public String makeCode() {
        int num = random.nextInt(900_000) + 100_000;
        return String.valueOf(num);
    }

    // 메일 발송
    public void sendPasswordResetCode(String toEmail, String loginId, String code) {
        sendMail(
            toEmail,
            "[냠머니] 비밀번호 재설정 인증번호",
            buildPasswordResetHtml(loginId, code)
        );
    }

    public void sendSignupCode(String toEmail, String code) {
        sendMail(
            toEmail,
            "[냠머니] 회원가입 이메일 인증번호",
            buildSignupHtml(code)
        );
    }
    
    private void sendMail(String toEmail, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);
        } catch (Exception e) {
            throw new IllegalStateException("이메일 전송 실패", e);
        }
    }
    
    private String buildPasswordResetHtml(String loginId, String code) {
        return """
            <div>
                <p><strong>%s</strong>님</p>
                <p>비밀번호 재설정을 위한 인증번호입니다.</p>
                <h2>%s</h2>
                <p>인증번호는 10분간 유효합니다.</p>
            </div>
            """.formatted(loginId, code);
    }

    private String buildSignupHtml(String code) {
        return """
            <div>
                <p>냠머니 회원가입을 위한 이메일 인증번호입니다.</p>
                <h2>%s</h2>
                <p>인증번호는 10분간 유효합니다.</p>
            </div>
            """.formatted(code);
    }
}
