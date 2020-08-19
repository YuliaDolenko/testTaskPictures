package com.test.taskAgileEngine.controller;

import com.test.taskAgileEngine.service.PictureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/pictures")
public class PictureController {

    @Autowired
    private PictureService pictureService;

    @GetMapping
    public Object getPicturesByPage(@RequestParam Integer pageNumber) {
        return pictureService.getPicturesByPage(pageNumber);
    }

    @GetMapping("/search/{searchTerm}")
    public Object getPicturesByTerm(@PathVariable("searchTerm") String searchTerm) {
        return pictureService.getPictureByTerm(searchTerm);
    }
}
