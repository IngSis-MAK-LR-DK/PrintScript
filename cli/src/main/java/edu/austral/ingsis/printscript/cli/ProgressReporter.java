package edu.austral.ingsis.printscript.cli;

/**
 * Prints parsing progress to the console as an approximate percentage of the file consumed.
 *
 * <p>Tracks a high-water mark, not a running sum: the lexer's own {@code peek}/{@code advance}
 * pattern re-reads the same byte offset more than once, so summing every reported delta would
 * overcount and pin the bar at 100% early. Taking the highest offset reached is exact regardless of
 * how many times any given position gets re-read.
 */
final class ProgressReporter {

    private final long totalBytes;
    private long bytesConsumed = 0;
    private int lastPercentPrinted = -1;

    ProgressReporter(long totalBytes) {
        this.totalBytes = totalBytes;
    }

    void reachedOffset(long offset) {
        if (offset > bytesConsumed) {
            bytesConsumed = offset;
        }
    }

    void report() {
        if (totalBytes <= 0) {
            return;
        }
        int percent = (int) Math.min(100, (bytesConsumed * 100) / totalBytes);
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
