package com.quizapp.web;

import com.quizapp.model.dto.SubscribeDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.service.interfaces.SubscribeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/subscribe")
@RequiredArgsConstructor
public class SubscribeController {

    private final SubscribeService subscribeService;

    @PostMapping
    public ModelAndView subscribe(@Valid @ModelAttribute("subscribeDTO")SubscribeDTO subscribeDTO,
                                  BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("subscribeDTO", subscribeDTO)
                    .addFlashAttribute("org.springframework.validation.BindingResult.subscribeDTO",
                            bindingResult);

            return new ModelAndView("redirect:/#subscribe");
        }

        Result result = this.subscribeService.subscribe(subscribeDTO);

        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("success", result.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("error", result.getMessage());
        }

        return new ModelAndView("redirect:/#subscribe");
    }
}