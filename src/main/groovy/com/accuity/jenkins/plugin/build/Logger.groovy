package com.accuity.jenkins.plugin.build

class Logger {

    PrintStream jenkinsLogger
    String prefix

    Logger(PrintStream jenkinsLogger, String prefix) {
        this.jenkinsLogger = jenkinsLogger
        this.prefix = prefix
    }

    def println(String message) {
        jenkinsLogger.println("[${prefix}] ${message}")
    }
}
