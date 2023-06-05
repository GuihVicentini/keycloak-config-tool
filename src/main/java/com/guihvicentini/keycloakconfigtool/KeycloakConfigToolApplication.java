package com.guihvicentini.keycloakconfigtool;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;


@SpringBootApplication
@ConfigurationPropertiesScan
public class KeycloakConfigToolApplication implements CommandLineRunner, ExitCodeGenerator {

	private IFactory iFactory;
	private KeycloakConfigToolCommands commands;
	private int exitCode;

	KeycloakConfigToolApplication(IFactory factory, KeycloakConfigToolCommands commands){
		this.iFactory = factory;
		this.commands = commands;
	}

	@Override
	public void run(String... args) throws Exception {
		exitCode = new CommandLine(commands, iFactory).execute(args);
	}

	@Override
	public int getExitCode() {
		return exitCode;
	}

	public static void main(String[] args) {
		System.exit(SpringApplication.exit(SpringApplication.run(KeycloakConfigToolApplication.class, args)));
	}
}
