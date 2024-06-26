import com.bonitasoft.engine.api.APIAccessor;
import com.sgdbf.model.Proposition;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;



public static String sendIntimationSmarte(Proposition proposition, String bonita_server_host, String bonita_server_port, APIAccessor apiAccessor,List<String> listMailSendTo)
{
	
	StringBuilder stringContent =  new StringBuilder("");
	
	String typeTxt = ""
	
	stringContent.append("<p>Bonjour,</p>");
	stringContent.append("<p>L'opération ");
	stringContent.append(" ");
	
	if("Nouvel équipement".equalsIgnoreCase(proposition.typeProposition) || "Nouveau matériel".equalsIgnoreCase(proposition.typeProposition) || "A renouveler".equalsIgnoreCase(proposition.typeProposition))
	{
		typeTxt = "d’acquisition"
	}
	else if("A vendre".equalsIgnoreCase(proposition.typeProposition))
	{
		typeTxt = "de vente"
	}
	else if("Transfert".equalsIgnoreCase(proposition.typeProposition))
	{
		typeTxt = "de transfert"
	}
	
	stringContent.append(typeTxt);
	stringContent.append(" pour la proposition n°");
	stringContent.append(proposition.persistenceId);
	stringContent.append(" (").append(proposition.typeProposition).append(")");
	stringContent.append(" est terminée.</p>");
		
	stringContent.append("<p>Cette proposition peut être consultée : ");
	stringContent.append(MailContent.getUrlDependsGroup(proposition, bonita_server_host, bonita_server_port, "") + "</p>");
	
	stringContent.append(MailContent.getContentMailTemplate());
	
	stringContent.append("<p>Cette proposition à été envoyée à : ");
	
	stringContent.append(MailContent.getMailSendTo(listMailSendTo, apiAccessor, proposition));
	

	return stringContent;
}


public static String sendTESTReassignation(Proposition proposition, String bonita_server_host, String bonita_server_port, String isFirstWF) {
	
		String typeWorkflow;
		if ("true".equalsIgnoreCase(isFirstWF)) {
			typeWorkflow = proposition.typeWorkflow;
		} else {
			typeWorkflow = proposition.typeWorkflowTargetAgency;
		}
		
		StringBuilder stringContent =  new StringBuilder("");
		stringContent.append("<p>Bonjour,</p>");
		stringContent.append("<p>La tache de budget n°");
		stringContent.append(proposition.persistenceId);
		stringContent.append(" (").append(proposition.typeProposition).append(")");
		stringContent.append(" vous a été transmise. (workflow " + typeWorkflow + ")</p>");
		
		stringContent.append("<p>Cette proposition peut être consultée : ");
		stringContent.append(MailContent.getUrlDisplayPropositionDetailApp(proposition, "ici", bonita_server_host, bonita_server_port) + "</p>");
		
		stringContent.append(MailContent.getContentMailTemplate());
		
		stringContent.append("<p>Cette proposition à été envoyée à : ");
		stringContent.append("lac.pop3p@saint-gobain.com");
		
	
		return stringContent;
	}

public static String sendToLAC(Proposition proposition, String bonita_server_host, String bonita_server_port, String isFirstWF) {
	
		String typeWorkflow;
		if ("true".equalsIgnoreCase(isFirstWF)) {
			typeWorkflow = proposition.typeWorkflow;
		} else {
			typeWorkflow = proposition.typeWorkflowTargetAgency;
		}
		
		StringBuilder stringContent =  new StringBuilder("");
		stringContent.append("<p>Bonjour,</p>");
		stringContent.append("<p>La proposition n°");
		stringContent.append(proposition.persistenceId);
		stringContent.append(" (").append(proposition.typeProposition).append(")");
		stringContent.append(" vous a été transmise. (workflow " + typeWorkflow + ")</p>");
		
		stringContent.append("<p>Cette proposition peut être consultée : ");
		stringContent.append(MailContent.getUrlDisplayPropositionDetailApp(proposition, "ici", bonita_server_host, bonita_server_port) + "</p>");
		
		stringContent.append(MailContent.getContentMailTemplate());
		
		stringContent.append("<p>Cette proposition à été envoyée à : ");
		stringContent.append("lac.pop3p@saint-gobain.com");
		
	
		return stringContent;
	}

public static String sendDemandeValidateDAD_DDD(String typeWorkflow, String niveauApprob, Proposition proposition, String bonita_server_host, String bonita_server_port, APIAccessor apiAccessor, List<String> listMailSendTo, String isFirstWF, String approval,long activityInstanceIdt) {
	
	StringBuilder stringContent =  new StringBuilder("");
	stringContent.append("<p>Bonjour,</p>");

	def propositionReason = "";
	
	if ("DADR".equals(proposition.typeWorkflow)) {
		propositionReason = "demande de réparation";
		approval = "SAV";
	} else {
		propositionReason = "proposition";
	}
	
	stringContent.append("<p>La " +propositionReason + " n°");
	stringContent.append(proposition.persistenceId.toString());
	stringContent.append(" (").append(proposition.typeProposition).append(")");
	stringContent.append(" est en attente de validation "+typeWorkflow+" de votre part pour le niveau " + niveauApprob + " : ");
	stringContent.append("<p>La validation peut être effectuée : ");
	
	stringContent.append(MailContent.getUrlValidateForm ("ici", bonita_server_host, bonita_server_port, activityInstanceIdt));
	
	stringContent.append("<p>La liste de toutes les propositions en attentes de validation peut être consultée également : ");
	
	stringContent.append(MailContent.getUrlApproverValidate("ici", bonita_server_host, bonita_server_port) + "</p>");
	
	stringContent.append("<p>Cette " + propositionReason + " peut être consultée : ");

	stringContent.append(MailContent.getUrlDependsGroup(proposition, bonita_server_host, bonita_server_port, approval) + "</p>");
			
	stringContent.append(MailContent.getContentMailTemplate());
	
	stringContent.append("<p>Cette " + propositionReason + " à été envoyée à : ");

	if ("SMARTE".equalsIgnoreCase(approval)) {
		stringContent.append(MailContent.getMailSMARTE(apiAccessor, proposition));
	} else {
		stringContent.append(MailContent.getMailSendTo(listMailSendTo, apiAccessor, proposition));
	}
	
	return stringContent;
}

