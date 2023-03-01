/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.options;

import java.io.PrintWriter;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

abstract class BaseCommand<T> implements Callable<T> {

	@Spec
	CommandSpec commandSpec;

	@Mixin
	OutputOptionsMixin outputOptions;

	@SuppressWarnings("unused")
	@Option(names = { "-h", "--help" }, usageHelp = true, description = "Display help information.")
	private boolean helpRequested;

	static CommandLine initialize(CommandLine commandLine) {
		CommandLine.IParameterExceptionHandler defaultParameterExceptionHandler = commandLine.getParameterExceptionHandler();
		return commandLine //
				.setParameterExceptionHandler((ex, args) -> {
					defaultParameterExceptionHandler.handleParseException(ex, args);
					return CommandResult.FAILURE;
				}) //
				.setExecutionExceptionHandler((ex, cmd, __) -> {
					commandLine.getErr().println(cmd.getColorScheme().richStackTraceString(ex));
					commandLine.getErr().println();
					commandLine.getErr().flush();
					cmd.usage(commandLine.getOut());
					return CommandResult.FAILURE;
				}) //
				.setCaseInsensitiveEnumValuesAllowed(true) //
				.setAtFileCommentChar(null);
	}

	@Override
	public final T call() {
		PrintWriter out = getOut();
		if (!outputOptions.isDisableBanner()) {
			displayBanner(out);
		}
		return execute(out);
	}

	private PrintWriter getOut() {
		return commandSpec.commandLine().getOut();
	}

	private void displayBanner(PrintWriter out) {
		out.println();
		CommandLine.Help.ColorScheme colorScheme = commandSpec.commandLine().getColorScheme();
		if (colorScheme.ansi().enabled()) {
			out.print("💚 ");
		}
		out.println(colorScheme.text(
			"@|italic Thanks for using JUnit!|@ Support its development at @|underline https://junit.org/sponsoring|@"));
		out.println();
		out.flush();
	}

	protected abstract T execute(PrintWriter out);
}