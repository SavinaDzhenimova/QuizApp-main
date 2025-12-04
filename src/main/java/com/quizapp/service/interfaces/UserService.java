package com.quizapp.service.interfaces;

import com.quizapp.model.dto.user.UpdatePasswordDTO;
import com.quizapp.model.dto.user.UserDTO;
import com.quizapp.model.dto.user.UserRegisterDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.model.entity.User;
import jakarta.transaction.Transactional;

import java.util.Optional;

public interface UserService {

    @Transactional
    UserDTO getUserInfo(String username);

    Result registerUser(UserRegisterDTO registerUserDTO);

    Integer sendInactiveSolvingQuizzesUsersEmails();

    @Transactional
    Integer resendWarnedInactiveSolvingQuizzesUsersEmails();

    Integer sendInactiveUsersWarnEmail();

    Integer removeWarnedInactiveLoginUsersAccounts();

    Result updatePassword(String username, UpdatePasswordDTO updatePasswordDTO);

    void resetUserPassword(User user, String password);

    void updateLastLoginTime(String username);

    Optional<User> getUserByUsername(String username);

    Optional<User> getUserByEmail(String email);

    User saveAndFlushUser(User user);

    void deleteById(Long id);
}