package com.ermetic.dosserver;

import com.ermetic.dosserver.services.ClientTimeFrame;
import com.ermetic.dosserver.services.IClientFrameTimeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Consumer;

@Component
public class InputListener implements CommandLineRunner {

    @Autowired
    IClientFrameTimeManager clientFrameTimeManager;

    private final Map<String, Consumer<String>> commands = new HashMap<>();


    @PostConstruct
    private void loadCommands() {
        commands.put("print", (args) -> {
            String[] s = args.split(" ");
            String clientId = s[1];
            ClientTimeFrame clientTimeFrame = clientFrameTimeManager.getClientTimeFrame(Integer.parseInt(clientId));
            System.out.println(clientTimeFrame);
        });
    }


    @Override
    public void run(String... args) throws Exception {
        boolean shouldContinue = true;
        Scanner scanner = new Scanner(System.in);

        while (shouldContinue) {
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("exit")) {
                shouldContinue = false;

            }

            commands.keySet().stream()
                    .filter(cmd -> input.toLowerCase(Locale.ROOT).startsWith(cmd.toLowerCase(Locale.ROOT)))
                    .findFirst()
                    .ifPresent(command -> commands.get(command).accept(input));

        }
    }
}
