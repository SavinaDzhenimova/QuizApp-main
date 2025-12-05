package com.quizapp.service.interfaces;

public interface EmailService {

    void sendInquiryEmail(String fullName, String email, String phoneNumber, String message);

    void sendInquiryReceivedEmail(String fullName, String email);

    void sendForgotPasswordEmail(String username, String email, String token);

    void sendUserRegisterEmail(String fullName, String email);

    void sendReportProblemEmail(String fullName, String email, String phoneNumber, String address, String requestType);

    void sendProblemReceivedEmail(String fullName, String email, String problemType);

    void sendSubscribeEmail(String email);

    void sendInactiveSolvingQuizzesEmail(String username, String email);

    void sendInactiveUserNotWarnedEmail(String username, String email);

    void sendAddedAdminEmail(String username, String email);
}