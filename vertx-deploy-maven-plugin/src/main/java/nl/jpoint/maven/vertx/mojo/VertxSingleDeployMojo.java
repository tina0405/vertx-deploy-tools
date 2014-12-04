package nl.jpoint.maven.vertx.mojo;

import nl.jpoint.maven.vertx.request.DeployRequest;
import nl.jpoint.maven.vertx.request.Request;
import nl.jpoint.maven.vertx.utils.DeployUtils;
import nl.jpoint.maven.vertx.utils.RequestExecutor;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

@Mojo(name = "single-deploy")
public class VertxSingleDeployMojo extends AbstractDeployMojo {

    @Parameter(property = "deploy.remoteIp")
    private String remoteIp;
    @Parameter(property = "deploy.stubbed", defaultValue = "true")
    private Boolean stubbed;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        DeployConfiguration configuration = new DeployConfiguration();
        configuration.getHosts().add(remoteIp);
        configuration.setTestScope(stubbed);

        super.activeConfiguration = configuration;
        final DeployUtils utils = new DeployUtils(getLog(), project);
        final RequestExecutor executor = new RequestExecutor(getLog());

        final List<Request> deployModuleRequests = utils.createDeployModuleList(activeConfiguration, MODULE_CLASSIFIER, activeConfiguration.doRestart());
        final List<Request> deployArtifactRequests = utils.createDeploySiteList(activeConfiguration, SITE_CLASSIFIER);

        DeployRequest deployRequest = new DeployRequest(deployModuleRequests, deployArtifactRequests, false, true);

        getLog().info("Executing deploy request, waiting for Vert.x to respond.... (this might take some time)");

        executor.executeDeployRequests(activeConfiguration, deployRequest, settings);

    }
}
