import com.sgdbf.model.ApproverAction
import com.sgdbf.model.Proposition;
import com.bonitasoft.engine.api.APIAccessor;
import java.util.List;
import java.util.logging.Logger;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.platform.UnknownUserException
import com.sgdbf.pop3p.groovy.MailRecipient;

public static String getUrlImage() {
	 return "https://uat.bpm.saint-gobain.com/bonita/images/POP3Pimage.png";
}

public static String getUrlIntranet() {
	return "https://portal.saint-gobain.com/web/pop3p";
}


public static String getObjectMail_DDD_DAD(String typeWorkflow, Proposition proposition, Boolean displayCopy, String isFirstWF) {
	String mentionFacultative = "";
	def creationDate = proposition.creationDate.toString().substring(0, 10);
	
	String status;
	if (isFirstWF.equals("true")) {
		status = proposition.propositionStatus;
	} else {
		status = proposition.propositionStatusTargetAgency;
	}

	String objectMail = "POP3P/BudgéPOP - "+typeWorkflow+ " (" + proposition.typeProposition + ") - " + status + " - " + proposition.entiteCode + " " + proposition.entiteName
	objectMail += " - " + proposition.agencyCode + " " + proposition.agencyName + " - n°" + proposition.persistenceId.toString() + " - " + proposition.creatorName + " - " + creationDate;

	return objectMail;
}


public static String getObjectMailAvantTransmission(Proposition proposition, Boolean displayCopy, String isFirstWF) {
	String objectMail = "";
	String mentionFacultative = "";
	String typePropo="";
	String typeWorkflow;
	if ("true".equalsIgnoreCase(isFirstWF)) {
		typeWorkflow = proposition.typeWorkflow;
	} else {
		typeWorkflow = proposition.typeWorkflowTargetAgency;
	}
	def creationDate = proposition.creationDate.toString().substring(0, 10);
	
	if (displayCopy) {
		mentionFacultative = "Copie pour information : ";
	}
	
	String status;
	if (isFirstWF.equals("true")) {
		status = proposition.propositionStatus;
	} else {
		status = proposition.propositionStatusTargetAgency;
	}
	
	
	if(status.contains("TRANSITION")){
		typePropo = "- "+proposition.getTypeWorkflow()+ " (" + proposition.typeProposition + ") - ";
	} else {
		typePropo = "(" + proposition.typeProposition + ") - ";
	}
	
	if ("DAD".equalsIgnoreCase(typeWorkflow) && ! "Transfert".equalsIgnoreCase(proposition.typeProposition) && !"Rachat LocFi".equalsIgnoreCase(proposition.typeProposition)) {
		objectMail = "POP3P/BudgéPOP "+typeWorkflow+ " (" + proposition.typeProposition + ") - en attente de transmission  BO ou LAC"+ " - " + proposition.entiteCode + " " + proposition.entiteName 
	    objectMail += " - " + proposition.agencyCode + " " + proposition.agencyName + " - n°" + proposition.persistenceId.toString() + " - " + proposition.creatorName + " - " + creationDate;
		}
	else
	{
		objectMail = "POP3P/BudgéPOP "+typePropo+ mentionFacultative + "" + status + " - " + proposition.entiteCode + " " + proposition.entiteName
		objectMail += " - " + proposition.agencyCode + " " + proposition.agencyName + " - n°" + proposition.persistenceId.toString() + " - " + proposition.creatorName + " - " + creationDate;
		
	}
	
	return objectMail;
}

public static String getObjectMail(Proposition proposition, Boolean displayCopy, String isFirstWF) {
	String objectMail = "";
	String mentionFacultative = "";
	String typePropo="";
	def creationDate = proposition.creationDate.toString().substring(0, 10);
	
	if (displayCopy) {
		mentionFacultative = "Copie pour information : ";
	}
	
	String status;
	// CFR 30/11/2020 è Use first status if null
	if ((isFirstWF == null) || "null".equals(isFirstWF) || isFirstWF.equals("true")) {
		status = proposition.propositionStatus;
	} else {
		status = proposition.propositionStatusTargetAgency;
	}
	
	
	if(status.contains("TRANSITION")){
		typePropo = "- "+proposition.getTypeWorkflow()+ " (" + proposition.typeProposition + ") - ";
	} else {
		typePropo = "(" + proposition.typeProposition + ") - ";
	}

		objectMail = "POP3P/BudgéPOP "+typePropo+ mentionFacultative + "" + status + " - " + proposition.entiteCode + " " + proposition.entiteName
		objectMail += " - " + proposition.agencyCode + " " + proposition.agencyName + " - n°" + proposition.persistenceId.toString() + " - " + proposition.creatorName + " - " + creationDate;
		
	
	
	return objectMail;
}

