package edu.austral.ingsis.printscript.cli;

/** Prints parsing progress to the console as an approximate percentage of the file consumed. */
final class ProgressReporter {

    private final CountingReader reader;
    private final long totalChars;
    private int lastPercentPrinted = -1;

    ProgressReporter(CountingReader reader, long totalChars) {
        this.reader = reader;
        this.totalChars = totalChars;
    }

    void report() {
        if (totalChars <= 0) {
            return;
        }
        int percent = (int) Math.min(100, (reader.charsRead() * 100) / totalChars);
        if (percent != lastPercentPrinted) {
            System.out.print("\rParsing... " + percent + "%");
            System.out.flush();
            lastPercentPrinted = percent;
        }
    }

    void finish() {
        System.out.println("\rParsing... 100%");
    }
}
