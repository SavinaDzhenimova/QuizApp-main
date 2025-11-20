package com.quizapp.service.interfaces;

public interface EmailService {
    void sendInquiryEmail(String fullName, String email, String phoneNumber, String message);

    void sendForgotPasswordEmail(String fullName, String email, String token);

    void sendUserRegisterEmail(String fullName, String email, String phoneNumber);

    void sendReportBugEmail(String fullName, String email, String phoneNumber, String address, String requestType);

    void sendSubscribeEmail(String email);
}
