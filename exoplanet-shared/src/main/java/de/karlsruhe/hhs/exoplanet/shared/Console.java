package de.karlsruhe.hhs.exoplanet.shared;

import java.io.IOException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.TerminalBuilder;


/**
 * @author Yannic Rieger
 */
public class Console {

    public LineReader getReader() {
        return this.reader;
    }

    private LineReader reader;

    public Console() {
        try {
            this.reader = LineReaderBuilder.builder().terminal(TerminalBuilder.builder().build()).build();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void println(final Object msg) {
        // Workaround to keep prompt (see https://github.com/jline/jline3/issues/75)
        this.reader.callWidget(LineReader.CLEAR);
        this.reader.getTerminal().writer().println(msg);
        this.reader.callWidget(LineReader.REDRAW_LINE);
        this.reader.callWidget(LineReader.REDISPLAY);
        this.reader.getTerminal().writer().flush();
    }
}
