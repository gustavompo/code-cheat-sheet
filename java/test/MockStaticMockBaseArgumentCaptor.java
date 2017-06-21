package any;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


@RunWith(PowerMockRunner.class)
@PrepareForTest({TaxiCallDatabase.class, BaseSNSService.class, UserDatabase.class})
@PowerMockIgnore("javax.net.ssl.*")
public class MockStaticMockBaseArgumentCaptor {

  @Test
  public void publishMilestoneOfTypeSTARTDecoratingWithCallAndUserInfo() throws DatabaseException {
    Long a = 666L;
    Long b = 777L;
    Platform c = Platform.ANDROID;
    String d = "mc";
    String title = "START";
    Double lon = 55D;
    OptionalValueKey x = OptionalValueKey.ANY_OPT;

    mockStaticDatabasesWith(a, b, c, d, Stream.of(x).collect(Collectors.toSet()));
    MilestonesEventPublisherService.MilestoneEvent milestoneEventPublished = invokePublishWith(a, title,
        lon);

    assertEquals(userDevicePlatform.name(), milestoneEventPublished.user.device);
    assertEquals(paymentMethod, milestoneEventPublished.call.paymentMethod);
    assertEquals(optional.getKey(), milestoneEventPublished.call.optionals);

    verifyStatic(times(1));
    TaxiCallDatabase.getTaxiCallByJobId(taxiJobId);

    verifyStatic(times(1));
    UserDatabase.getFullUserById(userId);
  }

  private MilestonesEventPublisherService.MilestoneEvent invokePublishWith(Long taxiJobId, String milestoneTitle, Double longitude) {
    MilestonesEventPublisherService service = PowerMockito.spy(new MilestonesEventPublisherService());
    doCallRealMethod().when(service).publish(anyLong(), anyString(), any(Date.class), anyDouble(), anyDouble());

    service.publish(taxiJobId, milestoneTitle, new Date(), -46D, longitude);

    ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
    verify(service).publishAsync(argument.capture());
    return new Gson().fromJson(argument.getValue(), MilestonesEventPublisherService.MilestoneEvent.class);
  }

  private void mockStaticDatabasesWith(Long taxiJobId, Long userId, Platform platform, String paymentMethod, Set<OptionalValueKey> optionals) throws DatabaseException {

    TaxiCall call = new TaxiCall(11L, userId, new Location(-26d, -46d), "", 1, "", paymentMethod, 0, new Date(), TaxiCall.TaxiCallStatus
        .DRIVER_SELECTED, 2, 2, 6662L, taxiJobId, null, null, "", "", 9766,  optionals);

    UserDeviceInfo userDeviceInfo = new UserDeviceInfo();
    userDeviceInfo.setPlatform(platform);
    User.FullUser user = new User.FullUser();
    user.setDeviceInfo(userDeviceInfo);

    mockStatic(TaxiCallDatabase.class);
    when(TaxiCallDatabase.getTaxiCallByJobId(anyLong())).thenReturn(call);

    mockStatic(UserDatabase.class);
    when(UserDatabase.getFullUserById(anyLong())).thenReturn(user);
  }
}