public static String getObjectMailRefusSmarte(Proposition proposition, Boolean displayCopy) {
	String objectMail = "";
	String mentionFacultative = "";
	def creationDate = proposition.creationDate.toString().substring(0, 10);
	
	if (displayCopy) {
		mentionFacultative = "Copie pour information : ";
	}
	String typePropo = "(" + proposition.typeProposition + ") - ";
	
	String status = proposition.typeProposition + " abandonné SMARTE";

		objectMail = "POP3P/BudgéPOP " + typePropo + mentionFacultative + "" + status + " - " + proposition.entiteCode + " " + proposition.entiteName
		objectMail += " - " + proposition.agencyCode + " " + proposition.agencyName + " - n°" + proposition.persistenceId.toString() + " - " + proposition.creatorName + " - " + creationDate;
		
	
	
	return objectMail;
}

// BPM-557 - Added by Amit on 1/3/2021
public static String getObjectCopyMailTransfertEntityBO(Proposition proposition, Boolean displayCopy, String isFirstWF) {
	String mentionFacultative = "";
	String typePropo="";
	def creationDate = proposition.creationDate.toString().substring(0, 10);
	
	if (displayCopy) {
		mentionFacultative = "Copie pour information : ";
	}
	typePropo = "(" + proposition.typeProposition + ") - ";
	
	String objectMail = "POP3P/BudgéPOP " + typePropo + mentionFacultative + " Transfert inter-entité en cours" + " - " + proposition.entiteCode + " " + proposition.entiteName
	objectMail += " - " + proposition.agencyCode + " " + proposition.agencyName + " - n°" + proposition.persistenceId.toString() + " - " + proposition.creatorName + " - " + creationDate;

	return objectMail;
}

public static String getObjectMailTransfertIntra(Proposition proposition, Boolean displayCopy, String isFirstWF) {
	String mentionFacultative = "";
	String typePropo="";
	def creationDate = proposition.creationDate.toString().substring(0, 10);
	
	if (displayCopy) {
		mentionFacultative = "Copie pour information : ";
	}
	typePropo = "(" + proposition.typeProposition + ") - ";
	
	String objectMail = "POP3P/BudgéPOP " + typePropo + mentionFacultative + " Terminée en tant que Transfert Intra-Entité" + " - " + proposition.entiteCode + " " + proposition.entiteName
	objectMail += " - " + proposition.agencyCode + " " + proposition.agencyName + " - n°" + proposition.persistenceId.toString() + " - " + proposition.creatorName + " - " + creationDate;

	return objectMail;
}

public static String getObjectMailLaunchDDDRenew(Proposition proposition) {
	def creationDate = proposition.creationDate.toString().substring(0, 10);
	
	String objectMail = "POP3P/BudgéPOP "+ "DDD" + " (" + proposition.typeProposition + ") - " + proposition.getPropositionStatusTargetAgency() + " - " + proposition.entiteCode + " " + proposition.entiteName
	objectMail += " - " + proposition.agencyCode + " " + proposition.agencyName + " - n°" + proposition.persistenceId.toString() + " - " + proposition.creatorName + " - " + creationDate;

	return objectMail;
}

public static String getObjectMail_Vente(Proposition proposition, Boolean displayCopy, String isFirstWF) {
	
	def creationDate = proposition.creationDate.toString().substring(0, 10);
	String objectMail = "POP3P/BudgéPOP - DAV (" + proposition.typeProposition + ") - Copie pour information : Vente terminée "+" - " + proposition.entiteCode + " " + proposition.entiteName
	objectMail += " - " + proposition.agencyCode + " " + proposition.agencyName + " - n°" + proposition.persistenceId.toString() + " - " + proposition.creatorName + " - " + creationDate;

	return objectMail;
}

