package com.example.bug_localizer.controller;

import com.example.bug_localizer.model.CloneRepoModel;
import com.example.bug_localizer.service.BugLocalizationService;
import com.example.bug_localizer.service.GitCloneService;
import org.apache.lucene.queryparser.classic.ParseException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.copy;
import static java.nio.file.Paths.get;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@RestController
@RequestMapping("/file")
public class BugLocalizerController {
    // define a location
    public static final String DIRECTORY = System.getProperty("user.home") + "/Downloads/uploads/";

    @Autowired
    GitCloneService gitCloneService;
    @Autowired
    BugLocalizationService bugLocalizationService;

    // Define a method to upload files
    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadFiles(@RequestParam("files") List<MultipartFile> multipartFiles,
                                                    @RequestParam("bugReport") MultipartFile bugReport,
                                                    @RequestParam("noOfBuggyFiles") String noOfBuggyFiles) throws IOException, ParseException {
        List<String> filenames = new ArrayList<>();
        System.out.println(multipartFiles);
        System.out.println(bugReport.getOriginalFilename());
        System.out.println(bugReport);
        System.out.println(noOfBuggyFiles);
        for(MultipartFile file : multipartFiles) {
            String filename = StringUtils.cleanPath(file.getOriginalFilename());
            Files.createDirectories(Paths.get(DIRECTORY));
            if(file.getOriginalFilename().endsWith(".java")) {
                Path fileStorage = get(DIRECTORY, filename).toAbsolutePath().normalize();
                copy(file.getInputStream(), fileStorage, REPLACE_EXISTING);
                filenames.add(filename);
            }
        }
        return ResponseEntity.ok().body(bugLocalizationService.getBuggyFiles(DIRECTORY));
    }

    @PostMapping(value = "/clone-repo")
    public ResponseEntity<String> cloneGitRepo(@RequestBody CloneRepoModel cloneRepoModel,
                                               @RequestParam("bugReport") MultipartFile bugReport) throws IOException, GitAPIException {
        System.out.println(bugReport.getOriginalFilename());
        gitCloneService.cloneRepository(cloneRepoModel.getRepoLink());
        return ResponseEntity.ok().body("Repo cloned successfully!!!");
    }
}
