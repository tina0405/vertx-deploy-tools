package nl.jpoint.vertx.deploy.agent.service;

import io.vertx.core.Vertx;
import nl.jpoint.vertx.deploy.agent.DeployConfig;
import nl.jpoint.vertx.deploy.agent.command.DownloadHttpArtifact;
import nl.jpoint.vertx.deploy.agent.command.ExtractArtifact;
import nl.jpoint.vertx.deploy.agent.command.ResolveSnapshotVersion;
import nl.jpoint.vertx.deploy.agent.request.ModuleRequest;
import nl.jpoint.vertx.deploy.agent.util.ArtifactContextUtil;
import rx.Observable;

import static rx.Observable.just;

interface DeployService<T extends ModuleRequest, R> {

    Observable<R> deployAsync(T deployRequest);

    DeployConfig getConfig();

    Vertx getVertx();

    String getLogType();

    default Observable<T> resolveSnapShotVersion(T moduleRequest) {
        if (moduleRequest.isSnapshot() && getConfig().isMavenRemote()) {
            ResolveSnapshotVersion<T> resolveVersion = new ResolveSnapshotVersion<>(getConfig(), getVertx());
            return resolveVersion.executeAsync(moduleRequest);
        } else {
            return just(moduleRequest);
        }
    }

    default Observable<T> downloadArtifact(T moduleRequest) {
        DownloadHttpArtifact<T> downloadArtifact = new DownloadHttpArtifact<>(getConfig(), getLogType());
        return downloadArtifact.executeAsync(moduleRequest);
    }


    default Observable<T> parseArtifactContext(T moduleRequest) {
        ArtifactContextUtil artifactContextUtil = new ArtifactContextUtil(moduleRequest, getConfig().getArtifactRepo().resolve(moduleRequest.getFileName()));
        moduleRequest.setRestartCommand(artifactContextUtil.getRestartCommand());
        moduleRequest.setTestCommand(artifactContextUtil.getTestCommand());
        moduleRequest.setBaseLocation(artifactContextUtil.getBaseLocation());
        return just(moduleRequest);
    }

    default Observable<T> extractArtifact(T moduleRequest) {
        ExtractArtifact<T> extractConfig = new ExtractArtifact<>(getVertx(), getConfig(), moduleRequest.getBaseLocation());
        return extractConfig.executeAsync(moduleRequest);
    }


}
