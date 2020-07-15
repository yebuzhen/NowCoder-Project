package com.nowcoder.community;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WkTests {

  public static void main(String[] args) {

    String cmd =
        "wkhtmltoimage --quality 75 https://www.nowcoder.com '/home/barea/Course/workspace/data/wk-images/3.png'";
    try {

      Process proc = Runtime.getRuntime().exec(cmd.toString());
      HtmlToPdfInterceptor error = new HtmlToPdfInterceptor(proc.getErrorStream());
      HtmlToPdfInterceptor output = new HtmlToPdfInterceptor(proc.getInputStream());
      error.start();
      output.start();
      proc.waitFor();
      System.out.println("ok");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

class HtmlToPdfInterceptor extends Thread {
  private InputStream is;

  public HtmlToPdfInterceptor(InputStream is) {
    this.is = is;
  }

  public void run() {
    try {
      InputStreamReader isr = new InputStreamReader(is, "utf-8");
      BufferedReader br = new BufferedReader(isr);
      String line;
      while ((line = br.readLine()) != null) {
        System.out.println(line); // 输出内容
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
