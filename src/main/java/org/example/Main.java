package org.example;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;


/*
 * Iterate through all files from an commits:
 * https://stackoverflow.com/questions/40590039/how-to-get-the-file-list-for-a-commit-with-jgit
*/

// JGit java: https://archive.eclipse.org/jgit/docs/jgit-2.0.0.201206130900-r/apidocs/org/eclipse/jgit/revwalk/RevCommit.html#getTree()



public class Main {

    public static void main(String[] args)
            throws IOException, InvalidRemoteException, TransportException, GitAPIException {

        // Local directory on this machine where we will clone remote repo.
        File localRepoDir = new File("LocalRepo/");

        // Monitor to get git command progress printed on java System.out console
        TextProgressMonitor consoleProgressMonitor = new TextProgressMonitor(new PrintWriter(System.out));

        /*
         * Git clone remote repo into local directory.
         *
         * Equivalent of --> $ git clone https://github.com/deepanshujain92/test_git.git
         */
        System.out.println("\n>>> Cloning repository\n");
        Repository repo = Git.cloneRepository().setProgressMonitor(consoleProgressMonitor).setDirectory(localRepoDir)
                .setURI("https://github.com/laurencefortin/ift3913-tp4").call().getRepository();

        try (Git git = new Git(repo)) {
            /*
             * Get list of all branches (including remote) & print
             *
             * Equivalent of --> $ git branch -a
             *
             */
            System.out.println("\n>>> Listing all branches\n");
            git.branchList().setListMode(ListMode.ALL).call().stream().forEach(r -> System.out.println(r.getName()));

            System.out.println("\n>>> Printing commit log\n");
            Iterable<RevCommit> commitLog = git.log().call();


            for (RevCommit revCommit : commitLog) {
                /*int nbJavaFilesInCommit =*/ getAllFilesInAnCommit(revCommit, repo);
               // System.out.println("commitId: " + revCommit.getId() + " nbJavaFiles: " + nbJavaFilesInCommit);


            }


        }
    }

    // src: https://stackoverflow.com/questions/19941597/use-jgit-treewalk-to-list-files-and-folders
    /*
     * Lire le contenu des fichier dans un commit git :
     * https://stackoverflow.com/questions/10993634/how-do-i-do-git-show-sha1file-using-jgit
    */
    public static void getAllFilesInAnCommit(RevCommit commit, Repository repository) throws IOException {

        int nbJavaFiles = 0;
        int wmcTotal = 0;
        double bcTotal = 0.0;

        RevTree tree = commit.getTree();
        TreeWalk treeWalk = new TreeWalk(repository);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(false);
        while (treeWalk.next()) {
            if (treeWalk.isSubtree()) {
               // System.out.println("dir: " + treeWalk.getPathString());
                treeWalk.enterSubtree();
            } else {

                if(treeWalk.getPathString().contains(".java")){
                    String fileContents = "";
                    // Recuperer le contenu des fichiers ".java"
                    ObjectId blobId = treeWalk.getObjectId(0);
                    try (ObjectReader objectReader = repository.newObjectReader()) {
                        ObjectLoader objectLoader = objectReader.open(blobId);
                        byte[] bytes = objectLoader.getBytes();
                        fileContents = new String(bytes, StandardCharsets.UTF_8);
                       // System.out.println(fileContents);
                    }

                    Reader inputString = new StringReader(fileContents);
                    BufferedReader reader = new BufferedReader(inputString);

                    wmcTotal += getWmc(reader);
                    bcTotal += getBc(reader);
                    nbJavaFiles ++;
                }
               // System.out.println("file: " + treeWalk.getPathString());
            }
        }
        if(nbJavaFiles != 0){
            double mWmc = wmcTotal / (double)nbJavaFiles;
            double mBc = bcTotal / (double) nbJavaFiles;
            System.out.println("Version: " + commit.getId() + " mWmc: "+mWmc +" mBc: "+mBc);
        }
       // return nbJavaFiles;
    }

    public static int getWmc(BufferedReader texte){
        int wmc = ClassMetrics.getWMC(texte);
        return wmc;
    }

    public static Double getBc(BufferedReader texte){
        int classCloc = ClassMetrics.getClasseCLOC(texte);
        int classLoc = ClassMetrics.getClasseLOC(texte);
        int wmc = getWmc(texte);
        double DC = ClassMetrics.getClasseDC(classCloc,classLoc);
        double BC = ClassMetrics.getClasseBC(DC,wmc);
        return BC;
    }



}