public static String sendDemandeValidate(Proposition proposition, String bonita_server_host, String bonita_server_port, APIAccessor apiAccessor, List<String> listMailSendTo, String isFirstWF, String approval,long activityInstanceIdt) {
	
	String niveauApprob;
	if (isFirstWF.equals("true")) {
		niveauApprob = proposition.niveauApprob;
	} else {
		niveauApprob = proposition.niveauApprobTargetAgency;
	}
	
	StringBuilder stringContent =  new StringBuilder("");
	stringContent.append("<p>Bonjour,</p>");

	def propositionReason = "";
	
	if ("DADR".equals(proposition.typeWorkflow)) {
		propositionReason = "demande de réparation";
		approval = "SAV";
	} else {
		propositionReason = "proposition";
	}
	
	stringContent.append("<p>La " +propositionReason + " n°");
	stringContent.append(proposition.persistenceId.toString());
	stringContent.append(" (").append(proposition.typeProposition).append(")");
	stringContent.append(" est en attente de validation "+proposition.getTypeWorkflow()+" de votre part pour le niveau " + niveauApprob + " : ");
	stringContent.append("<p>La validation peut être effectuée : ");
	
	stringContent.append(MailContent.getUrlValidateForm ("ici", bonita_server_host, bonita_server_port, activityInstanceIdt));
	
	stringContent.append("<p>La liste de toutes les propositions en attentes de validation peut être consultée également : ");
	
	stringContent.append(MailContent.getUrlApproverValidate("ici", bonita_server_host, bonita_server_port) + "</p>");
	
	stringContent.append("<p>Cette " + propositionReason + " peut être consultée : ");

	stringContent.append(MailContent.getUrlDependsGroup(proposition, bonita_server_host, bonita_server_port, approval) + "</p>");
			
	stringContent.append(MailContent.getContentMailTemplate());
	
	stringContent.append("<p>Cette " + propositionReason + " à été envoyée à : ");

	if ("SMARTE".equalsIgnoreCase(approval)) {
		stringContent.append(MailContent.getMailSMARTE(apiAccessor, proposition));
	} else {
		stringContent.append(MailContent.getMailSendTo(listMailSendTo, apiAccessor, proposition));
	}
	
//	if ("DADR".equals(proposition.typeWorkflow)) {
//		stringContent.append(", savpop3P@saint-gobain.com");
//	}
	
	return stringContent;
}


public static String sendRefus(Proposition proposition, String bonita_server_host, String bonita_server_port, APIAccessor apiAccessor, List<String> listMailSendTo, String isFirstWF, String approval) {

	String typeWorkflow;
	if ("true".equalsIgnoreCase(isFirstWF)) {
		typeWorkflow = proposition.typeWorkflow;
	} else {
		typeWorkflow = proposition.typeWorkflowTargetAgency;
	}
	
	StringBuilder stringContent =  new StringBuilder("");
	stringContent.append("<p>Bonjour,</p>");
	stringContent.append("<p>La proposition n°");
	stringContent.append(proposition.persistenceId);
	stringContent.append(" (").append(proposition.typeProposition).append(")");
	stringContent.append(" a été refusée. (workflow " + typeWorkflow + ")</p>");
	
	stringContent.append("<p>Cette proposition peut être consultée : ");
	stringContent.append(MailContent.getUrlDependsGroup(proposition, bonita_server_host, bonita_server_port, approval) + "</p>");
	
	stringContent.append(MailContent.getContentMailTemplate());
	
	stringContent.append("<p>Cette proposition à été envoyée à : ");
	if ("SMARTE".equalsIgnoreCase(approval)) {
		stringContent.append(MailContent.getMailSMARTE(apiAccessor, proposition));
	} else {
		stringContent.append(MailContent.getMailSendTo(listMailSendTo, apiAccessor, proposition));
	}

	return stringContent;
}

