import com.sgdbf.model.ApproverAction
import com.sgdbf.model.Proposition;
import com.sgdbf.model.TimeLine
import com.sgdbf.pop3p.groovy.EnvironmentParameters
import com.sgdbf.pop3p.groovy.ValidationDueDateCalculation
import com.bonitasoft.engine.api.APIAccessor;
import java.util.List;
import java.util.logging.Logger;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.platform.UnknownUserException
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.Clock;
import java.time.ZoneOffset;

public static TimeLine getTimeline(APIAccessor apiAccessor, Proposition currentProposition, String acteur, String status, String isFirstWorkFlowData, Long initiatorId) {
	
	Logger logger = Logger.getLogger("org.bonitasoft");
	
	TimeLine tlp = new TimeLine();
	tlp.Acteur = acteur;
	tlp.AnneeBudgetaire = currentProposition.budgetYear;
	if (currentProposition.materialNumber != null && currentProposition.materialNumber.length()>0){
		tlp.CodeClef= "MAT#"+currentProposition.materialNumber
	}else{
		tlp.CodeClef = "NEW#"+currentProposition.agencyCode+"#"+currentProposition.persistenceId
	}
	
	
	String bufferTypeWorkflow = currentProposition.typeWorkflow;
	
	/*if ((isFirstWorkFlowData == null || isFirstWorkFlowData.equals("false")) && currentProposition.typeProposition.equalsIgnoreCase("Transfert") && currentProposition.typeWorkflowTargetAgency.equalsIgnoreCase("DXX")) {
		bufferTypeWorkflow = currentProposition.typeWorkflowTargetAgency;
	}*/
	
	//loop modified by Amit for BPM-162
	
	if((isFirstWorkFlowData == null || isFirstWorkFlowData.equals("false")) && (currentProposition.typeProposition.equalsIgnoreCase("Transfert") || currentProposition.typeProposition.equalsIgnoreCase("A renouveler")))
	{
		bufferTypeWorkflow = currentProposition.typeWorkflowTargetAgency;
	}
	
	
	tlp.CodeWorkflow = bufferTypeWorkflow;
	OffsetDateTime dateCreation = OffsetDateTime.now(Clock.systemDefaultZone());
	
	//tlp.DateCreation = LocalDateTime.now();

	tlp.DateCreation = dateCreation;
	if (null != initiatorId) {
		tlp.IdentifiantActeur = initiatorId;
	}
	tlp.IdProposition = currentProposition.persistenceId;
	tlp.Statut = status.take(255);			// BPM-523 CFR 15/01/2021 : Make sure we don't exceed max length for status
	tlp.Motif = currentProposition.typeAcquisition;
	
	// CFR 02/11/2020 BPM-440 : add fields propositionStatus and statusMacroEtapes on timelines
	// UAT 30/11/2020 Use correct status depending on the branch : propositionStatus for DDD, propositionStatusTargetAgency for DAD
	if ((isFirstWorkFlowData != null) && ("false".equalsIgnoreCase(isFirstWorkFlowData))) {
		// DAD
		tlp.propositionStatus = currentProposition.propositionStatusTargetAgency;
	} else {
		// DDD
		tlp.propositionStatus = currentProposition.propositionStatus;
	}
	tlp.statusMacroEtapes = currentProposition.statusMacroEtapes;
	
	// CFR 09/12/2020 BPM-474 : add field actualRequestAmount to timelines
	tlp.actualRequestAmount = currentProposition.actualRequestAmount;
	
	tlp.taskDueDate = null;
		
	return tlp;

}

public static TimeLine getTimelineWithDueDate(APIAccessor apiAccessor, Proposition currentProposition, String acteur, String status, String isFirstWorkFlowData, Long initiatorId, java.time.OffsetDateTime dueDate) {
		
	TimeLine tlp = getTimeline(apiAccessor, currentProposition, acteur, status, isFirstWorkFlowData, initiatorId);
	tlp.taskDueDate = dueDate
		
	return tlp;

}