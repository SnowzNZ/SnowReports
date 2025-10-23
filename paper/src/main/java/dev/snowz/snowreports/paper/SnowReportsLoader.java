package dev.snowz.snowreports.paper;

import dev.snowz.snowreports.common.library.Library;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jspecify.annotations.NonNull;

import java.util.List;

public final class SnowReportsLoader implements PluginLoader {

    @Override
    public void classloader(@NonNull final PluginClasspathBuilder pluginClasspathBuilder) {
        loadLibraries(pluginClasspathBuilder);
        loadInvUi(pluginClasspathBuilder);
    }

    private void loadLibraries(final PluginClasspathBuilder pluginClasspathBuilder) {
        final MavenLibraryResolver resolver = new MavenLibraryResolver();

        resolver.addRepository(new RemoteRepository.Builder(
            "central",
            "default",
            "https://maven-central.storage-download.googleapis.com/maven2"
        ).build());

        List.of(Library.values()).forEach(lib ->
            resolver.addDependency(
                new Dependency(
                    new DefaultArtifact(lib.getMavenDependency()),
                    "runtime"
                ))
        );

        pluginClasspathBuilder.addLibrary(resolver);
    }

    private void loadInvUi(final PluginClasspathBuilder pluginClasspathBuilder) {
        final MavenLibraryResolver resolver = new MavenLibraryResolver();

        resolver.addRepository(new RemoteRepository.Builder(
            "xenondevs",
            "default",
            "https://repo.xenondevs.xyz/releases/"
        ).build());

        resolver.addDependency(
            new Dependency(
                new DefaultArtifact("xyz.xenondevs.invui:invui:pom:1.47"),
                null
            )
        );

        pluginClasspathBuilder.addLibrary(resolver);
    }
}