// CFR 20/11/2020 - New notification for button 'Refus' in task 'A transmettre BO ou LAC'
public static String sendAbandonSmarte(Proposition proposition, String bonita_server_host, String bonita_server_port, APIAccessor apiAccessor, List<String> listMailSendTo, String isFirstWF, String approval) {
			
		StringBuilder stringContent =  new StringBuilder("");
		stringContent.append("<p>Bonjour,</p>");
		stringContent.append("<p>La proposition n°");
		stringContent.append(proposition.persistenceId);
		stringContent.append(" (").append(proposition.typeProposition).append(")");
		stringContent.append(" a été abandonnée SMARTE (étape 'A transmettre BO ou LAC').</p>");
		stringContent.append("<p>Motif du refus : ");
		stringContent.append(proposition.commentBOTransmettreBO);
		stringContent.append("</p><p>Cette proposition peut être consultée : ");
		stringContent.append(MailContent.getUrlDependsGroup(proposition, bonita_server_host, bonita_server_port, approval) + "</p>");
		
		stringContent.append(MailContent.getContentMailTemplate());
		
		stringContent.append("<p>Cette proposition à été envoyée à : ");
		if ("SMARTE".equalsIgnoreCase(approval)) {
			stringContent.append(MailContent.getMailSMARTE(apiAccessor, proposition));
		} else {
			stringContent.append(MailContent.getMailSendTo(listMailSendTo, apiAccessor, proposition));
		}
	
		return stringContent;
}


public static String sendDemandeComplement(Proposition proposition, String bonita_server_host, String bonita_server_port, APIAccessor apiAccessor, List<String> listMailSendTo, String approval) {
	
	StringBuilder stringContent =  new StringBuilder("");
	stringContent.append("<p>Bonjour,</p>");
	stringContent.append("<p>La proposition n°");
	stringContent.append(proposition.persistenceId);
	stringContent.append(" (").append(proposition.typeProposition).append(")");
	stringContent.append(" nécessite un complément d'information. <br/><br/>Le commentaire suivant a été renseigné :<br>");
	stringContent.append(proposition.completeInfoMessage + "</p>");
	stringContent.append("<p>L'action peut être effectuée : ");
	stringContent.append(MailContent.getUrlHomePageSmarte("ici", bonita_server_host, bonita_server_port) + "</p>");
	
	stringContent.append("<p>Cette proposition peut être consultée : ");
	stringContent.append(MailContent.getUrlDependsGroup(proposition, bonita_server_host, bonita_server_port, approval) + "</p>");
	
	stringContent.append(MailContent.getContentMailTemplate());
	
	stringContent.append("<p>Cette proposition à été envoyée à : ");
	if ("SMARTE".equalsIgnoreCase(approval)) {
		stringContent.append(MailContent.getMailSMARTE(apiAccessor, proposition));
	} else {
		stringContent.append(MailContent.getMailSendTo(listMailSendTo, apiAccessor, proposition));
	}

	return stringContent;
}

public static String sendTransferValideATransmettreBoOuLac(Proposition proposition, String bonita_server_host, String bonita_server_port, APIAccessor apiAccessor, List<String> listMailSendTo, String approval) {
	
	StringBuilder stringContent =  new StringBuilder("");
	stringContent.append("<p>Bonjour,</p>");
	stringContent.append("<p>La proposition n°");
	stringContent.append(proposition.persistenceId);
	stringContent.append(" (").append(proposition.typeProposition).append(")");
	stringContent.append(" a été validée. (workflow DAD+DDD)<br>");
	
	stringContent.append("<p>La proposition est à l'étape transmettre au BO ou au LAC, vous pouvez prendre l'action : ");
	stringContent.append(MailContent.getUrlHomePageSmarte("ici", bonita_server_host, bonita_server_port) + "</p>");
	
	stringContent.append("<p>Cette proposition peut être consultée : ");
	stringContent.append(MailContent.getUrlDependsGroup(proposition, bonita_server_host, bonita_server_port, approval) + "</p>");
	
	stringContent.append(MailContent.getContentMailTemplate());
	
	stringContent.append("<p>Cette proposition à été envoyée à : ");
	if ("SMARTE".equalsIgnoreCase(approval)) {
		stringContent.append(MailContent.getMailSMARTE(apiAccessor, proposition));
	} else {
		stringContent.append(MailContent.getMailSendTo(listMailSendTo, apiAccessor, proposition));
	}

	return stringContent;
}

public static String sendBudgetAccepted(Proposition proposition, String bonita_server_host, String bonita_server_port, APIAccessor apiAccessor, List<String> listMailSendTo, String isFirstWF, String approval) {
	String typeWorkflow;
	if ("true".equalsIgnoreCase(isFirstWF)) {
		typeWorkflow = proposition.typeWorkflow;
	} else {
		typeWorkflow = proposition.typeWorkflowTargetAgency;
	}
	
	StringBuilder stringContent =  new StringBuilder("");
	stringContent.append("<p>Bonjour,</p>");
	stringContent.append("<p>La proposition n°");
	stringContent.append(proposition.persistenceId);
	stringContent.append(" (").append(proposition.typeProposition).append(")");
	stringContent.append(" a été validée. (workflow " + typeWorkflow + ")</p>");
	
	if ("SMARTE".equalsIgnoreCase(approval)) {
		stringContent.append("<p>La proposition est à l'étape de TRANSITION, vous pouvez prendre l'action : ");
		stringContent.append(MailContent.getUrlHomePageSmarte("ici", bonita_server_host, bonita_server_port) + "</p>");
	}
			
	stringContent.append("<p>Cette proposition peut être consultée : ");
	stringContent.append(MailContent.getUrlDependsGroup(proposition, bonita_server_host, bonita_server_port, approval) + "</p>");
	
	stringContent.append(MailContent.getContentMailTemplate());
	
	stringContent.append("<p>Cette proposition à été envoyée à : ");
	
	if ("SMARTE".equalsIgnoreCase(approval)) {
				stringContent.append(MailContent.getMailSMARTE(apiAccessor, proposition));
	} else {
		stringContent.append(MailContent.getMailSendTo(listMailSendTo, apiAccessor, proposition));
	}
	return stringContent;
}

