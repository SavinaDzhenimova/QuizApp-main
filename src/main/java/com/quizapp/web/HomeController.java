package com.quizapp.web;

import com.quizapp.model.dto.SubscribeDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {

    @GetMapping("/")
    public ModelAndView getIndexPage(Model model) {
        if (!model.containsAttribute("subscribeDTO")) {
            model.addAttribute("subscribeDTO", new SubscribeDTO());
        }

        return new ModelAndView("index");
    }

    @GetMapping("/about-us")
    public ModelAndView getAboutUsPage() {
        return new ModelAndView("about-us");
    }
}