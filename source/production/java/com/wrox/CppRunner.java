package com.wrox;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;



public class CppRunner {
    public static String RunCpp(String cpp, String para) {
        System.out.println(para);
        String res = "";
        try {
            Process pc;
            Runtime rt;
            rt = Runtime.getRuntime();
            pc = rt.exec(cpp);
            OutputStream fos = pc.getOutputStream();
            PrintStream ps = new PrintStream(fos);
            ps.print(para);

            ps.flush();
            ps.close();

            InputStream ios = pc.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(ios));

            try {
                res = br.readLine();
                // while ((s = br.readLine()) != null) {
                // System.out.println("RUN  " + s);
                // res += s;
                // }
                br.close();
                ios.close();
            } catch (Exception e) {
                System.out.println(e);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println("response : " + res);
        return res;
    }
}