public static String sendDXXAccepted(Proposition proposition, String bonita_server_host, String bonita_server_port, APIAccessor apiAccessor, List<String> listMailSendTo, String isFirstWF, String approval) {
	
	String typeWorkflow;
	if ("true".equalsIgnoreCase(isFirstWF)) {
		typeWorkflow = proposition.typeWorkflow;
	} else {
		typeWorkflow = proposition.typeWorkflowTargetAgency;
	}
	
	StringBuilder stringContent =  new StringBuilder("");
	stringContent.append("<p>Bonjour,</p>");
	stringContent.append("<p>La proposition n°");
	stringContent.append(proposition.persistenceId);
	stringContent.append(" (").append(proposition.typeProposition).append(")");
	stringContent.append(" a été validée. (workflow " + typeWorkflow + ")</p>");
	
	if ("SMARTE".equalsIgnoreCase(approval) && "DAD".equalsIgnoreCase(typeWorkflow) && ! "Transfert".equalsIgnoreCase(proposition.typeProposition) && !"Rachat LocFi".equalsIgnoreCase(proposition.typeProposition)) {
		stringContent.append("<p>La proposition est à l'étape transmettre au BO ou au LAC, vous pouvez prendre l'action : ");
		stringContent.append(MailContent.getUrlHomePageSmarte("ici", bonita_server_host, bonita_server_port) + "</p>");
	}
			
	stringContent.append("<p>Cette proposition peut être consultée : ");
	stringContent.append(MailContent.getUrlDependsGroup(proposition, bonita_server_host, bonita_server_port, approval) + "</p>");
	
	stringContent.append(MailContent.getContentMailTemplate());
	
	stringContent.append("<p>Cette proposition à été envoyée à : ");
	if ("SMARTE".equalsIgnoreCase(approval)) {
		stringContent.append(MailContent.getMailSMARTE(apiAccessor, proposition));
	} else {
		stringContent.append(MailContent.getMailSendTo(listMailSendTo, apiAccessor, proposition));
	}

	return stringContent;
}


public static String sendVenteTerminee(Proposition proposition, String bonita_server_host, String bonita_server_port, APIAccessor apiAccessor, List<String> listMailSendTo, String isFirstWF, String approval) {
	
	String typeWorkflow;
	if ("true".equalsIgnoreCase(isFirstWF)) {
		typeWorkflow = proposition.typeWorkflow;
	} else {
		typeWorkflow = proposition.typeWorkflowTargetAgency;
	}
	
	StringBuilder stringContent =  new StringBuilder("");
	stringContent.append("<p>Bonjour,</p>");
	stringContent.append("<p>La proposition n°");
	stringContent.append(proposition.persistenceId);
	stringContent.append(" (").append(proposition.typeProposition).append(")");
	stringContent.append(" - Vente terminée. (workflow DAV"+")</p>");
		
	stringContent.append("<p>Cette proposition peut être consultée : ");
	stringContent.append(MailContent.getUrlDependsGroup(proposition, bonita_server_host, bonita_server_port, approval) + "</p>");
	
	stringContent.append(MailContent.getContentMailTemplate());
	
	stringContent.append("<p>Cette proposition à été envoyée à : ");
	if ("SMARTE".equalsIgnoreCase(approval)) {
		stringContent.append(MailContent.getMailSMARTE(apiAccessor, proposition));
	} else {
		stringContent.append(MailContent.getMailSendTo(listMailSendTo, apiAccessor, proposition));
	}

	return stringContent;
}



public static String sendDemandeReported(Proposition proposition, String bonita_server_host, String bonita_server_port, APIAccessor apiAccessor, List<String> listMailSendTo, String decisionEtapeTransition, String approval) {
		
	StringBuilder stringContent =  new StringBuilder("");
	stringContent.append("<p>Bonjour,</p>");
	stringContent.append("<p>La proposition n°");
	stringContent.append(proposition.persistenceId);
	stringContent.append(" (").append(proposition.typeProposition).append(")");
	stringContent.append(" " + MailContent.getStringMotifProposition(decisionEtapeTransition)[0]);
	stringContent.append(" (workflow " + proposition.typeWorkflow + ")</p>");
	
	stringContent.append("<p>Motif " + MailContent.getStringMotifProposition(decisionEtapeTransition)[1] + " : " + proposition.completeInfoMessage + "</p>");
	
	if ("SMARTE".equalsIgnoreCase(approval) && !"refus".equalsIgnoreCase(decisionEtapeTransition)) {
		stringContent.append("<p>L'action peut être effectuée : ");
		stringContent.append(MailContent.getUrlHomePageSmarte("ici", bonita_server_host, bonita_server_port) + "</p>");
	}
	
	stringContent.append("<p>Cette proposition peut être consultée : ");
	stringContent.append(MailContent.getUrlDependsGroup(proposition, bonita_server_host, bonita_server_port, approval) + "</p>");
	
	stringContent.append(MailContent.getContentMailTemplate());
	
	stringContent.append("<p>Cette proposition à été envoyée à : ");
	if ("SMARTE".equalsIgnoreCase(approval)) {
		stringContent.append(MailContent.getMailSMARTE(apiAccessor, proposition));
	} else {
		stringContent.append(MailContent.getMailSendTo(listMailSendTo, apiAccessor, proposition));
	}

	return stringContent;
}

