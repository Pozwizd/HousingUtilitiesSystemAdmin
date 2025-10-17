package org.spacelab.housingutilitiessystemadmin.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemadmin.entity.location.Status;
import org.spacelab.housingutilitiessystemadmin.models.chairman.ChairmanRequest;
import org.spacelab.housingutilitiessystemadmin.models.chairman.ChairmanResponse;
import org.spacelab.housingutilitiessystemadmin.models.chairman.ChairmanResponseTable;
import org.spacelab.housingutilitiessystemadmin.models.filters.chairman.ChairmanRequestTable;
import org.spacelab.housingutilitiessystemadmin.service.ChairmanService;
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
@RequestMapping("/chairmen")
@AllArgsConstructor
@Slf4j
@Validated
public class ChairmanController {

    private final ChairmanService chairmanService;

    @PostMapping("/getAll")
    @ResponseBody
    public ResponseEntity<Page<ChairmanResponseTable>> getChairmanResponseTable(
            @Valid @RequestBody ChairmanRequestTable chairmanRequestTable) {
        log.info("Получен запрос на получение председателей с фильтрами: page={}, size={}, fullName={}, phone={}, email={}, login={}, status={}",
                chairmanRequestTable.getPage(), chairmanRequestTable.getSize(),
                chairmanRequestTable.getFullName(), chairmanRequestTable.getPhone(),
                chairmanRequestTable.getEmail(), chairmanRequestTable.getLogin(),
                chairmanRequestTable.getStatus());
        
        Page<ChairmanResponseTable> result = chairmanService.getChairmenTable(chairmanRequestTable);
        log.info("Возвращено {} председателей из {}", result.getNumberOfElements(), result.getTotalElements());
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/getChairman/{id}")
    @ResponseBody
    public CompletableFuture<ResponseEntity<ChairmanResponse>> getChairman(@PathVariable String id) {
        return chairmanService.findByIdAsync(id)
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/create")
    @ResponseBody
    public CompletableFuture<ResponseEntity<ChairmanResponse>> createChairman(
            @Valid @RequestBody ChairmanRequest chairmanRequest) {
        return chairmanService.createChairmanAsync(chairmanRequest)
                .thenApply(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    @ResponseBody
    public CompletableFuture<ResponseEntity<ChairmanResponse>> updateChairman(
            @PathVariable String id, @ModelAttribute ChairmanRequest chairmanRequest) {
        chairmanRequest.setId(id);
        return chairmanService.updateChairmanAsync(chairmanRequest)
                .thenApply(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public CompletableFuture<ResponseEntity<Boolean>> deleteChairman(@PathVariable String id) {
        return chairmanService.deleteByIdAsync(id)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/getStatuses")
    @ResponseBody
    public CompletableFuture<ResponseEntity<List<Status>>> getStatuses() {
        return CompletableFuture.completedFuture(ResponseEntity.ok(List.of(Status.values())));
    }

    @GetMapping("/search")
    @ResponseBody
    public CompletableFuture<ResponseEntity<List<ChairmanResponse>>> searchChairmen(
            @RequestParam(required = false) String q) {
        return CompletableFuture.completedFuture(
            ResponseEntity.ok(chairmanService.searchByName(q))
        );
    }

    @GetMapping({"/", ""})
    public ModelAndView getChairmenPage(Model model) {
        return new ModelAndView("chairman/chairmen").addObject("pageActive", "chairmen");
    }

    @GetMapping("/create")
    public ModelAndView getChairmanCreatePage(Model model) {
        model.addAttribute("pageTitle", "chairmen.createChairman");
        model.addAttribute("pageActive", "chairmen");
        model.addAttribute("isEdit", false);
        model.addAttribute("opened", true);
        return new ModelAndView("chairman/chairman-edit");
    }

    @GetMapping("/edit/{id}")
    public ModelAndView getChairmanEditPage(@PathVariable String id, Model model) {
        model.addAttribute("pageTitle", "chairmen.editChairman");
        model.addAttribute("pageActive", "chairmen");
        model.addAttribute("isEdit", true);
        model.addAttribute("opened", true);
        return new ModelAndView("chairman/chairman-edit");
    }

    @GetMapping("/card/{id}")
    public ModelAndView showChairmanProfile(@PathVariable String id, Model model) {
        model.addAttribute("pageTitle", "chairmen.chairman");
        model.addAttribute("pageActive", "chairmen");
        model.addAttribute("opened", true);
        return new ModelAndView("chairman/chairmanCard");
    }
}

