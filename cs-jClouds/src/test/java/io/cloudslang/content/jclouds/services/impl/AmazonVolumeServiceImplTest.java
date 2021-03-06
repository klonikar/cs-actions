package io.cloudslang.content.jclouds.services.impl;

import com.google.common.base.Optional;
import org.jclouds.ContextBuilder;
import org.jclouds.ec2.EC2Api;
import org.jclouds.ec2.features.ElasticBlockStoreApi;
import org.jclouds.ec2.features.InstanceApi;
import org.jclouds.ec2.options.CreateVolumeOptions;
import org.jclouds.ec2.options.DetachVolumeOptions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Properties;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by Mihai Tusa.
 * 6/23/2016.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({AmazonVolumeServiceImpl.class, ContextBuilder.class})
public class AmazonVolumeServiceImplTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private Properties propertiesMock;

    @Mock
    private ContextBuilder contextBuilderMock;

    @Mock
    private EC2Api ec2ApiMock;

    @Mock
    private ElasticBlockStoreApi ebsApiMock;

    @Mock
    private Optional<? extends InstanceApi> optionalInstanceApiMock;

    @Spy
    private AmazonVolumeServiceImpl volumeSpy = new AmazonVolumeServiceImpl("https://ec2.amazonaws.com",
            "AKIAIQHVQ4UM7SO673TW", "R1ZRPK4HPXU6cyBi1XY/IkYqQ+qR4Nfohkcd384Z", null, null);

    private AmazonVolumeServiceImpl toTest;

    @Before
    public void init() throws Exception {
        mockStatic(ContextBuilder.class);
        MockingHelper.addCommonMocksForMethods(volumeSpy, null, ec2ApiMock, optionalInstanceApiMock, ebsApiMock,
                AmazonVolumeServiceImpl.class);

        toTest = new AmazonVolumeServiceImpl("https://ec2.amazonaws.com", "AKIAIQHVQ4UM7SO673TW",
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
    public void createVolumeInAvailabilityZoneTest() throws Exception {
        volumeSpy.createVolumeInAvailabilityZone("some_region", "an_available_zone", "snap-c5920f60", "standard", "",
                "100", true, false);

        verify(volumeSpy, times(1)).lazyInit("some_region", false);
        verify(ebsApiMock, times(1)).createVolumeInAvailabilityZone(eq("an_available_zone"), any(CreateVolumeOptions.class));
        MockingHelper.commonVerifiersForMethods(optionalInstanceApiMock, ebsApiMock);
    }

    @Test
    public void createVolumeInAvailabilityZoneWithoutSnapshotTest() throws Exception {
        volumeSpy.createVolumeInAvailabilityZone("some_region", "an_available_zone", "", "", "1024", "100", true, false);

        verify(volumeSpy, times(1)).lazyInit("some_region", false);
        verify(ebsApiMock, times(1)).createVolumeInAvailabilityZone(eq("an_available_zone"), anyInt());
        MockingHelper.commonVerifiersForMethods(optionalInstanceApiMock, ebsApiMock);
    }

    @Test
    public void createVolumeInAvailabilityZoneWithSnapshotTest() throws Exception {
        volumeSpy.createVolumeInAvailabilityZone("some_region", "an_available_zone", "snap-c5920f60", "", "", "", true, false);

        verify(volumeSpy, times(1)).lazyInit("some_region", false);
        verify(ebsApiMock, times(1)).createVolumeInAvailabilityZone(eq("an_available_zone"), any(CreateVolumeOptions.class));
        MockingHelper.commonVerifiersForMethods(optionalInstanceApiMock, ebsApiMock);
    }

    @Test
    public void createVolumeInAvailabilityZoneValidationErrorNegativeSizeTest() throws Exception {
        MockingHelper.setExpectedExceptions(exception, RuntimeException.class,
                "The size [-5] provided for [standard] volumeType should be greater or equal than [1] GiBs value " +
                        "and smaller or equal than [1024] GiBs value.");

        volumeSpy.createVolumeInAvailabilityZone("", "", "", "", "-5", "1111", false, false);

        verify(volumeSpy, never()).lazyInit(anyString(), anyBoolean());
        verify(ebsApiMock, never()).createVolumeInAvailabilityZone(anyString(), any(CreateVolumeOptions.class));
    }

    @Test
    public void createVolumeInAvailabilityZoneValidationErrorLowerSizeTest() throws Exception {
        MockingHelper.setExpectedExceptions(exception, RuntimeException.class,
                "The size [0] provided for [gp2] volumeType should be greater or equal than [1] GiBs value " +
                        "and smaller or equal than [16384] GiBs value.");

        volumeSpy.createVolumeInAvailabilityZone("", "", "", "gp2", "0", "1111", false, false);

        verify(volumeSpy, never()).lazyInit(anyString(), anyBoolean());
        verify(ebsApiMock, never()).createVolumeInAvailabilityZone(anyString(), any(CreateVolumeOptions.class));
    }

    @Test
    public void createVolumeInAvailabilityZoneValidationErrorHigherSizeTest() throws Exception {
        MockingHelper.setExpectedExceptions(exception, RuntimeException.class,
                "The size [16385] provided for [sc1] volumeType should be greater or equal than [500] GiBs value " +
                        "and smaller or equal than [16384] GiBs value.");

        volumeSpy.createVolumeInAvailabilityZone("", "", "", "sc1", "16385", "1111", false, false);

        verify(volumeSpy, never()).lazyInit(anyString(), anyBoolean());
        verify(ebsApiMock, never()).createVolumeInAvailabilityZone(anyString(), any(CreateVolumeOptions.class));
    }

    @Test
    public void createVolumeInAvailabilityZoneValidationErrorIopsTest1() throws Exception {
        MockingHelper.setExpectedExceptions(exception, RuntimeException.class,
                "The size [499] provided for [st1] volumeType should be greater or equal than [500] GiBs value " +
                        "and smaller or equal than [16384] GiBs value.");

        volumeSpy.createVolumeInAvailabilityZone("", "", "", "st1", "499", "0", true, false);

        verify(volumeSpy, never()).lazyInit(anyString(), anyBoolean());
        verify(ebsApiMock, never()).createVolumeInAvailabilityZone(anyString(), any(CreateVolumeOptions.class));
    }

    @Test
    public void createVolumeInAvailabilityZoneValidationErrorIopsTest() throws Exception {
        MockingHelper.setExpectedExceptions(exception, RuntimeException.class,
                "The iops [10001] provided for [gp2] volumeType should be greater or equal than [100] IOPS value " +
                        "and smaller or equal than [10000] IOPS value.");

        volumeSpy.createVolumeInAvailabilityZone("", "", "", "gp2", "3334", "10001", true, false);

        verify(volumeSpy, never()).lazyInit(anyString(), anyBoolean());
        verify(ebsApiMock, never()).createVolumeInAvailabilityZone(anyString(), any(CreateVolumeOptions.class));
    }

    @Test
    public void createVolumeInAvailabilityZoneValidationErrorIopsTest2() throws Exception {
        MockingHelper.setExpectedExceptions(exception, RuntimeException.class,
                "The iops [99] provided for [io1] volumeType should be greater or equal than [100] IOPS value " +
                        "and smaller or equal than [20000] IOPS value.");

        volumeSpy.createVolumeInAvailabilityZone("", "", "", "io1", "3333", "99", true, false);

        verify(volumeSpy, never()).lazyInit(anyString(), anyBoolean());
        verify(ebsApiMock, never()).createVolumeInAvailabilityZone(anyString(), any(CreateVolumeOptions.class));
    }

    @Test
    public void deleteVolumeInRegion(){
        volumeSpy.deleteVolumeInRegion("some_region", "vol-b8d74e1c", false);

        verify(volumeSpy, times(1)).lazyInit("some_region", false);
        verify(ebsApiMock, times(1)).deleteVolumeInRegion(eq("some_region"), eq("vol-b8d74e1c"));
        MockingHelper.commonVerifiersForMethods(optionalInstanceApiMock, ebsApiMock);
    }

    @Test
    public void attachVolumeInRegionTest() {
        volumeSpy.attachVolumeInRegion("some_region", "vol-6dea0dc9", "i-2b84b0b1", "/dev/sdh", false);

        verify(volumeSpy, times(1)).lazyInit("some_region", false);
        verify(ebsApiMock, times(1)).attachVolumeInRegion(eq("some_region"), eq("vol-6dea0dc9"), eq("i-2b84b0b1"), eq("/dev/sdh"));
        MockingHelper.commonVerifiersForMethods(optionalInstanceApiMock, ebsApiMock);
    }

    @Test
    public void detachVolumeInRegionWithOptionsTest() throws Exception {
        volumeSpy.detachVolumeInRegion("some_region", "vol-6dea0dc9", "i-2b84b0b1", "/dev/sdh", false, false);

        verify(volumeSpy, times(1)).lazyInit("some_region", false);
        verify(ebsApiMock, times(1)).detachVolumeInRegion(eq("some_region"), eq("vol-6dea0dc9"), eq(false),
                any(DetachVolumeOptions.class), any(DetachVolumeOptions.class));
        MockingHelper.commonVerifiersForMethods(optionalInstanceApiMock, ebsApiMock);
    }

    @Test
    public void detachVolumeInRegionWithoutOptionsTest() throws Exception {
        volumeSpy.detachVolumeInRegion("some_region", "vol-6dea0dc9", "", "", true, false);

        verify(volumeSpy, times(1)).lazyInit("some_region", false);
        verify(ebsApiMock, times(1)).detachVolumeInRegion(eq("some_region"), eq("vol-6dea0dc9"), eq(true));
        MockingHelper.commonVerifiersForMethods(optionalInstanceApiMock, ebsApiMock);
    }
}
