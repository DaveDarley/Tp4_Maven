package org.example;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    /*
     * src: https://stackoverflow.com/questions/15822544/jgit-how-to-get-all-commits-of-a-branch-without-changes-to-the-working-direct
    */
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

            List<Ref> branches = git.branchList().call(); // list qui contient toutes les branches de mon repertoire

            for (Ref branch : branches) {
                String branchName = branch.getName();
                if(branchName.equals("refs/heads/master")){
                    //Recuperation de toutes les commits de ma branche master
                    Iterable<RevCommit> commits = git.log().add(repo.resolve(branchName)).call();

                    List<String[]> valuesToDisplay = new ArrayList<String[]>();

                    for (RevCommit revCommit : commits) {
                        String[] values = getAllFilesInAnCommit(revCommit, repo);
                        if(values.length > 0){
                            valuesToDisplay.add(values);
                        }
                    }
                    CSVMaker.toCsv("GitVersionsData",valuesToDisplay);
                }
            }
        }
    }


    /*
     * Src:
     * https://stackoverflow.com/questions/10993634/how-do-i-do-git-show-sha1file-using-jgit
     * https://stackoverflow.com/questions/19941597/use-jgit-treewalk-to-list-files-and-folders
    */
    /*
     * Fonction qui parcours tous les fichiers d'un commit git et retourne les metriques (mWmc,mcBc)
     * si il existe des fichiers .java dans le commit
     *  */
    public static String[] getAllFilesInAnCommit(RevCommit commit, Repository repository) throws IOException {

        int nbJavaFiles = 0;
        int wmcTotal = 0;
        double bcTotal = 0.0;
        String[] values = new String[]{};

        RevTree tree = commit.getTree();
        TreeWalk treeWalk = new TreeWalk(repository);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(false);
        while (treeWalk.next()) {

            if (treeWalk.isSubtree()) {
               // Je suis un repertoire non vide
                treeWalk.enterSubtree();
            } else {
                // Je suis un fichier
                if(treeWalk.getPathString().contains(".java")){
                    // Recuperer le contenu des fichiers ".java"
                    String fileContents = "";
                    ObjectId blobId = treeWalk.getObjectId(0);
                    try (ObjectReader objectReader = repository.newObjectReader()) {
                        ObjectLoader objectLoader = objectReader.open(blobId);
                        byte[] bytes = objectLoader.getBytes();
                        fileContents = new String(bytes, StandardCharsets.UTF_8);

//                        Files.write(Paths.get("FichierTest.txt"), fileContents.getBytes());
//                        BufferedReader texte = ClassMetrics.readFiles("FichierTest.txt");

                        Reader inputStringWmc = new StringReader(fileContents);
                        BufferedReader texteWmc = new BufferedReader(inputStringWmc);

                        wmcTotal += getWmc(texteWmc);

                        Reader inputStringBc = new StringReader(fileContents);
                        BufferedReader texteBc = new BufferedReader(inputStringBc);
                        bcTotal += getBc(texteBc);
                        nbJavaFiles ++;

                        System.out.println(fileContents);
                    }

                }
            }
        }

        if(nbJavaFiles != 0){
            double mWmc = wmcTotal / (double)nbJavaFiles;
            double mBc = bcTotal / (double) nbJavaFiles;
            values = new String[]{commit.getId()+"", nbJavaFiles+"", mWmc+"", mBc+""};
        }
        return values;
    }

    public static int getWmc(BufferedReader texte){
        int wmc = ClassMetrics.getWMC(texte);
        return wmc;
    }

    public static Double getBc(BufferedReader texte){
        System.out.println("------ Test BC ------");
        int classCloc = ClassMetrics.getClasseCLOC(texte);
        int classLoc = ClassMetrics.getClasseLOC(texte);
        int wmc = getWmc(texte);
        double DC = ClassMetrics.getClasseDC(classCloc,classLoc);
        double BC = ClassMetrics.getClasseBC(DC,wmc);

        System.out.println("Mon Loc: "+ classLoc);
        System.out.println("Mon Cloc: "+ classCloc);
        System.out.println("Mon Wmc: "+ wmc);
        System.out.println("Mon DC: "+ DC);

        System.out.println("Mon BC est: "+ BC);
        System.out.println("------ Fin Test BC ------");
        return BC;
    }



}
