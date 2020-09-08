package src.components;

import javax.mail.MessagingException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

/**
 * Class to keep a watch on the given directory for new addition of files.
 * It registers the given directory and sub-directories to keep a watch on it.
 * Whenever a new directory is added it is also registered for the watch
 * When a new file is created, file is processed by calling processFile() 
 * of FilReader Service class.
 * Once an event is completed the watch waits again till the occurrence of next event.
 */

public class WatchDirectoryService {

    private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
    private final boolean recursive;
    private boolean trace = false;
    
    private static long startTime;

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    /**
     * Creates a WatchService and registers the given directory and all sub-directories if present
     */
    WatchDirectoryService(Path directoryPath, boolean recursive) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey,Path>();
        this.recursive = recursive;

        if (recursive) {
            System.out.format("Scanning %s ...\n", directoryPath);
            registerAll(directoryPath);
            System.out.println("Done.");
        } else {
            register(directoryPath);
        }

        // enable trace after initial registration
        this.trace = true;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path directoryPath) throws IOException {
        WatchKey key = directoryPath.register(watcher, ENTRY_CREATE);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", directoryPath);
            } else {
                if (!directoryPath.equals(prev)) {
                    System.out.format("update: %s -> %s\n", prev, directoryPath);
                }
            }
        }
        keys.put(key, directoryPath);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {

        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path directoryPath, BasicFileAttributes attrs)
                throws IOException
            {
            	System.out.println("Registering " + directoryPath);
                register(directoryPath);
                return FileVisitResult.CONTINUE;
            }
        });
        
    }

    /**
     * Process all events for keys queued to the watcher
     * on EVENT_CREATE event check if the new file added is a directory or a file
     * For new directory - register it and register all the sub-directories
     * For new file - call the FileReaderService to process the file
     * @throws NoSuchAlgorithmException 
     * @throws MessagingException 
     */
    synchronized void processEvents() throws NoSuchAlgorithmException, MessagingException {
        for (;;) {

            // wait for key to be signaled
            WatchKey key;
            try {
                key = watcher.take();
                startTime = System.nanoTime();
            } 
            catch (InterruptedException x) {
                return;
            }

            Path directory = keys.get(key);
            if (directory == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                
				@SuppressWarnings("rawtypes")
				WatchEvent.Kind kind = event.kind();

                //If event is OVERFLOW, do nothing
                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = directory.resolve(name);

                // print out event
                System.out.format("%s: %s\n", event.kind().name(), child);

                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if (recursive && (kind == ENTRY_CREATE)) {
                    try {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                            registerAll(child);
                        }
                        else {
                                               
                        	new FileReaderService().processFile(child);
                        	
                        	System.out.println("Time taken to complete the event in secs: " + ((System.nanoTime() - startTime)/ 1000000000) + "\n");
                        }
                    } 
                    catch (IOException ex) {
                        System.out.println("Exception occured while handling ENTRY_CREATE event" + ex.toString());
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }
}