package com.quizapp.web;

import com.quizapp.model.dto.user.AddAdminDTO;
import com.quizapp.model.dto.user.AdminDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.service.interfaces.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    public ModelAndView showAdminsPage(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       @RequestParam(required = false) String username) {

        ModelAndView modelAndView = new ModelAndView("admins");

        Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());
        Page<AdminDTO> adminDTOs = this.adminService.getAllAdmins(username, pageable);

        modelAndView.addObject("admins", adminDTOs.getContent());
        modelAndView.addObject("currentPage", adminDTOs.getNumber());
        modelAndView.addObject("totalPages", adminDTOs.getTotalPages());
        modelAndView.addObject("totalElements", adminDTOs.getTotalElements());
        modelAndView.addObject("size", adminDTOs.getSize());
        modelAndView.addObject("username", username);

        return modelAndView;
    }

    @GetMapping("/add-admin")
    public ModelAndView showAddAdminPage(Model model) {
        if (!model.containsAttribute("addAdminDTO")) {
            model.addAttribute("addAdminDTO", new AddAdminDTO());
        }

        return new ModelAndView("add-admin");
    }

    @PostMapping("/add-admin")
    public ModelAndView addAdmin(@Valid @ModelAttribute("addAdminDTO") AddAdminDTO addAdminDTO,
                                 BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("addAdminDTO", addAdminDTO)
                    .addFlashAttribute("org.springframework.validation.BindingResult.addAdminDTO",
                            bindingResult);

            return new ModelAndView("add-admin");
        }

        Result result = this.adminService.addAdmin(addAdminDTO);

        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("success", result.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("error", result.getMessage());
        }

        return new ModelAndView("redirect:/admin/add-admin");
    }
}