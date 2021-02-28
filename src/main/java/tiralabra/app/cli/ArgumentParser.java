package tiralabra.app.cli;

import tiralabra.utils.ArrayList;
import tiralabra.utils.HashMap;

import java.util.function.Function;

public class ArgumentParser {
    public static class UnknownArgumentException extends Exception {
        UnknownArgumentException(String argument) {
            super(String.format("Unknown argument %s provided", argument));
        }
    }

    public static class InvalidUsageException extends Exception {
        InvalidUsageException(String message) {
            super(message);
        }
    }

    public interface FlagHandler {
        void handle(FlagOptions flag, String value);
    }

    public interface PositionalArgumentHandler {
        boolean handle(int index, String value);
    }

    public static class FlagOptions {
        private String longName;
        private String shortName;
        private boolean takesValue;
        private FlagHandler handler;

        FlagOptions(String longName, FlagHandler handler) {
            this.longName = longName;
            this.handler = handler;
        }

        public String getLongName() {
            return longName;
        }
    }

    private HashMap<String, FlagOptions> flag_handlers = new HashMap<>();
    private ArrayList<PositionalArgumentHandler> positional_handlers = new ArrayList<>();

    public void addPositionalArgumentHandler(PositionalArgumentHandler handlePositionalArgument) {
        positional_handlers.add(handlePositionalArgument);
    }

    public void addFlagHandler(String longName, FlagHandler handler) {
        flag_handlers.insert(longName, new FlagOptions(longName, handler));
    }

    public  void addFlagHandler(String shortName, String longName, FlagHandler handler) {
        FlagOptions options = new FlagOptions(longName, handler);
        options.shortName = shortName;
        flag_handlers.insert(shortName, options);
        flag_handlers.insert(longName, options);
    }

    public  void addFlagHandlerValue(String longName, FlagHandler handler) {
        FlagOptions options = new FlagOptions(longName, handler);
        options.takesValue = true;
        flag_handlers.insert(longName, options);
    }

    public  void addFlagHandlerValue(String shortName, String longName, FlagHandler handler) {
        FlagOptions options = new FlagOptions(longName, handler);
        options.shortName = shortName;
        options.takesValue = true;
        flag_handlers.insert(longName, options);
        flag_handlers.insert(shortName, options);
    }

    private String[] arguments;
    private int current_argument = 0;
    private boolean flags_expected = true;
    private int positional_argument_counter = 0;

    private String getCurrentArgument() {
        if (arguments == null)
            return null;

        if (current_argument >= arguments.length)
            return null;

        return arguments[current_argument];
    }

    private String nextArgument() {
        current_argument++;
        return getCurrentArgument();
    }

    private boolean handleFlag() throws UnknownArgumentException, InvalidUsageException {
        if (!flags_expected)
            return false;

        String arg = getCurrentArgument();
        String flag = null;
        String value = null;

        if (arg.startsWith("--")) {
            flag = arg.substring(2);
        } else if (arg.startsWith("-")) {
            flag = arg.substring(1);
        }

        if (flag == null)
            return false;

        FlagOptions options = flag_handlers.get(flag);

        if (options == null) {
            int separator_index = flag.indexOf('=');

            if (separator_index < 0) {
                throw new UnknownArgumentException(flag);
            }

            value = flag.substring(separator_index+1);
            flag = flag.substring(0, separator_index);

            options = flag_handlers.get(flag);

            if (options == null) {
                throw new UnknownArgumentException(flag);
            }
        }

        if (options.takesValue && value == null) {
            value = nextArgument();

            if (value == null) {
                throw new InvalidUsageException(String.format("Flag %s expects a value", options.toString()));
            }
        }

        options.handler.handle(options, value);

        return true;
    }

    public boolean handlePositionalArgument() {
        for (int i = 0; i < positional_handlers.size(); i++) {
            if (positional_handlers.get(i).handle(positional_argument_counter, getCurrentArgument())) {
                positional_argument_counter++;
                return true;
            }
        }

        return false;
    }

    public void parse(String[] args) throws InvalidUsageException, UnknownArgumentException {
        arguments = args;
        current_argument = 0;
        positional_argument_counter = 0;
        flags_expected = true;

        while (getCurrentArgument() != null) {
            boolean handled = false;

            if (!handled)
                handled = handleFlag();

            if (!handled)
                handled = handlePositionalArgument();

            if (!handled)
                throw new InvalidUsageException(String.format("Unexpected argument '%s'", getCurrentArgument()));

            nextArgument();
        }
    }
}
