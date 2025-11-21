package com.quizapp.service.interfaces;

public interface EmailService {

    void sendInquiryEmail(String fullName, String email, String phoneNumber, String message);

    void sendInquiryReceivedEmail(String fullName, String email);

    void sendForgotPasswordEmail(String fullName, String email, String token);

    void sendUserRegisterEmail(String fullName, String email);

    void sendReportProblemEmail(String fullName, String email, String phoneNumber, String address, String requestType);

    void sendSubscribeEmail(String email);
}
