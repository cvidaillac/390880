/**
 * 
 */
package org.mycompany.connector;

import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.identity.User;
import org.jsoup.Jsoup;

/*import com.myvega_systems.ws.locprotns.LocproPortType;
import com.myvega_systems.ws.locprotns.LocproServiceBUDGEPOP;
import com.myvega_systems.ws.locprotns.WSBPENREGHISTO;
import com.myvega_systems.ws.locprotns.WSBPENREGHISTOResponse;*/
import com.sgdbf.model.Proposition;
import com.sgdbf.model.TimeLine;

import com.vega_systems.ws.locprotns_dec20.LocproPortType;
import com.vega_systems.ws.locprotns_dec20.LocproServiceBUDGEPOP;
import com.vega_systems.ws.locprotns_dec20.WSBPENREGHISTO;
import com.vega_systems.ws.locprotns_dec20.WSBPENREGHISTOResponse;

/**
 *The connector execution will follow the steps
 * 1 - setInputParameters() --> the connector receives input parameters values
 * 2 - validateInputParameters() --> the connector can validate input parameters values
 * 3 - connect() --> the connector can establish a connection to a remote server (if necessary)
 * 4 - executeBusinessLogic() --> execute the connector
 * 5 - getOutputParameters() --> output are retrieved from connector
 * 6 - disconnect() --> the connector can close connection to remote server (if any)
 */
