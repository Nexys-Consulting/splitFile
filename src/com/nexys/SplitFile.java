package com.nexys;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SplitFile {

    static String inFile="inFile.txt";
    static String outFile="splitfile-";
    static String infoFile="infofiles.txt";

    public static void main(String[] args) {

        OptionParser parser = new OptionParser();
        OptionSpec<String> inFileOpt = parser.accepts("in").withRequiredArg().ofType(String.class).describedAs("Fichier à découper").
                required();
        OptionSpec<String> outFileOpt = parser.accepts("out").withOptionalArg().ofType(String.class).defaultsTo(outFile)
                .describedAs("Début du nom des fichiers découpés");
        OptionSpec<String> infoFileFileOpt = parser.accepts("infofile").withOptionalArg().ofType(String.class).defaultsTo(infoFile)
                .describedAs("Nom du fichier récap");
        parser.accepts( "help" ).forHelp();

        OptionSet options=null;
        try {
            options = parser.parse(args);
        } catch (Exception e) {
            try {
                parser.printHelpOn( System.out );
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            System.exit(1);
        }

        long startTime = System.currentTimeMillis();

        // Open in file
        File file = new File(inFileOpt.value(options));
        BufferedReader reader = null;

        if (!file.exists()) {
            System.err.println("Erreur fichier introuvable : " + file.getAbsolutePath());
            System.exit(1);
        }

        HashMap<String, Integer> m = new HashMap<String, Integer>();
        HashMap<String, BufferedWriter> of = new HashMap<String, BufferedWriter>();

        Long nbTotalLine = 0L;

        try {
            reader = new BufferedReader(new FileReader(file));
            String text = null;
            String article = null;
            while ((text = reader.readLine()) != null) {
                article = text.substring(5,8);
                m.put(article, m.containsKey(article)?m.get(article)+1:1);
                nbTotalLine++;
                if (nbTotalLine % 10000 == 0) {
                    System.out.println(nbTotalLine);
                }

                if (!of.containsKey(article)) {
                    String ofilename = new StringBuilder().append(outFileOpt.value(options)).append(article).append(".txt").toString();
                    System.out.println("Open file:" + ofilename);
                    BufferedWriter nwriter = new BufferedWriter(new FileWriter(new File(ofilename)));
                    of.put(article, nwriter);
                }
                BufferedWriter writer = of.get(article);
                writer.write(text);
                writer.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            for (BufferedWriter writer : of.values()) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Write recap file
        BufferedWriter writer =null;
        try {
            writer = new BufferedWriter(new FileWriter(new File(infoFileFileOpt.value(options))));
            for (Map.Entry<String, Integer> entry : m.entrySet()) {
                System.out.println("Article:"+entry.getKey() + " NB Line:"+ entry.getValue());
                writer.write(outFileOpt.value(options)+entry.getKey()+".txt;"+m.get(entry.getKey()) +"\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        long stopTime = System.currentTimeMillis();
        long millis = stopTime - startTime;
        String hms=String.format("%02d:%02d:%02d,%03d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1),
                TimeUnit.MILLISECONDS.toMillis(millis) % TimeUnit.SECONDS.toMillis(1)
        );
        System.out.println("Split take "+hms);

    }
}
