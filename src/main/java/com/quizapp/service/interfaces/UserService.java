package com.quizapp.service.interfaces;

import com.quizapp.model.dto.UserDTO;
import com.quizapp.model.dto.user.UserRegisterDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.model.entity.User;

import java.util.Optional;

public interface UserService {
    UserDTO getUserInfo(String username);

    Result registerUser(UserRegisterDTO registerUserDTO);

    Optional<User> getUserByUsername(String username);
}
