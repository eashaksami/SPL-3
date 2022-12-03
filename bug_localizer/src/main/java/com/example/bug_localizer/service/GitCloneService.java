package com.example.bug_localizer.service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GitCloneService {
    public static final String DIRECTORY = System.getProperty("user.home") + "/Downloads/";
    public String cloneRepository(String repoLink) throws GitAPIException, IOException {

        File dir = createTempDirectory();

        try(
                Git result = Git.cloneRepository()
                        .setProgressMonitor(new TextProgressMonitor(new PrintWriter(System.out)))
                        .setURI(repoLink)
                        .setDirectory(new File(dir.getAbsolutePath())).call()
        ) {
            result.getRepository().close();
        }

        printAllFileList(dir.getAbsolutePath());
        return DIRECTORY+ "Git Repo";
    }

    public File createTempDirectory() {
        String workingDirPath = "/home/sami/Desktop/SPL-3/Temp";

        File workingDirectory = new File(DIRECTORY);

        File dir = new File(DIRECTORY, "Git Repo");
        dir.mkdirs();

        return dir;
    }

    public void printAllFileList(String filePath) throws IOException {
        List<File> filesInFolder = Files.walk(Paths.get(filePath))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());

        System.out.println(filesInFolder);
    }
}
