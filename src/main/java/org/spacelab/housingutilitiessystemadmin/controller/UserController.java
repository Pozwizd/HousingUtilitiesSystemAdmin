package org.spacelab.housingutilitiessystemadmin.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.spacelab.housingutilitiessystemadmin.entity.location.Status;
import org.spacelab.housingutilitiessystemadmin.models.filters.user.UserRequestTable;
import org.spacelab.housingutilitiessystemadmin.models.user.UserRequest;
import org.spacelab.housingutilitiessystemadmin.models.user.UserResponse;
import org.spacelab.housingutilitiessystemadmin.models.user.UserResponseTable;
import org.spacelab.housingutilitiessystemadmin.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller
@RequestMapping("/users")
@AllArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping("/getAll")
    @ResponseBody
    public ResponseEntity<Page<UserResponseTable>> getUserResponseTable(@Valid @RequestBody UserRequestTable userRequestTable) {
        return ResponseEntity.ok(userService.getUsersTable(userRequestTable));
    }

    @GetMapping("/getUser/{id}")
    @ResponseBody
    public CompletableFuture<ResponseEntity<UserResponse>> getUser(@PathVariable ObjectId id) {
        return userService.findByIdAsync(id)
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/create")
    @ResponseBody
    public CompletableFuture<ResponseEntity<UserResponse>> createUser(@Valid @ModelAttribute UserRequest userRequest) {
        return userService.createUserAsync(userRequest)
                .thenApply(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    @ResponseBody
    public CompletableFuture<ResponseEntity<UserResponse>> updateUser(@PathVariable ObjectId id, @Valid @ModelAttribute UserRequest userRequest) {
        userRequest.setId(id);
        return userService.updateUserAsync(userRequest)
                .thenApply(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public CompletableFuture<ResponseEntity<Boolean>> deleteUser(@PathVariable ObjectId id) {
        return userService.deleteByIdAsync(id)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/getStatuses")
    @ResponseBody
    public CompletableFuture<ResponseEntity<List<Status>>> getStatuses() {
        return CompletableFuture.completedFuture(ResponseEntity.ok(List.of(Status.values())));
    }

    @GetMapping({"/", ""})
    public ModelAndView getHorizontalPage(Model model) {
        return new ModelAndView("user/users").addObject("pageActive", "users");
    }

    @GetMapping("/create")
    public ModelAndView getUserCreatePage(Model model) {
        model.addAttribute("pageTitle", "users.createUser");
        model.addAttribute("pageActive", "users");
        model.addAttribute("isEdit", false);
        model.addAttribute("opened", true);
        return new ModelAndView("user/user-edit");
    }

    @GetMapping("/edit/{id}")
    public ModelAndView getUserEditPage(@PathVariable ObjectId id, Model model) {
        model.addAttribute("pageTitle", "users.editUser");
        model.addAttribute("pageActive", "users");
        model.addAttribute("isEdit", true);
        model.addAttribute("opened", true);
        return new ModelAndView("user/user-edit");
    }

    @GetMapping("/card/{id}")
    public ModelAndView showUserProfile(@PathVariable ObjectId id, Model model) {
        model.addAttribute("pageTitle", "users.user");
        model.addAttribute("pageActive", "users");
        model.addAttribute("opened", true);
        return new ModelAndView("user/userCard");
    }

}