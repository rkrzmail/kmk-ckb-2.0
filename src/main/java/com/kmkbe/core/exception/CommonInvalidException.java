package com.kmkbe.core.exception;

import io.netty.util.internal.StringUtil;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
public class CommonInvalidException extends RuntimeException {
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
        Map<String, Object> result = new HashMap<>(Map.of(
                "title", title
        ));

        if (!StringUtil.isNullOrEmpty(headerMessage)) {
            result.put("headerMessage", headerMessage);
        }

        result.put("message", message);

        if (action != null) {
            result.put("action", action);
        }

        return result;
    }

    public static CommonInvalidException invalidEmail() {
        return CommonInvalidException.builder()
                .title("Format Email tidak valid")
                .message("Masukkan email dengan format yg valid")
                .action(CommonInvalidException.Action.builder().title("Kembali").uri("/auth/sign-in").build())
                .headerMessage("Email is invalid")
                .build();
    }

    public static CommonInvalidException notRegistered() {
        return CommonInvalidException.builder()
                .title("Email anda belum terdaftar")
                .message("Saat ini email anda belum terdaftar, silahkan lakukan daftar untuk melanjutkan proses")
                .action(Action.builder().title("Daftar").uri("/auth/sign-up").build())
                .headerMessage("User not found")
                .build();
    }

    public static CommonInvalidException alreadyRegistered() {
        return CommonInvalidException.builder()
                .title("Email anda telah terdaftar")
                .message("Saat ini email anda sudah terdaftar, silahkan Login untuk melanjutkan proses")
                .action(Action.builder().title("Login").uri("/auth/sign-in").build())
                .headerMessage("User already exists")
                .build();
    }

    public static CommonInvalidException invalidPin() {
        return CommonInvalidException.builder()
                .title("Email atau Pin tidak valid")
                .message("Email atau Pin salah, silahkan masukkan email dan pin yang valid")
                .action(Action.builder().title("Kembali").uri("/auth/sign-in").build())
                .headerMessage("Invalid Email or Pin")
                .build();
    }

    public static CommonInvalidException notActive() {
        return CommonInvalidException.builder()
                .title("Akun anda belum di konfirmasi Aktif")
                .message("Lanjutkan pendaftaran dengan data yg sama lalu konfirmasi dengan kode OTP yang dikirimkan ke email anda")
                .action(Action.builder().title("Daftar").uri("/auth/sign-up").build())
                .headerMessage("User not active yet")
                .build();
    }

    public static CommonInvalidException blacklist() {
        return CommonInvalidException.builder()
                .title("Perusaahaan anda terdapat di data blacklist")
                .message("Pada saat ini anda berada di daftar blacklist PT. Trakindo Utama, sehingga anda belum dapat menggunakan Dana Sakti")
                .action(Action.builder().title("Kembali").uri("/auth/sign-in").build())
                .headerMessage("User blacklist")
                .build();
    }

    public static CommonInvalidException invalidInternalUser() {
        return CommonInvalidException.builder()
                .title("Email atau Password tidak valid")
                .message("Email atau Password salah, silahkan masukkan email dan password yang valid")
                .action(Action.builder().title("Kemabli").uri("/internal/auth/sign-in").build())
                .headerMessage("Invalid Email or Password")
                .build();
    }

    public static CommonInvalidException cannotAccessResource() {
        return CommonInvalidException.builder()
                .title("Tidak ada akses")
                .message("Anda tidak memiliki akses untuk mengakses sumber ini")
                .build();
    }
}
