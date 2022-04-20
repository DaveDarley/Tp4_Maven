package org.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

public class CSVMaker {
    static void toCsv(String nomFichier, List<String[]> container){
        String file = nomFichier+".csv";
        File csvFile = new File(file);
        try {
            PrintWriter out = new PrintWriter(csvFile);
            out.printf("%s, %s, %s, %s\n",
                    "id_version","NC","mWMC","mcBC");

            for(int i = 0; i < container.size(); i++){
                for(int j = 0; j < container.get(i).length - 1; j++){
                    out.printf("%s, ",
                            container.get(i)[j]);
                }

                if(i == container.size() - 1){
                    out.printf("%s",
                            container.get(i)[container.get(i).length - 1]);
                }else{
                    out.printf("%s\n",
                            container.get(i)[container.get(i).length - 1]);
                }

            }
            out.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
