package com.quizapp.service.interfaces;

import com.quizapp.model.dto.user.UserRegisterDTO;
import com.quizapp.model.entity.Result;

public interface UserService {
    Result registerUser(UserRegisterDTO registerUserDTO);
}
