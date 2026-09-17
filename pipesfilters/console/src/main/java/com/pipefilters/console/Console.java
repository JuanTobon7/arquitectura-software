package com.pipefilters.console;

import com.pipefilters.commands.CommandsOrchestrator;
import com.pipefilters.commands.CommandsStrategy;

import java.util.Scanner;

public class Console {

    private static final String PROMPT =
            "pipesfilter> ";

    static void main() {

        CommandsStrategy strategy =
                new CommandsStrategy();

        CommandsOrchestrator orchestrator =
                new CommandsOrchestrator(strategy);

        Scanner scanner =
                new Scanner(System.in);

        System.out.print(PROMPT);

        while (scanner.hasNextLine()) {

            String command =
                    scanner.nextLine().trim();

            if (command.equalsIgnoreCase("exit")) {
                break;
            }

            if (command.isBlank()) {
                System.out.print(PROMPT);
                continue;
            }

            try {

                orchestrator.execute(command);

            } catch (Exception e) {

                System.out.println(
                        "Error: " + e.getMessage()
                );
            }

            System.out.print(PROMPT);
        }
    }
}