public static String sendIsIntraEnded(Proposition proposition, String bonita_server_host,
		String bonita_server_port, APIAccessor apiAccessor, List<String> listMailSendTo, String approval, String BoOrLac) {
			
	StringBuilder stringContent =  new StringBuilder("");
	stringContent.append("<p>Bonjour,</p>");
	stringContent.append("<p>La proposition n°");
	stringContent.append(proposition.persistenceId);
	stringContent.append(" (").append(proposition.typeProposition).append(")");
	// CFR 30/11/2020 - Lot2 Sprint2 UAT : removed 'a fini en mode Intra'
	//stringContent.append(" a fini en mode Intra. (workflow " + proposition.typeWorkflow + ")</p>");
	stringContent.append(" est terminée (Transfert intra-entité).</p>");
	
	// CFR 18/11/2020 BPM-459 : Add comment from SMARTE
	def String smarte_comment = proposition.getCommentBOTransmettreBO();
	stringContent.append(addComment(smarte_comment, BoOrLac));
			
	stringContent.append("<p>Cette proposition peut être consultée : ");
	stringContent.append(MailContent.getUrlDependsGroup(proposition, bonita_server_host, bonita_server_port, approval) + "</p>");
	
	stringContent.append(MailContent.getContentMailTemplate());
	
	stringContent.append("<p>Cette proposition à été envoyée à : ");
	if ("SMARTE".equalsIgnoreCase(approval)) {
		stringContent.append(MailContent.getMailSMARTE(apiAccessor, proposition));
	} else {
		stringContent.append(MailContent.getMailSendTo(listMailSendTo, apiAccessor, proposition));
	}

	return stringContent;
}

private static String addComment(String smarte_comment, String BoOrLac) {
	StringBuilder stringContent =  new StringBuilder("");
	
	if ((smarte_comment != null) && (! smarte_comment.isEmpty())) {
		stringContent.append("<p>Le commentaire suivant a été renseigné");
		if ((BoOrLac != null) && (! BoOrLac.isEmpty())) {
			stringContent.append(" pour ");
			stringContent.append(BoOrLac);
		}
		stringContent.append(' :<BR>');
		stringContent.append(smarte_comment);
	}
	
	return stringContent.toString();
}


public static String sendDemandeToBOAcquisition(Proposition proposition, String bonita_server_host, String bonita_server_port, APIAccessor apiAccessor, String isFirstWF, long activityInstanceIdt, String sendMailTo) {
	
	StringBuilder stringContent =  new StringBuilder("");
	stringContent.append("<p>Bonjour,</p>");
	stringContent.append("<p>La proposition n°");
	stringContent.append(proposition.persistenceId);
	stringContent.append(" (").append(proposition.typeProposition).append(")");
	stringContent.append(" vous a été transmise. (workflow DAD)</p>");
			
	// CFR 09/12/2020 BPM-477 : Add comment from SMARTE
	def String smarte_comment = proposition.getCommentBOTransmettreBO();
	stringContent.append(addComment(smarte_comment, "BO"));

	stringContent.append("<p>Une DAD est à prendre en charge BO, l'action peut être effectuée : ");
		
	stringContent.append(MailContent.getUrlBOForm("ici", bonita_server_host, bonita_server_port,activityInstanceIdt) + "</p>");
	
	stringContent.append("<p>Egalement la liste des taches peut être consultée : ");
	
	stringContent.append(MailContent.getUrlBOHomePage("ici", bonita_server_host, bonita_server_port) + "</p>");
	
	
	stringContent.append(MailContent.getContentMailTemplate());
	
	stringContent.append("<p>Cette proposition à été envoyée à : ");
	stringContent.append(sendMailTo);

	return stringContent;
}

public static String sendDemandeToBOArestituer(Proposition proposition, String bonita_server_host, String bonita_server_port, APIAccessor apiAccessor, String isFirstWF, long activityInstanceIdt, String sendMailTo) {
	
	StringBuilder stringContent =  new StringBuilder("");
	stringContent.append("<p>Bonjour,</p>");
	stringContent.append("<p>La demande de restitution (proposition n°");
	stringContent.append(proposition.persistenceId);
	stringContent.append(")");
	stringContent.append(" a été validée.</p>");
			
	// CFR 09/12/2020 BPM-477 : Add comment from SMARTE
	//def String smarte_comment = proposition.getCommentBOTransmettreBO();
	//stringContent.append(addComment(smarte_comment, "BO"));

	stringContent.append("<p>Cette proposition peut être consultée : ");
	stringContent.append(MailContent.getUrlDisplayPropositionDetailApp(proposition, "ici", bonita_server_host, bonita_server_port) + "</p>");
		
	
	
	stringContent.append(MailContent.getContentMailTemplate());
	
	stringContent.append("<p>Cette proposition à été envoyée à : ");
	stringContent.append(sendMailTo);

	return stringContent;
}


