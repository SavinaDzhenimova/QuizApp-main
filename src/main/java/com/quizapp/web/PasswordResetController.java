package com.quizapp.web;

import com.quizapp.exception.InvalidPasswordResetToken;
import com.quizapp.model.entity.Result;
import com.quizapp.service.interfaces.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @GetMapping("/forgot-password")
    public ModelAndView showForgotPasswordPage() {

        return new ModelAndView("forgot-password");
    }

    @PostMapping("/forgot-password")
    public ModelAndView sendForgotPasswordEmail(@RequestParam("email") String email,
                                                RedirectAttributes redirectAttributes) {

        Result result = this.passwordResetService.sendEmailForForgottenPassword(email);

        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("success", result.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("error", result.getMessage());

            return new ModelAndView("redirect:/users/forgot-password");
        }

        return new ModelAndView("redirect:/users/login");
    }

    @GetMapping("/reset-password")
    public ModelAndView showResetPassword(@RequestParam("token") String token) {

        boolean isValid = this.passwordResetService.isValidToken(token);

        if (!isValid) {
            throw new InvalidPasswordResetToken("Линкът за смяна на паролата е невалиден или изтекъл.");
        }

        ModelAndView modelAndView = new ModelAndView("reset-password");

        modelAndView.addObject("token", token);

        return modelAndView;
    }

    @PostMapping("/reset-password")
    public ModelAndView handleResetPassword(@RequestParam("token") String token,
                                            @RequestParam("password") String password,
                                            @RequestParam("confirmPassword") String confirmPassword,
                                            RedirectAttributes redirectAttributes) {

        Result result = this.passwordResetService.resetPassword(password, confirmPassword, token);

        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("success", result.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("error", result.getMessage());

            return new ModelAndView("redirect:/users/reset-password?token=" + token);
        }

        return new ModelAndView("redirect:/users/login");
    }
}