public static String getObjectMailSmarteEndNotif(Proposition proposition) {
	
	String statusTxt = ""
	
	if("Nouvel équipement".equalsIgnoreCase(proposition.typeProposition) || "Nouveau matériel".equalsIgnoreCase(proposition.typeProposition) || "A renouveler".equalsIgnoreCase(proposition.typeProposition))
	{
		statusTxt = "Acquisition terminée"
	}
	else if("A vendre".equalsIgnoreCase(proposition.typeProposition))
	{
		statusTxt = "Vente terminée"
	}
	else if("Transfert".equalsIgnoreCase(proposition.typeProposition))
	{
		statusTxt = "Transfert terminé"
	}
	else {
		statusTxt = "Opération terminée"
	}
	
	def creationDate = proposition.creationDate.toString().substring(0, 10);
	String typePropo = "(" + proposition.typeProposition + ") - ";
	
	String objectMail = "POP3P/BudgéPOP " + typePropo + "Copie pour information : "+statusTxt+" - " + proposition.entiteCode + " " + proposition.entiteName
	objectMail += " - " + proposition.agencyCode + " " + proposition.agencyName + " - n°" + proposition.persistenceId.toString() + " - " + proposition.creatorName + " - " + creationDate;

	return objectMail;
}

public static String getObjectMailSmarte(Proposition proposition) {
	
	def creationDate = proposition.creationDate.toString().substring(0, 10);
	String objectMail = "POP3P/BudgéPOP - DAV (" + proposition.typeProposition + ") - Copie pour information : Vente terminée "+" - " + proposition.entiteCode + " " + proposition.entiteName
	objectMail += " - " + proposition.agencyCode + " " + proposition.agencyName + " - n°" + proposition.persistenceId.toString() + " - " + proposition.creatorName + " - " + creationDate;

	return objectMail;
}


public static String getObjectMailLAC(Proposition proposition, String isFirstWF) {
	String mentionFacultative = "Transmission LAC";
	String typePropo=  "(" + proposition.typeProposition + ") - ";
	def creationDate = proposition.creationDate.toString().substring(0, 10);
	

	
	String status;
	if (isFirstWF.equals("true")) {
		status = proposition.propositionStatus;
	} else {
		status = proposition.propositionStatusTargetAgency;
	}
	
	String objectMail = "POP3P/BudgéPOP " + typePropo + mentionFacultative + " - " + status + " - " + proposition.entiteCode + " " + proposition.entiteName
	objectMail += " - " + proposition.agencyCode + " " + proposition.agencyName + " - n°" + proposition.persistenceId.toString() + " - " + proposition.creatorName + " - " + creationDate;

	return objectMail;
}

	
public static String getMailSMARTE (APIAccessor apiAccessor, Proposition proposition) {
	return BonitaUsers.getUserProfessionalContactInfo(apiAccessor,proposition.creatorId).email;
}

public static String getMailSendTo (List<String> listMails, APIAccessor apiAccessor, Proposition proposition) {
	String sendTo = ""
	if(listMails && listMails.size() > 0) {
		for (String mail : listMails) {
			sendTo += mail + ", ";
		}
		sendTo = sendTo.substring(0, sendTo.length()-2);
	}
	
	return sendTo;
}	

/**
  *  Return the url of the POP3P Image surrounded by src HTML tag
 **/                 
public static String getImagePop3P () {
	return "<p><img src='" + MailContent.getUrlImage() + "' alt='POP3P'></p>";
}

/**
 * Return the url of the Intranet Portal of Saint Gobain surrounded by src HTML tag
 */
public static String getIntranet () {
	return "<a href='" + MailContent.getUrlIntranet() + "'>Intranet</a>";
}

/**
  * Return the url of the proposition displayed on the "searchPropositions" page
 **/
public static String getUrlDisplayProposition(String urlBase, Proposition proposition, String labelLink, String bonita_server_host, String bonita_server_port) {
	return "<a href='" + getUrlBase(bonita_server_host, bonita_server_port) + urlBase + "/searchPropositions/?idProposition=" + proposition.persistenceId + "'>" + labelLink + "</a>";
}


