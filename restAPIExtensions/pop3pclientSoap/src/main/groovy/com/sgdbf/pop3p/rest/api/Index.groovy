package com.sgdbf.pop3p.rest.api;

import groovy.json.JsonBuilder

import java.nio.charset.MalformedInputException;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Map;

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.bonitasoft.engine.identity.User
import org.bonitasoft.web.extension.ResourceProvider
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.bonitasoft.engine.api.IdentityAPI
import org.bonitasoft.engine.api.ProcessAPI
import org.bonitasoft.web.extension.rest.RestAPIContext
import org.bonitasoft.web.extension.rest.RestApiController

// Class names with clientBudgePop-1.0.10.jar
// BPM-521 : For new library LyreWebServices-2.3.0.jar, changed package name from com.vega_systems.ws.locprotn to com.vega_systems.ws.locprotns_dec20
import com.vega_systems.ws.locprotns.APPROBATION
import com.vega_systems.ws.locprotns.ArrayOfMATELIGIBLE
import com.vega_systems.ws.locprotns.CARACTERISTIQUE
import com.vega_systems.ws.locprotns.CTSCONTRATS
import com.vega_systems.ws.locprotns.CTSHORSCONTRATS
import com.vega_systems.ws.locprotns.LocproPortType
import com.vega_systems.ws.locprotns.LocproServiceBUDGEPOP
import com.vega_systems.ws.locprotns.MATELIGIBLE
import com.vega_systems.ws.locprotns.MATERIEL
import com.vega_systems.ws.locprotns.PERIMETRE
import com.vega_systems.ws.locprotns.PROPBUDGET
import com.vega_systems.ws.locprotns.SGIDAPPROB
import com.vega_systems.ws.locprotns.WSBPDETAILMATERIELResult
import com.vega_systems.ws.locprotns.WSBPHIERARCHIEResult
import com.vega_systems.ws.locprotns.WSBPLSTMATERIELV2Result;
import com.vega_systems.ws.locprotns.WSBPMATELIGIBLESResult
import com.vega_systems.ws.locprotns.WSBPPERIMETREResult
import com.vega_systems.ws.locprotns.WSBPPLANNINGResult

class Index implements RestApiController {

	public static final Logger LOGGER = LoggerFactory.getLogger("com.sgdbf.groovy.rest-api.pop3pclientSoap");
	private static final String ELLIGIBLE_WS_FLAG_VALUE_TRUE = "TRUE";
	private static final String ELLIGIBLE_WS_FLAG_VALUE_FALSE = "FALSE";
	private static final String RESPONSE_ERROR_TEMPLATE = """{"errorMsg" : "%s"}""";
		
	@Override
	RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
		IdentityAPI identityAPI=context.getApiClient().getIdentityAPI();
		def resultList=[];

