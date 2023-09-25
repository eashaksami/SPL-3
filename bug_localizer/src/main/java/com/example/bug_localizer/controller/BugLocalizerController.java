package com.example.bug_localizer.controller;

import com.example.bug_localizer.service.BugLocalizationService;
import com.example.bug_localizer.service.GitCloneService;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

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
                                                    @RequestParam("noOfBuggyFiles") String noOfBuggyFiles)
            throws IOException, ParseException {
        copyFiles(multipartFiles);
        int noOfBugReport = Integer.parseInt(noOfBuggyFiles);

        List<String> response = bugLocalizationService.getBuggyFiles(DIRECTORY, bugReport, noOfBugReport);

        deleteFile();

        return ResponseEntity.ok().body(response);
    }

    @PostMapping(value = "/clone-repo")
    public ResponseEntity<String> cloneGitRepo(@RequestParam("gitRepoLink") String gitRepoLink) throws IOException, GitAPIException {
        String gitRepoLocation = gitCloneService.cloneRepository(gitRepoLink);
        return ResponseEntity.ok().body(gitRepoLocation);
    }

    private void copyFiles(List<MultipartFile> multipartFiles) throws IOException {
        for (MultipartFile file : multipartFiles) {
            String filename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            Files.createDirectories(Paths.get(DIRECTORY));
            if (file.getOriginalFilename().endsWith(".java")) {
                Path fileStorage = get(DIRECTORY, filename).toAbsolutePath().normalize();
                copy(file.getInputStream(), fileStorage, REPLACE_EXISTING);
            }
        }
    }

    private void deleteFile() throws IOException {
        FileUtils.deleteDirectory(new File(DIRECTORY));
    }
}
