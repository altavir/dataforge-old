package hep.dataforge.storage.servlet;

import hep.dataforge.exceptions.StorageException;
import hep.dataforge.storage.api.Loader;
import hep.dataforge.storage.api.Storage;

/**
 * Created by darksnake on 13-Oct-16.
 */
public class StorageRenderer {

    public static void renderStorage(StringBuilder b, Storage storage) throws StorageException {
        b.append("<div class=\"node\">\n");
        if (!storage.loaders().isEmpty()) {
            b.append("<div class=\"leaf\">\n"
                    + "<ul>");
            storage.loaders().values().stream()
                    .sorted((o1, o2) -> o1.getName().compareTo(o2.getName()))
                    .forEach(loader -> renderLoaderEntry(b, loader));

            b.append("</ul>"
                    + "</div>\n");
        }
        if (!storage.shelves().isEmpty()) {
            b.append("<ul>\n");
            storage.shelves().values().stream()
                    .sorted((o1, o2) -> o1.getName().compareTo(o2.getName()))
                    .forEach(shelf -> {
                        b.append(String.format("<li><strong>+ %s</strong></li>%n", shelf.getName()));
                        try {
                            renderStorage(b, shelf);
                        } catch (StorageException e) {
                            b.append("Error loading storage: " + e.getMessage());
                        }
                    });
            b.append("</ul>");
        }
        b.append("</div>\n");
    }

    public static void renderLoaderEntry(StringBuilder b, Loader loader) {
        String href = "/storage?path=" + loader.getPath();
        b.append(String.format("<li><a href=\"%s\">%s</a> (%s)</li>", href, loader.getName(), loader.getType()));
    }
}
