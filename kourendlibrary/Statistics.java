package net.runelite.client.plugins.kourendlibrary;

public class Statistics {

  private static final Statistics instance = new Statistics();

  public static Statistics get() {
    return instance;
  }

  private int booksGained = 0;
  private int resets = -1;

  private Statistics() {
  }

  public int getBooksGained() {
    return booksGained;
  }

  public void incrementBooksGained() {
    this.booksGained++;
  }


  public int getResets() {
    return resets;
  }

  public void incrementResets() {
    this.resets++;
  }
}
