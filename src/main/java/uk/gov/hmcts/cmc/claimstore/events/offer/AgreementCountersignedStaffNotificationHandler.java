package uk.gov.hmcts.cmc.claimstore.events.offer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.SettlementReachedStaffNotificationService;

@Component
public class AgreementCountersignedStaffNotificationHandler {

    private final SettlementReachedStaffNotificationService notificationService;

    @Autowired
    public AgreementCountersignedStaffNotificationHandler(
        SettlementReachedStaffNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void onAgreementCountersigned(AgreementCountersignedEvent event) {
        notificationService.notifySettlementReached(
            event.getClaim()
        );
    }

}
