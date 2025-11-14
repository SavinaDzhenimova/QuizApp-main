package com.quizapp.web.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.quizapp.model.dto.AddCategoryDTO;
import com.quizapp.model.dto.CategoryDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.service.interfaces.CategoryService;
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

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/categories")
    public ModelAndView showCategories() {
        ModelAndView modelAndView = new ModelAndView("categories");

        List<CategoryDTO> allCategories = this.categoryService.getAllCategories();

        modelAndView.addObject("categories", allCategories);

        return modelAndView;
    }

    @GetMapping("/start-quiz")
    public ModelAndView showCategoriesPage() {
        ModelAndView modelAndView = new ModelAndView("start-quiz");

        List<CategoryDTO> allCategories = this.categoryService.getAllCategories();

        modelAndView.addObject("categories", allCategories);

        return modelAndView;
    }

    @GetMapping("/categories/add-category")
    public ModelAndView showAddCategoryPage(Model model) {
        if (!model.containsAttribute("addCategoryDTO")) {
            model.addAttribute("addCategoryDTO", new AddCategoryDTO());
        }

        return new ModelAndView("add-category");
    }

    @PostMapping("/categories/add-category")
    public ModelAndView addCategory(@Valid @ModelAttribute("addCategoryDTO") AddCategoryDTO addCategoryDTO,
                                    BindingResult bindingResult, RedirectAttributes redirectAttributes) throws JsonProcessingException {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("addCategoryDTO", addCategoryDTO)
                    .addFlashAttribute("org.springframework.validation.BindingResult.addCategoryDTO",
                            bindingResult);

            return new ModelAndView("add-category");
        }

        Result result = this.categoryService.addCategory(addCategoryDTO);

        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("success", result.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("error", result.getMessage());
        }

        return new ModelAndView("redirect:/categories/add-category");
    }
}