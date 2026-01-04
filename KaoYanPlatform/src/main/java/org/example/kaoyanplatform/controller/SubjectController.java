package org.example.kaoyanplatform.controller;

import org.example.kaoyanplatform.common.Result;
import org.example.kaoyanplatform.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subject")
public class SubjectController {

    @Autowired
    private SubjectService subjectService;

    @GetMapping("/list")
    public Result getList() {
        return Result.success(subjectService.list());
    }

    @GetMapping("/tree")
    public Result getTree(@RequestParam(required = false) Long userId) {
        // If userId is not passed, maybe get from SecurityContext, but for now allow param
        return Result.success(subjectService.getTree(userId));
    }
}