public static String sendDemandeToLACAcquisition(Proposition proposition, String bonita_server_host, String bonita_server_port, APIAccessor apiAccessor, String isFirstWF, long activityInstanceIdt, String sendMailTo) {
	
	StringBuilder stringContent =  new StringBuilder("");
	stringContent.append("<p>Bonjour,</p>");
	stringContent.append("<p>La proposition n°");
	stringContent.append(proposition.persistenceId);
	stringContent.append(" (").append(proposition.typeProposition).append(")");
	stringContent.append(" vous a été transmise. (workflow DAD)</p>");
			
	// CFR 09/12/2020 BPM-477 : Add comment from SMARTE
	def String smarte_comment = proposition.getCommentBOTransmettreBO();
	stringContent.append(addComment(smarte_comment, "LAC"));
	
	stringContent.append("<p>Une DAD est à prendre en charge LAC, l'action peut être effectuée : ");
		
	stringContent.append(MailContent.getUrlLACForm("ici", bonita_server_host, bonita_server_port,activityInstanceIdt) + "</p>");
	
	stringContent.append("<p>Egalement la liste des taches peut être consultée : ");
	
	stringContent.append(MailContent.getUrlLACHomePage("ici", bonita_server_host, bonita_server_port) + "</p>");
	
	
	stringContent.append(MailContent.getContentMailTemplate());
	
	stringContent.append("<p>Cette proposition à été envoyée à : ");
	stringContent.append(sendMailTo);

	return stringContent;
}

public static String sendDemandeToLACArestituer(Proposition proposition, String bonita_server_host, String bonita_server_port, APIAccessor apiAccessor, String isFirstWF, long activityInstanceIdt, String sendMailTo) {
	
	StringBuilder stringContent =  new StringBuilder("");
	stringContent.append("<p>Bonjour,</p>");
	stringContent.append("<p>La demande de restitution (proposition n°");
	stringContent.append(proposition.persistenceId);
	stringContent.append(") ");
	stringContent.append(" a été validée.</p>");
			
	// CFR 09/12/2020 BPM-477 : Add comment from SMARTE
	//def String smarte_comment = proposition.getCommentBOTransmettreBO();
	//stringContent.append(addComment(smarte_comment, "LAC"));
	
	stringContent.append("<p>Cette proposition peut être consultée : ");
	stringContent.append(MailContent.getUrlDisplayPropositionDetailApp(proposition, "ici", bonita_server_host, bonita_server_port) + "</p>");
	
	
	stringContent.append(MailContent.getContentMailTemplate());
	
	stringContent.append("<p>Cette proposition à été envoyée à : ");
	stringContent.append(sendMailTo);

	return stringContent;
}

// CFR 14/10/2020 - BPM-384 : New email notification for motif 'A prolonger'
public static String sendDemandeProlongationToBoOrLAC(Proposition proposition, String bonita_server_host, String bonita_server_port, APIAccessor apiAccessor, String isFirstWF, long activityInstanceIdt, String sendMailTo, boolean is_lac) {
	
	StringBuilder stringContent =  new StringBuilder("");
	stringContent.append("<p>Bonjour,</p>");
	stringContent.append("<p>La proposition n°");
	stringContent.append(proposition.persistenceId);
	stringContent.append(" (").append(proposition.typeProposition).append(")");
	stringContent.append(" vous a été transmise. (workflow DAD)</p>");
	
	// CFR 09/12/2020 BPM-477 : Add comment from SMARTE
	def String smarte_comment = proposition.getCommentBOTransmettreBO();
	def String bo_or_lac = "BO";
	if (is_lac) {
		bo_or_lac = "LAC";
	}
	stringContent.append(addComment(smarte_comment, bo_or_lac));	
	stringContent.append("<p>Une prolongation de contrat est à effectuer au ").append(bo_or_lac).append(", l'action peut être effectuée : ");
	
		
	stringContent.append(MailContent.getUrlLACForm("ici", bonita_server_host, bonita_server_port,activityInstanceIdt) + "</p>");
	
	stringContent.append("<p>Egalement la liste des taches peut être consultée : ");
	
	stringContent.append(MailContent.getUrlLACHomePage("ici", bonita_server_host, bonita_server_port) + "</p>");
	
	
	stringContent.append(MailContent.getContentMailTemplate());
	
	stringContent.append("<p>Cette proposition à été envoyée à : ");
	stringContent.append(sendMailTo);

	return stringContent;
}


public static String sendDemandeToBOAcquisitionTEST(String bonita_server_host, String bonita_server_port, APIAccessor apiAccessor, long activityInstanceIdt, String sendMailTo) {
	
	StringBuilder stringContent =  new StringBuilder("");
	stringContent.append("<p>Bonjour,</p>");
	stringContent.append("<p>La proposition n° test");
	stringContent.append(" vous a été transmise. (workflow DAD)</p>");
			
	stringContent.append("<p>Une DAD est à prendre en charge BO, l'action peut être effectuée : ");
	stringContent.append(MailContent.getUrlBOForm("ici", bonita_server_host, bonita_server_port,activityInstanceIdt) + "</p>");
	stringContent.append("<p>Cette proposition à été envoyée à : ");
	stringContent.append(sendMailTo);

	return stringContent;
}

