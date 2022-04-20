package org.example;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ClassMetrics {

    /*
     * Tiré du TP1
    */


    /**
     * Fonction qui permet de lire un fichier
     * @param chemin
     * @return
     */
    static BufferedReader readFiles(String chemin){
        FileReader fileToParse = null;
        BufferedReader texte  = null;
        try {
            fileToParse = new FileReader(chemin);
            texte = new BufferedReader(fileToParse);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return texte;
    }


    /**
     * Fonction retournant le nb de ligne de code d'un fichier.
     * Les lignes vides ne sont pas prises en compte lors du calcul
     * Ligne code = nb ligne total - ligne vide
     * @param text
     * @return
     */
    static int getClasseLOC(BufferedReader text){

        int nbLineOfFile = 0;
        int nbEmptyLine = 0;
        String line = null;

        try {
            while((line=text.readLine()) != null){
                nbLineOfFile++;
                nbEmptyLine += isLineEmpty(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return nbLineOfFile - nbEmptyLine;
    }


    /**
     * Fonction comptant le nombre de ligne qui contient
     * un commentaire dans un fichier texte
     * @param texte
     * @return
     */
    static int getClasseCLOC(BufferedReader texte){

        int nbLineWithComments = 0;
        String line = null;

        try {
            while((line=texte.readLine()) != null){

                line = line.trim();

                // Ma ligne commence par /*
                if( (isLineEmpty(line) == 0) && parseLine(line) == 2){
                    nbLineWithComments++;

                    if( /*line.length()>2 && (line.charAt(line.length()-1)!='/') &&
                      (!line.contains("/**"))*/ line.contains("*/") ){ //alors j'ai ce commentaire /* lalala */ ....

                    }else{
                        // je suis dans le cas /* */ ou /** */
                        while((line=texte.readLine()) != null && !(line.contains("*/")) ){
                            if(isLineEmpty(line)==0){
                                nbLineWithComments++;
                            }
                        }
                        nbLineWithComments++;
                    }
                }else{
                    // Soit j'ai une ligne de texte ou une ligne de commentaire ou ligne mix commentaire et texte
                    if(isLineEmpty(line) == 0 && parseLine(line) != 0){
                        //System.out.println(line);
                        nbLineWithComments+= parseLine(line);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return nbLineWithComments;
    }


    /**
     * Fonction qui récupère la métrique DC pour une classe java
     * @param classeCLOC
     * @param classeLOC
     * @return
     */
    static double getClasseDC(int classeCLOC, int classeLOC) {
        return (double) classeCLOC / (double) classeLOC;
    }


    /**
     * Fonction qui decrit si la ligne passee en parametre
     * est vide ou pas
     * @param line
     * @return
     */
    static int isLineEmpty(String line){

        String lineToParse = line.trim(); // enleve whitespace si y en a en debut de ligne
        if(lineToParse.length() == 0){ // ligne vide
            return 1;
        }

        return 0;
    }


    /**
     * Fonction permettant de dire si la ligne passe en parametre est:
     * - une ligne de texte: commence par (A-Z) et ne contient pas // /*
     * - mix commentaire et texte: commence par (A-Z) et contient // ou /*
     * - ligne de commentaire: commence par //
     * - bloc commentaire , javadoc: commence par /*
     * @param line
     * @return
     */
    static int parseLine(String line){

        String lineToParse = line.trim(); // enleve whitespace si y en a en debut de ligne
        int answer = 0;

        if(lineToParse.length() != 0 && lineToParse.charAt(0) != '/'){ // ma ligne commence par une lettre (A-Z)

            if( (lineToParse.contains("//")) || (lineToParse.contains("/*"))  ){ // cette ligne contient et du texte et des commentaires
                answer = 1;
            }else{ // ligne texte
                answer = 0;
            }

        }else {
            // Alors "peut-etre" bloc commentaire ou juste une ligne de commentaire

            if(lineToParse.length() != 0 && lineToParse.charAt(0) == '/' && lineToParse.charAt(1) == '/'){
                answer = 1;
            }

            else if(lineToParse.length() != 0 && lineToParse.charAt(0) == '/' && lineToParse.charAt(1) == '*'){
                answer = 2;
            }
        }

        return answer;
    }


    /**
     * Fonction qui prend en paramètre la densité de commentaires
     * et la somme des complexités cyclomatique de McCabe d'une classe java
     * et retourne le degré selon lequel cette classe est bien commentée
     * @param classeDC
     * @param WMC
     * @return
     */
    public static double getClasseBC(double classeDC, int WMC) {
        return classeDC / (double) WMC;
    }


    /**
     * Fonction qui prend en paramètre la densité de commentaires
     * et la somme des complexités cyclomatique de McCabe d'un paquet java
     * et retourne le degré selon lequel ce paquet est bien commenté
     * @param paquetDC
     * @param WCP
     * @return
     */
    public static double getPaquetBC(double paquetDC, int WCP) {
        return paquetDC / (double) WCP;
    }


    /**
     * Fonction qui prend une ligne de code et qui retourne cette même ligne
     * sans les commentaires
     * @param line
     * @return
     */
    static String[] reformLine(String[] line) {
        String[] newLine = {"", ""};
        String[] lineWords = line[0].split("\\s");

        // Patterns of comments
        String simpleComment = "//.*";
        String complexCommentStart = "/\\*|/\\*\\*";
        String complexCommentEnd = "[*]/";
        Pattern simplePattern = Pattern.compile(simpleComment, Pattern.CASE_INSENSITIVE);
        Pattern complexPatternStart = Pattern.compile(complexCommentStart, Pattern.CASE_INSENSITIVE);
        Pattern complexPatternEnd = Pattern.compile(complexCommentEnd, Pattern.CASE_INSENSITIVE);

        boolean simpleCommented = false;
        boolean complexCommented = Boolean.parseBoolean(line[1]);
        for (String lineWord : lineWords) {
            if (!simpleCommented) {
                // Si on tombe sur un commentaire de type // la ligne est terminé
                if (simplePattern.matcher(lineWord).find()) {
                    simpleCommented = true;
                } //Si on tombe sur un commentaire de la forme /** ou /*
                else if (complexPatternStart.matcher(lineWord).find()) {
                    complexCommented = true;
                } else {
                    // Si nous ne somme pas dans un commentaire, on ajoute le mot à la ligne
                    if (!complexCommented) {
                        newLine[0] += lineWord + " ";
                    } // Sinon on regarde si l'on tombe sur une fin de commentaire (*/)
                    else if (complexPatternEnd.matcher(lineWord).find()) {
                        String[] splitComment = lineWord.split("\\*/");
                        // dans ce cas on n'ajoute pas la partie avec la fin de commentaire
                        // mais on ajoute bien la suivante
                        if (splitComment.length > 1) {
                            newLine[0] += splitComment[1] + " ";
                        }
                        complexCommented = false;
                    }
                }
            }
        }
        newLine[1] = Boolean.toString(complexCommented);

        return newLine;
    }

    /**
     * Fonction qui prend un BufferedReader d'une classe java en entre et retourne la somme pondérée
     * des complexités cyclomatiques de McCabe de toutes les méthodes d'une classe
     * @param texte
     * @return
     */
    static int getWMC(BufferedReader texte) {
        int mccabeComplexity = 0;
        int numberOfMethods = 0;
        String line = null;
        String[] noCommentLine;

        boolean complexCommented = false;

        // Création des patterns pour récupérer le nombre de prédicats et de méthodes
        String condition = "(while|if|for)\\(.*\\)" +
                "|\\bwhile\\b|\\bif\\b|\\bfor\\b|\\b[{]?else[}]?\\b|\\bcase\\b|\\bdefault\\b";
        String booleanCondition = "\\b(&&|[|]{2}|\\?|and)\\b";
        String method = ".+\\s.+\\(\\w+\\s\\w+\\)";
        Pattern conditionPattern = Pattern.compile(condition);
        Pattern booleanConditionPattern = Pattern.compile(booleanCondition);
        Pattern methodPattern = Pattern.compile(method);

        String[] new_line = {"", "false"};
        try {
            while((line=texte.readLine()) != null){
                new_line[0] = line;
                new_line[1] = Boolean.toString(complexCommented);
                // on récupère la ligne sans commentaires
                noCommentLine = reformLine(new_line);
                complexCommented = Boolean.parseBoolean(noCommentLine[1]);

                // Si la ligne contient un pattern de prédicats (while, if, etc)
                if(conditionPattern.matcher(noCommentLine[0]).find()) {
                    mccabeComplexity += 1;
                }
                // Si la ligne contient un pattern de boolean_condition (&&, ||, and, etc)
                if(booleanConditionPattern.matcher(noCommentLine[0]).find()) {
                    String[] split_line =  noCommentLine[0].split("\\s");
                    for (String s : split_line) {
                        if (booleanConditionPattern.matcher(s).find()) {
                            mccabeComplexity += 1;
                        }
                    }
                }
                // Si la ligne contient la déclaration d'une méthode
                if(methodPattern.matcher(noCommentLine[0]).find()) {
                    mccabeComplexity += 1;
                    numberOfMethods += 1;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Si il n'y a pas de déclaration de méthode alors la compléxité est de 1
        if(numberOfMethods == 0) {
            return 1;
        }
        return mccabeComplexity / numberOfMethods;
    }
}
