package examplemod.client.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.CopyOption;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;

import com.labymedia.ultralight.UltralightJava;
import com.labymedia.ultralight.UltralightLoadException;
import com.labymedia.ultralight.gpu.UltralightGPUDriverNativeUtil;

import examplemod.client.ExampleMod;
import net.minecraft.client.Minecraft;

public class ResourceManager {
    private ResourceManager(){}

    public static final File modDir = new File(Minecraft.getMinecraft().mcDataDir, ExampleMod.MODID);
    public static final File ultraLightDir = new File(modDir, "ultralight");
    public static final File binDir = new File(ultraLightDir, "bin/linux-x64");
    public static final File resourceDir = new File(ultraLightDir, "resources");

    public static void loadUltralight() throws URISyntaxException, UltralightLoadException, IOException {
        String rootPath = "/assets/" + ExampleMod.MODID;

        URI uri = ResourceManager.class.getResource(rootPath).toURI();
        try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap())) {


            copyFolder(fileSystem.getPath(rootPath + "/public"), new File(ultraLightDir, "public").toPath());
            copyFolder(fileSystem.getPath(rootPath + "/bin"), binDir.toPath());
            copyFolder(fileSystem.getPath(rootPath + "/resources"), resourceDir.toPath());

        } catch (IOException e) {
            e.printStackTrace();
        }

        //UltralightJava.extractNativeLibrary(binDir.toPath());
        //	UltralightGPUDriverNativeUtil.extractNativeLibrary(binDir.toPath());

        /*System.load(new File(binDir.toPath().toFile(), "UltralightCore.dll").toPath().toAbsolutePath().toString());
        System.load(new File(binDir.toPath().toFile(), "glib-2.0-0.dll").toPath().toAbsolutePath().toString());
        System.load(new File(binDir.toPath().toFile(), "gobject-2.0-0.dll").toPath().toAbsolutePath().toString());
        System.load(new File(binDir.toPath().toFile(), "gmodule-2.0-0.dll").toPath().toAbsolutePath().toString());
        System.load(new File(binDir.toPath().toFile(), "gio-2.0-0.dll").toPath().toAbsolutePath().toString());
        System.load(new File(binDir.toPath().toFile(), "gstreamer-full-1.0.dll").toPath().toAbsolutePath().toString());
        System.load(new File(binDir.toPath().toFile(), "WebCore.dll").toPath().toAbsolutePath().toString());
        System.load(new File(binDir.toPath().toFile(), "Ultralight.dll").toPath().toAbsolutePath().toString());
        System.load(new File(binDir.toPath().toFile(), "ultralight-java-gpu.dll").toPath().toAbsolutePath().toString());
        System.load(new File(binDir.toPath().toFile(), "AppCore.dll").toPath().toAbsolutePath().toString());
        System.load(new File(binDir.toPath().toFile(), "ultralight-java.dll").toPath().toAbsolutePath().toString());
        System.load(new File(binDir.toPath().toFile(), "libultralight-java.so").toPath().toAbsolutePath().toString());
        System.load(new File(binDir.toPath().toFile(), "libultralight-java-gpu.so").toPath().toAbsolutePath().toString());*/
        Path rootPath2 = Paths.get(binDir.toPath().toUri());

        try {
            Files.walk(rootPath2).forEach(path -> {
            	if (Files.isDirectory(path) == false) {
            		System.out.println(Files.isDirectory(path));
            		System.out.println(path);
            		System.load(path.toAbsolutePath().toString());
				}
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void copyFolder(Path source, Path target, CopyOption... options) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                Files.createDirectories(target.resolve(source.relativize(dir).toString()));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.copy(file, target.resolve(source.relativize(file).toString()), options);
                return FileVisitResult.CONTINUE;
            }
        });
    }


}