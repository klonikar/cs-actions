package io.cloudslang.content.jclouds.actions.snapshots;

import com.hp.oo.sdk.content.annotations.Action;
import com.hp.oo.sdk.content.annotations.Output;
import com.hp.oo.sdk.content.annotations.Param;
import com.hp.oo.sdk.content.annotations.Response;
import com.hp.oo.sdk.content.plugin.ActionMetadata.MatchType;
import com.hp.oo.sdk.content.plugin.ActionMetadata.ResponseType;
import io.cloudslang.content.jclouds.entities.constants.Inputs;
import io.cloudslang.content.jclouds.entities.constants.Outputs;
import io.cloudslang.content.jclouds.entities.inputs.CommonInputs;
import io.cloudslang.content.jclouds.entities.inputs.CustomInputs;
import io.cloudslang.content.jclouds.entities.inputs.VolumeInputs;
import io.cloudslang.content.jclouds.execute.snapshots.DeleteSnapshotInRegionExecutor;
import io.cloudslang.content.jclouds.utils.ExceptionProcessor;

import java.util.Map;

/**
 * Created by Mihai Tusa.
 * 6/29/2016.
 */
public class DeleteSnapshotInRegionAction {
    /**
     * Deletes the specified snapshot.
     * <p>
     * Note: When you make periodic snapshots of a volume, the snapshots are incremental, and only the blocks on the device
     * that have changed since your last snapshot are saved in the new snapshot. When you delete a snapshot, only the data
     * not needed for any other snapshot is removed. So regardless of which prior snapshots have been deleted, all active
     * snapshots will have access to all the information needed to restore the volume. You cannot delete a snapshot of the
     * root device of an EBS volume used by a registered AMI. You must first de-register the AMI before you can delete the
     * snapshot. For more information, see Deleting an Amazon EBS Snapshot in the Amazon Elastic Compute Cloud User Guide.
     *
     * @param provider   Cloud provider on which you have the snapshot - Valid values: "amazon" or "openstack".
     * @param endpoint   Endpoint to which request will be sent. Ex: "https://ec2.amazonaws.com" for Amazon AWS or
     *                   "http://hostOrIp:5000/v2.0" for OpenStack.
     * @param identity   Optional - username of your account or the Access Key ID. For OpenStack provider the required
     *                   format is 'alias:username'.
     * @param credential Optional - password of the user or the Secret Access Key that correspond to the identity input.
     * @param proxyHost  Optional - proxy server used to access the web site. If empty no proxy will be used.
     * @param proxyPort  Optional - proxy server port.
     * @param debugMode  Optional - if "true" then the execution logs will be shown in CLI console.
     * @param region     Optional - region where snapshot, to be deleted, belongs. Ex: "RegionOne", "us-east-1".
     *                   ListRegionAction can be used in order to get all regions - Default: "us-east-1"
     * @param snapshotId ID of the EBS snapshot to be deleted.
     * @return A map with strings as keys and strings as values that contains: outcome of the action, returnCode of the
     * operation, or failure message and the exception if there is one
     */
    @Action(name = "Delete Snapshot in Region",
            outputs = {
                    @Output(Outputs.RETURN_CODE),
                    @Output(Outputs.RETURN_RESULT),
                    @Output(Outputs.EXCEPTION)
            },
            responses = {
                    @Response(text = Outputs.SUCCESS, field = Outputs.RETURN_CODE, value = Outputs.SUCCESS_RETURN_CODE,
                            matchType = MatchType.COMPARE_EQUAL, responseType = ResponseType.RESOLVED),
                    @Response(text = Outputs.FAILURE, field = Outputs.RETURN_CODE, value = Outputs.FAILURE_RETURN_CODE,
                            matchType = MatchType.COMPARE_EQUAL, responseType = ResponseType.ERROR)
            }
    )
    public Map<String, String> execute(@Param(value = Inputs.CommonInputs.PROVIDER, required = true) String provider,
                                       @Param(value = Inputs.CommonInputs.ENDPOINT, required = true) String endpoint,
                                       @Param(value = Inputs.CommonInputs.IDENTITY) String identity,
                                       @Param(value = Inputs.CommonInputs.CREDENTIAL, encrypted = true) String credential,
                                       @Param(value = Inputs.CommonInputs.PROXY_HOST) String proxyHost,
                                       @Param(value = Inputs.CommonInputs.PROXY_PORT) String proxyPort,
                                       @Param(value = Inputs.CommonInputs.DEBUG_MODE) String debugMode,

                                       @Param(value = Inputs.CustomInputs.REGION) String region,
                                       @Param(value = Inputs.VolumeInputs.SNAPSHOT_ID, required = true) String snapshotId)
            throws Exception {

        CommonInputs inputs = new CommonInputs.CommonInputsBuilder()
                .withProvider(provider)
                .withEndpoint(endpoint)
                .withIdentity(identity)
                .withCredential(credential)
                .withProxyHost(proxyHost)
                .withProxyPort(proxyPort)
                .withDebugMode(debugMode)
                .build();

        CustomInputs customInputs = new CustomInputs.CustomInputsBuilder()
                .withRegion(region)
                .build();

        VolumeInputs volumeInputs = new VolumeInputs.VolumeInputsBuilder()
                .withCustomInputs(customInputs)
                .withSnapshotId(snapshotId)
                .build();

        try {
            return new DeleteSnapshotInRegionExecutor().execute(inputs, volumeInputs);
        } catch (Exception e) {
            return ExceptionProcessor.getExceptionResult(e);
        }
    }
}