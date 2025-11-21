package com.quizapp.web;

import com.quizapp.exception.InvalidPasswordResetToken;
import com.quizapp.model.dto.ResetPasswordDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.service.interfaces.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
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
    public ModelAndView showResetPassword(@RequestParam("token") String token, Model model) {

        boolean isValid = this.passwordResetService.isValidToken(token);
        if (!isValid) {
            throw new InvalidPasswordResetToken("Линкът за смяна на паролата е невалиден или изтекъл.");
        }

        ResetPasswordDTO resetPasswordDTO;
        if (model.containsAttribute("resetPasswordDTO")) {
            resetPasswordDTO = (ResetPasswordDTO) model.getAttribute("resetPasswordDTO");
        } else {
            resetPasswordDTO = new ResetPasswordDTO();
        }
        resetPasswordDTO.setToken(token);

        ModelAndView modelAndView = new ModelAndView("reset-password");

        modelAndView.addObject("token", token);
        modelAndView.addObject("resetPasswordDTO", resetPasswordDTO);

        return modelAndView;
    }

    @PostMapping("/reset-password")
    public ModelAndView handleResetPassword(@Valid @ModelAttribute("resetPasswordDTO")ResetPasswordDTO resetPasswordDTO,
                                            BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("resetPasswordDTO", resetPasswordDTO)
                    .addFlashAttribute("org.springframework.validation.BindingResult.resetPasswordDTO",
                            bindingResult);

            return new ModelAndView("redirect:/users/reset-password");
        }

        Result result = this.passwordResetService.resetPassword(resetPasswordDTO);

        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("success", result.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("error", result.getMessage());

            return new ModelAndView("redirect:/users/reset-password?token=" + resetPasswordDTO.getToken());
        }

        return new ModelAndView("redirect:/users/login");
    }
}