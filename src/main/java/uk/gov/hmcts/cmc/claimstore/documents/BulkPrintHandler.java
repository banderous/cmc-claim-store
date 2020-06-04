package uk.gov.hmcts.cmc.claimstore.documents;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.Printable;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.PrintablePdf;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.PrintableTemplate;
import uk.gov.hmcts.cmc.claimstore.events.DocumentReadyToPrintEvent;
import uk.gov.hmcts.cmc.claimstore.events.GeneralLetterReadyToPrintEvent;
import uk.gov.hmcts.cmc.claimstore.events.legaladvisor.DirectionsOrderReadyToPrintEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.PaperDefenceReadyToPrintEvent;
import uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.time.LocalDate;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintService.DIRECTION_ORDER_LETTER_TYPE;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintService.GENERAL_LETTER_TYPE;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintService.PAPER_DEFENCE_TYPE;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildCoverSheetFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDefendantLetterFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDirectionsOrderFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildLetterFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildOconFormFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildPaperDefenceCoverLetterFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;

@Component
@ConditionalOnProperty(prefix = "send-letter", name = "url")
public class BulkPrintHandler {

    private final PrintService bulkPrintService;

    @Autowired
    public BulkPrintHandler(PrintService bulkPrintService) {
        this.bulkPrintService = bulkPrintService;
    }

    @EventListener
    public void print(DocumentReadyToPrintEvent event) {
        requireNonNull(event);
        Claim claim = event.getClaim();
        bulkPrintService.print(
            claim,
            ImmutableList.of(
                new PrintableTemplate(
                    event.getDefendantLetterDocument(),
                    buildDefendantLetterFileBaseName(claim.getReferenceNumber())),
                new PrintableTemplate(
                    event.getSealedClaimDocument(),
                    buildSealedClaimFileBaseName(claim.getReferenceNumber()))
            )
        );
    }

    @EventListener
    public void print(DirectionsOrderReadyToPrintEvent event) {
        requireNonNull(event);
        Claim claim = event.getClaim();
        bulkPrintService.printPdf(
            claim,
            ImmutableList.of(
                new PrintableTemplate(
                    event.getCoverSheet(),
                    buildCoverSheetFileBaseName(claim.getReferenceNumber())),
                new PrintablePdf(
                    event.getDirectionsOrder(),
                    buildDirectionsOrderFileBaseName(claim.getReferenceNumber()))
            ),
            DIRECTION_ORDER_LETTER_TYPE
        );
    }

    @EventListener
    public void print(GeneralLetterReadyToPrintEvent event) {
        requireNonNull(event);
        Claim claim = event.getClaim();
        bulkPrintService.printPdf(
            claim,
            ImmutableList.of(
                new PrintablePdf(
                    event.getGeneralLetterDocument(),
                    buildLetterFileBaseName(claim.getReferenceNumber(),
                        String.valueOf(LocalDate.now())))
            ),
            GENERAL_LETTER_TYPE
        );
    }

    @EventListener
    public void print(PaperDefenceReadyToPrintEvent event) {
        //how to convert from ccd document to document
        requireNonNull(event);
        Claim claim = event.getClaim();

        PrintablePdf coverLetter = new PrintablePdf(
                event.getCoverLetter(),
                buildPaperDefenceCoverLetterFileBaseName(claim.getReferenceNumber()));

        PrintablePdf oconForm = new PrintablePdf(
                event.getOconForm(),
                buildOconFormFileBaseName(claim.getReferenceNumber()));

        ImmutableList.Builder<Printable> printableDocs = ImmutableList.<Printable>builder()
                .add(coverLetter)
                .add(oconForm);

        bulkPrintService.printPdf(claim, printableDocs.build(), PAPER_DEFENCE_TYPE);
    }

}