public static String sendDemandeToBOTransfert(Proposition proposition, String bonita_server_host, String bonita_server_port, APIAccessor apiAccessor, String isFirstWF, long activityInstanceIdt, String sendMailTo) {
	
	StringBuilder stringContent =  new StringBuilder("");
	stringContent.append("<p>Bonjour,</p>");
	stringContent.append("<p>La proposition n°");
	stringContent.append(proposition.persistenceId);
	stringContent.append(" (").append(proposition.typeProposition).append(")");
	stringContent.append(" vous a été transmise. (workflow DAD)</p>");
			
	// CFR 09/12/2020 BPM-477 : Add comment from SMARTE
	def String smarte_comment = proposition.getCommentBOTransmettreBO();
	stringContent.append(addComment(smarte_comment, "BO"));

	stringContent.append("<p>Un transfert à débuté, une action de 'Relogotage' est en attente<br/>L'action peut être effectuée : ");
	
	stringContent.append(MailContent.getUrlBOForm("ici", bonita_server_host, bonita_server_port,activityInstanceIdt) + "</p>");
	
	stringContent.append("<p>Egalement la liste des taches peut être consultée : ");
	
	
	stringContent.append(MailContent.getUrlBOHomePage("ici", bonita_server_host, bonita_server_port) + "</p>");
	
	stringContent.append(MailContent.getContentMailTemplate());
	
	stringContent.append("<p>Cette proposition à été envoyée à : ");
	stringContent.append(sendMailTo);

	return stringContent;
}

public static String sendCopyToBOTransfert(Proposition proposition, String bonita_server_host, String bonita_server_port, String sendMailTo) {
	
	StringBuilder stringContent =  new StringBuilder("");
	stringContent.append("<p>Bonjour,</p>");
	stringContent.append("<p>La proposition n°");
	stringContent.append(proposition.persistenceId);
	stringContent.append(" (Transfert)");
	stringContent.append(" est en cours au BO.</p>");
			
	stringContent.append("<p>Cette proposition peut être consultée : ");
	stringContent.append(MailContent.getUrlDependsGroup(proposition, bonita_server_host, bonita_server_port, "") + "</p>");
	
	stringContent.append(MailContent.getContentMailTemplate());
	
	stringContent.append("<p>Cette proposition à été envoyée à : ");
	stringContent.append(sendMailTo);

	return stringContent;
}

public static String sendDemandeToLacTransfert(Proposition proposition, String bonita_server_host, String bonita_server_port, APIAccessor apiAccessor, String isFirstWF, long activityInstanceIdt, String sendMailTo) {
	
	StringBuilder stringContent =  new StringBuilder("");
	stringContent.append("<p>Bonjour,</p>");
	stringContent.append("<p>La proposition n°");
	stringContent.append(proposition.persistenceId);
	stringContent.append(" (").append(proposition.typeProposition).append(")");
	stringContent.append(" vous a été transmise. (workflow DAD)</p>");

	// CFR 09/12/2020 BPM-477 : Add comment from SMARTE
	def String smarte_comment = proposition.getCommentBOTransmettreBO();
	stringContent.append(addComment(smarte_comment, "LAC"));

	stringContent.append("<p>Un transfert à débuté, une action de 'Relogotage' est en attente<br/>L'action peut être effectuée : ");
	
	stringContent.append(MailContent.getUrlLACForm("ici", bonita_server_host, bonita_server_port,activityInstanceIdt) + "</p>");
	
	stringContent.append("<p>Egalement la liste des taches peut être consultée : ");
	
	
	stringContent.append(MailContent.getUrlLACHomePage("ici", bonita_server_host, bonita_server_port) + "</p>");
	
	stringContent.append(MailContent.getContentMailTemplate());
	
	stringContent.append("<p>Cette proposition à été envoyée à : ");
	stringContent.append(sendMailTo);

	return stringContent;
}

public static String sendDemandeToGEEMCOVente(Proposition proposition, String bonita_server_host, String bonita_server_port, APIAccessor apiAccessor, String isFirstWF) {
	
	StringBuilder stringContent =  new StringBuilder("");
	stringContent.append("<p>Bonjour,</p>");
	stringContent.append("<p>La proposition n°");
	stringContent.append(proposition.persistenceId);
	stringContent.append(" (").append(proposition.typeProposition).append(")");
	stringContent.append(" vous a été transmise. (workflow DAV)</p>");
			
	stringContent.append("<p>Une vente a débuté, des validations sont demandées.<br/>L'action peut être effectuée : ");
	stringContent.append(MailContent.getUrlGEEMCOHomePage("ici", bonita_server_host, bonita_server_port) + "</p>");
	
	stringContent.append(MailContent.getContentMailTemplate());
	
	stringContent.append("<p>Cette proposition à été envoyée à : ");
	stringContent.append("GEEMCOpop3p@saint-gobain.com");

	return stringContent;
}


public static String sendDemandeReparationAccepted(Proposition proposition, String bonita_server_host, String bonita_server_port, APIAccessor apiAccessor, List<String> listMailSendTo, String isFirstWF, String approval) {
	
	StringBuilder stringContent =  new StringBuilder("");
	stringContent.append("<p>Bonjour,</p>");
	stringContent.append("<p>La demande de réparation n°");
	stringContent.append(proposition.persistenceId);
	stringContent.append(" (").append(proposition.typeProposition).append(")");
	stringContent.append(" a été validée. (workflow " + proposition.typeWorkflow + ")</p>");
				
	stringContent.append("<p>Cette demande de réparation peut être consultée : ");
	stringContent.append(MailContent.getUrlDependsGroup(proposition, bonita_server_host, bonita_server_port, approval) + "</p>");
	
	stringContent.append(MailContent.getContentMailTemplate());
	
	stringContent.append("<p>Cette demande de réparation à été envoyée à : ");
	
	stringContent.append("savpop3P@saint-gobain.com, "+MailContent.getMailSendTo(listMailSendTo, apiAccessor, proposition));
	
	return stringContent;
	
}