		// Retrieve method parameter
		def method = request.getParameter "method"
		if (method == null) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST, String.format(RESPONSE_ERROR_TEMPLATE, "the parameter method is missing"));
		}

		// Retrieve f parameter
		def filters = request.getParameterMap().get("f")
		HashMap filterMap = new HashMap();

		if (filters != null) {
			for(e in filters){
				//filterMap.get("key") = "Value"
				String[] parts = e.split("=");
				// CFR 24/11/2020 : Avoid possible ArrayIndexOutOfBoundsException
				if (parts.size() >= 2) {
					String key = parts[0];
					String value = parts[1];
					filterMap.put(key, value);
				}
			}
		}



		// Here is an example of how you can retrieve configuration parameters from a properties file
		// It is safe to remove this if no configuration is required
		Properties props = loadProperties("configuration.properties", context.resourceProvider)
		String urlWSDL = props["urlWSDL"]
		String serviceCookie = props["serviceCookie"]
		
		URL urlBudgePop = new URL(urlWSDL);
		LocproServiceBUDGEPOP budgePopService = new LocproServiceBUDGEPOP(urlBudgePop);

		LocproPortType soapService = budgePopService.getLocproPort();

		try {

			//Switch sur la méthode appelée :
              if ("getPerimeter".equalsIgnoreCase(method)){
				//Controle des parametres de la methode
				if (filterMap.get("SGID") == null) {
					return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST, String.format(RESPONSE_ERROR_TEMPLATE, "the parameter SGID for the method getPerimeter is needed"));
				}

				//Appel du service SOAP 'PERIMETRE'
				WSBPPERIMETREResult result = soapService.wsbpPERIMETRE(
						serviceCookie,
						filterMap.get("SGID"));

					
				//Traitement du resultat
				if (result != null && result.getPERIMETRES() != null){
					for (PERIMETRE currentPerimeter in result.getPERIMETRES().getPERIMETRE()) {
						Map item=new HashMap();
						item.codeWorkflow = currentPerimeter.codeWorkflow
						item.codeNoeud = currentPerimeter.codeNoeud
						item.intitule = currentPerimeter.intitule
						item.role = currentPerimeter.role
						
						resultList.add(item);
					}
				}
				
				
			}else if ("getBudgetPlanning".equalsIgnoreCase(method)){
				DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy")
				
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd")
				DateFormat df1 = new SimpleDateFormat("dd/MM/yyyy")
				
				
				//Controle des parametres de la methode
				if (filterMap.get("codeAgence") == null) {
					return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST, String.format(RESPONSE_ERROR_TEMPLATE, "the parameter codeAgence for the method getBudgetPlanning is needed"));
				}
				if (filterMap.get("anneeBudget") == null) {
					return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST, String.format(RESPONSE_ERROR_TEMPLATE, "the parameter anneeBudget for the method getBudgetPlanning is needed"));
				}

				//Appel du service SOAP 'PLANNING'
				WSBPPLANNINGResult result = soapService.wsbpPLANNING(
						serviceCookie,
						filterMap.get("anneeBudget"),
						filterMap.get("codeAgence"))
				
				//Traitement du resultat
				if (result != null && result.getPROPBUDGETS() != null){

					for (PROPBUDGET currentBudgetPlan in result.getPROPBUDGETS().getPROPBUDGET()) {
						Map item=new HashMap();
						item.niveau = currentBudgetPlan.niveau
						item.intitule = currentBudgetPlan.intitule
						item.codeNoeud = currentBudgetPlan.codeNoeud
						item.nomNoeud = currentBudgetPlan.nomNoeud
						
						//changes made by Amit for BPM-280
						if (currentBudgetPlan.datePresentation != null && currentBudgetPlan.datePresentation.length()>0 && !"NULL".equals(currentBudgetPlan.datePresentation)){
							
							item.datePresentation = df.format(df.parse(currentBudgetPlan.datePresentation))
						}
						item.enveloppeBudget = currentBudgetPlan.enveloppeBudget
						
						if (currentBudgetPlan.rf1 != null && currentBudgetPlan.rf1.length()>0 && !"NULL".equals(currentBudgetPlan.rf1)){
							item.rf1 = df.format(df.parse(currentBudgetPlan.rf1))
						}
						if (currentBudgetPlan.rf2 != null && currentBudgetPlan.rf2.length()>0 && !"NULL".equals(currentBudgetPlan.rf2)){
							item.rf2 = df.format(df.parse(currentBudgetPlan.rf2))
						}
						if (currentBudgetPlan.rf3 != null && currentBudgetPlan.rf3.length()>0 && !"NULL".equals(currentBudgetPlan.rf3)){
							item.rf3 = df.format(df.parse(currentBudgetPlan.rf3))
						}
						if (currentBudgetPlan.rf4 != null && currentBudgetPlan.rf4.length()>0 && !"NULL".equals(currentBudgetPlan.rf4)){
							item.rf4 = df.format(df.parse(currentBudgetPlan.rf4))
						}
						
						/*LOGGER.warn("item:"+item)
						LOGGER.warn("currentBudgetPlan.datePresentation:"+currentBudgetPlan.datePresentation)
						if (currentBudgetPlan.datePresentation != null && currentBudgetPlan.datePresentation.length()>0){
							item.datePresentation = java.time.LocalDate.parse(currentBudgetPlan.datePresentation, f).toString()

						}
						item.enveloppeBudget = currentBudgetPlan.enveloppeBudget
//						if (resultList.size() == result.getPROPBUDGETS().getPROPBUDGET().size()){ 
						LOGGER.warn("item.datePresentation:"+item.datePresentation+" currentBudgetPlan.rf1:"+currentBudgetPlan.rf1)
							if (currentBudgetPlan.rf1 != null && currentBudgetPlan.rf1.length()>0){
								item.rf1 =java.time.LocalDate.parse(currentBudgetPlan.rf1, f).toString()
							}
							LOGGER.warn("item.rf1:"+item.rf1+" currentBudgetPlan.rf2:"+currentBudgetPlan.rf2)
							if (currentBudgetPlan.rf2 != null && currentBudgetPlan.rf2.length()>0){
								item.rf2 = java.time.LocalDate.parse(currentBudgetPlan.rf2, f).toString()
							}
							LOGGER.warn("item.rf2:"+item.rf2+" currentBudgetPlan.rf3:"+currentBudgetPlan.rf3)
							if (currentBudgetPlan.rf3 != null && currentBudgetPlan.rf3.length()>0){
								item.rf3 = java.time.LocalDate.parse(currentBudgetPlan.rf3, f).toString()
							}
							LOGGER.warn("item.rf3:"+item.rf3+" currentBudgetPlan.rf4:"+currentBudgetPlan.rf4)
							if (currentBudgetPlan.rf4 != null && currentBudgetPlan.rf4.length()>0){
								item.rf4 = java.time.LocalDate.parse(currentBudgetPlan.rf4, f).toString()
							}
							LOGGER.warn("item.rf4:"+item.rf4)*/
						
						
						resultList.add(item);
					}
				}
				
			// API called by the Initialisation pages.
			}else if ("getAgencyMaterials".equalsIgnoreCase(method)){
// ICI							
				//Controle des parametres de la methode
				if (filterMap.get("agencyCode") == null) {
					return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST, String.format(RESPONSE_ERROR_TEMPLATE, "the parameter agencyCode for the method getAgencyMaterials is needed"));
				}
				
				// BPM-443 : Add optional parameter matInactifs to retrieve inactive materials
				String matInactifs=null;
				
				if (filterMap.get("matInactifs") != null) {
					matInactifs = filterMap.get("matInactifs") ;
					LOGGER.info("pop3pClientSoap: Inactive materials requested (matInactifs=" + matInactifs + ") on : " + urlWSDL);
				} else {
					LOGGER.info("pop3pClientSoap: Query for active materials on : " + urlWSDL);					
				}
				Map<String, String[]> mapFiltersProcess = UtilityFiltering.parsingFiltersParameters(filters);
				
				WSBPLSTMATERIELV2Result result = soapService.wsbpLSTMATERIELV2(
					serviceCookie, // COOKIE_F903KY
					"", // territoryID
					filterMap.get("agencyCode"), // agencyCode
					"", // lyreNumber
					"", // supplierID
					"", // SGID
					matInactifs, // BPM-443 : New parameter to retrieve inactive materials
					filterMap.get("appCode"), // BPM-559
					"", // offset
					""	// limit
				);
				
				//Traitement du resultat
				if (result != null && result.MATERIELS != null){
					HashMap<String, String> item;
					
					for (MATERIEL currentMaterial in result.MATERIELS.getMATERIEL()) {
						item=new HashMap<String, String>();
						if (currentMaterial.lyrenumber && currentMaterial.modelName) {
							item.lyreNumber = currentMaterial.lyrenumber;
							// BPM-521 Add plateNumber in libellen when not empty
							String plate_number = currentMaterial.plateNumber;
							if ((plate_number != null) && (! plate_number.trim().isEmpty())) {
								item.libelle = currentMaterial.lyrenumber + " - " + plate_number.trim() + " - " + currentMaterial.modelName.trim();
							} else {
								item.libelle = currentMaterial.lyrenumber + " - " + currentMaterial.modelName.trim();
							}
							resultList.add(item);
						}
					}
				}

				} else if ("getEligibleMaterials".equalsIgnoreCase(method)){
					// CFR 27/09/2021 - LOT3 Sprint3 - BPM-676 : New search filters
					//Controle des parametres de la methode
					def String sgid_smarte = filterMap.get("sgidSmarte");
					def String type_select = filterMap.get("typeSelect");
					def String code_noeud = filterMap.get("codeNoeud");
					
					if (sgid_smarte == null) {
						return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST, String.format(RESPONSE_ERROR_TEMPLATE, "the parameter sgidSmarte for the method getEligibleMaterials is required"))
					} else if (type_select == null) {
						return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST, String.format(RESPONSE_ERROR_TEMPLATE, "the parameter typeSelect for the method getEligibleMaterials is required"))
					} else if (code_noeud == null) {
						return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST, String.format(RESPONSE_ERROR_TEMPLATE, "the parameter codeNoeud for the method getEligibleMaterials is required"))
					}

					// Identify which search is enabled on web service
					Map<String, Boolean> enableWSSearch = UtilityFiltering.getWSEnabledSearches(props);
						
					// Get optional flags values
					def String withReasonCout = getFlagValueWithTrueDefault(filterMap, "cout");
					def String withReasonContrat = getFlagValueWithTrueDefault(filterMap, "contrat");
					def String withReasonUtilisation = getFlagValueWithTrueDefault(filterMap, "utilisation"); 
					def String withReasonRachatLocfi = getFlagValueWithTrueDefault(filterMap, "rachatLocfi")
					
					// Exercice : only if rachatLocFi is TRUE
					def String exercice = (withReasonRachatLocfi.equalsIgnoreCase(ELLIGIBLE_WS_FLAG_VALUE_TRUE)) ? filterMap.get("exercice") : null; 
					LOGGER.info("pop3pClientSoap.getEligibleMaterials : Search elligible materials for " + type_select + "=" + code_noeud + " with reason cout=" + withReasonCout + ", contrat=" + withReasonContrat + ", utilisation=" + withReasonUtilisation + ", rachatLocfi=" + withReasonRachatLocfi + " with URL: " + urlWSDL); 
					
					// Determine if we pass to web service
					def String filter_marque = UtilityFiltering.getWebServiceFilterValue(UtilityFiltering.FILTER_FIELD_MARQUE, enableWSSearch, filterMap); 
					def String filter_modele = UtilityFiltering.getWebServiceFilterValue(UtilityFiltering.FILTER_FIELD_MODELE, enableWSSearch, filterMap);
					def String filter_genre = UtilityFiltering.getWebServiceFilterValue(UtilityFiltering.FILTER_FIELD_GENRE, enableWSSearch, filterMap);
					def String filter_sous_genre = UtilityFiltering.getWebServiceFilterValue(UtilityFiltering.FILTER_FIELD_SOUSGENRE, enableWSSearch, filterMap);
					
					// Additional restriction after WS query 
					Map<String, String[]> mapFiltersProcess = UtilityFiltering.parsingFiltersParameters(filters, enableWSSearch);

					//Appel du service SOAP 'MATELIGIBLES'
					WSBPMATELIGIBLESResult result = soapService.wsbpMATELIGIBLES(serviceCookie,
						sgid_smarte,
						type_select, 
						code_noeud,
						filter_modele,
						filter_genre,
						filter_sous_genre,
						filter_marque,
						withReasonCout,
						withReasonContrat,
						withReasonUtilisation,
						withReasonRachatLocfi,
						exercice
					)


				//Traitement du resultat
				if (result != null) {
					def ArrayOfMATELIGIBLE materiels_eligibles = result.getMATELIGIBLES();
					if (materiels_eligibles != null) {
						HashMap<String, HashMap> resultMap = new HashMap();

						for (MATELIGIBLE currentMaterial in materiels_eligibles.getMATELIGIBLE()) {
							HashMap item;
							if (resultMap.get(currentMaterial.numMat) == null){
								item=new HashMap();
								
								//LOGGER.info(currentMaterial.toString());
								
								item.numMat = currentMaterial.numMat
								item.causeEligibilite = stripTrailingComa(currentMaterial.causeEligibilite);
								item.codeAgence = currentMaterial.codeAgence
								item.genre = currentMaterial.genre
								
								// CFR 27/09/2021 - LOT3 Sprint3 - BPM-678 : new fields added
								item.libelleGenre = currentMaterial.libelleGenre
								item.codeSousGenre = currentMaterial.codeSousGenre
								item.libelleSousGenre = currentMaterial.libelleSousGenre
								item.marque = currentMaterial.marque							
								item.modele = currentMaterial.modele
								item.typeAchat = currentMaterial.typeAchat
								item.codeEnergie = currentMaterial.codeEnergie
								item.dateEntree = currentMaterial.dateEntree
								item.dateCirculation = currentMaterial.dateCirculation
								item.compteur = currentMaterial.compteur
								item.dateDbtContratLoc = currentMaterial.dateDbtContratLoc 
								item.dateFinContratLoc = currentMaterial.dateFinContratLoc
								
								// BPM-865 : Get numeric values using getDecimalValue()
								item.valeurAchat = getDecimalValue(currentMaterial.valeurAchat);
								item.coutsSAVTotal = getDecimalValue(currentMaterial.coutsSAVTotal);
								item.engagementReprise = getDecimalValue(currentMaterial.engagementReprise);
								item.loyerFinancementMensuel = getDecimalValue(currentMaterial.loyerFinancementMensuel);
								
								item.codeTerritoire = currentMaterial.codeTerritoire
								item.libelleTerritoire = currentMaterial.libelleTerritoire
								
								item.codeEntite = currentMaterial.codeEntite
								item.libelleEntite = currentMaterial.libelleEntite
								item.displayEntite = currentMaterial.codeEntite + " - " + currentMaterial.libelleEntite;
								
								item.codeRegion = currentMaterial.codeRegion
								item.libelleRegion = currentMaterial.libelleRegion
								item.displayRegion = currentMaterial.codeRegion  + " - " + currentMaterial.libelleRegion 
								
								item.codeSecteur = currentMaterial.codeSecteur
								item.libelleSecteur = currentMaterial.libelleSecteur
								item.displaySecteur = currentMaterial.codeSecteur + " - " + currentMaterial.libelleSecteur
								 
								item.codeSite = currentMaterial.codeSite
								item.libelleSite = currentMaterial.libelleSite
								item.displaySite = currentMaterial.codeSite + " - " + currentMaterial.libelleSite
								
								item.libelleAgenceAffectation = currentMaterial.libelleAgenceAffectation
								item.displayAgence = item.codeAgence + " - " + currentMaterial.libelleAgenceAffectation
								
								item.codeAgenceLoc = currentMaterial.codeAgenceLoc
								item.libelleAgenceLoc = currentMaterial.libelleAgenceLoc
								
								// CFR 13/05/2022 - BPM-814 : add new energy fields
								item.codeEnergie = currentMaterial.codeEnergie;
								item.libelleEnergie = currentMaterial.libelleEnergie;
								item.montantRetrofit =  getDecimalValue(currentMaterial.montantRetrofit);
								item.montantRetrofitString = formatCurrencyAmount(item.montantRetrofit);
								item.retrofitB100 = getBooleanValue(currentMaterial.retrofitB100);
								item.b100 = getBooleanValue(currentMaterial.b100);
								item.xtl = getBooleanValue(currentMaterial.XTL);
								
								// BPM-880 : ZFE is no longer a boolean, it contains the ZFE name
								// WAS: item.zfe = getBooleanValue(currentMaterial.ZFE);
								String zfe_name = currentMaterial.ZFE;
								item.zfeName = zfe_name;
								item.zfe = ((zfe_name == null) || zfe_name.isAllWhitespace()) ? false : true;
								
								item.biofuel = getBiofuelDescription(item.b100, item.xtl);
								item.critair = currentMaterial.critair;
								
								resultMap.put(item.numMat, item)
							}else {
								//Gestion d'un item qui existe deja : la cause est ajoutée a l'item exitant
								item = resultMap.get(currentMaterial.numMat)
								item.causeEligibilite += ", " + stripTrailingComa(currentMaterial.causeEligibilite)
							}
	
						} // end for

						LOGGER.debug("pop3pClientSoap.getEligibleMaterials : Found " + resultMap.size() + " elligible materials");
					
						for (Iterator iter = resultMap.iterator(); iter.hasNext(); ) {
							HashMap currentItem = iter.next().value;
							if (UtilityFiltering.isOkWithFiltering(mapFiltersProcess, currentItem)) {
								resultList.add(currentItem);
							}
						}					
					} else {
						// Empty result
						LOGGER.warn("pop3pClientSoap.getEligibleMaterials : Empty result from Lyre !");
						def String error_msg = result.getERRORMSG();
						if ((error_msg != null) && (! error_msg.empty)) {
							LOGGER.error("pop3pClientSoap.getEligibleMaterials : Error from Lyre: " + error_msg);
							return buildResponse(responseBuilder, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, String.format(RESPONSE_ERROR_TEMPLATE, sanitizeJsonErrorMsg(error_msg)));
						}
					}
				} else {
					LOGGER.error("pop3pClientSoap.getEligibleMaterials : NULL result from Lyre !");
					return buildResponse(responseBuilder, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, String.format(RESPONSE_ERROR_TEMPLATE, "NULL result from Lyre Web Service"));
				}
				LOGGER.info("pop3pClientSoap.getEligibleMaterials : Returning " + resultList.size() + " elligible materials");
				
			}else if ("getMaterialDetails".equalsIgnoreCase(method)){
				//Controle des parametres de la methode
				if (filterMap.get("lyreNumber") == null) {
					return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST, String.format(RESPONSE_ERROR_TEMPLATE, "the parameter lyreNumber for the method getMaterialDetails is needed"))
				}

				//Appel du service SOAP 'DETAILMATERIEL'
				WSBPDETAILMATERIELResult result = soapService.wsbpDETAILMATERIEL(
						serviceCookie,
						null,
						null,
						filterMap.get("lyreNumber"),
						null,
						null)

				//Traitement du resultat
				if (result != null && result.getMATERIELS() != null){
					HashMap<String, Map> resultMap = new HashMap();
					
					for (MATERIEL currentMaterial in result.getMATERIELS().getMATERIEL()) {
						Map item = setDetailMaterial(currentMaterial)

						resultList.add(item);

					}

				}

			}else if ("getPropositionApprovers".equalsIgnoreCase(method)){
				//Controle des parametres de la methode
				if (filterMap.get("codeWorkflow") == null) {
					return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST, """{"error" : "the parameter codeWorkflow for the method getPropositionApprovers is needed"}""")
				}
				if (filterMap.get("genre") == null) {
					return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter genre for the method getPropositionApprovers is needed"}""")
				}
				if (filterMap.get("codeAgence") == null) {
					return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter codeAgence for the method getPropositionApprovers is needed"}""")
				}
				if (filterMap.get("montant") == null) {
					return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter montant for the method getPropositionApprovers is needed"}""")
				}

				//Appel du service SOAP 'DETAILMATERIEL'
				WSBPHIERARCHIEResult result = soapService.wsbpHIERARCHIE(
						serviceCookie,
						filterMap.get("codeWorkflow"),
						filterMap.get("genre"),
						filterMap.get("codeAgence"),
						filterMap.get("montant"))

				//Traitement du resultat
				if (result != null && result.getAPPROBATIONS() != null){
					HashMap<String, Map> resultMap = new HashMap();
					TreeMap<String,Map<String, Object>> treeMap = new TreeMap<String,Map<String, Object>>();

					List<APPROBATION> approbationsList = result.getAPPROBATIONS().getAPPROBATION();
					for (int i = 0; i < approbationsList.size(); i++) {
						Map<String, Object> item=new HashMap<String, Object>();
						APPROBATION currentApprob = approbationsList.get(i);
						String smarteLabel = "";
						
						try {
							if (currentApprob.getSGidSmarte()=="Sans approbation")
							{
								smarteLabel += currentApprob.getSGidSmarte();
							}
							else if(currentApprob.getSGidSmarte()=="Sans approbateur"){
								smarteLabel += "Pas d'approbateur identifié";
							}
							else
							{
							User currentUser = identityAPI.getUserByUserName(currentApprob.getSGidSmarte());
							smarteLabel += currentUser.firstName +" "+ currentUser.lastName+" ("+currentApprob.getSGidSmarte()+")";
							}
						}catch (Exception e) {
							smarteLabel += "Utilisateur inconnu: "+currentApprob.getSGidSmarte();
						}
						
						item.put("SGidSmarte",smarteLabel);
						
						item.put("codeNoeudApprob",currentApprob.getCodeNoeudApprob());

						item.put("libNoeudApprob",currentApprob.getLibNoeudApprob());
						item.put("nomNoeudApprob",currentApprob.getNomNoeudApprob());

						if (currentApprob.getSGIDAPPROBS()!= null && !currentApprob.getSGIDAPPROBS().getSGIDAPPROB().isEmpty()){
							List<SGIDAPPROB> listApprobs = currentApprob.getSGIDAPPROBS().getSGIDAPPROB();
							ArrayList<String> arrayApprobs = new ArrayList<String>();
							String approbs = ""

							for (int j = 0; j < listApprobs.size(); j++) {
								String currentSGID = listApprobs.get(j).getSgidApprob();
								try {
									if (currentSGID=="Sans approbation")
									{
										approbs += currentSGID+", "
									}
									else if(currentSGID=="Sans approbateur"){
										approbs += "Pas d'approbateur identifié"+", "
									}
									else
									{
									User currentUser = identityAPI.getUserByUserName(currentSGID);
									approbs += currentUser.firstName +" "+ currentUser.lastName+" ("+currentSGID+"), ";
									}
								} catch (Exception e) {
									LOGGER.error("Erreur lors de la recuperation du user "+ currentSGID+" : "+e.getMessage())
									approbs += "Utilisateur inconnu: "+currentSGID+", "
								}
							}
							item.put("listApprobs",approbs.substring(0, approbs.length()-2));
						}

						treeMap.put(currentApprob.getNiveauApprob(), item);
					}
					resultList.addAll(treeMap.values())

				}
			}else {
				return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST, String.format(RESPONSE_ERROR_TEMPLATE, "this method is unknown or not allowed"))
			}

		} catch (Exception e) {
			LOGGER.error("Erreur pendant l'appel au web service Lyre : ", e.toString());
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST, String.format(RESPONSE_ERROR_TEMPLATE, sanitizeJsonErrorMsg(e.getMessage())))
		}
		return buildResponse(responseBuilder, HttpServletResponse.SC_OK, new JsonBuilder(resultList).toPrettyString())

	}
	
	private String stripTrailingComa(String cause_eligibility) {
		if ((cause_eligibility != null) && (cause_eligibility.length() > 0)) {
			cause_eligibility = cause_eligibility.trim();
			if (cause_eligibility.endsWith(",")) {
				cause_eligibility = cause_eligibility.substring(0, cause_eligibility.length()-1);
			}
		}
		return cause_eligibility;
	}
	
	private String sanitizeJsonErrorMsg(String error_msg) {
		def String res = error_msg;
		
		try {
			if (res != null) {
				// Remove any new line
				res = res.replaceAll("\r","");
				res = res.replaceAll("\n","");
				
				// Remove any backslash
				res = res.replaceAll("\\","");
					
				// Escape double quotes
				res = res.replaceAll("\"","\\\"");
				
			}
		} catch (Exception e) {
			LOGGER.error("Exception sanitizeJsonErrorMsg : ", e.toString());
		}
		
		return res;
	}

	private String getFlagValueWithTrueDefault(Map<String, String > filterMap, String parameter_name) {
		String param_value = filterMap.get(parameter_name);
		def String flag_value = ((param_value != null) && param_value.equalsIgnoreCase(ELLIGIBLE_WS_FLAG_VALUE_FALSE)) ? ELLIGIBLE_WS_FLAG_VALUE_FALSE : ELLIGIBLE_WS_FLAG_VALUE_TRUE;
		
		return flag_value;
	}
	
	private String getBiofuelDescription(boolean b100, boolean xtl) {
		String desc='';
		
		if (b100) {
			desc += 'B100';
		}
		if (xtl) {
			if (desc.length() > 0) {
				desc += ', ';
			}
			desc += 'XTL';
		}
		return desc;	
	}
	
	private Boolean getBooleanValue(String parameter_value) {
		def Boolean boolean_value = false;
		
		if (parameter_value != null) {
			if (parameter_value.equalsIgnoreCase("true")) {
				boolean_value = true;
			}
		}
			
		return boolean_value;
	}
	
	// Parse string as a Doble with comma separator for decimal part
	private Double getDecimalValue(String parameter_value) {
		def Double double_value = null;
		
		try {
			if ((parameter_value != null) && (parameter_value.length() > 0)) {
				NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
				Number num_value = format.parse(parameter_value);
				double_value = num_value.doubleValue();
			}
		} catch (Exception e) {
			LOGGER.error("getDecimalValue exception : ", e.toString());
		}
		return double_value;
	}
	
	private String formatCurrencyAmount(Double amount) {
		String res="";
		
		if (amount != null) {
			DecimalFormat fr_format = DecimalFormat.getCurrencyInstance(Locale.FRANCE);
			res = fr_format.format(amount);
		}
		
		return res;
	}
	
	private Map setDetailMaterial(MATERIEL currentMaterial) {
		Map item=new HashMap();
		item.accordEnCours     			= currentMaterial.accordEnCours
		item.activityID        			= currentMaterial.activityID
		item.age               			= currentMaterial.age
		item.agencyCode        			= currentMaterial.agencycode
		item.auBudget            		= currentMaterial.auBudget
		item.carrosserie       			= currentMaterial.carrosserie
		item.comptAnnPass      			= currentMaterial.comptAnnPass
		item.comptCour         			= currentMaterial.comptCour
		item.conducteur        			= currentMaterial.conducteur
		item.consoMoy            		= currentMaterial.consoMoy
		item.consoMoyCent        		= currentMaterial.consoMoyCent
		item.cumulAnnee          		= currentMaterial.cumulAnnee
		item.cumulCtsContrats    		= currentMaterial.cumulCtsContrats
		item.cumulCtsHorsContrats		= currentMaterial.cumulCtsHorsContrats
		item.dateDernMajMat      		= currentMaterial.dateDernMajMat
		item.dateLivrNouvMat     		= currentMaterial.dateLivrNouvMat
		item.derRelev          			= currentMaterial.derRelev
		item.dtDerRelev        			= currentMaterial.dtDerRelev
		item.dtMisc            			= currentMaterial.dtMisc
		item.enCommande        			= currentMaterial.enCommande
		item.entryDate         			= currentMaterial.entryDate
		item.genreID           			= currentMaterial.genreID
		item.locationAgencyCode			= currentMaterial.locationAgencyCode
		item.lyreNumber        			= currentMaterial.lyrenumber
		item.mailGestionnaireBO  		= currentMaterial.mailGestionnaireBO
		item.marqueID          			= currentMaterial.marqueID
		item.modelName         			= currentMaterial.modelName
		item.montantAuBudget     		= currentMaterial.montantAuBudget
		item.name              			= currentMaterial.name
		item.plateNumber       			= currentMaterial.plateNumber
		item.refNouvMat          		= currentMaterial.refNouvMat
		item.renouvStatut        		= currentMaterial.renouvStatut
		item.sectionID         			= currentMaterial.sectionID
		item.sectorID          			= currentMaterial.sectorID
		item.serialNumber      			= currentMaterial.serialNumber
		item.siteID            			= currentMaterial.siteID
		item.sommeAccords        		= currentMaterial.sommeAccords
		item.sommeCouts          		= currentMaterial.sommeCouts
		item.ssGenreID         			= currentMaterial.ssGenreID
		item.statut            			= currentMaterial.statut
		item.typeFiche         			= currentMaterial.typeFiche
		item.valeurAchat         		= currentMaterial.valeurAchat
		item.valeurAchatRem      		= currentMaterial.valeurAchatRem
		item.valeurFinLOCFI      		= currentMaterial.valeurFinLOCFI
		item.engagement      			= currentMaterial.engagement

		// BPM-812 ; Add new fields for energy
		item.critair = currentMaterial.critair
		item.montantRetrofit = currentMaterial.montantRetrofit
		item.retrofitB100 = getBooleanValue(currentMaterial.retrofitB100);
		item.b100 =  getBooleanValue(currentMaterial.b100);
		item.xtl = getBooleanValue(currentMaterial.XTL);
		
		// BPM-880 : ZFE is no longer a boolean
		//item.zfe = getBooleanValue(currentMaterial.ZFE);
		String zfe_name = currentMaterial.ZFE;
		item.zfeName = zfe_name;
		item.zfe = ((zfe_name == null) || zfe_name.isAllWhitespace()) ? false : true;
		
		//Contrat Acquisition
		Map contratacqui=new HashMap();
		contratacqui.compteurContractuel= currentMaterial.contratacqui.compteurContractuel
		contratacqui.compteurDebut      = currentMaterial.contratacqui.compteurDebut
		contratacqui.dtEntree           = currentMaterial.contratacqui.dtEntree
		contratacqui.dtFinContrat       = currentMaterial.contratacqui.dtFinContrat
		contratacqui.montantMens        = currentMaterial.contratacqui.montantMens
		contratacqui.montantMensCond    = currentMaterial.contratacqui.montantMensCond
		contratacqui.nbKms              = currentMaterial.contratacqui.nbKms
		contratacqui.nbMois             = currentMaterial.contratacqui.nbMois
		contratacqui.nomTiersFinanceur  = currentMaterial.contratacqui.nomTiersFinanceur
		contratacqui.numContrat         = currentMaterial.contratacqui.numContrat
		contratacqui.numTel             = currentMaterial.contratacqui.numTel
		contratacqui.statut             = currentMaterial.contratacqui.statut
		contratacqui.typeAcqui          = currentMaterial.contratacqui.typeAcqui
		item.contratacqui = contratacqui

		//Contrat MAINTENANCE
		Map contratmaint=new HashMap();

		if(currentMaterial.CONTRATMAINT) {
			contratmaint.statut             = currentMaterial.CONTRATMAINT.statut
			contratmaint.numContrat         = currentMaterial.CONTRATMAINT.numContrat
			contratmaint.contratType 		= currentMaterial.CONTRATMAINT.contratType
			contratmaint.nomTiers    		= currentMaterial.CONTRATMAINT.nomTiers
			contratmaint.numTel             = currentMaterial.CONTRATMAINT.numTel
			contratmaint.dtEntree           = currentMaterial.CONTRATMAINT.dtEntree
			contratmaint.nbMois             = currentMaterial.CONTRATMAINT.nbMois
			contratmaint.nbKms              = currentMaterial.CONTRATMAINT.nbKms
			contratmaint.montantMens        = currentMaterial.CONTRATMAINT.montantMens
			contratmaint.dtFinContrat       = currentMaterial.CONTRATMAINT.dtFinContrat
		}
		
		item.contratmaint = contratmaint

		//CG
		Map cg=new HashMap();
		cg.PTAC    						= currentMaterial.CG.PTAC
		cg.PTRA    						= currentMaterial.CG.PTRA
		cg.PV      						= currentMaterial.CG.PV
		cg.puisDIN 						= currentMaterial.CG.puisDIN
		cg.puisFisc						= currentMaterial.CG.puisFisc
		item.cg = cg

		//Caracteristiques
		Map caracteristiques = new HashMap();
		if (currentMaterial.getCARACTERISTIQUES() != null){
			ArrayList<CARACTERISTIQUE> listCarc = currentMaterial.getCARACTERISTIQUES().getCARACTERISTIQUE();
			if (listCarc != null && ! listCarc.isEmpty()){
				for (var in listCarc) {
					if(var.codeCarac != null) {
						caracteristiques.put(var.codeCarac, var.value);
						// BPM-812 ; Add the label that is now sent by Lyre as an additional field 'libelle'
						// For code ENERGIE, we add a field 'ValueEnergie' with the label
						if (var.libelle != null) {
							String codeCarac = var.codeCarac;
							String codeLibelle = "Value" + codeCarac.toLowerCase().capitalize();
							caracteristiques.put(codeLibelle, var.libelle);
						}
					}
				}
			}
		}
		item.caracteristiques = caracteristiques

		//CTSCONTRATSS
		Map ctsContrats = new HashMap();
		if (currentMaterial.getCTSCONTRATSS() != null){
			ArrayList<CTSCONTRATS> listCtsContrats = currentMaterial.getCTSCONTRATSS().getCTSCONTRATS();
			if (listCtsContrats != null && ! listCtsContrats.isEmpty()){
				for (var in listCtsContrats) {
					if(var.type != null) {
						ctsContrats.put(var.type, var.montant)
					}
				}
			}
		}
		item.ctsContrats = ctsContrats

		//CTSHORSCONTRATSS
		Map ctsHorsContrats = new HashMap();
		if (currentMaterial.getCTSHORSCONTRATSS() != null){
			ArrayList<CTSHORSCONTRATS> listCtsHorsContrats = currentMaterial.getCTSHORSCONTRATSS().getCTSHORSCONTRATS();
			if (listCtsHorsContrats != null && ! listCtsHorsContrats.isEmpty()){
				for (var in listCtsHorsContrats) {
					if(var.type != null) {
						ctsHorsContrats.put(var.type, var.montant)
					}
				}
			}
		}
		item.ctsHorsContrats = ctsHorsContrats

		return item
	}

	/**
	 * Build an HTTP response.
	 *
	 * @param  responseBuilder the Rest API response builder
	 * @param  httpStatus the status of the response
	 * @param  body the response body
	 * @return a RestAPIResponse
	 */
	RestApiResponse buildResponse(RestApiResponseBuilder responseBuilder, int httpStatus, Serializable body) {
		return responseBuilder.with {
			withResponseStatus(httpStatus)
			withResponse(body)
			build()
		}
	}

	/**
	 * Returns a paged result like Bonita BPM REST APIs.
	 * Build a response with a content-range.
	 *
	 * @param  responseBuilder the Rest API response builder
	 * @param  body the response body
	 * @param  p the page index
	 * @param  c the number of result per page
	 * @param  total the total number of results
	 * @return a RestAPIResponse
	 */
	RestApiResponse buildPagedResponse(RestApiResponseBuilder responseBuilder, Serializable body, int p, int c, long total) {
		return responseBuilder.with {
			withContentRange(p,c,total)
			withResponse(body)
			build()
		}
	}

	/**
	 * Load a property file into a java.util.Properties
	 */
	Properties loadProperties(String fileName, ResourceProvider resourceProvider) {
		Properties props = new Properties()
		resourceProvider.getResourceAsStream(fileName).withStream { InputStream s ->
			props.load s
		}
		props
	}

	private static boolean isOkWithFiltering(Map < String, String[] > mapFiltersProcess, Map taskItem) {
		boolean isValide = true;
		for (Map.Entry < String, String[] > entry: mapFiltersProcess.entrySet()) {
			if (taskItem.containsKey(entry.getKey()) && isValide) {
				String filterValue = entry.getValue()[0];
				filterValue = filterValue.toLowerCase()
				String filterOperator = entry.getValue()[1];
				String taskValue = taskItem.get(entry.getKey()).toString();
				taskValue = taskValue.toLowerCase()

				if (filterOperator.equals("E")) {
					if (taskValue != filterValue) {
						isValide = false;
					}
				} else {
					if (!taskValue.contains(filterValue)) {
						isValide = false;
					}
				}
				if (!isValide) {
					break;
				}

			}
		}
		return isValide;
	}
	
}