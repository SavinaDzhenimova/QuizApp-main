package com.quizapp.service.interfaces;

import com.quizapp.model.dto.user.RegisterUserDTO;
import com.quizapp.model.entity.Result;

public interface UserService {
    Result registerUser(RegisterUserDTO registerUserDTO);
}
