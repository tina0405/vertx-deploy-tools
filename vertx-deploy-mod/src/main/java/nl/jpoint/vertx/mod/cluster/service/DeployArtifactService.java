package nl.jpoint.vertx.mod.cluster.service;

import nl.jpoint.vertx.mod.cluster.command.*;
import nl.jpoint.vertx.mod.cluster.request.DeployArtifactRequest;
import nl.jpoint.vertx.mod.cluster.request.ModuleRequest;
import nl.jpoint.vertx.mod.cluster.util.ArtifactContextUtil;
import nl.jpoint.vertx.mod.cluster.util.LogConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;

import java.nio.file.Paths;

public class DeployArtifactService implements DeployService<DeployArtifactRequest> {
    private static final Logger LOG = LoggerFactory.getLogger(DeployArtifactService.class);

    private final Vertx vertx;
    private final JsonObject config;
    private final ConcurrentSharedMap<String, String> installedArtifacts;

    public DeployArtifactService(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.config = config;
        this.installedArtifacts = vertx.sharedData().getMap("installedArtifacts");
    }

    @Override
    public boolean deploy(DeployArtifactRequest deployRequest) {

        if (deployRequest.isSnapshot()) {
            Command resolveVersion = new ResolveSnapshotVersion(config, LogConstants.DEPLOY_SITE_REQUEST);
            JsonObject result = resolveVersion.execute(deployRequest);

            if (result.getBoolean("success")) {
                deployRequest.setSnapshotVersion(result.getString("version"));
            }
        }

        if (installedArtifacts.containsKey(deployRequest.getGroupId() + ":" + deployRequest.getArtifactId())
                && installedArtifacts.get(deployRequest.getGroupId() + ":" + deployRequest.getArtifactId()).equals(deployRequest.getSnapshotVersion())) {
            LOG.info("[{} - {}]: Same SNAPSHOT version ({}) of Artifact {} already installed.", LogConstants.DEPLOY_SITE_REQUEST, deployRequest.getId(), deployRequest.getSnapshotVersion(), deployRequest.getModuleId());
            return true;
        }

        DownloadArtifact downloadArtifactCommand = new DownloadArtifact(config);
        JsonObject downloadResult = downloadArtifactCommand.execute(deployRequest);

        if (!downloadResult.getBoolean("success")) {
            return false;
        }
        ArtifactContextUtil artifactContextUtil = new ArtifactContextUtil(config.getString("artifact.repo") + "/" + deployRequest.getFileName());

        ExtractArtifact extractSite = new ExtractArtifact(vertx, config, Paths.get(artifactContextUtil.getBaseLocation()), true);
        JsonObject extractResult = extractSite.execute(deployRequest);

        if (deployRequest.getSnapshotVersion() != null) {
            installedArtifacts.put(deployRequest.getGroupId() + ":" + deployRequest.getArtifactId(), deployRequest.getSnapshotVersion());
        }
        return extractResult.getBoolean("success");
    }
}
