package de.samply.opal;

public class TaskNotReadyException extends OpalEngineException {
    public TaskNotReadyException() {
        super("Task not ready yet");
    }
}

