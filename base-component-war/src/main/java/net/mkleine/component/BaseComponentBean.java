package net.mkleine.component;

public class BaseComponentBean {

  public void setProperty(String property) {
    System.out.println("-------------------------------------------------------");
    System.out.println("\n \t injected property is :" + property + " \n");
    System.out.println("-------------------------------------------------------");
  }

  public void setVerbose(String verbose) {
    System.out.println("-------------------------------------------------------");
    System.out.println("\n \t verbose :" + verbose + " \n");
    System.out.println("-------------------------------------------------------");
  }
}
