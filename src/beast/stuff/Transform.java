package beast.stuff;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 *
 */
public class Transform {

    public static void main(String[] args) throws Exception{
        java.io.File file;

        if (args != null && args.length > 0) {
            file = new java.io.File(args[0]);
        } else {
            throw new Exception("there is no file");
        }

        //calculateR0(file);
        //calculateMedian(file);
        addParameter(file);



    }

    public static void calculateR0(java.io.File file) throws Exception{
        BufferedReader fin = null;


        ArrayList<Double> R0 = new ArrayList<Double>();

        try {
            fin = new BufferedReader(new FileReader(file));
            String str;
            do {
                str = fin.readLine();


            } while (str.contains("#"));
            if (str.contains("birthRate")) {
                ArrayList<String> fieldList= new ArrayList<String>(Arrays.asList(str.split("\\s+")));
                int birthIndex = fieldList.indexOf("birthRate");
                int deathIndex = fieldList.indexOf("deathRate");
                while (fin.ready()) {
                    String str1 = fin.readLine();
                    String[] fields = str1.split("\\s+");
                    R0.add(Double.parseDouble(fields[birthIndex])-Double.parseDouble(fields[deathIndex]));
                }

            }
        } catch (IOException e) {
            //
        }
        finally {
            if (fin != null) {
                fin.close();
            }
        }
        Collections.sort(R0);
        double median = R0.get(R0.size()/2);
        System.out.println(median);
    }

    public static void calculateMedian(java.io.File file) throws Exception {
        BufferedReader fin = null;

        ArrayList<Double> data = new ArrayList<Double>();

        try {
            fin = new BufferedReader(new FileReader(file));
            while (fin.ready()) {
                String str =fin.readLine();
                data.add(Double.parseDouble(str));
            }
        } catch (IOException e) {
            //
        }
        finally {
            if (fin != null) {
                fin.close();
            }
        }
        Collections.sort(data);
        double median = data.get(data.size()/2);
        System.out.println(median);
    }

    public static void addParameter(java.io.File file) throws Exception{
        String fileName = file.getName();
        int end = fileName.indexOf("_");
        String seed = fileName.substring(0, end);
        java.io.File newLogFile = new java.io.File("/Users/agav755/logs/" + seed + ".log");

        BufferedReader fin = null;
        PrintWriter writer = null;

        try {
            fin = new BufferedReader(new FileReader(file));
            writer = new PrintWriter(new FileWriter(newLogFile));
            while (fin.ready()) {
                String str =fin.readLine();
                while (str.contains("#")) {
                    writer.println(str);
                    str = fin.readLine();
                }
                if (str.contains("birthRate")) {
                    ArrayList<String> fieldList= new ArrayList<String>(Arrays.asList(str.split("\\s+")));
                    int birthIndex = fieldList.indexOf("birthRate");
                    int deathIndex = fieldList.indexOf("deathRate");
                    int psiIndex = fieldList.indexOf("samplingRate2");
                    str = str + "birthdeath" + "\t"+ "birthsampling";
                    writer.println(str);

                    while (fin.ready()) {
                        String str1 = fin.readLine();
                        String[] fields = str1.split("\\s+");
                        double birth_death = Double.parseDouble(fields[birthIndex]) - Double.parseDouble(fields[deathIndex]);
                        double birthsampling = Double.parseDouble(fields[birthIndex])*Double.parseDouble(fields[psiIndex]);
                        str1 = str1 + Double.toString(birth_death) + "\t" + Double.toString(birthsampling);
                        writer.println(str1);
                    }

                }

            }
        } catch (IOException e) {
            //
        }
        finally {
            if (fin != null) {
                fin.close();
            }
            if (writer != null) {
                writer.close();
            }
        }

    }
}
