package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.GeneralLetterContent;
import uk.gov.hmcts.cmc.claimstore.events.GeneralLetterReadyToPrintEvent;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.PrintableDocumentService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBodyMapper;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.docassembly.exception.DocumentGenerationFailedException;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType.GENERAL_LETTER;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.UTC_ZONE;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class GeneralLetterService {
    public static final String DRAFT_LETTER_DOC = "draftLetterDoc";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final DocAssemblyService docAssemblyService;
    private final ApplicationEventPublisher publisher;
    private final PrintableDocumentService printableDocumentService;
    private final Clock clock;
    private final UserService userService;
    private final DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper;
    private final DocumentManagementService documentManagementService;

    public GeneralLetterService(
        DocAssemblyService docAssemblyService,
        ApplicationEventPublisher publisher,
        PrintableDocumentService printableDocumentService,
        Clock clock,
        UserService userService,
        DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper,
        DocumentManagementService documentManagementService
    ) {
        this.docAssemblyService = docAssemblyService;
        this.publisher = publisher;
        this.printableDocumentService = printableDocumentService;
        this.clock = clock;
        this.userService = userService;
        this.docAssemblyTemplateBodyMapper = docAssemblyTemplateBodyMapper;
        this.documentManagementService = documentManagementService;
    }

    public CallbackResponse prepopulateData(String authorisation) {
        var userDetails = userService.getUserDetails(authorisation);
        String caseworkerName = userDetails.getFullName();
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(ImmutableMap.of("generalLetterContent",
                GeneralLetterContent.builder().caseworkerName(caseworkerName).build()
            ))
            .build();
    }

    public String generateLetter(CCDCase ccdCase, String authorisation, String templateId)
        throws DocumentGenerationFailedException {
        logger.info("General Letter: creating general letter");
        var docAssemblyResponse = docAssemblyService.renderTemplate(ccdCase, authorisation, templateId,
            docAssemblyTemplateBodyMapper.generalLetterBody(ccdCase));
        return docAssemblyResponse.getRenditionOutputLocation();
    }

    public CCDCase publishLetter(CCDCase ccdCase, Claim claim, String authorisation, String documentName) {
        var draftLetterDoc = ccdCase.getDraftLetterDoc();
        printLetter(authorisation, draftLetterDoc, claim);

        return ccdCase.toBuilder()
            .caseDocuments(updateCaseDocumentsWithGeneralLetter(ccdCase, draftLetterDoc, documentName, authorisation))
            .draftLetterDoc(null)
            .contactChangeParty(null)
            .contactChangeContent(null)
            .generalLetterContent(null)
            .build();
    }

    public CCDCase attachGeneralLetterToCase(
        CCDCase ccdCase,
        CCDDocument document,
        String documentName,
        String authorization
    ) {

        List<CCDCollectionElement<CCDClaimDocument>> updatedCaseDocuments =
            updateCaseDocumentsWithGeneralLetter(
                ccdCase, document, documentName, authorization);

        return ccdCase.toBuilder()
            .caseDocuments(updatedCaseDocuments)
            .build();
    }

    private List<CCDCollectionElement<CCDClaimDocument>> updateCaseDocumentsWithGeneralLetter(
        CCDCase ccdCase,
        CCDDocument ccdDocument,
        String documentName,
        String authorisation) {

        var documentMetadata = documentManagementService.getDocumentMetaData(
            authorisation,
            URI.create(ccdDocument.getDocumentUrl()).getPath()
        );

        CCDCollectionElement<CCDClaimDocument> claimDocument = CCDCollectionElement.<CCDClaimDocument>builder()
            .value(CCDClaimDocument.builder()
                .documentLink(CCDDocument.builder()
                    .documentFileName(documentName)
                    .documentUrl(ccdDocument.getDocumentUrl())
                    .documentBinaryUrl(documentMetadata.links.binary.href)
                    .build())
                .documentName(documentName)
                .createdDatetime(LocalDateTime.now(clock.withZone(UTC_ZONE)))
                .size(documentMetadata.size)
                .documentType(GENERAL_LETTER)
                .build())
            .build();
        return ImmutableList.<CCDCollectionElement<CCDClaimDocument>>builder()
            .addAll(ccdCase.getCaseDocuments())
            .add(claimDocument)
            .build();
    }

    public void printLetter(String authorisation, CCDDocument document, Claim claim) {
        Document downloadedLetter = printableDocumentService.process(document, authorisation);
        GeneralLetterReadyToPrintEvent event = new GeneralLetterReadyToPrintEvent(claim, downloadedLetter);
        publisher.publishEvent(event);
    }
}
