package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.StatesPaidEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.documents.output.PDF.EXTENSION;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildResponseFileBaseName;
import static uk.gov.hmcts.cmc.email.EmailAttachment.pdf;

@Service
public class StatesPaidStaffNotificationService {
    private final EmailService emailService;
    private final StaffEmailProperties emailProperties;
    private final StatesPaidEmailContentProvider emailContentProvider;
    private final DefendantResponseReceiptService defendantResponseReceiptService;

    @Autowired
    public StatesPaidStaffNotificationService(
        EmailService emailService,
        StaffEmailProperties emailProperties,
        StatesPaidEmailContentProvider emailContentProvider,
        DefendantResponseReceiptService defendantResponseReceiptService
    ) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.emailContentProvider = emailContentProvider;
        this.defendantResponseReceiptService = defendantResponseReceiptService;
    }

    public void notifyStaffClaimantResponseStatesPaidSubmittedFor(Claim claim) {
        EmailContent emailContent;
        emailContent = emailContentProvider.createContent(wrapInMap(claim));

        emailService.sendEmail(
            emailProperties.getSender(),
            new EmailData(
                emailProperties.getRecipient(),
                emailContent.getSubject(),
                emailContent.getBody(),
                singletonList(createResponsePdfAttachment(claim))
            )
        );

    }

    public static Map<String, Object> wrapInMap(Claim claim) {

        Map<String, Object> map = new HashMap<>();
        Response response = claim.getResponse().orElseThrow(IllegalArgumentException::new);
        ClaimantResponse claimantResponse = claim.getClaimantResponse().orElseThrow(IllegalArgumentException::new);

        map.put("claim", claim);
        map.put("response", response);
        map.put("claimantResponse", claimantResponse.getType());
        map.put("defendantEmail", claim.getDefendantEmail());
        map.put("defendantMobilePhone", response
                .getDefendant()
                .getMobilePhone()
                .orElse(null));

        return map;
    }

    private EmailAttachment createResponsePdfAttachment(Claim claim) {
        byte[] defendantResponse = defendantResponseReceiptService.createPdf(claim);
        requireNonNull(defendantResponse);

        return pdf(defendantResponse, buildResponseFileBaseName(claim.getReferenceNumber()) + EXTENSION);
    }
}