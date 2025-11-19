package com.quizapp.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class LoginController {

    @GetMapping("/login")
    public ModelAndView login() {
        return new ModelAndView("login");
    }

    @GetMapping("/login-error")
    public ModelAndView loginError(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Невалидно потребителско име или парола!");

        return new ModelAndView("redirect:/users/login");
    }
}