package com.inwza.e2e.support;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MicroserviceProcessLauncher {

    private static final String VERSION = "0.1.0-SNAPSHOT";

    private final Path ecosystemRoot;
    private final List<Process> processes = new ArrayList<>();
    private final List<Path> logFiles = new ArrayList<>();

    public MicroserviceProcessLauncher(Path ecosystemRoot) {
        this.ecosystemRoot = ecosystemRoot;
    }

    public void startService(
            String moduleName,
            int httpPort,
            int grpcPort,
            Map<String, String> extraEnv)
            throws IOException {
        Path jar = ecosystemRoot.resolve(moduleName + "/target/" + moduleName + "-" + VERSION + "-boot.jar");
        if (!Files.isRegularFile(jar)) {
            throw new IllegalStateException("Missing boot jar: " + jar);
        }

        Path logFile = Files.createTempFile(moduleName + "-", ".log");
        logFiles.add(logFile);

        Map<String, String> env = new HashMap<>(System.getenv());
        env.put("SERVER_PORT", String.valueOf(httpPort));
        if (grpcPort > 0) {
            env.put("GRPC_SERVER_PORT", String.valueOf(grpcPort));
        }
        env.put("SPRING_PROFILES_ACTIVE", "local");
        env.putAll(extraEnv);

        ProcessBuilder builder = new ProcessBuilder("java", "-jar", jar.toAbsolutePath().toString());
        builder.environment().putAll(env);
        builder.redirectOutput(logFile.toFile());
        builder.redirectError(ProcessBuilder.Redirect.appendTo(logFile.toFile()));

        Process process = builder.start();
        processes.add(process);
        System.out.printf("Started %s pid=%d log=%s%n", moduleName, process.pid(), logFile);
    }

    public void destroyAll() {
        for (Process process : processes) {
            if (process.isAlive()) {
                process.destroy();
                try {
                    if (!process.waitFor(15, TimeUnit.SECONDS)) {
                        process.destroyForcibly();
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    process.destroyForcibly();
                }
            }
        }
        processes.clear();
    }

    public List<Path> logFiles() {
        return List.copyOf(logFiles);
    }
}