/**
  * Return the url of the proposition displayed on the "searchPropositions" page for a Approvers
 **/
public static String getUrlDisplayPropositionApprovers(Proposition proposition,  String labelLink, String bonita_server_host, String bonita_server_port) {
	return MailContent.getUrlDisplayProposition("/bonita/apps/approvers", proposition, labelLink, bonita_server_host, bonita_server_port);
}

/**
  * Return the url of the proposition displayed on the "searchPropositions" page for a SMARTE
 **/
public static String getUrlDisplayPropositionSMARTE(Proposition proposition, String labelLink, String bonita_server_host, String bonita_server_port) {
	return MailContent.getUrlDisplayProposition("/bonita/apps/pop3p", proposition, labelLink, bonita_server_host, bonita_server_port);
}
	
/**
 * Return the url of the home page for a SMARTE
**/
public static String getUrlHomePageSmarte(String labelLink, String bonita_server_host, String bonita_server_port) {
	return ("<a href='" + getUrlBase(bonita_server_host, bonita_server_port) + "/bonita/apps/pop3p/homePage'>" + labelLink + "</a>");
}

public static String getUrlLACHomePage(String labelLink, String bonita_server_host, String bonita_server_port) {
	return ("<a href='" + getUrlBase(bonita_server_host, bonita_server_port) + "/bonita/apps/lac/homePageLac'>" + labelLink + "</a>");
}

/**
 * Return the url of the home page for a Approver
**/
public static String getUrlApproverValidate(String labelLink, String bonita_server_host, String bonita_server_port) {
	return ("<a href='" + getUrlBase(bonita_server_host, bonita_server_port) + "/bonita/apps/approvers/validatePropositions'>" + labelLink + "</a>");
}

/**
 * Return the url of the home page for a BO
**/
public static String getUrlBOHomePage (String labelLink, String bonita_server_host, String bonita_server_port) {
	return ("<a href='" + getUrlBase(bonita_server_host, bonita_server_port) + "/bonita/apps/BO/homePage'>" + labelLink + "</a>");
}

public static String getUrlBOForm (String labelLink, String bonita_server_host, String bonita_server_port, long activityInstanceIdt) {
	
	return ("<a href='" + getUrlBase(bonita_server_host, bonita_server_port) + "/bonita/portal/form/taskInstance/"+activityInstanceIdt+"'>" +labelLink+ "</a>");
}

public static String getUrlLACForm (String labelLink, String bonita_server_host, String bonita_server_port, long activityInstanceIdt) {
	
	return ("<a href='" + getUrlBase(bonita_server_host, bonita_server_port) + "/bonita/portal/form/taskInstance/"+activityInstanceIdt+"'>" +labelLink+ "</a>");
}

public static String getUrlValidateForm (String labelLink, String bonita_server_host, String bonita_server_port, long activityInstanceIdt) {
	
	return ("<a href='" + getUrlBase(bonita_server_host, bonita_server_port) + "/bonita/portal/form/taskInstance/"+activityInstanceIdt+"'>" +labelLink+ "</a>");
	
	
}

public static String getUrlGEEMCOHomePage(String labelLink, String bonita_server_host, String bonita_server_port) {
	return ("<a href='" + getUrlBase(bonita_server_host, bonita_server_port) + "/bonita/apps/geemco/ApproveGEEMCOHumanTasks/'>" + labelLink + "</a>");
}

public static String getUrlDisplayPropositionSAV(String labelLink, String bonita_server_host, String bonita_server_port) {
	return ("<a href='" + getUrlBase(bonita_server_host, bonita_server_port) + "/bonita/apps/SAV/'>" + labelLink + "</a>");
}





/**
  * Return the url of the proposition displayed on the "searchPropositions" page
 **/
public static String getUrlDisplayPropositionDetailApp(Proposition proposition, String labelLink, String bonita_server_host, String bonita_server_port) {
	return ("<a href='" + getUrlBase(bonita_server_host, bonita_server_port) + "/bonita/apps/displayDetail/detailPropositionPage/?propositionId=" + proposition.persistenceId + "'>" + labelLink + "</a>");
}


