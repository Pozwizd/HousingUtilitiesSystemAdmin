package org.spacelab.housingutilitiessystemadmin.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@AllArgsConstructor
public class LoginController {

    @GetMapping("/login")
    public ModelAndView login(Model model) {
        model.addAttribute("title", "Login");
        return new ModelAndView("auth/auth-login-cover");
    }

}