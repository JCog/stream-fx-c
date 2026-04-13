package dev.jcog.streamfxc.util;

import dev.jcog.streamfxc.misc.Controller;
import javafx.scene.media.AudioClip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class AudioFile {
    private static final Logger log = LoggerFactory.getLogger(AudioFile.class);
    private static final String[] formats = {"wav"};

    private final AudioClip clip;
    private final long duration; // milliseconds

    public AudioFile(String path) {
        File file = new File(path);
        AudioFormat format;
        try {
            format = AudioSystem.getAudioInputStream(file).getFormat();
        } catch (UnsupportedAudioFileException | IOException e) {
            log.error(e.getMessage());
            duration = 0;
            clip = null;
            return;
        }
        duration = (long) (file.length() / (format.getFrameSize() * format.getFrameRate()) * 1000);
        clip = new AudioClip(file.toURI().toString());
    }

    public AlertFuture playClip() {
        AlertFuture future = new AlertFuture();
        Controller.getScheduler().schedule(future::complete, duration, TimeUnit.MILLISECONDS);
        clip.play();
        return future;
    }

    // ranges from 0 to 1
    public void setVolume(double volume) {
        clip.setVolume(volume);
    }

    public static List<AudioFile> getAudioFilesInDir(String dir) {
        List<AudioFile> audioFiles = new ArrayList<>();
        List<Path> pathList;
        try (Stream<Path> paths = Files.walk(Paths.get(dir))) {
            pathList = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> Arrays.stream(formats).anyMatch(format -> path.toString().endsWith(format)))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            log.error("Error loading audio files in \"{}\" directory: {}", dir, e.getMessage());
            return audioFiles;
        }
        for (Path path : pathList) {
            AudioFile clip = new AudioFile(path.toString());
            audioFiles.add(clip);
        }
        log.info("Loaded {} audio files from \"{}\"", audioFiles.size(), dir);
        return audioFiles;
    }
}
