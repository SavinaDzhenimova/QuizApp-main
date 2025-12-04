package com.quizapp.web;

import com.quizapp.model.dto.user.AddAdminDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.service.interfaces.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    public ModelAndView showAdminPage() {
        return new ModelAndView("admin");
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