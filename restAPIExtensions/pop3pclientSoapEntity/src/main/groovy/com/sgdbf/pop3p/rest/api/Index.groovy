package com.sgdbf.pop3p.rest.api;

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.bonitasoft.engine.api.IdentityAPI
import org.bonitasoft.web.extension.ResourceProvider
import org.bonitasoft.web.extension.rest.RestAPIContext
import org.bonitasoft.web.extension.rest.RestApiController
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.vega_systems.ws.locprotns.CARACTERISTIQUE
import com.vega_systems.ws.locprotns.CTSCONTRATS
import com.vega_systems.ws.locprotns.CTSHORSCONTRATS
import com.vega_systems.ws.locprotns.ENTITE
import com.vega_systems.ws.locprotns.LocproPortType
import com.vega_systems.ws.locprotns.LocproServiceBUDGEPOP
import com.vega_systems.ws.locprotns.MATERIEL
import com.vega_systems.ws.locprotns.WSBPLISTEENTITES
import com.vega_systems.ws.locprotns.WSBPLISTEENTITESResult

import groovy.json.JsonBuilder



class Index implements RestApiController {

	private static final Logger LOGGER = LoggerFactory.getLogger('org.bonitasoft')

	@Override
	RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
		IdentityAPI identityAPI=context.getApiClient().getIdentityAPI();
		def resultList=[];

		// Retrieve method parameter
		def method = request.getParameter "method"
		if (method == null) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter method is missing"}""")
		}

		// Retrieve f parameter
		def filters = request.getParameterMap().get("f")
		HashMap filterMap = new HashMap();

		if (filters != null) {
			for(e in filters){
				//filterMap.get("key") = "Value"
				String[] parts = e.split("=");
				String key = parts[0];
				String value = parts[1];
				filterMap.put(key, value);
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
			if ("getEntities".equalsIgnoreCase(method)){
				//Controle des parametres de la methode
				if (filterMap.get("SGID") == null) {
					return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter SGID for the method getEntities is needed"}""")
				}
				WSBPLISTEENTITES t = new WSBPLISTEENTITES();
				t.setCOOKIEF903KY(serviceCookie);
				t.setSgid(filterMap.get("SGID"));
				//Appel du service SOAP 'ENTITE'
				WSBPLISTEENTITESResult result = soapService.wsbpLISTEENTITES(t).getWSBPLISTEENTITESResult();

				//Traitement du resultat
				if (result != null && result.getENTITES() != null){
					for (ENTITE currentEntity in result.getENTITES().getENTITE()) {
						Map item=new HashMap();
						item.entite = currentEntity.getCodeEntite()+" - "+currentEntity.getNomEntite();
						resultList.add(item);
					}
				}
				
				
			}
else {
				return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "this method is unknown or not allowed"}""")
			}

		} catch (Exception e) {
			LOGGER.error("Erreur pendant la récupération des entités : ", e)
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST, new JsonBuilder(e).toPrettyString())
		}
		return buildResponse(responseBuilder, HttpServletResponse.SC_OK, new JsonBuilder(resultList).toPrettyString())

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
						caracteristiques.put(var.codeCarac, var.value)
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