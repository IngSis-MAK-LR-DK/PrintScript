package edu.austral.ingsis.printscript.cli;

import java.util.Scanner;

import edu.austral.ingsis.printscript.interpreter.InputProvider;

/** Prompts on {@code System.out} and reads one line back from {@code System.in}. */
final class StdInInputProvider implements InputProvider {

    private final Scanner scanner = new Scanner(System.in);

    @Override
    public String read(String prompt) {
        System.out.println(prompt);
        return scanner.nextLine();
    }
}
