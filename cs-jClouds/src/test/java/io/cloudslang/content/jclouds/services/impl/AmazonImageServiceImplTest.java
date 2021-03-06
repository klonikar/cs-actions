package io.cloudslang.content.jclouds.services.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import io.cloudslang.content.jclouds.entities.inputs.CommonInputs;
import io.cloudslang.content.jclouds.entities.inputs.CustomInputs;
import io.cloudslang.content.jclouds.entities.inputs.ImageInputs;
import io.cloudslang.content.jclouds.utils.InputsUtil;
import org.jclouds.ContextBuilder;
import org.jclouds.ec2.EC2Api;
import org.jclouds.ec2.features.AMIApi;
import org.jclouds.ec2.features.InstanceApi;
import org.jclouds.ec2.options.CreateImageOptions;
import org.jclouds.ec2.options.DescribeImagesOptions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Properties;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anySetOf;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * Created by Mihai Tusa.
 * 5/19/2016.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({AmazonImageServiceImpl.class, ContextBuilder.class})
public class AmazonImageServiceImplTest {
    @Mock
    private Properties propertiesMock;

    @Mock
    private ContextBuilder contextBuilderMock;

    @Mock
    private EC2Api ec2ApiMock;

    @Mock
    private AMIApi amiApiMock;

    @Mock
    private Optional<? extends InstanceApi> optionalInstanceApi;

    @Spy
    private AmazonImageServiceImpl imageSpy = new AmazonImageServiceImpl("https://ec2.amazonaws.com",
            "AKIAIQHVQ4UM7SO673TW", "R1ZRPK4HPXU6cyBi1XY/IkYqQ+qR4Nfohkcd384Z", null, null);

    private AmazonImageServiceImpl toTest;

    @Before
    public void init() throws Exception {
        mockStatic(ContextBuilder.class);
        addCommonMocksForMethods();

        toTest = new AmazonImageServiceImpl("https://ec2.amazonaws.com", "AKIAIQHVQ4UM7SO673TW",
                "R1ZRPK4HPXU6cyBi1XY/IkYqQ+qR4Nfohkcd384Z", null, null);
    }

    @After
    public void tearDown() {
        toTest = null;
    }

    @Test
    public void testInit() throws Exception {
        MockingHelper.addCommonMocksForInitMethod(contextBuilderMock, propertiesMock);

        toTest.init(true);

        MockingHelper.commonVerifiersForInitMethod(contextBuilderMock, propertiesMock);
        verifyNoMoreInteractions(propertiesMock);
    }

    @Test
    public void testLazyInit() throws Exception {
        MockingHelper.addCommonMocksForInitMethod(contextBuilderMock, propertiesMock);

        toTest.lazyInit("us-east-1", true);

        MockingHelper.commonVerifiersForInitMethod(contextBuilderMock, propertiesMock);
    }

    @Test
    public void createImageInRegionTest() {
        imageSpy.createImageInRegion("", "", "", "", true, false);

        verify(imageSpy, times(1)).lazyInit(eq(""), eq(false));
        verify(amiApiMock, times(1)).createImageInRegion(anyString(), anyString(), anyString(), any(CreateImageOptions.class));
        commonVerifiersForMethods();
    }

    @Test
    public void deregisterImageInRegionTest() {
        imageSpy.deregisterImageInRegion("", "", false);

        verify(imageSpy, times(1)).lazyInit(eq(""), eq(false));
        verify(amiApiMock, times(1)).deregisterImageInRegion(eq(""), eq(""));
        commonVerifiersForMethods();
    }

    @Test
    public void getLaunchPermissionForImageTest() {
        imageSpy.getLaunchPermissionForImage("us-east-1", "ami-abcdef16", false);

        verify(imageSpy, times(1)).lazyInit(eq("us-east-1"), eq(false));
        verify(amiApiMock, times(1)).getLaunchPermissionForImageInRegion(eq("us-east-1"), eq("ami-abcdef16"));
        commonVerifiersForMethods();
    }

    @Test
    public void addLaunchPermissionsToImageTest() {
        Set<String> userIds = InputsUtil.getStringsSet("firstId|secondId|thirdId", "|");
        Set<String> userGroups = InputsUtil.getStringsSet("firstGroup,secondGroup,thirdGroup", ",");

        imageSpy.addLaunchPermissionsToImage("some_region", userIds, userGroups, "ami-abcdef16", false);

        verify(imageSpy, times(1)).lazyInit(eq("some_region"), eq(false));
        verify(amiApiMock, times(1)).addLaunchPermissionsToImageInRegion(eq("some_region"), anySetOf(String.class),
                anySetOf(String.class), eq("ami-abcdef16"));
        commonVerifiersForMethods();
    }

