package edu.austral.ingsis.printscript.cli;

/**
 * Prints parsing progress as a rough percentage of the file read so far.
 *
 * <p>Keeps the highest offset seen rather than a running total. The lexer peeks ahead and sometimes
 * re-reads the same offset, so adding up every callback would overcount and hit 100% before the
 * file is actually done.
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