/**
 * Return the beginning of url
**/
public static String getUrlBase(String bonita_server_host, String bonita_server_port) {
	return "https://" + bonita_server_host + ":" + bonita_server_port;
}

/**
 * Return the end of the mail
**/
public static String getContentMailTemplate() {
	
	String mail;
	mail = "<p>Nouveau ! Retrouvez-nous à présent sur l'" + getIntranet() + " Saint Gobain</p>" 
	mail += getImagePop3P() + "<i>Ce mail a été envoyé par un robot, merci de ne pas y répondre.</i>";

	return mail;
}

/**
 * 
 * Return the url depends on the approval group
 * This function has been deactivated since the client want to redirect all the url to the "displayDetail" apps
 */
public static String getUrlDependsGroup(Proposition proposition, String bonita_server_host, String bonita_server_port, String approval) {
	String url ="";
	url = MailContent.getUrlDisplayPropositionDetailApp(proposition, "ici", bonita_server_host, bonita_server_port);
	
//	switch (approval) {
//		case "SMARTE" : 
//			url = MailContent.getUrlDisplayPropositionSMARTE(proposition, "ici", bonita_server_host, bonita_server_port);
//			break;
//		case "approver" : 
//			url = MailContent.getUrlDisplayPropositionApprovers(proposition, "ici", bonita_server_host, bonita_server_port);
//			break;
//		case "copy" : 
//			url = MailContent.getUrlDisplayPropositionDetailApp(proposition, "ici", bonita_server_host, bonita_server_port);
//			break;
//		case "SAV" : 
//			url = MailContent.getUrlDisplayPropositionSAV(proposition, "ici", bonita_server_host, bonita_server_port);
//			break;
//	}
			
	return url;
}


public static String[] getStringMotifProposition(String decisionEtapeTransition) {
	String[] dataToReturn = new String[2];
	switch (decisionEtapeTransition) {
		case "reportAN1" :
			dataToReturn[0] = "a été reportée à l'année suivante.";
			dataToReturn[1] = "du report";
			break;
		case "revision" :
			dataToReturn[0] = "doit être révisée.";
			dataToReturn[1] = "de la révision";
			break;
		case "refus" : 
			dataToReturn[0] = "a été refusée.";
			dataToReturn[1] = "du refus";
			break;
	}
	return dataToReturn;
}


public static HashSet<String> getEmailsFromListApprovers(APIAccessor apiAccessor, List hierarchyList) {
	Logger logger = Logger.getLogger("org.bonitasoft");
	
	HashSet listOfApproversMail = new HashSet();
	for (def approvers in hierarchyList) {
		for (def approverUserName in approvers.listApprobs) {
			try {
				// CFR 09/10/2020 - BPM-386 : Use new MailRecipient class to retrieve user email - code was :
				// User user = apiAccessor.getIdentityAPI().getUserByUserName(approverUserName)
				// listOfApproversMail.add(BonitaUsers.getUserProfessionalContactInfo(apiAccessor,user.getId()).email)
				def String user_email = MailRecipient.getEmailFromUserName(approverUserName, apiAccessor);
				if (!user_email.isEmpty()) {
					listOfApproversMail.add(user_email);
				}
			} catch (Exception e) {
				logger.warning("Mail - The user " + approverUserName + " does not exist in Bonita");
			}
		}
	}
	return listOfApproversMail;
}

public static HashSet<String> getEmailsFromListCopy(APIAccessor apiAccessor, List mailCopy) {
	HashSet<String> setResult = new HashSet();

	if (mailCopy && mailCopy.size()>0) {
		for (def copy in mailCopy) {
			if(copy["userMail"]!="null" && copy["userMail"]!="") {
			setResult.add(copy["userMail"]);
			}
		}
	}	
	return setResult;
}


public static String convertHashSetToString(HashSet<String> setResult) {
	Logger logger = Logger.getLogger("org.bonitasoft");
	
	String result ="";
	if (setResult.size()>0) {
		Iterator iterator = setResult.iterator()
		while (iterator.hasNext()){
			result += iterator.next() + ",";
		}
		result = result.substring(0, result.length()-1)
	}
	return result;
}

