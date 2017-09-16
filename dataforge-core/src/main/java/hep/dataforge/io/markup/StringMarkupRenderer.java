package hep.dataforge.io.markup;

/**
 * A renderer that renders to string
 */
public class StringMarkupRenderer extends StreamMarkupRenderer {
    private StringBuilder builder = new StringBuilder();


    @Override
    protected synchronized void printText(String string) {
        builder.append(string);
    }

    @Override
    protected synchronized void ln() {
        builder.append("\n");
    }

    @Override
    public String toString() {
        return builder.toString();
    }

    public synchronized void reset(){
        this.builder = new StringBuilder();
    }
}
