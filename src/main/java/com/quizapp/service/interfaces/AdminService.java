package com.quizapp.service.interfaces;

import com.quizapp.model.dto.user.AddAdminDTO;
import com.quizapp.model.entity.Result;

public interface AdminService {
    Result addAdmin(AddAdminDTO addAdminDTO);
}
