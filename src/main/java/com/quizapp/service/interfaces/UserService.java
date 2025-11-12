package com.quizapp.service.interfaces;

import com.quizapp.model.dto.SolvedQuizDTO;
import com.quizapp.model.dto.UserDTO;
import com.quizapp.model.dto.user.UserRegisterDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.model.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDTO getUserInfo(String username);

    Result registerUser(UserRegisterDTO registerUserDTO);

    Optional<User> getUserByUsername(String username);

    User saveAndFlushUser(User user);

    List<SolvedQuizDTO> getSolvedQuizzesByUsername(String username);
}