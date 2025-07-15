package com.kmkbe.modules.branch_admin.request;

import lombok.Data;

@Data
public class CallbackRequest {
    private String callbackType;
    private CallbackData data;
    private String message;

    @Data
    public static class CallbackData {
        // Untuk ACTIVATION_COMPLETE
        private String name;
        private String email;
        private String phone;

        // Untuk DOCUMENT_SIGN_COMPLETE
        private String refNo;
        private String documentId;
    }
}