package io.cloudslang.content.jclouds.services.helpers;

import io.cloudslang.content.jclouds.entities.aws.AWSApiAction;
import io.cloudslang.content.jclouds.entities.aws.AuthorizationHeader;
import io.cloudslang.content.jclouds.entities.constants.Constants;
import io.cloudslang.content.jclouds.entities.inputs.AWSInputsWrapper;
import io.cloudslang.content.jclouds.services.AmazonSignatureService;
import io.cloudslang.content.jclouds.utils.InputsUtil;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mihai Tusa.
 * 8/12/2016.
 */
public class AWSApiNetworkServiceHelper {
    private static final String NETWORK_INTERFACE_ID = "NetworkInterfaceId";
    private static final String DEVICE_ID = "DeviceIndex";
    private static final String ATTACHMENT_ID = "AttachmentId";
    private static final String FORCE = "Force";
    private static final String SET_FLAG = "1";

    public Map<String, String> getApiQueryParamsMap(AWSInputsWrapper inputs, String actionName) {
        Map<String, String> queryParamsMap = new HashMap<>();
        if (StringUtils.isBlank(inputs.getHttpClientInputs().getQueryParams())) {
            if (AWSApiAction.ATTACH_NETWORK_INTERFACE.getValue().equalsIgnoreCase(actionName)) {
                queryParamsMap = new AWSApiNetworkServiceHelper().getAttachNetworkInterfaceQueryParamsMap(inputs);
            } else if (AWSApiAction.DETACH_NETWORK_INTERFACE.getValue().equalsIgnoreCase(actionName)) {
                queryParamsMap = new AWSApiNetworkServiceHelper().getDetachNetworkInterfaceQueryParamsMap(inputs);
            }

            String queryParamsString = queryParamsMap.isEmpty() ? Constants.Miscellaneous.EMPTY :
                    InputsUtil.getParamsString(queryParamsMap, Constants.Miscellaneous.EQUAL, Constants.Miscellaneous.AMPERSAND);

            inputs.getHttpClientInputs().setQueryParams(queryParamsString);
        } else {
            queryParamsMap = InputsUtil.getHeadersOrQueryParamsMap(new HashMap<String, String>(),
                    inputs.getHttpClientInputs().getQueryParams(), Constants.Miscellaneous.AMPERSAND,
                    Constants.Miscellaneous.EQUAL, false);
        }
        return queryParamsMap;
    }

    public Map<String, String> getNullOrHeadersMap(Map<String, String> headersMap, AWSInputsWrapper inputs) {
        if (headersMap == null || headersMap.isEmpty()) {
            headersMap = new HashMap<>();
        }
        return StringUtils.isBlank(inputs.getHttpClientInputs().getHeaders()) ? headersMap :
                InputsUtil.getHeadersOrQueryParamsMap(headersMap, inputs.getHttpClientInputs().getHeaders(),
                        Constants.AWSParams.HEADER_DELIMITER, Constants.Miscellaneous.COLON, true);
    }

    public void setQueryApiCallHeaders(AWSInputsWrapper inputs, Map<String, String> headersMap, Map<String, String> queryParamsMap)
            throws SignatureException, MalformedURLException {
        AuthorizationHeader signedHeaders = new AmazonSignatureService().signRequestHeaders(inputs, headersMap, queryParamsMap);

        inputs.getHttpClientInputs().setHeaders(signedHeaders.getAuthorizationHeader());
    }

    private Map<String, String> getAttachNetworkInterfaceQueryParamsMap(AWSInputsWrapper inputs) {
        Map<String, String> queryParamsMap = new HashMap<>();
        queryParamsMap.put(Constants.AWSParams.ACTION, AWSApiAction.ATTACH_NETWORK_INTERFACE.getValue());
        queryParamsMap.put(Constants.AWSParams.VERSION, inputs.getVersion());
        queryParamsMap.put(NETWORK_INTERFACE_ID, inputs.getNetworkInterfaceId());
        queryParamsMap.put(Constants.AWSParams.INSTANCE_ID, inputs.getCustomInputs().getInstanceId());
        queryParamsMap.put(DEVICE_ID, inputs.getDeviceIndex());

        return queryParamsMap;
    }

    private Map<String, String> getDetachNetworkInterfaceQueryParamsMap(AWSInputsWrapper inputs) {
        Map<String, String> queryParamsMap = new HashMap<>();
        queryParamsMap.put(Constants.AWSParams.ACTION, AWSApiAction.DETACH_NETWORK_INTERFACE.getValue());
        queryParamsMap.put(Constants.AWSParams.VERSION, inputs.getVersion());
        queryParamsMap.put(ATTACHMENT_ID, inputs.getAttachmentId());

        if (inputs.isForceDetach()) {
            queryParamsMap.put(FORCE, SET_FLAG);
        }

        return queryParamsMap;
    }
}