public static String sendDemandeReparationStartValidation(Proposition proposition, String bonita_server_host, String bonita_server_port, APIAccessor apiAccessor, List<String> listMailSendTo, String isFirstWF, String approval) {
	
	StringBuilder stringContent =  new StringBuilder("");
	stringContent.append("<p>Bonjour,</p>");
	stringContent.append("<p>Une demande de réparation a été créée, elle porte le numéro n°");
	stringContent.append(proposition.persistenceId);
	stringContent.append(" (").append(proposition.typeProposition).append(")");
	
	stringContent.append("<p>Cette demande de réparation peut être consultée : ");
	stringContent.append(MailContent.getUrlDependsGroup(proposition, bonita_server_host, bonita_server_port, approval) + "</p>");
	
	stringContent.append(MailContent.getContentMailTemplate());
	
	stringContent.append("<p>Cette demande de réparation à été envoyée à : ");
	
	stringContent.append(MailContent.getMailSendTo(listMailSendTo, apiAccessor, proposition));
	
	return stringContent;
}


public static String sendDemandeReparationRefused(Proposition proposition, String bonita_server_host, String bonita_server_port, APIAccessor apiAccessor, List<String> listMailSendTo, String isFirstWF, String approval) {
	
	StringBuilder stringContent =  new StringBuilder("");
	stringContent.append("<p>Bonjour,</p>");
	stringContent.append("<p>La demande de réparation n°");
	stringContent.append(proposition.persistenceId);
	stringContent.append(" (").append(proposition.typeProposition).append(")");
	stringContent.append(" a été refusée. (workflow " + proposition.typeWorkflow + ")</p>");
				
	stringContent.append("<p>Cette demande de réparation peut être consultée : ");
	stringContent.append(MailContent.getUrlDependsGroup(proposition, bonita_server_host, bonita_server_port, approval) + "</p>");
	
	stringContent.append(MailContent.getContentMailTemplate());
	
	stringContent.append("<p>Cette demande de réparation à été envoyée à : ");
	
	stringContent.append("savpop3P@saint-gobain.com, "+MailContent.getMailSendTo(listMailSendTo, apiAccessor, proposition));
	
	//stringContent.append("savpop3P@saint-gobain.com");
	
	return stringContent;
}


public static String noApprovers(Proposition proposition, String bonita_server_host, String bonita_server_port, APIAccessor apiAccessor, List<String> listMailSendTo, String approval) {
	StringBuilder stringContent =  new StringBuilder("");
	def propositionReason = "";
	def urlBackToBrouillon = "";
	if ("DADR".equals(proposition.typeWorkflow)) {
		propositionReason = "demande de réparation";
		urlBackToBrouillon = MailContent.getUrlDisplayPropositionSAV("ici", bonita_server_host, bonita_server_port);
		approval = "SAV";
	} else {
		propositionReason = "proposition";
		urlBackToBrouillon = MailContent.getUrlHomePageSmarte("ici", bonita_server_host, bonita_server_port);
	}

	stringContent.append("<p>Bonjour,</p>");
	stringContent.append("<p>Aucun approver n'a été trouvé pour la "+ propositionReason +" n°");
	stringContent.append(proposition.persistenceId);
	stringContent.append(" (").append(proposition.typeProposition).append(")");
	stringContent.append("</p>");

	stringContent.append("<p>Cette demande est retournée à l'état de brouillon : ");
	stringContent.append(urlBackToBrouillon + "</p>");
	
	stringContent.append(MailContent.getContentMailTemplate());
	
	stringContent.append("<p>Cette " + propositionReason + " à été envoyée à : ");	
	stringContent.append(MailContent.getMailSendTo(listMailSendTo, apiAccessor, proposition));
	
	return stringContent;

	
}

// CFR 20/11/2020 BPM-461 : New method for task notification on process POP3P_DDDRenouvellement
public static String sendLaunchDddRenew(Proposition proposition, String bonita_server_host, String bonita_server_port, APIAccessor apiAccessor, List<String> listMailSendTo) {
	
	StringBuilder stringContent =  new StringBuilder("");
	stringContent.append("<p>Bonjour,</p>");
	stringContent.append("<p>La DDD de renouvellement de la proposition n°");
	stringContent.append(proposition.persistenceId);
	stringContent.append(" doit être lancée.</p>");
	
	stringContent.append("<p>Vous pouvez prendre l'action : ");
	stringContent.append(MailContent.getUrlHomePageSmarte("ici", bonita_server_host, bonita_server_port) + "</p>");
			
	stringContent.append("<p>Cette proposition peut être consultée : ");
	stringContent.append(MailContent.getUrlDependsGroup(proposition, bonita_server_host, bonita_server_port, "SMARTE") + "</p>");
	
	stringContent.append(MailContent.getContentMailTemplate());
	
	stringContent.append("<p>Cette proposition à été envoyée à : ");
	stringContent.append(MailContent.getMailSendTo(listMailSendTo, apiAccessor, proposition));
	
	return stringContent;
}