public class SaveHistoryToLYREBATCHImpl extends
		AbstractSaveHistoryToLYREBATCHImpl {

	private Logger LOGGER = Logger.getLogger("org.bonitasoft");

	@Override
	protected void executeBusinessLogic() throws ConnectorException{
		//LOGGER.info("Entree connecteur SaveHistoryToLYREImpl");
		//Get access to the connector input parameters
		Proposition currentProp = getCurrentProposition();
		List<TimeLine> timeLines = currentProp.getTimeLines();
	    //TimeLine  currentTimeLine = getCurrentTimeLine();
		String urlWSDL = getUrlWSDL();
		String serviceCookie =getCookie();
		WSBPENREGHISTOResponse result;
		
		DecimalFormat df = new DecimalFormat("###.##");

		try {
			//Create the client connection
			URL urlBudgePop = new URL(urlWSDL);
			LocproServiceBUDGEPOP budgePopService = new LocproServiceBUDGEPOP(urlBudgePop);
			LocproPortType soapService = budgePopService.getLocproPort();
			
			//Prepare the message to send
			String userFirstName = null, userLastName = null, userSGID = null;
		
			
			if (currentProp != null){
				//Si la proposition est nulle, pas d'enregistrement
				//LOGGER.info("Debut traitement pour proposition "+ currentProp.getPersistenceId());
				
				//recuperation des infos du dernier TimeLine
				String statut = null, acteur = null, codeClef = null, tache = null, codeWorkflow = null, propositionStatus = null, macroStatus = null;
				if (timeLines != null && timeLines.size()>0)
				{
					for (int i=0;i<timeLines.size();i++)
					//for (TimeLine currentTimeLine : timeLines) 
					{
						TimeLine currentTimeLine = timeLines.get(i);
						if (currentTimeLine != null)
						{
							statut = currentTimeLine.getStatut();
							acteur = currentTimeLine.getActeur();
							codeClef = currentTimeLine.getCodeClef();
							tache = currentTimeLine.getTache();
							codeWorkflow = currentTimeLine.getCodeWorkflow();
							propositionStatus = currentTimeLine.getPropositionStatus(); // Added by Amit for BPM-462
							macroStatus = currentTimeLine.getStatusMacroEtapes();
							try {
							//Get User infos
								Long userId = currentTimeLine.getIdentifiantActeur();
								if (null != userId){
	
									User user= apiAccessor.getIdentityAPI().getUser(userId);
									userFirstName = user.getFirstName();
									userLastName = user.getLastName();
									userSGID = user.getUserName();
									//LOGGER.info("User details : " + userFirstName + "-"+userLastName+ "-"+ userSGID);
								}
							} catch(Exception e) {
								LOGGER.info("User not set for sending a TimeLine");
							}
							
							
						//2 StatutProposition
						String propStatus = null; 
						if(propositionStatus != null && !"".equals(propositionStatus)) // Added by Amit for BPM-462
						{
							propStatus = propositionStatus;
						}
						else 
						{
							if("DDD".equals(currentProp.getTypeWorkflow()) && "DAD".equals(currentProp.getTypeWorkflowTargetAgency()) && "DAD".equals(codeWorkflow))
							{
								propStatus = currentProp.getPropositionStatusTargetAgency();
			
							}
							else
							{
								propStatus = currentProp.getPropositionStatus();
							}
						}	
					
						//5 Exercice
						String exercice = null;
						if(currentProp.getBudgetYear() != null){
							exercice = currentProp.getBudgetYear().toString();
						}
						
						java.time.OffsetDateTime dateEve = currentTimeLine.getDateCreation();
						String month;
						String day;
						String hour;
						String minutes;
						String secondes;
						//6 DateEvenement
						//String dateEvenement = java.time.OffsetDateTime.now().toString();
		//				DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
		//				DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH);
		//				LocalDateTime date = LocalDateTime.parse(currentTimeLine.getDateCreation().toString(), inputFormatter);
		//				String dateEvenement = outputFormatter.format(date);
						 if (dateEve.getMonth().getValue()<10) {
							 month = "0"+Integer.toString(dateEve.getMonth().getValue());
						 }
						 else
						 {
							 month = Integer.toString(dateEve.getMonth().getValue());
						 }
						 
						 if (dateEve.getDayOfMonth()<10) {
							 day = "0"+Integer.toString(dateEve.getDayOfMonth());
						 }
						 else
						 {
							 day = Integer.toString(dateEve.getDayOfMonth());
						 }
						
						 if (dateEve.getHour()<10) {
							 hour = "0"+Integer.toString(dateEve.getHour());
						 }
						 else
						 {
							 hour = Integer.toString(dateEve.getHour());
						 }
						 
						 if (dateEve.getMinute()<10) {
							 minutes = "0"+Integer.toString(dateEve.getMinute());
						 }
						 else
						 {
							 minutes = Integer.toString(dateEve.getMinute());
						 }
						 
						 
						 if (dateEve.getSecond()<10) {
							 secondes = "0"+Integer.toString(dateEve.getSecond());
						 }
						 else
						 {
							 secondes = Integer.toString(dateEve.getSecond());
						 }
						 
						 
						 String dateEvenement = Integer.toString(dateEve.getYear())+"-"+month+"-"+day+" "+hour+":"+minutes+":"+secondes;
						//String dateEvenement = dateEve.toString();
						
						//String dateEvenement =  currentTimeLine.getDateCreation().toString();
						 
						 
						 //7 macroStatut
						 String macroStatusPropostion = null;
						 if(macroStatus != null)
							 macroStatusPropostion = macroStatus;
						 else {
							 if(i == timeLines.size()-1)
							 	 macroStatusPropostion = currentProp.getStatusMacroEtapes().toString();
							 else {
								 if("BUDGET".equals(codeWorkflow) || "DADR".equals(codeWorkflow))
									 macroStatusPropostion = "budgeter";
								 else
									 macroStatusPropostion = "commander";
							 }
						 }
						
						//12 Role
						//Pour l'instant ROLE n'est pas récupéré
						String role = null;
						
						
						//13 currentProp.getPersistenceId()
						String numProposition = currentProp.getPersistenceId().toString();
						
						
						//34 ExerciceInitial
						String exerciceInitial = null;
						if(currentProp.getInitialBudgetYear() != null){
							exerciceInitial =currentProp.getInitialBudgetYear().toString();
						}
						//35 budgetHorsBudget
						String budget = null;
						if (currentProp.isBudgetRespected() != null){
							budget= currentProp.isBudgetRespected().toString();
						}
						
						//37 nombreMois
						String nbMonth = null;
						if (currentProp.getNbMonth() != null){
							nbMonth = currentProp.getNbMonth().toString();
						}
						
						//38 currentProp.getPercentage()
						String pourcentage = null;
						if (currentProp.getPercentage() != null){
							pourcentage = currentProp.getPercentage().toString();
						}
						//39 currentProp.getPropositionAmount()
						String montant = null;
						if (currentProp.getPropositionAmount() != null){
							montant = df.format(currentProp.getPropositionAmount()).toString();
						}
						//40  currentProp.getMonthlyAmount()
						String montantMensuel = null;
						if ( currentProp.getMonthlyAmount() != null){
							montantMensuel =  df.format(currentProp.getMonthlyAmount()).toString();
						}
					
						//41 currentProp.getMonthlyPropositionAmount()
						String montantMensuelProposition = null;
						if (currentProp.getMonthlyPropositionAmount() != null){
							montantMensuelProposition = df.format(currentProp.getMonthlyPropositionAmount()).toString();
						}
						
						//42 Date estimee
						String dateEstimee = null;
						if (currentProp.getEstimatedDeliveryDate() != null){
							dateEstimee = currentProp.getEstimatedDeliveryDate().toString();
						}
						
						//43 PitchSmart
						String pitchSmart = null;
						if(currentProp.getPitchSmart()!=null && currentProp.getPitchSmart()!="")
						{
							pitchSmart = Jsoup.parse(currentProp.getPitchSmart()).text();
							
						}
						else{
								pitchSmart = currentProp.getPitchSmart();
						}
						
						//BPM-474 : New field actualRequestAmount
						String valeur_demande = null;
						if (currentProp.getActualRequestAmount() != null){
							valeur_demande = currentProp.getActualRequestAmount().toString();
						}
						
						//45 sourceFlag
						String reqSource = null;
						if(currentProp.getRequestSource()!=null && !"".equals(currentProp.getRequestSource()))
						{
							reqSource = currentProp.getRequestSource().toString();
							
						}
						else{
								if("Sinistre".equals(currentProp.getTypeProposition()) || "Curatif".equals(currentProp.getTypeProposition()) || "CTRL réglementaire".equals(currentProp.getTypeProposition()) || "Autres".equals(currentProp.getTypeProposition()))
								{
									reqSource = "S";
								}
								else {
									reqSource = "B";
								}
						}
						
						//Save History to LYRE
						
						/*WSBPENREGHISTO wsbpSaveHistory = new WSBPENREGHISTO();
						wsbpSaveHistory.setCOOKIEF903KY(serviceCookie);
						wsbpSaveHistory.setStatut(statut);
						//wsbpSaveHistory.setStatutProposition(currentProp.getPropositionStatus());
						//Added by Amit for BPM-285
						if("DDD".equals(currentProp.getTypeWorkflow()) && "DAD".equals(currentProp.getTypeWorkflowTargetAgency()) && "DAD".equals(codeWorkflow))
						{
							wsbpSaveHistory.setStatutProposition(currentProp.getPropositionStatusTargetAgency());
						}
						else
						{
							wsbpSaveHistory.setStatutProposition(currentProp.getPropositionStatus());
						}
						
						//LOGGER.warning("7:"+currentProp.getPropositionStatusTargetAgency()+" :"+currentProp.getPropositionStatus());
						wsbpSaveHistory.setActeur(acteur);
						wsbpSaveHistory.setCodeClef(codeClef);
						wsbpSaveHistory.setExercice(exercice);
						//LOGGER.warning("8:"+acteur+":"+codeClef+":"+exercice);
						wsbpSaveHistory.setDateEvenement(dateEvenement);
						wsbpSaveHistory.setMacroStatut(currentProp.getStatusMacroEtapes());
						wsbpSaveHistory.setTache(tache);
						wsbpSaveHistory.setSgidUser(userSGID);
						wsbpSaveHistory.setNomUser(userLastName);
						wsbpSaveHistory.setPrenomUser(userFirstName);
						wsbpSaveHistory.setRole(role);
						//LOGGER.warning("9:"+dateEvenement+":"+dateEvenement+":"+currentProp.getStatusMacroEtapes()+":"+tache+":"+userSGID+":"+userLastName+":"+userLastName+":"+role);
						wsbpSaveHistory.setNumProposition(numProposition);
						wsbpSaveHistory.setNumMateriel(currentProp.getMaterialNumber());
						wsbpSaveHistory.setCodeTerritoire(currentProp.getTerritoryCode());
						wsbpSaveHistory.setLibelleTerritoire(currentProp.getTerritoryName()); 
						wsbpSaveHistory.setCodeEntite(currentProp.getEntiteCode());
						wsbpSaveHistory.setLibelleEntite(currentProp.getEntiteName()); 
						wsbpSaveHistory.setCodeSecteur(currentProp.getSectorCode()); 
						wsbpSaveHistory.setLibelleSecteur(currentProp.getSectorName());
						wsbpSaveHistory.setCodeSite(currentProp.getSiteCode());
						//LOGGER.warning("10:"+numProposition+":"+currentProp.getMaterialNumber()+":"+currentProp.getTerritoryCode()+":"+currentProp.getTerritoryName()+":"+currentProp.getEntiteCode()+":"+currentProp.getEntiteName()+":"+currentProp.getSectorCode()+":"+currentProp.getSectorName()+":"+currentProp.getSiteCode());
						wsbpSaveHistory.setLibelleSite(currentProp.getSiteName()); 
						wsbpSaveHistory.setCodeAgence(currentProp.getAgencyCode()); 
						wsbpSaveHistory.setLibelleAgence(currentProp.getAgencyName()); 
						wsbpSaveHistory.setCodeGenre(currentProp.getTypeMaterial());
						wsbpSaveHistory.setLibelleGenre(currentProp.getTypeMaterialName()); 
						wsbpSaveHistory.setCodeSousGenre(currentProp.getSubTypeMaterial());
						wsbpSaveHistory.setLibelleSousGenre(currentProp.getSubTypeMaterialName()); 
						//LOGGER.warning("11:"+currentProp.getSiteName()+":"+currentProp.getAgencyCode()+":"+currentProp.getAgencyName()+":"+currentProp.getTypeMaterial()+":"+currentProp.getTypeMaterialName()+":"+currentProp.getSubTypeMaterial()+":"+currentProp.getSubTypeMaterialName());
						wsbpSaveHistory.setCodeAgenceTransfert(currentProp.getTransferAgencyCode()); 
						wsbpSaveHistory.setLibelleAgenceTransfert(currentProp.getTransferAgencyName());
						wsbpSaveHistory.setRoulantNonRoulant(currentProp.getGeneralStatusMaterial());
						wsbpSaveHistory.setType(codeWorkflow); 
						wsbpSaveHistory.setMotifDemande(currentProp.getTypeProposition()); 
						wsbpSaveHistory.setExerciceInitial(exerciceInitial); 
						wsbpSaveHistory.setBudgetHorsBudget(budget);
						//LOGGER.warning("12:"+currentProp.getTransferAgencyCode()+":"+currentProp.getTransferAgencyName()+":"+codeWorkflow+":"+currentProp.getTypeProposition()+":"+exerciceInitial+":"+budget);
						wsbpSaveHistory.setTypeAcquisition(currentProp.getTypeAcquisition()); 
						wsbpSaveHistory.setNombreMois(nbMonth); 
						wsbpSaveHistory.setPourcentageNegocie(pourcentage); 
						wsbpSaveHistory.setValeurBudgete(montant);
						//LOGGER.warning("14:"+currentProp.getTypeAcquisition()+":"+nbMonth+":"+pourcentage+":"+montant);
						wsbpSaveHistory.setChargeMensuelleActuelle(montantMensuel);
						wsbpSaveHistory.setChargeMensuelleFuture(montantMensuelProposition); 
						wsbpSaveHistory.setDateLivraisonPrevue(dateEstimee);
						//LOGGER.warning("15:"+montantMensuel+":"+montantMensuelProposition+":"+dateEstimee);
						
						//wsbpSaveHistory.setArgumentaireSmarte(currentProp.getPitchSmart());
						//Added by Amit for BPM-285
						if(currentProp.getPitchSmart()!=null && currentProp.getPitchSmart()!="")
						{
							String myText= Jsoup.parse(currentProp.getPitchSmart()).text();
							wsbpSaveHistory.setArgumentaireSmarte(myText);
							//LOGGER.warning("17:"+myText);
						}
						else{
							wsbpSaveHistory.setArgumentaireSmarte(currentProp.getPitchSmart());
						}
						//LOGGER.warning("16:"+currentProp.getPitchSmart());
						
						result = soapService.wsbpENREGHISTO(wsbpSaveHistory);*/
						soapService.wsbpENREGHISTO(
								serviceCookie, 
								statut, propStatus, acteur,
								codeClef, exercice,
								dateEvenement,
								macroStatusPropostion,
								tache,
								userSGID, userFirstName, userLastName, role, 
								numProposition, 
								currentProp.getMaterialNumber(), 
								currentProp.getTerritoryCode(),
								currentProp.getTerritoryName(), 
								currentProp.getEntiteCode(), 
								currentProp.getEntiteName(), 
								currentProp.getSectorCode(), 
								currentProp.getSectorName(),
								currentProp.getSiteCode(), 
								currentProp.getSiteName(), 
								currentProp.getAgencyCode(), 
								currentProp.getAgencyName(), 
								currentProp.getTypeMaterial(),
								currentProp.getTypeMaterialName(), 
								currentProp.getSubTypeMaterial(), 
								currentProp.getSubTypeMaterialName(), 
								currentProp.getTransferAgencyCode(), 
								currentProp.getTransferAgencyName(),
								currentProp.getGeneralStatusMaterial(), 
								currentProp.getTypeProposition(), 
								currentProp.getTypeWorkflow(), 
								exerciceInitial, 
								budget,
								currentProp.getTypeAcquisition(), 
								nbMonth, 
								pourcentage, 
								montant,
								montantMensuel,
								montantMensuelProposition, 
								dateEstimee,
								pitchSmart,
								currentProp.getCommentSmart(),
								valeur_demande,
								reqSource				
								);
						}
					}
				}
			}
		
			
		} catch (Exception e) {
			LOGGER.severe("Erreur pendant l'execution du connecteur :" + e.getMessage());
			throw new ConnectorException(e);
		}
	
	 }

	@Override
	public void connect() throws ConnectorException{
		//[Optional] Open a connection to remote server
	
	}

	@Override
	public void disconnect() throws ConnectorException{
		//[Optional] Close connection to remote server
	
	}

}
