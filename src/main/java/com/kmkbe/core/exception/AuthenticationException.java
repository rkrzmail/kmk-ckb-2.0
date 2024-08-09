package com.kmkbe.core.exception;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class AuthenticationException extends RuntimeException {
    private String title;
    private String headerMessage;
    private String message;
    private Action action;

    @Getter
    @Builder
    private static class Action {
        private String title;
        private String uri;
    }

    public Map<String, Object> getPayload() {
        return Map.of(
                "title", title,
                "headerMessage", headerMessage,
                "message", message,
                "action", action
        );
    }

    public static AuthenticationException invalidEmail() {
        return AuthenticationException.builder()
                .title("Format Email tidak valid")
                .message("Masukkan email dengan format yg valid")
                .action(AuthenticationException.Action.builder().title("Kembali").uri("/auth/sign-in").build())
                .headerMessage("Email is invalid")
                .build();
    }

    public static AuthenticationException notRegistered() {
        return AuthenticationException.builder()
                .title("Email anda belum terdaftar")
                .message("Saat ini email anda belum terdaftar, silahkan lakukan daftar untuk melanjutkan proses")
                .action(Action.builder().title("Daftar").uri("/auth/sign-up").build())
                .headerMessage("User not found")
                .build();
    }

    public static AuthenticationException alreadyRegistered() {
        return AuthenticationException.builder()
                .title("Email anda telah terdaftar")
                .message("Saat ini email anda sudah terdaftar, silahkan Login untuk melanjutkan proses")
                .action(Action.builder().title("Login").uri("/auth/sign-in").build())
                .headerMessage("User already exists")
                .build();
    }

    public static AuthenticationException invalidPin() {
        return AuthenticationException.builder()
                .title("Email atau Pin tidak valid")
                .message("Email atau Pin salah, silahkan masukkan email dan pin yang valid")
                .action(Action.builder().title("Kemabli").uri("/auth/sign-in").build())
                .headerMessage("Invalid Email or Pin")
                .build();
    }

    public static AuthenticationException notActive() {
        return AuthenticationException.builder()
                .title("Akun anda belum di konfirmasi Aktif")
                .message("Lanjutkan pendaftaran dengan data yg sama lalu konfirmasi dengan kode OTP yang dikirimkan ke email anda")
                .action(Action.builder().title("Daftar").uri("/auth/sign-up").build())
                .headerMessage("User not active yet")
                .build();
    }

    public static AuthenticationException blacklist() {
        return AuthenticationException.builder()
                .title("Perusaahaan anda terdapat di data blacklist")
                .message("Pada saat ini anda berada di daftar blacklist PT. Trakindo Utama, sehingga anda belum dapat menggunakan Dana Sakti")
                .action(Action.builder().title("Kembali").uri("/auth/sign-in").build())
                .headerMessage("User blacklist")
                .build();
    }

    public static AuthenticationException invalidInternalUser() {
        return AuthenticationException.builder()
                .title("Email atau Password tidak valid")
                .message("Email atau Password salah, silahkan masukkan email dan password yang valid")
                .action(Action.builder().title("Kemabli").uri("/internal/auth/sign-in").build())
                .headerMessage("Invalid Email or Password")
                .build();
    }
}
