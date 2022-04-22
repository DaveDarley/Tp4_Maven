package org.example;

/*
 * maven repository pour jgit et common:
 * https://mvnrepository.com/
 */

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;

import org.apache.commons.io.FileUtils;


// JGit java: https://archive.eclipse.org/jgit/docs/jgit-2.0.0.201206130900-r/apidocs/org/eclipse/jgit/revwalk/RevCommit.html#getTree()

/*
 * Comprehension Jgit et src:
 * https://www.baeldung.com/jgit
 * https://itsallbinary.com/git-commands-from-java-using-jgit-programmatically-git-clone-checkout-b-commit-a-log-status-branch/
 * https://stackoverflow.com/questions/15822544/jgit-how-to-get-all-commits-of-a-branch-without-changes-to-the-working-direct
* */

/*
 * Delete repertoire Local Java:
 * https://stackoverflow.com/questions/20281835/how-to-delete-a-folder-with-files-using-java
 */


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
                .setURI("https://github.com/jfree/jfreechart").call().getRepository();

        try (Git git = new Git(repo)) {

            List<Ref> branches = git.branchList().call(); // list qui contient toutes les branches de mon repertoire

            for (Ref branch : branches) {
                String branchName = branch.getName();
                if(branchName.equals("refs/heads/master")){
                    //Recuperation de toutes les commits de ma branche master
                    Iterable<RevCommit> commits = git.log().add(repo.resolve(branchName)).call();

                    List<String[]> valuesToDisplay = new ArrayList<String[]>();
                    int i = 1;
                    for (RevCommit revCommit : commits) {
                        System.out.println(i);
                        if(i == 212){ // pr prendre juste 5% des commits du jfreechart (5% de 4211 == 211)
                            CSVMaker.toCsv("GitVersionsData",valuesToDisplay);

                            // Pour supprimer le dossier creer lors du clonage du repertoire Github
                            FileUtils.forceDelete(new File("LocalRepo"));
                            return;
                        }

                        String[] values = getAllFilesInAnCommit(revCommit, repo);
                        if(values.length > 0){
                            valuesToDisplay.add(values);
                        }
                        i++;

                    }

                    // PR TESTER AVEC AUTRE REPERTOIRE QUE LE JFREECHART
                    //CSVMaker.toCsv("GitVersionsData",valuesToDisplay);
                    //FileUtils.forceDelete(new File("LocalRepo"));
                }
            }
        }
    }


    /*
     * Src:
     * https://stackoverflow.com/questions/10993634/how-do-i-do-git-show-sha1file-using-jgit
     * https://stackoverflow.com/questions/19941597/use-jgit-treewalk-to-list-files-and-folders
     * https://stackoverflow.com/questions/40590039/how-to-get-the-file-list-for-a-commit-with-jgit
    */

    /*
     * Fonction qui parcours tous les fichiers d'un commit git et retourne les metriques (mWmc,mcBc)
     * si il existe des fichiers .java dans le commit
    */
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

                        Files.write(Paths.get("FichierJava.txt"), fileContents.getBytes());

                        wmcTotal += getWmc("FichierJava.txt");
                        bcTotal += getBc("FichierJava.txt");
                        nbJavaFiles ++;
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

    public static int getWmc(String fichier){
        BufferedReader texteWmc = ClassMetrics.readFiles(fichier);
        int wmc = ClassMetrics.getWMC(texteWmc);
        return wmc;
    }

    /*
     * Pas du tt efficace mais manque de temps
     * A chaque fois je dois re-creer un BufferedReader pour pouvoir lire le fichier
     * sinon apres la premiere lecture le pointeur se trouve a la fin du texte.
     */

    public static Double getBc(String fichier){

        BufferedReader texteCloc = ClassMetrics.readFiles(fichier);
        int classCloc = ClassMetrics.getClasseCLOC(texteCloc);

        BufferedReader texteLoc = ClassMetrics.readFiles(fichier);
        int classLoc = ClassMetrics.getClasseLOC(texteLoc);

        int wmc = getWmc(fichier);

        double DC = ClassMetrics.getClasseDC(classCloc,classLoc);
        double BC = ClassMetrics.getClasseBC(DC,wmc);

        return BC;
    }



}
