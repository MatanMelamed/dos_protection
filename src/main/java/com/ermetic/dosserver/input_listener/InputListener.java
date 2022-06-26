package com.ermetic.dosserver.input_listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class InputListener implements CommandLineRunner {
    
    @Autowired
    private ConfigurableApplicationContext context;


    @Override
    public void run(String... args) {
        System.out.println("Enter any key to stop server (char + enter)");
        new Scanner(System.in).nextLine();
        context.close();
    }
}
