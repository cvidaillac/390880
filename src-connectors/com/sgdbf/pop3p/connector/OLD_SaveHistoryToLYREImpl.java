/**
 * 
 */
package com.sgdbf.pop3p.connector;

import java.net.URL;
import java.util.List;

import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.identity.User;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.sgdbf.model.Proposition;
import com.sgdbf.model.TimeLine;

//09/09/2021 LOT3 Sprint2 - BPM-661 : Use updated library LyreWebServices-3.0.2.jar with distinct package namge to avoid conflicts
import com.vega_systems.ws.locprotns_sept21.LocproPortType;
import com.vega_systems.ws.locprotns_sept21.LocproServiceBUDGEPOP;

/*import com.myvega_systems.ws.locprotns.LocproPortType;
import com.myvega_systems.ws.locprotns.LocproServiceBUDGEPOP;
import com.myvega_systems.ws.locprotns.WSBPENREGHISTO;
import com.myvega_systems.ws.locprotns.WSBPENREGHISTOResponse;*/

/*import com.vega_systems.ws.LocproTNS.LocproPortType;
import com.vega_systems.ws.LocproTNS.LocproPortTypeProxy;
import com.vega_systems.ws.LocproTNS.LocproServiceBUDGEPOP;
import com.vega_systems.ws.LocproTNS.LocproServiceBUDGEPOPLocator;
*/

// CFR 09/12/2020 BPM-474 : To avoid naming colision with GetHierarchy connector using older version of the library corresponding ot the Lyre Web Service, 
// we use a distinct package name com.vega_systems.ws.savehisto (instead of com.vega_systems.ws.locprotns)
// Associated library is LYREWsSaveHistory-1.0.15 generated by wsimport with option -p com.vega_systems.ws.savehisto
//import com.vega_systems.ws.savehisto.LocproPortType;
//import com.vega_systems.ws.savehisto.LocproServiceBUDGEPOP;
//CFR 18/12/2020 LOT2 Sprint4 - BPM-519 : Use updated library LyreWebServices-2.3.0.jar with distinct package namge to avoid conflicts
//import com.vega_systems.ws.locprotns_dec20.LocproPortType;
//import com.vega_systems.ws.locprotns_dec20.LocproServiceBUDGEPOP;


/**
 *The connector execution will follow the steps
 * 1 - setInputParameters() --> the connector receives input parameters values
 * 2 - validateInputParameters() --> the connector can validate input parameters values
 * 3 - connect() --> the connector can establish a connection to a remote server (if necessary)
 * 4 - executeBusinessLogic() --> execute the connector
 * 5 - getOutputParameters() --> output are retrieved from connector
 * 6 - disconnect() --> the connector can close connection to remote server (if any)
 */
public class OLD_SaveHistoryToLYREImpl extends AbstractOLD_SaveHistoryToLYREImpl {
	public static final Logger LOGGER = (Logger) LoggerFactory.getLogger(SaveHistoryToLYREImpl.class); 
	//private Logger LOGGER = Logger.getLogger("org.bonitasoft");