    @Test
    public void removeLaunchPermissionsFromImageTest() {
        Set<String> userIds = InputsUtil.getStringsSet("firstId|secondId", "|");
        Set<String> userGroups = InputsUtil.getStringsSet("firstGroup,secondGroup", "");

        imageSpy.removeLaunchPermissionsFromImage("some_region", userIds, userGroups, "ami-abcdef16", false);

        verify(imageSpy, times(1)).lazyInit(eq("some_region"), eq(false));
        verify(amiApiMock, times(1)).removeLaunchPermissionsFromImageInRegion(eq("some_region"), anySetOf(String.class),
                anySetOf(String.class), eq("ami-abcdef16"));
        commonVerifiersForMethods();
    }

    @Test
    public void resetLaunchPermissionsOnImageTest() {
        imageSpy.resetLaunchPermissionsOnImage("some_region", "ami-abcdef16", false);

        verify(imageSpy, times(1)).lazyInit(eq("some_region"), eq(false));
        verify(amiApiMock, times(1)).resetLaunchPermissionsOnImageInRegion(eq("some_region"), eq("ami-abcdef16"));
        commonVerifiersForMethods();
    }

    @Test
    public void describeImagesInRegionNoOptionsTest() throws Exception {
        imageSpy.describeImagesInRegion(getCommonInputs(""), getImageInputs("", "", "", "", ""));

        verify(imageSpy, times(1)).lazyInit("us-east-1", false);
        verify(amiApiMock, times(1)).describeImagesInRegion(eq("us-east-1"), eq(DescribeImagesOptions.NONE));
        commonVerifiersForMethods();
    }

    @Test
    public void describeImagesInRegionWithoutFiltersTest() throws Exception {
        imageSpy.describeImagesInRegion(getCommonInputs("|"),
                getImageInputs("another_region", "", "", "firstImageId|secondImageId|thirdImageId", "firstOwner|secondOwner|thirdOwner"));

        verify(imageSpy, times(1)).lazyInit(eq("another_region"), eq(false));
        verify(amiApiMock, times(1)).describeImagesInRegion(eq("another_region"), any(DescribeImagesOptions.class));
        commonVerifiersForMethods();
    }

    @Test
    public void describeImagesInRegionWithFiltersTest() throws Exception {
        imageSpy.describeImagesInRegion(getCommonInputs("|"),
                getImageInputs("another_region", "identityOne", "windows", "firstImageId|secondImageId|thirdImageId",
                        "firstOwner|secondOwner|thirdOwner"));

        verify(imageSpy, times(1)).lazyInit(eq("another_region"), eq(false));
        verify(amiApiMock, times(1))
                .describeImagesInRegionWithFilter(eq("another_region"), any(ArrayListMultimap.class), any(DescribeImagesOptions.class));
        commonVerifiersForMethods();
    }

    private void commonVerifiersForMethods() {
        verify(optionalInstanceApi, times(1)).get();
        verifyNoMoreInteractions(amiApiMock);
    }

    private void addCommonMocksForMethods() {
        doNothing().when(imageSpy).lazyInit(anyString(), anyBoolean());
        doNothing().when(imageSpy).init(anyBoolean());
        imageSpy.ec2Api = ec2ApiMock;
        doReturn(optionalInstanceApi).when(ec2ApiMock).getAMIApiForRegion(anyString());
        doReturn(optionalInstanceApi).when(ec2ApiMock).getAMIApi();
        doReturn(amiApiMock).when(optionalInstanceApi).get();
    }

    private CommonInputs getCommonInputs(String delimiter) {
        return new CommonInputs.CommonInputsBuilder().withDelimiter(delimiter).build();
    }

    private ImageInputs getImageInputs(String region, String identityId, String platform, String imageIdsString,
                                       String ownersString) throws Exception {
        CustomInputs customInputs = new CustomInputs.CustomInputsBuilder()
                .withRegion(region)
                .withIdentityId(identityId)
                .withPlatform(platform)
                .build();

        return new ImageInputs.ImageInputsBuilder()
                .withCustomInputs(customInputs)
                .withImageIdsString(imageIdsString)
                .withOwnersString(ownersString)
                .build();
    }
}
