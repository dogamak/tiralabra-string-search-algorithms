package tiralabra.app.cli;

import tiralabra.utils.ArrayList;
import tiralabra.utils.HashMap;

import java.util.function.Function;

/**
 * Command-line argument parser.
 *
 * <h4>Supports:</h4>
 * <ul>
 *   <li>Positional arguments</li>
 *   <li>Toggle flags</li>
 *   <li>Value-taking flags</li>
 *   <li>Shorthand versions of flags</li>
 * </ul>
 */
public class ArgumentParser {
    /**
     * Exception thrown when the parser encounters an undefined flag name.
     */
    public static class UnknownArgumentException extends Exception {
        UnknownArgumentException(String argument) {
            super(String.format("Unknown argument %s provided", argument));
        }
    }

    /**
     * Exception thrown when an invalid combination of arguments has been provided.
     */
    public static class InvalidUsageException extends Exception {
        InvalidUsageException(String message) {
            super(message);
        }
    }

    /**
     * Functional interface for flag handing callbacks.
     */
    public interface FlagHandler {
        /**
         * Executed when a particular flag has been parsed.
         *
         * @param flag - Information about the flag.
         * @param value - Value provided for the flag or {@code null} if no value was given.
         */
        void handle(FlagOptions flag, String value);
    }

    /**
     * Functional interface for positional argument handling callbacks.
     */
    public interface PositionalArgumentHandler {
        /**
         * Executed when a positional argument has been parsed.
         *
         * @param index - Number of the positional argument counting from the start of the command line.
         * @param value - Value of the argument.
         *
         * @return Whether this handler consumed the argument. If {@code true} is returned, no further
         *         callbacks are executed for this argument.
         */
        boolean handle(int index, String value);
    }

    /**
     * Collection of options that define a command-line flag.
     */
    public static class FlagOptions {
        /**
         * Long-form name of the flag without the leading dashes, e.g. {@code --pattern} or {@code --knuth-morris-pratt}.
         */
        private String longName;

        /**
         * Shorthand name for the flag without the leading dash, usually a single character, e.g. {@code -p} or {@code -i}.
         */
        private String shortName;

        /**
         * Whether or not this flag takes a value.
         */
        private boolean takesValue;

        /**
         * Callback which is called for each instance of this flag.
         */
        private FlagHandler handler;

        /**
         * Constructs a flag from it's long name and a handler.
         *
         * Be default, the created flag has no short name and does not take a value.
         *
         * @param longName - Long-form name of the flag.
         * @param handler - Handler callback for the flag.
         */
        FlagOptions(String longName, FlagHandler handler) {
            this.longName = longName;
            this.handler = handler;
        }

        /**
         * Get the long-form name of the flag.
         *
         * @return Long-form name of the flag.
         */
        public String getLongName() {
            return longName;
        }
    }

    /**
     * Map from flag names (both long- and shorthand-form) to {@link FlagOptions}.
     */
    private HashMap<String, FlagOptions> flag_handlers = new HashMap<>();

    /**
     * List of {@link PositionalArgumentHandler PositionalArgumentHandlers}.
     */
    private ArrayList<PositionalArgumentHandler> positional_handlers = new ArrayList<>();

    /**
     * Array of command-line arguments we are currently parsing.
     */
    private String[] arguments;

    /**
     * Index of the command-line argument up to which arguments
     * have been parsed.
     */
    private int current_argument = 0;

    /**
     * Whether the parser expects named flags at this point.
     */
    private boolean flags_expected = true;

    /**
     * Counter for determining the number of a positional argument.
     */
    private int positional_argument_counter = 0;

    /**
     * Register a handler for positional arguments, which is called for every positional argument
     * not handled by any other handler.
     *
     * @param handlePositionalArgument - Functional handler callback
     */
    public void addPositionalArgumentHandler(PositionalArgumentHandler handlePositionalArgument) {
        positional_handlers.add(handlePositionalArgument);
    }

    /**
     * Register a handler for a named flag which does not take a value.
     *
     * @param longName - Long-form name of the flag.
     * @param handler - Functional handler callback.
     */
    public void addFlagHandler(String longName, FlagHandler handler) {
        flag_handlers.insert(longName, new FlagOptions(longName, handler));
    }

    /**
     * Register a handler for a named flag which does not take a value.
     *
     * @param shortName - Shorthand name of the flag.
     * @param longName - Long-form name of the flag.
     * @param handler - Functional handler callback.
     */
    public  void addFlagHandler(String shortName, String longName, FlagHandler handler) {
        FlagOptions options = new FlagOptions(longName, handler);
        options.shortName = shortName;
        flag_handlers.insert(shortName, options);
        flag_handlers.insert(longName, options);
    }

    /**
     * Register a handler for a named flag which does not take a value.
     *
     * @param longName - Long-form name of the flag.
     * @param handler - Functional handler callback.
     */
    public  void addFlagHandlerValue(String longName, FlagHandler handler) {
        FlagOptions options = new FlagOptions(longName, handler);
        options.takesValue = true;
        flag_handlers.insert(longName, options);
    }

    /**
     * Register a handler for a named flag which does take a value.
     *
     * @param shortName - Shorthand name of the flag.
     * @param longName - Long-form name of the flag.
     * @param handler - Functional handler callback.
     */
    public  void addFlagHandlerValue(String shortName, String longName, FlagHandler handler) {
        FlagOptions options = new FlagOptions(longName, handler);
        options.shortName = shortName;
        options.takesValue = true;
        flag_handlers.insert(longName, options);
        flag_handlers.insert(shortName, options);
    }

    /**
     * Gets the argument currently under parsing.
     *
     * @return A raw command-line argument.
     */
    private String getCurrentArgument() {
        if (arguments == null)
            return null;

        if (current_argument >= arguments.length)
            return null;

        return arguments[current_argument];
    }

    /**
     * Marks the current argument as parsed, moves to the next argument and returns it.
     *
     * @return A raw command-line argument.
     */
    private String nextArgument() {
        current_argument++;
        return getCurrentArgument();
    }

    /**
     * Tries to parse an argument as a named flag.
     *
     * @return {@code true} if the argument was successfully parsed, {@code false} if the argument is not a named flag.
     * @throws UnknownArgumentException when the argument contains an undefined named flag
     * @throws InvalidUsageException when value is not provided for an flag expecting a value
     */
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

    /**
     * Tries to handle an argument as a positional argument.
     *
     * Calls each of the handlers in {@link #positional_handlers} until one of them returns {@code true}.
     *
     * @return Whether or not a handler accepting this argument was found.
     */
    public boolean handlePositionalArgument() {
        for (int i = 0; i < positional_handlers.size(); i++) {
            if (positional_handlers.get(i).handle(positional_argument_counter, getCurrentArgument())) {
                positional_argument_counter++;
                return true;
            }
        }

        return false;
    }

    /**
     * Parses the given command-line arguments.
     *
     * @param args - Command-line arguments.
     * @throws UnknownArgumentException if an undefined named flag is provided
     * @throws InvalidUsageException  if no value is provided for a flag which requires a value
     */
    public void parse(String[] args) throws InvalidUsageException, UnknownArgumentException {
        arguments = args;
        current_argument = 0;
        positional_argument_counter = 0;
        flags_expected = true;

        while (getCurrentArgument() != null) {
            boolean handled = handleFlag();

            if (!handled)
                handled = handlePositionalArgument();

            if (!handled)
                throw new InvalidUsageException(String.format("Unexpected argument '%s'", getCurrentArgument()));

            nextArgument();
        }
    }
}
