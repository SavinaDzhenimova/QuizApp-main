package com.quizapp.service.interfaces;

import com.quizapp.model.dto.user.AddAdminDTO;
import com.quizapp.model.dto.user.AdminDTO;
import com.quizapp.model.entity.Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {

    Result addAdmin(AddAdminDTO addAdminDTO);

    Page<AdminDTO> getAllAdmins(String username, Pageable pageable);

    Result deleteAdminById(Long id);
}