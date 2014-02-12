package nl.jpoint.maven.vertx.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties({"endpoint"})
public class DeployModuleRequest extends Request {

    private static final String ENDPOINT = "/deploy/module";

    private final int instances;
    private final boolean restart;

    public DeployModuleRequest(String group_id, String artifact_id, String version, int instances, boolean restart) {
        super(group_id, artifact_id, version);
        this.instances = instances;
        this.restart = restart;
    }

    public int getInstances() {
        return instances;
    }

    @Override
    public String getEndpoint() {
        return ENDPOINT;
    }

    public boolean doRestart() { return restart; }
}