	@Override
	protected void executeBusinessLogic() throws ConnectorException{
		LOGGER.debug("**********************Entree connecteur SaveHistoryToLYREImpl*********************");
		//Get access to the connector input parameters

		Proposition currentProp = getCurrentProposition();
	
		String urlWSDL = getUrlWSDL();
		String serviceCookie =getCookie();
		//WSBPENREGHISTOResponse result;
		//LOGGER.info("urlWSDL " + urlWSDL);
		//LOGGER.info("urlWSDL " + serviceCookie);

		try {
			//Create the client connection
			URL urlBudgePop = new URL(urlWSDL);
			
			// CFR 09/12/2020 BPM-474 : Changed for new library LYREWsSaveHistory-1.0.15
			//LocproServiceBUDGEPOP budgePopService = new LocproServiceBUDGEPOPLocator();
			//LocproPortType soapService = budgePopService.getLocproPort(urlBudgePop);
			LocproServiceBUDGEPOP budgePopService = new LocproServiceBUDGEPOP(urlBudgePop);
			LocproPortType soapService = budgePopService.getLocproPort(); 
			
			
							
			//Prepare the message to send
			String userFirstName = null, userLastName = null, userSGID = null;
		
			
			if (currentProp != null){
				//Si la proposition est nulle, pas d'enregistrement
				//LOGGER.info("Debut traitement pour proposition "+ currentProp.getPersistenceId());
				
				//recuperation des infos du dernier TimeLine
				String statut = null, acteur = null, codeClef = null, tache = null, codeWorkflow = null, propositionStatus = null, taskDueDate = null;
				List<TimeLine> timeLines = currentProp.getTimeLines();
				if (timeLines != null && timeLines.size()>0){
					TimeLine tl = timeLines.get(timeLines.size()-1);
					if (tl != null){
						statut = tl.getStatut();
						acteur = tl.getActeur();
						codeClef = tl.getCodeClef();
						tache = tl.getTache();
                        codeWorkflow = tl.getCodeWorkflow();
                        propositionStatus = tl.getPropositionStatus(); // Added by Amit for BPM-462
                        //BPM-661 : new field added by Amit for task due date
                        if(tl.getTaskDueDate() != null)
                        {
                        	taskDueDate = tl.getTaskDueDate().toString();
                        }
                        
						try {
						//Get User infos
							Long userId = tl.getIdentifiantActeur();
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
						
					}
				}
				
				//3 StatutProposition
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
				
				//6 DateEvenement
				String dateEvenement = java.time.OffsetDateTime.now().toString();
				
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
					montant = currentProp.getPropositionAmount().toString();
				}
				//40  currentProp.getMonthlyAmount()
				String montantMensuel = null;
				if ( currentProp.getMonthlyAmount() != null){
					montantMensuel =  currentProp.getMonthlyAmount().toString();
				}
			
				//41 currentProp.getMonthlyPropositionAmount()
				String montantMensuelProposition = null;
				if (currentProp.getMonthlyPropositionAmount() != null){
					montantMensuelProposition = currentProp.getMonthlyPropositionAmount().toString();
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
				
				//Save History to LYRE
				
				/*WSBPENREGHISTO wsbpSaveHistory = new WSBPENREGHISTO();
				wsbpSaveHistory.setCOOKIEF903KY(serviceCookie);
				wsbpSaveHistory.setStatut(statut);
				

				String isFirstWorkFlow = (String)apiAccessor.getProcessAPI().getProcessDataInstance("isFirstWorkFlowData", getExecutionContext().getProcessInstanceId()).getValue();
				if("false".equals(isFirstWorkFlow))
				{
					wsbpSaveHistory.setStatutProposition(currentProp.getPropositionStatusTargetAgency());
				}
				LOGGER.warn("**********************setStatutProposition(currentProp.getPropositionStatus())********************* "+currentProp.getPropositionStatus());
				LOGGER.warn("isFirstWorkFlow :"+isFirstWorkFlow);
				
				if(propositionStatus != null && !"".equals(propositionStatus)) // Added by Amit for BPM-462
				{
					wsbpSaveHistory.setStatutProposition(propositionStatus);
				}
				else {
				
					if("DDD".equals(currentProp.getTypeWorkflow()) && "DAD".equals(currentProp.getTypeWorkflowTargetAgency()) && "DAD".equals(codeWorkflow))
					{
						wsbpSaveHistory.setStatutProposition(currentProp.getPropositionStatusTargetAgency());
	
					}
					else
					{
						wsbpSaveHistory.setStatutProposition(currentProp.getPropositionStatus());
					}
				}

				wsbpSaveHistory.setActeur(acteur);
				wsbpSaveHistory.setCodeClef(codeClef);
				wsbpSaveHistory.setExercice(exercice);
				wsbpSaveHistory.setDateEvenement(dateEvenement);
				wsbpSaveHistory.setMacroStatut(currentProp.getStatusMacroEtapes());
				wsbpSaveHistory.setTache(tache);
				wsbpSaveHistory.setSgidUser(userSGID);
				wsbpSaveHistory.setNomUser(userLastName);
				wsbpSaveHistory.setPrenomUser(userFirstName);
				wsbpSaveHistory.setRole(role);
				wsbpSaveHistory.setNumProposition(numProposition);
				wsbpSaveHistory.setNumMateriel(currentProp.getMaterialNumber());
				wsbpSaveHistory.setCodeTerritoire(currentProp.getTerritoryCode());
				wsbpSaveHistory.setLibelleTerritoire(currentProp.getTerritoryName()); 
				wsbpSaveHistory.setCodeEntite(currentProp.getEntiteCode());
				wsbpSaveHistory.setLibelleEntite(currentProp.getEntiteName()); 
				wsbpSaveHistory.setCodeSecteur(currentProp.getSectorCode()); 
				wsbpSaveHistory.setLibelleSecteur(currentProp.getSectorName());
				wsbpSaveHistory.setCodeSite(currentProp.getSiteCode());
				wsbpSaveHistory.setLibelleSite(currentProp.getSiteName()); 
				wsbpSaveHistory.setCodeAgence(currentProp.getAgencyCode()); 
				wsbpSaveHistory.setLibelleAgence(currentProp.getAgencyName()); 
				wsbpSaveHistory.setCodeGenre(currentProp.getTypeMaterial());
				wsbpSaveHistory.setLibelleGenre(currentProp.getTypeMaterialName()); 
				wsbpSaveHistory.setCodeSousGenre(currentProp.getSubTypeMaterial());
				wsbpSaveHistory.setLibelleSousGenre(currentProp.getSubTypeMaterialName()); 
				wsbpSaveHistory.setCodeAgenceTransfert(currentProp.getTransferAgencyCode()); 
				wsbpSaveHistory.setLibelleAgenceTransfert(currentProp.getTransferAgencyName());
				wsbpSaveHistory.setRoulantNonRoulant(currentProp.getGeneralStatusMaterial());
				wsbpSaveHistory.setType(codeWorkflow); 
				wsbpSaveHistory.setMotifDemande(currentProp.getTypeProposition()); 
				wsbpSaveHistory.setExerciceInitial(exerciceInitial); 
				wsbpSaveHistory.setBudgetHorsBudget(budget);
				wsbpSaveHistory.setTypeAcquisition(currentProp.getTypeAcquisition()); 
				wsbpSaveHistory.setNombreMois(nbMonth); 
				wsbpSaveHistory.setPourcentageNegocie(pourcentage); 
				wsbpSaveHistory.setValeurBudgete(montant);
				wsbpSaveHistory.setChargeMensuelleActuelle(montantMensuel);
				wsbpSaveHistory.setChargeMensuelleFuture(montantMensuelProposition); 
				wsbpSaveHistory.setDateLivraisonPrevue(dateEstimee);
				
				if(currentProp.getPitchSmart()!=null && currentProp.getPitchSmart()!=""){
					String myText= Jsoup.parse(currentProp.getPitchSmart()).text();
					wsbpSaveHistory.setArgumentaireSmarte(myText);
					}
					else{
						wsbpSaveHistory.setArgumentaireSmarte(currentProp.getPitchSmart());
					}
								
				
				result = soapService.wsbpENREGHISTO(wsbpSaveHistory);*/
				
				// CFR 09/12/2020 BPM-474 : Changed for new library LYREWsSaveHistory-1.0.15
				//soapService.WSBP_ENREG_HISTO(
				
				soapService.wsbpENREGHISTO(
						serviceCookie, 
						statut, propStatus, acteur,
						codeClef, exercice,
						dateEvenement,
						currentProp.getStatusMacroEtapes(),
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
						currentProp.getTransferAgencyName(),		// CFR 07/12/2020 Lot2-Sprint2 UAT correction
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
						currentProp.getRequestSource(),			// CFR 18/12/2020 BPM-519 : new field added for origin of proposiition
						taskDueDate								// 01/09/2021 BPM-661 : new field added for task due date
						);
					
			}
		
			
		} catch (Exception e) {
			LOGGER.debug("Erreur pendant l'execution du connecteur :" + e.getMessage());
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
